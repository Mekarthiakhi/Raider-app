package com.fleettrack.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fleettrack.app.ui.theme.CardShape
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    borderWidth: Dp = 1.dp,
    borderColor: Color = SurfaceBorder,
    backgroundColor: Color = SurfaceWhite,
    elevation: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = BorderStroke(borderWidth, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Box(content = content)
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = BorderStroke(borderWidth, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Box(content = content)
        }
    }
}
