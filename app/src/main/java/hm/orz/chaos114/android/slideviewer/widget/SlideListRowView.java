package hm.orz.chaos114.android.slideviewer.widget;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.util.List;

import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.databinding.ViewSlideListRowBinding;
import hm.orz.chaos114.android.slideviewer.model.Slide;
import hm.orz.chaos114.android.slideviewer.model.TalkMetaData;

public class SlideListRowView extends RelativeLayout {

    private ViewSlideListRowBinding binding;

    public SlideListRowView(Context context) {
        this(context, null);
    }

    public SlideListRowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideListRowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_slide_list_row, this, true);
    }

    public void bind(List<Slide> slides, TalkMetaData talkMetaData) {
        Glide.with(getContext()).load(slides.get(0).getPreview()).into(binding.slideListRowImage);
        if (talkMetaData != null) {
            binding.slideListRowTitle.setText(talkMetaData.getTitle());
            binding.slideListRowUser.setText(getContext().getString(R.string.slide_list_author, talkMetaData.getUser()));
        }
    }
}
