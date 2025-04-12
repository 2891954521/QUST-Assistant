package com.qust.helper.ui.drawables

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Drawables.Electric: ImageVector
    get() {
        if (_Electric != null) {
            return _Electric!!
        }
        _Electric = ImageVector.Builder(
            name = "Electric",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(735.3f, 102.5f)
                lineTo(250.1f, 521f)
                lineToRelative(260.1f, 82.9f)
                lineToRelative(-203.1f, 317.6f)
                lineToRelative(519.1f, -384.4f)
                lineToRelative(-272.7f, -80.3f)
            }
        }.build()

        return _Electric!!
    }

@Suppress("ObjectPropertyName")
private var _Electric: ImageVector? = null
