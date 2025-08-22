package com.demo.lightcontrolapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var mqttService: MqttService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mqttService = MqttService(this)
        mqttService.connect()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LightControlScreen(mqttService)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mqttService.disconnect()
    }
}

@Composable
fun LightControlScreen(mqttService: MqttService) {
    var isLightOn by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableStateOf(0L) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    
    LaunchedEffect(Unit) {
        mqttService.subscribe { message ->
            when (message.uppercase()) {
                "STATUS_ON" -> {
                    isLightOn = true
                    isLoading = false
                }
                "STATUS_OFF" -> {
                    isLightOn = false
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(1000)
        mqttService.publishMessage("STATUS")
    }
    
    val trackWidth = 80.dp
    val trackHeight = 40.dp
    val thumbSize = 32.dp
    val thumbPadding = (trackHeight - thumbSize) / 2f
    val travelDistance = trackWidth - thumbSize - 2 * thumbPadding

    val thumbOffsetX by animateDpAsState(
        targetValue = if (isLightOn) thumbPadding + travelDistance else thumbPadding,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "thumbOffsetX"
    )

    val backgroundBrush = if (isLightOn) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFF8E1),
                Color(0xFFFFF3E0)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF1E293B),
                Color(0xFF334155)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isLightOn) 1.1f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Icon(
                painter = painterResource(id = R.drawable.lightbulb_24),
                contentDescription = "Light bulb",
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 32.dp),
                tint = if (isLightOn) Color(0xFFFBBF24) else Color(0xFF94A3B8)
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(bottom = 32.dp),
                    color = Color(0xFFFBBF24)
                )
                Text(
                    text = "กำลังเช็คสถานะไฟล่าสุด...",
                    fontSize = 18.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(trackWidth)
                        .height(trackHeight)
                        .clip(CircleShape)
                        .background(
                            if (isLightOn) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFBBF24),
                                        Color(0xFFF97316)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF475569),
                                        Color(0xFF334155)
                                    )
                                )
                            }
                        )
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = if (isLightOn) Color(0x40FBBF24) else Color(0x40000000)
                        )
                        .clickable {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastClickTime >= 1000) {
                                lastClickTime = currentTime
                                scope.launch {
                                    mqttService.publishMessage(if (!isLightOn) "ON" else "OFF")
                                }
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(thumbSize)
                            .offset(x = thumbOffsetX, y = thumbPadding)
                            .clip(CircleShape)
                            .background(if (isLightOn) Color.White else Color(0xFFCBD5E1))
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                spotColor = if (isLightOn) Color(0x40FBBF24) else Color(0x40000000)
                            )
                    )

                    if (isLightOn) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0x40FBBF24),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                }

                Text(
                    text = if (isLightOn) "เปิดไฟแล้ว" else "ปิดไฟอยู่",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLightOn) Color(0xFFD97706) else Color(0xFFCBD5E1),
                    modifier = Modifier.padding(top = 32.dp)
                )

                Text(
                    text = if (isLightOn) "แสงสว่างจ้า" else "มืดมิด",
                    fontSize = 18.sp,
                    color = if (isLightOn) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        if (isLightOn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x1AFBBF24),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "พัฒนาโดย: KongWatcharapong",
                fontSize = 14.sp,
                color = if (isLightOn) Color(0xFFD97706) else Color(0xFF94A3B8),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}