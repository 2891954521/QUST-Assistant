package com.qust.helper.next.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qust.helper.next.repository.SettingRepository
import kotlin.math.max
import kotlin.math.min

val LocalScale = staticCompositionLocalOf { Scale() }

@Composable
fun AppScale(content: @Composable (() -> Unit)){
    val setting by SettingRepository.uiSetting.collectAsStateWithLifecycle()
    if(setting.percentageLayout){
        LocalScaleProvider(setting.uiScale, setting.fontScale, content)
    }else{
        content()
    }
}

@Composable
expect fun LocalScaleProvider(uiScale: Float, fontScale: Float, content: @Composable (() -> Unit))

enum class UiScale(val title: String, val scale: Float) {
    SMALLEST("最小", 0.5F),
    SMALL("小", 0.75F),
    DEFAULT("默认", 1F),
    BIG("大", 1.12F),
    BIGGEST("最大", 1.25F),
}

enum class FontScale(val title: String, val scale: Float) {
    SMALLEST("最小", 0.5F),
    SMALL("小", 0.75F),
    DEFAULT("默认", 1F),
    BIG("大", 1.5F),
    BIGGEST("最大", 2F),
}

class Scale(
    widthPixels: Int = -1,
    heightPixels: Int = -1,

    /**
     * 设计稿尺寸，单位是 dp，由各平台按自己的参考机型传入
     *
     * 注意：百分比布局下界面的观感尺度是由这两个值决定的。
     * 数值越大 = 设计空间越大 = 每个 dp 在屏幕上占的像素越少 = 界面看起来越小
     */
    designWidth: Float = DESIGN_WIDTH,
    designHeight: Float = DESIGN_HEIGHT,

    uiScale: Float = 1F,
    fontScale: Float = 1F,
) {

    companion object {
        // 兜底设计稿尺寸：只有在百分比布局关闭、没有真实窗口尺寸时才会用到
        const val DESIGN_WIDTH = 1024F // 设计稿宽度（dp）
        const val DESIGN_HEIGHT = 640F // 设计稿高度（dp）
    }

    /**
     * 窗口的像素尺寸，单位是 px。
     *
     * 组成界面时使用的是 dp（当前 Density 下的单位），换算关系是 `dp = px * uiReScale`。
     */
    var screenWidthPx: Int = DESIGN_WIDTH.toInt()
    var screenHeightPx: Int = DESIGN_HEIGHT.toInt()

    /**
     * 界面使用的密度，等于 窗口(px) / 设计稿(dp)，已经包含平台密度和用户的 UI 缩放设置
     */
    var uiScale: Float = 1F

    /**
     * px 转到 dp 的比例，即 1 / uiScale。
     *
     * 用像素值（例如窗口像素尺寸）计算弹出的内容大小时，必须乘上这个值才是 dp 单位下正确的大小
     */
    var uiReScale: Float = 1F

    var fontScale: Float = 1F

    var uiScaleRate: Float = 1F
    var fontScaleRate: Float = 1F

    init {
        this.uiScaleRate = uiScale
        this.fontScaleRate = fontScale

        setDensity(widthPixels, heightPixels, designWidth, designHeight)
    }

    fun setDensity(
        widthPixels: Int,
        heightPixels: Int,
        designWidth: Float = DESIGN_WIDTH,
        designHeight: Float = DESIGN_HEIGHT,
    ) {
        val width: Int = if(widthPixels <= 0) designWidth.toInt() else widthPixels
        val height: Int = if(heightPixels <= 0) designHeight.toInt() else heightPixels

        // 让设计稿的长边对齐窗口的长边，设计稿宽高比和窗口不一致时会按短边缩放
        val landscape: Boolean = width >= height
        val designLong: Float = max(designWidth, designHeight)
        val designShort: Float = min(designWidth, designHeight)

        val dw: Float = if(landscape) designLong else designShort
        val dh: Float = if(landscape) designShort else designLong

        // 窗口尺寸是像素、设计稿是 dp，所以这里算出来的正好就是该用的 Density.density
        val scale: Float = min(width / dw, height / dh)

        this.screenWidthPx = width
        this.screenHeightPx = height

        this.uiScale = scale * uiScaleRate
        this.uiReScale = 1 / this.uiScale

        // 这里不能再乘 scale：Density.density 已经把整个界面（包括 sp 字号）缩放了一次
        this.fontScale = fontScaleRate
    }

    fun getDensity() = Density(density = uiScale, fontScale = fontScale)
}