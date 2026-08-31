package com.qust.helper.next.ui.drawables

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Drawables.GridView: ImageVector
    get() {
        if (_GridView != null) {
            return _GridView!!
        }
        _GridView = ImageVector.Builder(
            name = "GridView",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(5f, 11f)
                horizontalLineToRelative(4f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(5f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                horizontalLineTo(5f)
                curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
                verticalLineToRelative(4f)
                curveTo(3f, 10.1f, 3.9f, 11f, 5f, 11f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(5f, 21f)
                horizontalLineToRelative(4f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineToRelative(-4f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                horizontalLineTo(5f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(4f)
                curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(13f, 5f)
                verticalLineToRelative(4f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(4f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(5f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                horizontalLineToRelative(-4f)
                curveTo(13.9f, 3f, 13f, 3.9f, 13f, 5f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(15f, 21f)
                horizontalLineToRelative(4f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineToRelative(-4f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                horizontalLineToRelative(-4f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(4f)
                curveTo(13f, 20.1f, 13.9f, 21f, 15f, 21f)
                close()
            }
        }.build()

        return _GridView!!
    }

@Suppress("ObjectPropertyName")
private var _GridView: ImageVector? = null
