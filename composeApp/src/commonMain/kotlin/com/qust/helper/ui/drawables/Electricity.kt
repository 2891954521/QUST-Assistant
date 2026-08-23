package com.qust.helper.ui.drawables

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Drawables.Electricity: ImageVector
    get() {
        if (_Electricity != null) {
            return _Electricity!!
        }
        _Electricity = ImageVector.Builder(
            name = "Electricity",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(11f, 21f)
                horizontalLineToRelative(-1f)
                lineToRelative(1f, -7f)
                horizontalLineTo(7.5f)
                curveToRelative(-0.88f, 0f, -0.33f, -0.75f, -0.31f, -0.78f)
                curveToRelative(0.19f, -0.29f, 4.31f, -6.22f, 4.31f, -6.22f)
                horizontalLineToRelative(1f)
                lineToRelative(-1f, 7f)
                horizontalLineTo(14f)
                lineToRelative(-3f, 8f)
                close()
            }
        }.build()

        return _Electricity!!
    }

@Suppress("ObjectPropertyName")
private var _Electricity: ImageVector? = null
