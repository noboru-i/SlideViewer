package hm.orz.chaos114.android.slideviewer.util;

import android.content.Context;
import android.net.Uri;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hm.orz.chaos114.android.slideviewer.infra.dao.TalkDao;
import hm.orz.chaos114.android.slideviewer.infra.model.Talk;
import hm.orz.chaos114.android.slideviewer.infra.model.TalkMetaData;
import io.reactivex.Observable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Utility class for SlideShare.
 */
public final class SlideShareLoader {
    private SlideShareLoader() {
        // prevent instantiate.
    }

    public static Observable<TalkMetaData> load(Context applicationContext, Uri uri) {
        return Observable.create(subscriber -> {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(uri.toString())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Timber.d(e, "onFailure");
                    subscriber.onError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        Document document = Jsoup.parse(response.body().string());

                        String dataId = document.select("div.speakerdeck-embed").get(0).attr("data-id");
                        Timber.d("dataId = %s", dataId);
                        String url = "https://speakerdeck.com/player/" + dataId + "?";
                        Timber.d("src = %s", url);
                        String title = document.select("#talk-details header h1").get(0).text();
                        Timber.d("title = %s", title);
                        String user = document.select("#talk-details header h2 a").get(0).text();
                        Timber.d("user = %s", user);

                        TalkMetaData talkMetaData = new TalkMetaData();
                        talkMetaData.setTitle(title);
                        talkMetaData.setUser(user);

                        subscriber.onNext(talkMetaData);

                        Request request = new Request.Builder()
                                .url(url)
                                .build();
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Timber.d(e, "onFailure");
                                subscriber.onError(e);
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    final Talk tmpTalk;
                                    String responseString = response.body().string();
                                    Pattern pattern = Pattern.compile("var talk = ([^;]*)");
                                    Matcher matcher = pattern.matcher(responseString);
                                    if (matcher.find()) {
                                        Timber.d("group = %s", matcher.group(1));
                                        Gson gson = new GsonBuilder()
                                                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                                .create();
                                        tmpTalk = gson.fromJson(matcher.group(1), Talk.class);
                                        Timber.d("talkObject = %s", tmpTalk);
                                    } else {
                                        Timber.d("not match");
                                        subscriber.onError(new RuntimeException("not match. " + responseString));
                                        return;
                                    }

                                    TalkDao dao = new TalkDao(applicationContext);
                                    dao.saveIfNotExists(tmpTalk, tmpTalk.getSlides(), talkMetaData);

                                    talkMetaData.setTalk(tmpTalk);
                                    subscriber.onNext(talkMetaData);
                                    subscriber.onComplete();

                                } catch (Exception e) {
                                    Timber.e(e);
                                    subscriber.onError(e);
                                }
                            }
                        });
                    } catch (Exception e) {
                        Timber.e(e);
                        subscriber.onError(e);
                    }
                }
            });
        });
    }
}
