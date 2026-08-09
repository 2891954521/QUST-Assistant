package com.qust.helper.next.module.image

import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

actual class PlatformPaint: com.qust.helper.next.module.image.Paint() {

    val paint: Paint = Paint()

    override var color: Color
        get() = Color(paint.color)
        set(value) {
            paint.color = value.toArgb()
        }

    override var strokeWidth: Int
        get() = paint.strokeWidth.toInt()
        set(value) {
            paint.strokeWidth = value.toFloat()
        }

    override var fontSize: Int
        get() = paint.textSize.toInt()
        set(value) {
            paint.textSize = value.toFloat()
        }

}