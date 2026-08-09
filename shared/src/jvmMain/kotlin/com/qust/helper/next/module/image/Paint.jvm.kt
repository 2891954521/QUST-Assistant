package com.qust.helper.next.module.image

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.awt.Font

actual class PlatformPaint actual constructor() : Paint(){

    var _color = java.awt.Color(0)

    var _font = Font(null, Font.PLAIN, 22)

    override var color: Color
        get() = Color(_color.rgb)
        set(value) {
            _color = java.awt.Color(color.toArgb(), true)
        }

    override var strokeWidth: Int = 0

    override var fontSize: Int
        get() = _font.size
        set(value) {
            _font = _font.deriveFont(value.toFloat())
        }

}