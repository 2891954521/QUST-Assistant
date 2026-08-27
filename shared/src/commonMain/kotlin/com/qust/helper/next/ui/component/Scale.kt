package com.qust.helper.next.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Density
import com.russhwolf.settings.get
import com.qust.helper.next.common.setting.AppSetting
import kotlin.math.min

val LocalScale = staticCompositionLocalOf { Scale() }

var AppUIScale by mutableFloatStateOf(AppSetting["uiScale", 1F])

var AppFontScale by mutableFloatStateOf(AppSetting["fontScale", 1F])

@Composable
expect fun LocalScaleProvider(content: @Composable (() -> Unit))

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

enum class UiOrientation(val code: Int) {
    PORTRAIT(1),
    LANDSCAPE(2);

    companion object {
        fun from(code: Int) = if(code == 1) PORTRAIT else LANDSCAPE
    }
}

class Scale(
    widthPixels: Int = -1,
    heightPixels: Int = -1,

    uiScale: Float = 1F,
    fontScale: Float = 1F,

    orientation: UiOrientation = UiOrientation.LANDSCAPE,
) {

    companion object {
        const val DESIGN_WIDTH = 1024F // 设计稿宽度
        const val DESIGN_HEIGHT = 640F // 设计稿高度
    }

    var screenWidth: Int = 1024
    var screenHeight: Int = 640

    /**
     * UI显示到当前屏幕上需要的缩放比例
     */
    var uiScale: Float = 1F

    /**
     * 反向缩放比例，在正常页面计算弹出层的大小时需要额外乘以这个值才是正确的大小
     */
    var uiReScale: Float = 1F

    var fontScale: Float = 1F

    var uiScaleRate: Float = 1F
    var fontScaleRate: Float = 1F

    init {
        this.uiScaleRate = uiScale
        this.fontScaleRate = fontScale

        setDensity(widthPixels, heightPixels, orientation)
    }

    fun setDensity(
        widthPixels: Int,
        heightPixels: Int,
        orientation: UiOrientation
    ) {
        val designWidth: Int
        val designHeight: Int

        // 由于屏幕旋转系统会自动交换宽高，而设计稿常量是固定的，所以这里要手动交换
        if(orientation == UiOrientation.PORTRAIT) {
            designWidth = DESIGN_HEIGHT.toInt()
            designHeight = DESIGN_WIDTH.toInt()
        } else {
            designWidth = DESIGN_WIDTH.toInt()
            designHeight = DESIGN_HEIGHT.toInt()
        }

        val screenWidth: Int = if(widthPixels <= 0) designWidth else widthPixels
        val screenHeight: Int = if(heightPixels <= 0) designHeight else heightPixels

        val scale: Float = min(screenWidth / DESIGN_WIDTH, screenHeight / DESIGN_HEIGHT)
        val reScale: Float = 1 / scale

        this.screenWidth = screenWidth // (screenWidth * uiScaleRate).toInt()
        this.screenHeight = screenHeight // (screenHeight * uiScaleRate).toInt()

        this.uiScale = scale * uiScaleRate
        this.uiReScale = reScale / uiScaleRate

        this.fontScale = fontScaleRate // scale * fontScaleRate

//        Logger.i("device width: ${this.screenWidth}, height: ${this.screenHeight}")
//        Logger.i("scale: ${scale}, reScale: $reScale")
//        Logger.i("uiScale: ${this.uiScale}, uiReScale: ${this.uiReScale}")
    }

    fun getDensity() = Density(density = uiScale, fontScale = fontScale)
}