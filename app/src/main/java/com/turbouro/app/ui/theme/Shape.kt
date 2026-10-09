package com.turbouro.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val SmallButtonShape = RoundedCornerShape(10.dp)
val PrimaryButtonShape = RoundedCornerShape(12.dp)
val CardShape = RoundedCornerShape(16.dp)
val LargeCardShape = RoundedCornerShape(20.dp)
val BottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
val ChipShape = RoundedCornerShape(999.dp)
val OverlayWindowShape = RoundedCornerShape(10.dp)

val TurboUroShapes = Shapes(
    small = SmallButtonShape,
    medium = CardShape,
    large = LargeCardShape
)
