package com.qust.helper.ui.drawables

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Drawables.Globe: ImageVector
    get() {
        if (_Globe != null) {
            return _Globe!!
        }
        _Globe = ImageVector.Builder(
            name = "Globe",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000)), pathFillType = PathFillType.EvenOdd) {
                moveTo(12f, 2.5f)
                curveTo(6.75f, 2.5f, 2.5f, 6.75f, 2.5f, 12f)
                curveTo(2.5f, 17.25f, 6.75f, 21.5f, 12f, 21.5f)
                curveTo(17.25f, 21.5f, 21.5f, 17.25f, 21.5f, 12f)
                curveTo(21.5f, 6.75f, 17.25f, 2.5f, 12f, 2.5f)
                close()

                moveTo(12f, 5f)
                curveTo(8.13f, 5f, 5f, 8.13f, 5f, 12f)
                curveTo(5f, 15.87f, 8.13f, 19f, 12f, 19f)
                curveTo(15.87f, 19f, 19f, 15.87f, 19f, 12f)
                curveTo(19f, 8.13f, 15.87f, 5f, 12f, 5f)
                close()

                moveTo(2f, 11.2f)
                lineTo(22f, 11.2f)
                lineTo(22f, 12.8f)
                lineTo(2f, 12.8f)
                close()

                moveTo(11.2f, 2f)
                lineTo(12.8f, 2f)
                lineTo(12.8f, 22f)
                lineTo(11.2f, 22f)
                close()
            }
        }.build()

        return _Globe!!
    }

@Suppress("ObjectPropertyName")
private var _Globe: ImageVector? = null
