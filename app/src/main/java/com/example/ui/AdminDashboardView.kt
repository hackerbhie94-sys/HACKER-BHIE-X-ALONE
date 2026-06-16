package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardView(onBack: () -> Unit) {
    var activeSubMenu by remember { mutableStateOf(0) } // 0: Live System Analytics, 1: Infraction Reports, 2: Channel broadcasts
    val analyticsData by ChatEngine.analytics.collectAsState()
    val reportLists by ChatEngine.reports.collectAsState()
    val profiles by ChatEngine.contacts.collectAsState()
    val roomsState by ChatEngine.rooms.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "OPERATOR CONTROL STATION",
                                color = Color(0xFF00FF00),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Central terminal command segment",
                                color = Color.LightGray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Exit to Segment", tint = Color.Red)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF050505))
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                NavigationBar(
                    containerColor = Color(0xFF050505)
                ) {
                    listOf(
                        Triple("Analytics", Icons.Default.Analytics, 0),
                        Triple("Infractions", Icons.Default.Report, 1),
                        Triple("Frequencies", Icons.Default.RssFeed, 2)
                    ).forEach { (lbl, icon, index) ->
                        NavigationBarItem(
                            selected = activeSubMenu == index,
                            onClick = { activeSubMenu = index },
                            icon = { Icon(imageVector = icon, contentDescription = lbl, modifier = Modifier.size(18.dp)) },
                            label = { Text(text = lbl, fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = Color(0xFF00FF00),
                                indicatorColor = Color(0xFF00FF00),
                                unselectedTextColor = Color.Gray,
                                unselectedIconColor = Color.Gray
                            )
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF050505)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = activeSubMenu,
                label = "adminNavigationAnim"
            ) { subIndex ->
                when (subIndex) {
                    0 -> AnalyticsPage(analyticsData, reportLists.size)
                    1 -> InfractionsPage(reportLists, profiles)
                    2 -> FrequenciesPage(roomsState)
                }
            }
        }
    }
}

// Analytics and dynamic canvas metrics segment

@Composable
fun AnalyticsPage(data: AnalyticsData, infractionsCount: Int) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = Color(0xFF00FF00), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "LIVE SYSTEM TELEMETRY INDEX",
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Raw metrics cards list
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnalyticsCard(
                title = "CODER NODES",
                count = data.activeUsers.toString(),
                color = Color(0xFF00FF00),
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                title = "ACTIVE HANDSHAKES",
                count = data.totalChats.toString(),
                color = Color(0xFF00FF00),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnalyticsCard(
                title = "ACTIVE FREQUENCIES",
                count = "${data.totalChannels} channels",
                color = Color(0xFF00FF00),
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                title = "INTRUSIONS SUSPECTED",
                count = infractionsCount.toString(),
                color = Color.Red,
                modifier = Modifier.weight(1f)
            )
        }

        // Custom drawn Analytics Canvas diagram
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111111), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "TRANSMISSION FLOOD ACTIVITY (24H)",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                CanvasDiagram(modifier = Modifier.fillMaxWidth().height(150.dp))

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("00:00 (UTC)", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("12:00 (UTC)", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("24:00 (UTC)", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun CanvasDiagram(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val tracePath = Path()
        // Wave coordinates simulation points
        val stepY = height / 4f
        for (i in 1..3) {
            drawLine(
                color = Color.Gray.copy(alpha = 0.15f),
                start = androidx.compose.ui.geometry.Offset(0f, stepY * i),
                end = androidx.compose.ui.geometry.Offset(width, stepY * i),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw simulated curves
        val lengthList = 10
        val stepX = width / (lengthList - 1)
        val yCoords = listOf(0.8f, 0.65f, 0.72f, 0.45f, 0.55f, 0.32f, 0.25f, 0.48f, 0.15f, 0.3f)

        tracePath.moveTo(0f, height * yCoords[0])
        for (idx in 1 until lengthList) {
            tracePath.lineTo(idx * stepX, height * yCoords[idx])
        }

        drawPath(
            path = tracePath,
            color = Color(0xFF00FF00),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun AnalyticsCard(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(text = title, color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = count, color = color, fontSize = 20.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// Infractions moderation station segment list

@Composable
fun InfractionsPage(reports: List<Report>, profiles: List<UserProfile>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            Icon(imageVector = Icons.Default.Report, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "SUBMITTED NODE INFRACTION INCIDENTS (${reports.size})",
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        if (reports.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "CLEAN HANDSHAKE SEGMENT: NO REPORT ARCHIVES", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reports) { report ->
                    val reportedUser = profiles.find { it.id == report.reportedUserId }
                    InfractionRowItem(report = report, reportedUser = reportedUser)
                }
            }
        }
    }
}

@Composable
fun InfractionRowItem(report: Report, reportedUser: UserProfile?) {
    var showActionConfirmDialog by remember { mutableStateOf(false) }
    var operationType by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REPORT_INCIDENT ID: #${report.id.take(6).uppercase()}",
                    color = Color.Red,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF3B1010))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "CRITICAL PENDING",
                        color = Color.Red,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Target node: ${reportedUser?.username ?: "Offline node [#${report.reportedUserId.take(5)}]"}",
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Reason payload: ${report.reason}",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        operationType = "DISMISS"
                        showActionConfirmDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("DISMISS INCIDENT", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        operationType = "BAN"
                        showActionConfirmDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("MUTILE NODE / BAN", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showActionConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showActionConfirmDialog = false },
            title = { Text(text = "$operationType CONFIRMATION", fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("Are you authorized to dispatch a $operationType command packet across the node cluster in real-time Firestore database synchronization channels?", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
            confirmButton = {
                Button(
                    onClick = {
                        if (operationType == "BAN") {
                            ChatEngine.adminDeleteUser(report.reportedUserId)
                            ChatEngine.adminDismissReport(report.id)
                        } else {
                            ChatEngine.adminDismissReport(report.id)
                        }
                        showActionConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("AUTHORIZE DISPATCH", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showActionConfirmDialog = false }) {
                    Text("CANCEL", color = Color.Gray, fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = Color(0xFF111111)
        )
    }
}

// Broadcast frequencies custom triggers

@Composable
fun FrequenciesPage(rooms: List<ChatRoom>) {
    val channels = rooms.filter { it.type == RoomType.CHANNEL }
    var selectedChannelId by remember { mutableStateOf("") }
    var broadcastContent by remember { mutableStateOf("") }
    var alertSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Campaign, contentDescription = null, tint = Color(0xFF00FF00), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "GLOBAL EMERGENCY BROADCAST MODULATOR",
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Inject real time notifications, zero-day threat announcements, or critical system state packets to channels.",
            color = Color.LightGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )

        if (channels.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                Text("Zero active broadcast channels configured on node registry.", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        } else {
            Column {
                Text("1. Target broadcast channel registry:", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(6.dp))
                channels.forEach { ch ->
                    val isSel = selectedChannelId == ch.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) Color(0xFF00FF00).copy(alpha = 0.1f) else Color(0xFF111111))
                            .border(1.dp, if (isSel) Color(0xFF00FF00) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                            .clickable { selectedChannelId = ch.id }
                            .padding(12.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "📡 ${ch.name}", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            if (isSel) {
                                Text(text = "ARMED", color = Color(0xFF00FF00), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column {
                Text("2. Broadcast package description payload:", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = broadcastContent,
                    onValueChange = { broadcastContent = it },
                    placeholder = { Text("Compile packet...", fontFamily = FontFamily.Monospace, color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("emergency_broadcast_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            Button(
                onClick = {
                    if (selectedChannelId.isNotBlank() && broadcastContent.isNotBlank()) {
                        ChatEngine.sendMessage(
                            roomId = selectedChannelId,
                            text = "🚨 EMERGENCY METRIC PACKAGE: $broadcastContent",
                            replyToId = null
                        )
                        broadcastContent = ""
                        alertSuccess = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("TRIGGER GLOBAL BROADCAST DISPATCH", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            if (alertSuccess) {
                LaunchedEffect(Unit) {
                    delay(3000)
                    alertSuccess = false
                }
                Text("✔️ GLOBAL BROADCAST SUCCESSFULLY DISPATCHED AND REGISTERED", color = Color(0xFF00FF00), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}
