package hm.orz.chaos114.android.slideviewer.ui

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.AndroidInjection
import hm.orz.chaos114.android.slideviewer.R
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySlideListBinding
import hm.orz.chaos114.android.slideviewer.infra.model.Talk
import hm.orz.chaos114.android.slideviewer.infra.repository.TalkRepository
import hm.orz.chaos114.android.slideviewer.util.AdRequestGenerator
import hm.orz.chaos114.android.slideviewer.widget.SlideListRowView
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class SlideListActivity : AppCompatActivity() {

    @Inject
    lateinit var talkRepository: TalkRepository
    @Inject
    lateinit var adRequestGenerator: AdRequestGenerator

    private lateinit var binding: ActivitySlideListBinding
    private lateinit var adapter: SlideListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_slide_list)
        adapter = SlideListAdapter(talkRepository)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setTitle(R.string.slide_list_title)

        binding.list.adapter = adapter
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.emptyLayout.setOnClickListener { _ -> openSpeakerDeck() }

        binding.adView.loadAd(adRequestGenerator.generate())
    }

    override fun onPause() {
        binding.adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.adView.resume()

        talkRepository.list()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { talks ->
                    if (talks.isEmpty()) {
                        binding.emptyLayout.visibility = View.VISIBLE
                        binding.list.visibility = View.GONE
                    } else {
                        binding.emptyLayout.visibility = View.GONE
                        binding.list.visibility = View.VISIBLE
                    }
                    adapter.updateData(talks)
                }
    }

    override fun onDestroy() {
        binding.adView.destroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.slide_list_activity_menus, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                openSpeakerDeck()
                true
            }
            R.id.menu_share -> {
                shareUrl()
                true
            }
            R.id.menu_about -> {
                AboutActivity.start(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareUrl() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=hm.orz.chaos114.android.slideviewer")
        startActivity(intent)
    }

    private fun openSpeakerDeck() {
        WebViewActivity.start(this, "https://speakerdeck.com/")
    }

    private class SlideListAdapter constructor(
            private val talkRepository: TalkRepository
    ) : RecyclerView.Adapter<ViewHolder>() {
        private var mTalks: List<Talk> = ArrayList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = SlideListRowView(parent.context)
            view.setOnClickListener { _ -> onClick(parent.context, view.tag as Talk) }
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = mTalks[position]
            val slides = item.slides
            val talkMetaData = talkRepository.findMetaData(item)

            (holder.itemView as SlideListRowView).bind(slides!!, talkMetaData)
            holder.itemView.setTag(talkMetaData!!.talk)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemCount(): Int {
            return mTalks.size
        }

        internal fun updateData(talks: List<Talk>) {
            mTalks = talks
            notifyDataSetChanged()
        }

        private fun onClick(context: Context, talk: Talk) {
            SlideActivity.start(context, talk.url!!)
        }
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
