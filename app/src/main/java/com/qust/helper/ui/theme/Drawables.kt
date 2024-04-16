package com.qust.helper.ui.theme

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

object Drawables {

	var _Brightness: ImageVector? = null

	val Brightness: ImageVector
		get() {
			if (_Brightness != null) {
				return _Brightness!!
			}
			_Brightness = materialIcon(name = "Drawables.Brightness") {
				materialPath {
					moveTo(20.0F, 8.69F)
					lineTo(20.0F, 6.0F)
					curveToRelative(0.0F, -1.1F, -0.9F, -2.0F, -2.0F, -2.0F)
					horizontalLineToRelative(-2.69F)
					lineToRelative(-1.9F, -1.9F)
					curveToRelative(-0.78F, -0.78F, -2.05F, -0.78F, -2.83F, 0.0F)
					lineTo(8.69F, 4.0F)
					lineTo(6.0F, 4.0F)
					curveToRelative(-1.1F, 0.0F, -2.0F, 0.9F, -2.0F, 2.0F)
					verticalLineToRelative(2.69F)
					lineToRelative(-1.9F, 1.9F)
					curveToRelative(-0.78F, 0.78F, -0.78F, 2.05F, 0.0F, 2.83F)
					lineToRelative(1.9F, 1.9F)
					lineTo(4.0F, 18.0F)
					curveToRelative(0.0F, 1.1F, 0.9F, 2.0F, 2.0F, 2.0F)
					horizontalLineToRelative(2.69F)
					lineToRelative(1.9F, 1.9F)
					curveToRelative(0.78F, 0.78F, 2.05F, 0.78F, 2.83F, 0.0F)
					lineToRelative(1.9F, -1.9F)
					lineTo(18.0F, 20.0F)
					curveToRelative(1.1F, 0.0F, 2.0F, -0.9F, 2.0F, -2.0F)
					verticalLineToRelative(-2.69F)
					lineToRelative(1.9F, -1.9F)
					curveToRelative(0.78F, -0.78F, 0.78F, -2.05F, 0.0F, -2.83F)
					lineTo(20.0F, 8.69F)

					moveTo(12.0F, 18.0F)
					curveToRelative(-3.31F, 0.0F, -6.0F, -2.69F, -6.0F, -6.0F)
					reflectiveCurveToRelative(2.69F, -6.0F, 6.0F, -6.0F)
					reflectiveCurveToRelative(6.0F, 2.69F, 6.0F, 6.0F)
					reflectiveCurveToRelative(-2.69F, 6.0F, -6.0F, 6.0F)

					moveTo(12.0F, 8.0F)
					curveToRelative(-2.21F, 0.0F, -4.0F, 1.79F, -4.0F, 4.0F)
					reflectiveCurveToRelative(1.79F, 4.0F, 4.0F, 4.0F)
					reflectiveCurveToRelative(4.0F, -1.79F, 4.0F, -4.0F)
					reflectiveCurveToRelative(-1.79F, -4.0F, -4.0F, -4.0F)
					close()
				}
			}
			return _Brightness!!
		}
}