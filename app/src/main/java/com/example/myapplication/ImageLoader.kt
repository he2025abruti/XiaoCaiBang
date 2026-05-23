package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ImageLoader {

    private val handler = Handler(Looper.getMainLooper())

    // 内存缓存，使用最大可用内存的 1/8
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    /**
     * 异步加载图片到 ImageView
     * @param url 图片 URL
     * @param imageView 目标 ImageView
     * @param circular 是否裁剪为圆形
     */
    fun loadImage(url: String, imageView: ImageView, circular: Boolean = true) {
        // 先检查缓存
        val cached = memoryCache.get(url)
        if (cached != null) {
            imageView.setImageBitmap(cached)
            return
        }

        // 标记 URL，防止复用 ViewHolder 时图片错位
        imageView.tag = url
        imageView.setImageBitmap(null)

        Thread {
            val bitmap = downloadImage(url)
            if (bitmap != null) {
                val result = if (circular) circularCrop(bitmap) else bitmap
                memoryCache.put(url, result)
                handler.post {
                    // 仅当 ImageView 的 tag 仍为当前 URL 时才设置（防止错位）
                    if (imageView.tag == url) {
                        imageView.setImageBitmap(result)
                    }
                }
            }
        }.start()
    }

    /**
     * 同步下载图片
     */
    private fun downloadImage(urlStr: String): Bitmap? {
        var conn: HttpURLConnection? = null
        var input: InputStream? = null
        return try {
            val url = URL(urlStr)
            conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.doInput = true
            conn.connect()

            input = conn.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            null
        } finally {
            input?.close()
            conn?.disconnect()
        }
    }

    /**
     * 将方形 Bitmap 裁剪为圆形
     */
    fun circularCrop(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, size, size)

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // 居中裁剪
        val srcLeft = (bitmap.width - size) / 2
        val srcTop = (bitmap.height - size) / 2
        val srcRect = Rect(srcLeft, srcTop, srcLeft + size, srcTop + size)
        canvas.drawBitmap(bitmap, srcRect, rect, paint)

        return output
    }

    /**
     * 清除缓存
     */
    fun clearCache() {
        memoryCache.evictAll()
    }
}
