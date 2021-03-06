package hm.orz.chaos114.android.slideviewer.infra.network

import android.net.Uri
import hm.orz.chaos114.android.slideviewer.infra.model.Slide
import hm.orz.chaos114.android.slideviewer.infra.model.Talk
import hm.orz.chaos114.android.slideviewer.infra.model.TalkMetaData
import hm.orz.chaos114.android.slideviewer.infra.repository.TalkRepository
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.IOException

/**
 * Utility class for SlideShare.
 */
class SlideShareLoader constructor(
        private val talkRepository: TalkRepository
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
            Timber.d("document = %s", document)
            response.close()

            val dataId = document.select("div.speakerdeck-embed")[0].attr("data-id")
            Timber.d("dataId = %s", dataId)
            val url = "https://speakerdeck.com/player/$dataId?"
            Timber.d("src = %s", url)
            val title = document.select("h1")[0].text()
            Timber.d("title = %s", title)
            val user = document.select("h4 .text-dark")[0].text()
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
            val document2 = Jsoup.parse(response2.body()!!.string())
            response2.close()

            val slideList = document2.select(".js-sd-player-slides")[0].children().map {
                Slide(original = it.dataset().get("url"), preview = it.dataset().get("preview-url"))
            }
            Timber.d("slideList = %s", slideList)
            val talk = Talk(url = uri.toString(), slides = slideList)

            talkRepository.saveIfNotExists(talk, slideList, talkMetaData)

            talkMetaData.talk = talk
            subscriber.onNext(talkMetaData)
            subscriber.onComplete()
        }
    }
}
