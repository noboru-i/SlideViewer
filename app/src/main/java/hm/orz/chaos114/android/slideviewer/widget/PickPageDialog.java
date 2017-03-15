package hm.orz.chaos114.android.slideviewer.widget;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.SeekBar;

import java.util.Locale;

import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.databinding.DialogPickPageBinding;

/**
 * Show dialog for pick page.
 */
public final class PickPageDialog {
    private PickPageDialog() {
        // prevent
    }

    public static void show(Context context, int currentPage, int maxPage, OnPickPageListener listener) {
        DialogPickPageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_pick_page, null, false);
        binding.maxPageView.setText(String.format(Locale.getDefault(), "%d / %d", (currentPage + 1), maxPage));
        binding.seekBar.setMax(maxPage - 1);
        binding.seekBar.setProgress(currentPage);
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                binding.maxPageView.setText(String.format(Locale.getDefault(), "%d / %d", (progress + 1), maxPage));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        new AlertDialog.Builder(context)
                .setTitle("Select page")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> listener.onPickPage(binding.seekBar.getProgress()))
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)
                .setView(binding.getRoot())
                .show();
    }

    public interface OnPickPageListener {
        void onPickPage(int page);
    }
}
