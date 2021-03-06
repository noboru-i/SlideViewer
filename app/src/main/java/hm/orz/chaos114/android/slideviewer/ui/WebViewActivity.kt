package hm.orz.chaos114.android.slideviewer.ui

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import hm.orz.chaos114.android.slideviewer.R
import hm.orz.chaos114.android.slideviewer.databinding.ActivityWebViewBinding
import hm.orz.chaos114.android.slideviewer.util.UrlHelper
import timber.log.Timber

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view)

        init()

        if (savedInstanceState == null) {
            val url = intent.getStringExtra(EXTRA_URL)
            binding.webView.loadUrl(url)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webView.destroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun init() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayShowTitleEnabled(false)
            it.setHomeButtonEnabled(true)
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Timber.d("url: %s", url)
                val uri = Uri.parse(url)
                if (UrlHelper.isSpeakerDeckUrl(uri) && UrlHelper.canOpen(uri)) {
                    SlideActivity.start(this@WebViewActivity, url)
                    finish()
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                binding.toolbar.title = view.title
            }
        }
        binding.webView.settings.let {
            it.javaScriptEnabled = true
            it.builtInZoomControls = true
            it.loadWithOverviewMode = true
            it.useWideViewPort = true
        }
    }

    companion object {
        const val EXTRA_URL = "extra_url"

        internal fun start(context: Context, url: String) =
                context.startActivity(Intent(context, WebViewActivity::class.java).also {
                    it.putExtra(EXTRA_URL, url)
                })
    }
}
