package hm.orz.chaos114.android.slideviewer.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object IntentUtil {

    fun browse(context: Context, url: String) {
        browse(context, Uri.parse(url))
    }

    fun browse(context: Context, uri: Uri) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = uri
        context.startActivity(intent)
    }
}// no-op
