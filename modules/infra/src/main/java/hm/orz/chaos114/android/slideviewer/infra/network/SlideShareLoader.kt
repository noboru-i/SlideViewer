package hm.orz.chaos114.android.slideviewer.infra.network

import android.content.Context
import android.net.Uri
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import hm.orz.chaos114.android.slideviewer.infra.dao.TalkDao
import hm.orz.chaos114.android.slideviewer.infra.model.Talk
import hm.orz.chaos114.android.slideviewer.infra.model.TalkMetaData
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.IOException
import java.util.regex.Pattern

/**
 * Utility class for SlideShare.
 */
class SlideShareLoader constructor(
        private val applicationContext: Context
) {

    fun load(uri: Uri): Observable<TalkMetaData> {
        return Observable.create create@{ subscriber ->

            val client = OkHttpClient()
            val request = Request.Builder()
                    .url(uri.toString())
                    .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Timber.d("onFailure 1")
                subscriber.onError(IOException("onFailure 1")) // TODO
                return@create
            }
            val document = Jsoup.parse(response.body()!!.string())
            response.close()

            val dataId = document.select("div.speakerdeck-embed")[0].attr("data-id")
            Timber.d("dataId = %s", dataId)
            val url = "https://speakerdeck.com/player/$dataId?"
            Timber.d("src = %s", url)
            val title = document.select("#talk-details header h1")[0].text()
            Timber.d("title = %s", title)
            val user = document.select("#talk-details header h2 a")[0].text()
            Timber.d("user = %s", user)

            val talkMetaData = TalkMetaData()
            talkMetaData.title = title
            talkMetaData.user = user

            subscriber.onNext(talkMetaData)

            val request2 = Request.Builder()
                    .url(url)
                    .build()
            val response2 = client.newCall(request2).execute()
            if (!response2.isSuccessful) {
                Timber.d("onFailure 2")
                subscriber.onError(IOException("onFailure 2")) // TODO
                return@create
            }
            val tmpTalk: Talk
            val responseString = response2.body()!!.string()
            response2.close()
            val pattern = Pattern.compile("var talk = ([^;]*)")
            val matcher = pattern.matcher(responseString)
            if (matcher.find()) {
                Timber.d("group = %s", matcher.group(1))
                val gson = GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create()
                tmpTalk = gson.fromJson(matcher.group(1), Talk::class.java)
                Timber.d("talkObject = %s", tmpTalk)
            } else {
                Timber.d("not match")
                subscriber.onError(RuntimeException("not match. " + responseString))
                return@create
            }

            val dao = TalkDao(applicationContext)
            dao.saveIfNotExists(tmpTalk, tmpTalk.slides!!, talkMetaData)

            talkMetaData.talk = tmpTalk
            subscriber.onNext(talkMetaData)
            subscriber.onComplete()
        }
    }
}
