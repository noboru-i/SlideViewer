package hm.orz.chaos114.android.slideviewer.ocr

import android.content.Context
import hm.orz.chaos114.android.slideviewer.ocr.model.Language
import hm.orz.chaos114.android.slideviewer.ocr.util.DirectorySettings
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class LanguageDownloader {
    fun download(context: Context, language: Language): Single<File> {
        return Single.create<File>({ subscriber ->
            val request = Request.Builder()
                    .url(language.url)
                    .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute();
            if (!response.isSuccessful) {
                Timber.d("download error %s", response)
                subscriber.onError(IOException("Unexpected code " + response))
                return@create
            }

            Timber.d("onResponse: %d", response.code())

            val dir = DirectorySettings.getTessdataDir(context)
            val file = File(dir, language.id + ".traineddata")
            FileOutputStream(file).use {
                it.write(response.body()!!.bytes())
            }
            subscriber.onSuccess(file)
        }).subscribeOn(Schedulers.computation())
    }
}
