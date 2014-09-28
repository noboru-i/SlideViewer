package hm.orz.chaos114.android.slideviewer.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.dao.TalkDao;
import hm.orz.chaos114.android.slideviewer.model.Slide;
import hm.orz.chaos114.android.slideviewer.model.Talk;
import hm.orz.chaos114.android.slideviewer.model.TalkMetaData;
import hm.orz.chaos114.android.slideviewer.util.LruCache;

public class SlideListActivity extends Activity {

    @InjectView(R.id.slide_list_list_view)
    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_list);

        ButterKnife.inject(this);

        TalkDao dao = new TalkDao(this);
        List<Talk> talks = dao.list();
        SlideListAdapter adapter = new SlideListAdapter(talks);
        mListView.setAdapter(adapter);
    }

    class SlideListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<Talk> mTalks;
        private RequestQueue mQueue;
        private ImageLoader.ImageCache mImageCache;

        SlideListAdapter(List<Talk> talks) {
            mTalks = talks;
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mQueue = Volley.newRequestQueue(SlideListActivity.this);
            mImageCache = new LruCache();
        }

        @Override
        public int getCount() {
            return mTalks.size();
        }

        @Override
        public Talk getItem(int position) {
            return mTalks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            Talk item = getItem(position);
            List<Slide> slides = item.getSlides();
            TalkDao dao = new TalkDao(SlideListActivity.this);
            TalkMetaData talkMetaData = dao.findMetaData(item);
            if (v == null) {
                v = mInflater.inflate(R.layout.slide_list_row, null);
            }

            NetworkImageView slideImage = (NetworkImageView) v.findViewById(R.id.slide_list_row_image);
            slideImage.setImageUrl(slides.get(0).getPreview(), new ImageLoader(mQueue, mImageCache));
            if (talkMetaData != null) {
                TextView titleView = (TextView) v.findViewById(R.id.slide_list_row_title);
                titleView.setText(talkMetaData.getTitle());
                TextView userView = (TextView) v.findViewById(R.id.slide_list_row_user);
                userView.setText("by " + talkMetaData.getUser());
            }

            return v;
        }
    }
}
