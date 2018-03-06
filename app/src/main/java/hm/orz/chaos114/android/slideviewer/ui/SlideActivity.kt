package hm.orz.chaos114.android.slideviewer.ui

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.gms.ads.InterstitialAd
import dagger.android.AndroidInjection
import hm.orz.chaos114.android.slideviewer.R
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySlideBinding
import hm.orz.chaos114.android.slideviewer.infra.model.Slide
import hm.orz.chaos114.android.slideviewer.infra.model.Talk
import hm.orz.chaos114.android.slideviewer.infra.network.SlideShareLoader
import hm.orz.chaos114.android.slideviewer.infra.repository.SettingsRepository
import hm.orz.chaos114.android.slideviewer.infra.repository.TalkRepository
import hm.orz.chaos114.android.slideviewer.ocr.OcrRecognizer
import hm.orz.chaos114.android.slideviewer.util.AdRequestGenerator
import hm.orz.chaos114.android.slideviewer.util.AnalyticsManager
import hm.orz.chaos114.android.slideviewer.util.IntentUtil
import hm.orz.chaos114.android.slideviewer.util.UrlHelper
import hm.orz.chaos114.android.slideviewer.widget.PickPageDialog
import io.reactivex.MaybeObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SlideActivity : AppCompatActivity() {

    @Inject
    lateinit var ocrRecognizer: OcrRecognizer
    @Inject
    lateinit var talkRepository: TalkRepository
    @Inject
    lateinit var loader: SlideShareLoader

    private lateinit var binding: ActivitySlideBinding

    private var menu: Menu? = null
    private var adapter: SlideAdapter? = null
    private var loadingDialog: LoadingDialogFragment? = null
    private var interstitialAd: InterstitialAd? = null

    private var uri: Uri? = null
    private var talk: Talk? = null
    private var recognizeTextMap: MutableMap<String, String>? = null
    private var currentLanguageId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_slide)

        AnalyticsManager.sendScreenView(TAG)

        recognizeTextMap = HashMap()

        setSupportActionBar(binding.toolbar)

        if (supportActionBar == null) {
            throw AssertionError("getSupportActionBar() needs non-null.")
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        adapter = SlideAdapter(this)
        loadingDialog = LoadingDialogFragment.newInstance()

        binding.slideViewPager.offscreenPageLimit = 5
        binding.slideViewPager.adapter = adapter
        binding.slideViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // no-op
            }

            override fun onPageSelected(position: Int) {
                val page = position + 1
                setPageNumbers(page, talk!!.slides!!.size)
                AnalyticsManager.sendChangePageEvent(TAG, talk!!.url!!, page)

                setRecognizedText()
            }

            override fun onPageScrollStateChanged(state: Int) {
                // no-op
            }
        })

        binding.prevButton.setOnClickListener { _ -> movePrev() }
        binding.nextButton.setOnClickListener { _ -> moveNext() }
        binding.slidePageNumbers.setOnClickListener { _ ->
            val current = binding.slideViewPager.currentItem
            val max = talk!!.slides!!.size
            val listener = object : PickPageDialog.OnPickPageListener {
                override fun onPickPage(page: Int) {
                    binding.slideViewPager.currentItem = page
                }
            }
            PickPageDialog.show(this, current, max, listener)
        }

        val intent = intent
        val action = intent.action
        if (Intent.ACTION_VIEW == action) {
            val uri = intent.data
            Timber.d("uri = %s", uri!!.toString())
            this.uri = uri
        } else if (Intent.ACTION_SEND == action) {
            val uri = intent.getStringExtra(Intent.EXTRA_TEXT)
            Timber.d("uri = %s", uri)
            this.uri = Uri.parse(uri)
        } else {
            throw RuntimeException("invalid intent.")
        }
        if (!UrlHelper.isSpeakerDeckUrl(uri!!)) {
            Toast.makeText(this, "Unsupported url.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        if (!UrlHelper.canOpen(uri!!)) {
            WebViewActivity.start(this, uri!!.toString())
            finish()
            return
        }

        loadAd()
        startLoad(false)

        ocrRecognizer.listen()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { (url, recognizedText) ->
                    Timber.d("original is: %s", url)
                    Timber.d("text is recognized: %s", recognizedText)
                    recognizeTextMap!![url] = recognizedText
                    setRecognizedText()
                }
    }

    override fun onPause() {
        binding.slideAdView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.slideAdView.resume()


        // reset text if language is changed.
        val settingsRepository = SettingsRepository(this)
        binding.recognizeText.visibility = if (settingsRepository.enableOcr) View.VISIBLE else View.GONE
        if (!settingsRepository.enableOcr
                || currentLanguageId == null
                || currentLanguageId != settingsRepository.selectedLanguage) {
            currentLanguageId = settingsRepository.selectedLanguage
            // reset
            recognizeTextMap = HashMap()
        }
        setRecognizedText()
        adapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        binding.slideAdView.destroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.slide_activity_menus, menu)
        this.menu = menu

        return super.onCreateOptionsMenu(menu)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_MENU -> {
                    // Menuボタン押下
                    if (menu != null) {
                        menu!!.performIdentifierAction(R.id.overflow_options, 0)
                    }
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onBackPressed() {
        beforeFinish()
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                beforeFinish()
                finish()
                return true
            }
            R.id.menu_reload -> {
                binding.slideViewPager.currentItem = 0
                startLoad(true)
                return true
            }
            R.id.menu_share -> {
                shareUrl()
                return true
            }
            R.id.menu_show_by_browser -> {
                shareBrowser()
                return true
            }
            R.id.menu_setting -> {
                SettingActivity.start(this)
                return true
            }
            R.id.menu_about -> {
                startAboutActivity()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun beforeFinish() {
        // start list activity
        val intent = Intent(this, SlideListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)

        displayInterstitial()
    }

    private fun loadAd() {
        val adRequest = AdRequestGenerator.generate(this)
        binding.slideAdView.loadAd(adRequest)

        // インタースティシャルを作成する。
        interstitialAd = InterstitialAd(this)
        interstitialAd!!.adUnitId = getString(R.string.admob_unit_id)
        interstitialAd!!.loadAd(adRequest)
    }

    private fun startLoad(refresh: Boolean) {
        if (refresh) {
            talk = null
            adapter!!.notifyDataSetChanged()
            talkRepository.deleteByUrl(uri!!.toString())
        }
        talkRepository.findByUrl(uri!!.toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MaybeObserver<Talk> {
                    override fun onSubscribe(d: Disposable) {
                        // no-op
                    }

                    override fun onSuccess(t: Talk) {
                        talk = t
                        // DBにデータがある場合の描画処理
                        val (_, _, title, user) = talk!!.talkMetaDataCollection.iterator().next()
                        binding.slideTitle.text = title
                        binding.slideUser.text = user
                        setPageNumbers(1, talk!!.slides!!.size)
                        adapter!!.notifyDataSetChanged()
                    }

                    override fun onError(e: Throwable) {
                        // no-op
                    }

                    override fun onComplete() {
                        loadingDialog!!.show(fragmentManager, null)

                        loader.load(uri!!)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ (_, talk1, title, user) ->
                                    binding.slideTitle.text = title
                                    binding.slideUser.text = user

                                    if (talk1 != null) {
                                        AnalyticsManager.sendStartEvent(TAG, talk1.url!!)

                                        talk = talk1
                                        loadingDialog!!.dismiss()
                                        setPageNumbers(1, talk!!.slides!!.size)
                                        adapter!!.notifyDataSetChanged()
                                    }
                                }) { throwable -> Toast.makeText(this@SlideActivity, "failed.", Toast.LENGTH_SHORT).show() }
                    }
                })
    }

    /**
     * インタースティシャルを表示する準備が出来ていたら表示する。
     */
    fun displayInterstitial() {
        if (interstitialAd!!.isLoaded) {
            interstitialAd!!.show()
        }
    }

    private fun setPageNumbers(current: Int, max: Int) {
        binding.slidePageNumbers.text = current.toString() + " / " + max
    }

    private fun shareUrl() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, uri!!.toString())
        startActivity(intent)
    }

    private fun shareBrowser() {
        IntentUtil.browse(this, uri!!)
    }

    private fun startAboutActivity() {
        AboutActivity.start(this)
    }

    private fun setRecognizedText() {
        if (talk == null) {
            return
        }
        val (_, _, original) = talk!!.slides!![binding.slideViewPager.currentItem]
        if (recognizeTextMap!!.containsKey(original)) {
            binding.recognizeText.text = recognizeTextMap!!.get(original)
        } else {
            binding.recognizeText.setText(R.string.recognizing)
        }
    }

    private fun moveNext() {
        val current = binding.slideViewPager.currentItem
        binding.slideViewPager.setCurrentItem(current + 1, false)
    }

    private fun movePrev() {
        val current = binding.slideViewPager.currentItem
        binding.slideViewPager.setCurrentItem(current - 1, false)
    }

    private inner class SlideAdapter internal constructor(c: Context) : PagerAdapter() {
        private val inflater: LayoutInflater

        init {
            inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        override fun getCount(): Int {
            return if (talk == null) {
                0
            } else talk!!.slides!!.size
        }

        override fun getItemPosition(`object`: Any): Int {
            // update when call notifyDataSetChanged.
            // http://stackoverflow.com/a/7287121
            return PagerAdapter.POSITION_NONE
        }

        override fun isViewFromObject(view: View, o: Any): Boolean {
            return view == o
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val slide = talk!!.slides!![position]
            val layout = inflater.inflate(R.layout.view_slide, container, false)
            val imageView = layout.findViewById<View>(R.id.slide_image) as PhotoView
            val refreshButton = layout.findViewById<View>(R.id.refresh_button) as TextView
            refreshButton.setOnClickListener { _ -> loadImage(slide, layout, position) }
            imageView.setOnPhotoTapListener { _, x, _ ->
                if (x < 0.5f) {
                    movePrev()
                } else {
                    moveNext()
                }
            }

            loadImage(slide, layout, position)
            setRecognizedText()

            container.addView(layout)
            return layout
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        private fun loadImage(slide: Slide, layout: View, position: Int) {
            Timber.d("loadImage: %d", position)
            val progressBar = layout.findViewById<View>(R.id.slide_image_progress) as ProgressBar
            val imageView = layout.findViewById<View>(R.id.slide_image) as ImageView
            val refreshButton = layout.findViewById<View>(R.id.refresh_button) as TextView
            progressBar.visibility = View.VISIBLE
            refreshButton.visibility = View.GONE

            Glide.with(this@SlideActivity)
                    .asBitmap()
                    .load(slide.original)
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                            Timber.d("Glide onException")
                            progressBar.visibility = View.GONE
                            refreshButton.visibility = View.VISIBLE
                            return false
                        }

                        override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                            Timber.d("Glide onResourceReady: %d", position)
                            if (recognizeTextMap!!.containsKey(slide.original) && position == binding.slideViewPager.currentItem) {
                                setRecognizedText()
                                Timber.d("onResourceReady: %d, contains and same position", position)
                                return false
                            }
                            ocrRecognizer.recognize(slide.original!!, resource)
                            return false
                        }
                    })
                    .into(imageView)
        }
    }

    companion object {
        private val TAG = SlideActivity::class.java.getSimpleName()

        internal fun start(context: Context, url: String) {
            val intent = Intent(context, SlideActivity::class.java)
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        }
    }
}
