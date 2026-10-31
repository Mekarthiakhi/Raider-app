package com.fleettrack.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fleettrack.app.ui.theme.EarningsGold
import java.text.NumberFormat
import java.util.Locale

@Composable
fun EarningsText(
    amount: Double,
    modifier: Modifier = Modifier,
    color: Color = EarningsGold,
    style: TextStyle = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold
    ),
    animate: Boolean = true
) {
    val animatedValue = remember { Animatable(if (animate) 0f else amount.toFloat()) }

    LaunchedEffect(amount) {
        if (animate) {
            animatedValue.animateTo(
                targetValue = amount.toFloat(),
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
        } else {
            animatedValue.snapTo(amount.toFloat())
        }
    }

    val displayAmount = if (animate) animatedValue.value.toDouble() else amount
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val formattedText = formatter.format(displayAmount)

    Text(
        text = formattedText,
        modifier = modifier,
        color = color,
        style = style
    )
}

