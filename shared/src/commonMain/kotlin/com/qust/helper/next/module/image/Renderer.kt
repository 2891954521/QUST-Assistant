package com.qust.helper.next.module.image

import androidx.compose.ui.graphics.Color

/**
 * 离屏渲染渲染器
 */
interface Renderer {

    companion object {
        fun createRenderer(width: Int, height: Int): PlatformRenderer = PlatformRenderer(width, height)
    }

    val width: Int

    val height: Int

    /**
     * 使用指定颜色填充整个画布
     */
    fun drawColor(color: Color)

    /**
     * 平移
     */
    fun translate(x: Int, y: Int)

    /**
     * 旋转
     * 
     * @param angle 旋转角度 (角度制)
     * @param x 旋转中心点x坐标
     * @param y 旋转中心点y坐标
     */
    fun rotate(angle: Float, x: Int, y: Int)

    /**
     * 绘制文本
     */
    fun drawText(text: String, x: Int, y: Int, paint: PlatformPaint)

    /**
     * 绘制单行文本，超出边界则截断
     */
    fun drawSingleLineText(text: String, x: Int, y: Int, maxWidth: Int, paint: PlatformPaint)

    /**
     * 绘制多行文本，超出边界自动换行
     */
    fun drawMultiLineText(text: String, x: Int, y: Int, maxWidth: Int, paint: PlatformPaint)

    /**
     * 绘制单行文本，超出边界则自动缩小字体大小
     */
    fun drawSingleLineAutoSizeText(text: String, x: Int, y: Int, maxWidth: Int, paint: PlatformPaint)

    /**
     * 绘制二维码
     */
    fun drawQrCode(text: String, x: Int, y: Int, size: Int, paint: PlatformPaint)

    /**
     * 获取图片像素数组
     */
    fun getPixels(): IntArray

    /**
     * 将图片写出到文件
     */
    fun writeToFile(path: String)

    /**
     * 释放资源
     */
    fun dispose()
}

expect class PlatformRenderer(width: Int, height: Int): Renderer