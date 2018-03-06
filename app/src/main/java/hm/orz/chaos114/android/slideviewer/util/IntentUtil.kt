package hm.orz.chaos114.android.slideviewer.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object IntentUtil {

    fun browse(context: Context, url: String) {
        browse(context, Uri.parse(url))
    }

    fun browse(context: Context, uri: Uri) {
        Intent().let {
            it.action = Intent.ACTION_VIEW
            it.data = uri
            context.startActivity(it)
        }
    }
}
