package hm.orz.chaos114.android.slideviewer.util;

import android.graphics.Bitmap;

import com.android.volley.toolbox.ImageLoader;

public class LruCache implements ImageLoader.ImageCache {

    private android.util.LruCache<String, Bitmap> mMemoryCache;

    public LruCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // 最大メモリに依存
        int cacheSize = maxMemory / 8;

        mMemoryCache = new android.util.LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // 使用キャッシュサイズ(KB単位)
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    // ImageCacheのインターフェイス実装
    @Override
    public Bitmap getBitmap(String url) {
        return mMemoryCache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        mMemoryCache.put(url, bitmap);
    }
}
