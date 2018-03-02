package hm.orz.chaos114.android.slideviewer.ocr.util

import android.content.Context
import hm.orz.chaos114.android.slideviewer.ocr.model.Language
import java.io.File

object DirectorySettings {
    @JvmStatic
    fun hasFile(context: Context, language: Language): Boolean {
        val dir = getTessdataDir(context)
        val file = File(dir, language.id + ".traineddata")
        return file.exists()
    }

    @JvmStatic
    fun getTessdataDir(context: Context): File {
        val dir = File(context.getExternalFilesDir("tesseract"), "tessdata")
        if (!dir.exists() && !dir.mkdirs()) {
            throw RuntimeException("cannot create file dir.")
        }
        return dir
    }
}
