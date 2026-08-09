package com.qust.helper.next.module.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import com.qust.helper.next.module.image.PlatformPaint
import com.qust.helper.next.module.image.Renderer
import java.io.FileOutputStream

actual class PlatformRenderer actual constructor(
    override val width: Int,
    override val height: Int
): Renderer {

    val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    override fun drawColor(color: Color) {
        canvas.drawColor(color.toArgb())
    }

    override fun translate(x: Int, y: Int) {
        canvas.translate(x.toFloat(), y.toFloat())
    }

    override fun rotate(angle: Float, x: Int, y: Int) {
        canvas.rotate(angle, x.toFloat(), y.toFloat())
    }

    override fun drawText(text: String, x: Int, y: Int, paint: PlatformPaint) {
        canvas.drawText(text, x.toFloat(), y.toFloat(), paint.paint)
    }

    override fun drawSingleLineText(text: String, x: Int, y: Int, maxWidth: Int, paint: PlatformPaint) {
        val count = findMaxCount(paint.paint, text, maxWidth - x)
        if(count >= text.length){
            canvas.drawText(text, x.toFloat(), y.toFloat(), paint.paint)
        }else{
            canvas.drawText(text.substring(0, count) + "···", x.toFloat(), y.toFloat(), paint.paint)
        }
    }

    override fun drawMultiLineText(text: String, x: Int, y: Int, maxWidth: Int, paint: PlatformPaint) {
        val count = findMaxCount(paint.paint, text, maxWidth - x)

        if(count >= text.length) {
            canvas.drawText(text, x.toFloat(), y.toFloat(), paint.paint)
        }else{
            val origin = paint.fontSize
            paint.fontSize = origin * 8 / 10

            val count = findMaxCount(paint.paint, text, maxWidth - x)

            canvas.drawText(text.substring(0, count), x.toFloat(), y.toFloat(), paint.paint)

            val second = text.substring(count)
            if(x + paint.paint.measureText(second) < maxWidth){
                canvas.drawText(second, x.toFloat(), (y + origin).toFloat(), paint.paint)

            }else{
                val count2 = findMaxCount(paint.paint, second, maxWidth - x)
                canvas.drawText(second.substring(0, count2) + "···", x.toFloat(), (y + origin).toFloat(), paint.paint)
            }

            paint.fontSize = origin
        }
    }

    override fun drawSingleLineAutoSizeText(text: String, x: Int, y: Int, maxWidth: Int, paint: PlatformPaint) {
        val origin = paint.fontSize

        findMaxSize(paint.paint, origin, text, maxWidth - x)
        canvas.drawText(text, x.toFloat(), y.toFloat(), paint.paint)

        paint.fontSize = origin
    }

    override fun drawQrCode(text: String, x: Int, y: Int, size: Int, paint: PlatformPaint) {
        // TODO
    }

    override fun getPixels(): IntArray {
        return IntArray(width * height).also {
            bitmap.getPixels(it, 0, width, 0, 0, width, height)
        }
    }

    override fun writeToFile(path: String) {
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, FileOutputStream(path))
    }

    override fun dispose() {
        bitmap.recycle()
    }


    /**
     * 使用 Android 字体测量引擎计算指定宽度内能容纳的最大字符数
     */
    private fun findMaxCount(paint: Paint, text: String, maxWidth: Int): Int {
        if (text.isEmpty() || maxWidth <= 0) return 0
        return paint.breakText(text, true, maxWidth.toFloat(), null)
    }

    /**
     * 二分查找找能容下的最大字号（会修改paint字号）
     */
    private fun findMaxSize(paint: Paint, originSize: Int, text: String, maxWidth: Int) {
        var left = 1F
        var right = originSize.toFloat()
        while(left <= right) {
            val mid = left + (right - left) / 2
            paint.textSize = mid
            if(paint.measureText(text) < maxWidth) {
                left = mid + 1F
            } else {
                right = mid - 1F
            }
        }
    }
}