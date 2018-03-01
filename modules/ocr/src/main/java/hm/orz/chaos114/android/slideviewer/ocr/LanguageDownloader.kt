package hm.orz.chaos114.android.slideviewer.ocr

import android.content.Context
import hm.orz.chaos114.android.slideviewer.ocr.model.Language
import hm.orz.chaos114.android.slideviewer.ocr.util.DirectorySettings
import io.reactivex.Single
import okhttp3.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class LanguageDownloader {
    fun download(context: Context, language: Language): Single<File> {
        return Single.create { subscriber ->
            val request = Request.Builder()
                    .url(language.url)
                    .build()

            val client = OkHttpClient()
            client.newCall(request).enqueue(
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Timber.d(e, "onFailure")
                            subscriber.onError(e)
                        }

                        @Throws(IOException::class)
                        override fun onResponse(call: Call, response: Response) {
                            Timber.d("onResponse: %d", response.code())

                            val dir = DirectorySettings.getTessdataDir(context)
                            val file = File(dir, language.id + ".traineddata")
                            var fos: FileOutputStream? = null
                            try {
                                fos = FileOutputStream(file)
                                fos.write(response.body()!!.bytes())
                            } catch (e: IOException) {
                                throw RuntimeException(e)
                            } finally {
                                if (fos != null) {
                                    try {
                                        fos.close()
                                    } catch (e: IOException) {
                                        // ignore
                                        Timber.d(e)
                                    }

                                }
                            }
                            subscriber.onSuccess(file)
                        }
                    }
            )
        }
    }
}