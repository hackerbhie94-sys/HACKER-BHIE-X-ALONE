package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SplashView(onSplashFinished: () -> Unit) {
    var bootStep by remember { mutableStateOf(0) }
    val bootLogs = listOf(
        "Initializing Hacker handshake logic...",
        "Authorizing secure firewall tunnel...",
        "Injecting custom cryptographic layers...",
        "handshakes established securely. Welcome."
    )

    LaunchedEffect(Unit) {
        delay(600)
        bootStep = 1
        delay(900)
        bootStep = 2
        delay(800)
        bootStep = 3
        delay(1000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
    ) {
        MatrixRainBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF111111))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00FF00).copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Image(
                    painter = painterResource(id = R.drawable.img_hacker_logo_1781609532505),
                    contentDescription = "Hacker Bhai X Alone Mask Emblem",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "HACKER BHAI X ALONE",
                color = Color(0xFF00FF00),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            Text(
                text = "Elite Secure Chat & Broadcasting",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(12.dp)
                    .clip(MaterialTheme.shapes.small),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = " SYSTEM BOOT LOGS:",
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ">> " + bootLogs.getOrNull(bootStep).orEmpty(),
                    color = Color(0xFF00FF00),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .clip(CircleShape),
                color = Color(0xFF00FF00),
                trackColor = Color(0xFF003300)
            )
        }
    }
}

@Composable
fun MatrixRainBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "matrixRainPulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Simulated binary matrix lines running down the screen
        val cols = 15
        val step = width / cols
        for (i in 0 until cols) {
            val startX = i * step + step / 2
            val randomOffset = Random.nextFloat() * height
            val streamSpeed = (8..15).random()
            
            // Draw digital hacker pulses
            for (j in 0..10) {
                val yVal = (randomOffset + (j * 40f) + (System.currentTimeMillis() / streamSpeed)) % height
                drawCircle(
                    color = Color(0xFF00FF00).copy(alpha = pulseAlpha * (j / 10f)),
                    radius = (2..5).random().toFloat(),
                    center = androidx.compose.ui.geometry.Offset(startX, yVal)
                )
            }
        }
    }
}
