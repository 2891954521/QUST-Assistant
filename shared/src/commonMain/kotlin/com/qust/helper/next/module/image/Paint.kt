package com.qust.helper.next.module.image

import androidx.compose.ui.graphics.Color

abstract class Paint {

    companion object {
        fun createPaint(): PlatformPaint = PlatformPaint()
    }

    abstract var color: Color

    abstract var strokeWidth: Int

    abstract var fontSize: Int

}

expect class PlatformPaint(): Paint