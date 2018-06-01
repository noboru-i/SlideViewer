package hm.orz.chaos114.android.slideviewer.widget

import android.content.Context
import android.databinding.DataBindingUtil
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout

import com.bumptech.glide.Glide

import hm.orz.chaos114.android.slideviewer.R
import hm.orz.chaos114.android.slideviewer.databinding.ViewSlideListRowBinding
import hm.orz.chaos114.android.slideviewer.infra.model.Slide
import hm.orz.chaos114.android.slideviewer.infra.model.TalkMetaData

class SlideListRowView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val binding: ViewSlideListRowBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_slide_list_row, this, true)

    fun bind(slides: List<Slide>?, talkMetaData: TalkMetaData?) {
        slides?.let {
            Glide.with(context).load(it[0].preview).into(binding.slideListRowImage)
        }
        talkMetaData?.let {
            binding.slideListRowTitle.text = it.title
            binding.slideListRowUser.text = context.getString(R.string.slide_list_author, it.user)
        }
    }
}
