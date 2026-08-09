package com.qust.helper.next.module.image

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.awt.RenderingHints
import java.awt.font.TextAttribute
import java.awt.font.TextMeasurer
import java.awt.image.BufferedImage
import java.io.FileOutputStream
import java.text.AttributedString
import javax.imageio.ImageIO

actual class PlatformRenderer actual constructor(
    override val width: Int,
    override val height: Int
) : Renderer {

    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    val graphics = image.createGraphics().apply {
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    }

    override fun drawColor(color: Color) {
        graphics.color = java.awt.Color(color.toArgb(), true)
        graphics.fillRect(0, 0, width, height)
    }

    override fun translate(x: Int, y: Int) {
        graphics.translate(x.toDouble(), y.toDouble())
    }

    override fun rotate(angle: Float, x: Int, y: Int) {
        graphics.rotate(Math.toRadians(angle.toDouble()), width / 2.0, height / 2.0)
    }

    override fun drawText(text: String, x: Int, y: Int, paint: PlatformPaint) {
        graphics.color = paint._color
        graphics.font = paint._font
        graphics.drawString(text, x, y)
    }

    override fun drawSingleLineText(text: String, x: Int, y: Int, maxWidth: Int, paint: PlatformPaint) {
        graphics.color = paint._color
        graphics.font = paint._font
        val count = findMaxCount(text, maxWidth - x)
        if(count >= text.length){
            graphics.drawString(text, x, y)
        }else{
            graphics.drawString(text.substring(0, count) + "···", x, y)
        }
    }

    override fun drawMultiLineText(text: String, x: Int, y: Int, maxWidth: Int, paint: PlatformPaint) {
        val count = findMaxCount(text, maxWidth - x)

        if(count >= text.length) {
            graphics.drawString(text, x, y)
        }else{
            val origin = paint.fontSize
            paint.fontSize = origin * 8 / 10

            val count1 = findMaxCount(text, maxWidth - x)
            graphics.drawString(text.substring(0, count1), x, y)

            val second = text.substring(count1)
            val count2 = findMaxCount(second, maxWidth - x)
            if(count2 >= second.length){
                graphics.drawString(second, x, y + origin)
            }else{
                val count2 = findMaxCount(second, maxWidth - x)
                graphics.drawString(second.substring(0, count2) + "···", x, y + origin)
            }

            paint.fontSize = origin
        }
    }

    override fun drawSingleLineAutoSizeText(text: String, x: Int, y: Int, maxWidth: Int, paint: PlatformPaint) {
        val origin = paint.fontSize

        findMaxSize(origin, text, maxWidth - x)

        graphics.color = paint._color
        graphics.drawString(text, x, y)

        paint.fontSize = origin
    }


    override fun drawQrCode(text: String, x: Int, y: Int, size: Int, paint: PlatformPaint) {
        // TODO
    }

    override fun getPixels(): IntArray {
        return image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
    }

    override fun writeToFile(path: String) {
        FileOutputStream(path).use { output ->
            check(ImageIO.write(image, "png", output)) {
                "No PNG writer is available"
            }
        }
    }

    override fun dispose() {
        graphics.dispose()
    }


    /**
     * 使用字体排版引擎计算指定宽度内能容纳的最大字符数
     */
    private fun findMaxCount(text: String, maxWidth: Int): Int {
        if (text.isEmpty() || maxWidth <= 0) return 0

        val attributedText = AttributedString(text).apply {
            addAttribute(TextAttribute.FONT, graphics.font)
        }
        val textMeasurer = TextMeasurer(attributedText.iterator, graphics.fontRenderContext)
        return textMeasurer.getLineBreakIndex(0, maxWidth.toFloat())
    }

    /**
     * 二分查找找能容下的最大字号（会修改graphics font）
     */
    private fun findMaxSize(originSize: Int, text: String, maxWidth: Int) {
        var left = 1F
        var right = originSize.toFloat()
        while(left <= right) {
            val mid = left + (right - left) / 2
            graphics.font = graphics.font.deriveFont(mid)
            if(graphics.fontMetrics.stringWidth(text) < maxWidth) {
                left = mid + 1F
            } else {
                right = mid - 1F
            }
        }
    }
}
