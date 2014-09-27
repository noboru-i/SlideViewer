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

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.dao.TalkDao;
import hm.orz.chaos114.android.slideviewer.model.Talk;

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

        SlideListAdapter(List<Talk> talks) {
            mTalks = talks;
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            if (v == null) {
                v = mInflater.inflate(R.layout.slide_list_row, null);
            }

            TextView titleView = (TextView) v.findViewById(R.id.slide_list_row_title);
            titleView.setText(item.getUrl());

            return v;
        }
    }
}
