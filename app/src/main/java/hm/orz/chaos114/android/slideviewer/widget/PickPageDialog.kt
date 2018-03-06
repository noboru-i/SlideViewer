package hm.orz.chaos114.android.slideviewer.widget

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.SeekBar
import hm.orz.chaos114.android.slideviewer.R
import hm.orz.chaos114.android.slideviewer.databinding.DialogPickPageBinding
import java.util.Locale

/**
 * Show dialog for pick page.
 */
object PickPageDialog {

    fun show(context: Context, currentPage: Int, maxPage: Int, listener: OnPickPageListener) {
        val binding = DataBindingUtil.inflate<DialogPickPageBinding>(LayoutInflater.from(context), R.layout.dialog_pick_page, null, false)
        binding.maxPageView.text = String.format(Locale.getDefault(), "%d / %d", currentPage + 1, maxPage)
        binding.seekBar.max = maxPage - 1
        binding.seekBar.progress = currentPage
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) {
                    return
                }
                binding.maxPageView.text = String.format(Locale.getDefault(), "%d / %d", progress + 1, maxPage)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        AlertDialog.Builder(context)
                .setTitle("Select page")
                .setPositiveButton(android.R.string.ok) { _, _ -> listener.onPickPage(binding.seekBar.progress) }
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .setView(binding.root)
                .show()
    }

    interface OnPickPageListener {
        fun onPickPage(page: Int)
    }
}
