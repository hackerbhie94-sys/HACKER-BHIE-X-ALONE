package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveChatView(roomId: String, onBack: () -> Unit) {
    val roomsState = ChatEngine.rooms.collectAsState()
    val messagesMap = ChatEngine.messages.collectAsState()
    val typingMap = ChatEngine.typingIndicator.collectAsState()
    val currentUserState = ChatEngine.currentUser.collectAsState()

    val room = roomsState.value.find { it.id == roomId } ?: return
    val messagesList = messagesMap.value[roomId] ?: emptyList()
    val typingName = typingMap.value[roomId]
    val currUser = currentUserState.value ?: return

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var textInput by remember { mutableStateOf("") }
    var selectedMsgForActions by remember { mutableStateOf<Message?>(null) }
    var replyTargetMsg by remember { mutableStateOf<Message?>(null) }
    
    // Voice note simulation state
    var isRecordingSimulated by remember { mutableStateOf(false) }
    var recordDurationSeconds by remember { mutableStateOf(0) }

    // Dialog state for attachments
    var showAttachmentSheet by remember { mutableStateOf(false) }

    // Setup: Mark room read on entry or incoming messages
    LaunchedEffect(messagesList.size) {
        ChatEngine.markRoomRead(roomId)
        if (messagesList.isNotEmpty()) {
            listState.animateScrollToItem(messagesList.size - 1)
        }
    }

    // Voice recorder simulator progress clock
    LaunchedEffect(isRecordingSimulated) {
        if (isRecordingSimulated) {
            recordDurationSeconds = 0
            while (isRecordingSimulated) {
                delay(1000)
                recordDurationSeconds++
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF00FF00).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = room.name.take(2).uppercase(),
                                color = Color(0xFF00FF00),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = room.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (room.type == RoomType.CHANNEL) "Broadcast Channel" else "Secure Handshake Handlers",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF00FF00))
                    }
                },
                actions = {
                    IconButton(onClick = { /* Simulated Secure Call */ }) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF00FF00))
                    }
                    if (room.type == RoomType.GROUP && room.adminList.contains(currUser.id)) {
                        IconButton(onClick = { /* Group Settings */ }) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Admin controls", tint = Color(0xFF00FF00))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF050505))
            )
        },
        containerColor = Color(0xFF050505)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            // Main Chat Room details banner if it's a channel and we are not subscribed
            if (room.type == RoomType.CHANNEL && !room.memberList.contains(currUser.id)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111111))
                        .padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Follow channel to receive broadcast packages",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { ChatEngine.subscribeToChannel(roomId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("JOIN", color = Color.Black, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Message Flow container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF050505))
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    
                    items(messagesList) { message ->
                        if (!message.deletedForMe) {
                            MessageBubble(
                                message = message,
                                isMe = message.senderId == currUser.id,
                                onClick = { selectedMsgForActions = message }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // Scroll to bottom dynamic micro anchor button
                val showScrollAnchor by remember { derivedStateOf { listState.firstVisibleItemIndex > 2 } }
                if (showScrollAnchor) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF00FF00))
                            .clickable {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(messagesList.size)
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Scroll to Bottom", tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Typing Indicator display panel
            typingName?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF00FF00))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$it is typing interactive stream code...",
                            color = Color(0xFF00FF00),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Reply Active Panel Box overlay
            replyTargetMsg?.let { reply ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111111))
                        .padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Replying to ${reply.senderName}",
                                fontSize = 11.sp,
                                color = Color(0xFF00FF00),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (reply.isVoice) "🎤 Voice message" else reply.text,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                maxLines = 1,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(onClick = { replyTargetMsg = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Discard Reply", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Bottom bar input controls 
            val canPost = room.type != RoomType.CHANNEL || room.adminList.contains(currUser.id)
            if (canPost) {
                Column {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF111111))
                            .padding(8.dp)
                    ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Attachments Trigger button
                        IconButton(
                            onClick = { showAttachmentSheet = true },
                            modifier = Modifier.testTag("attach_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Files", tint = Color(0xFF00FF00))
                        }

                        // Text Field Input
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Compile message...", fontFamily = FontFamily.Monospace, color = Color.Gray, fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("msg_text_field"),
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00FF00),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Voice Recorder simulated trigger button
                        if (textInput.isEmpty()) {
                            IconButton(
                                onClick = {
                                    if (isRecordingSimulated) {
                                        isRecordingSimulated = false
                                        ChatEngine.sendMessage(
                                            roomId = roomId,
                                            text = "🎤 Recorded Voice handpulse note",
                                            fileType = FileType.VOICE,
                                            isVoice = true,
                                            voiceDuration = recordDurationSeconds
                                        )
                                    } else {
                                        isRecordingSimulated = true
                                    }
                                },
                                modifier = Modifier
                                    .testTag("voice_record_btn")
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isRecordingSimulated) Color.Red else Color.Transparent)
                            ) {
                                Icon(
                                    imageVector = if (isRecordingSimulated) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = "Send Voice Message",
                                    tint = if (isRecordingSimulated) Color.White else Color(0xFF00FF00)
                                )
                            }
                        } else {
                            // Standard Send Message click button
                            IconButton(
                                onClick = {
                                    if (textInput.isNotBlank()) {
                                        ChatEngine.sendMessage(
                                            roomId = roomId,
                                            text = textInput,
                                            replyToId = replyTargetMsg?.id,
                                            replyToText = replyTargetMsg?.text
                                        )
                                        textInput = ""
                                        replyTargetMsg = null
                                    }
                                },
                                modifier = Modifier.testTag("send_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "Transmit", tint = Color(0xFF00FF00))
                            }
                        }
                    }
                }
               }
            } else {
                // Read Only alert
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111111))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔒 Only administrators can broadcast in this channel panel.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    // Modal Bottom action sheets for Long pressed items
    selectedMsgForActions?.let { msg ->
        AlertDialog(
            onDismissRequest = { selectedMsgForActions = null },
            title = { Text(text = "Infoterm handshakes", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color(0xFF00FF00)) },
            text = {
                Column {
                    Text(text = "Select action for target segment envelope.", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Reaction palette triggers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("🔥", "❤️", "👍", "😂", "😮").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 28.sp,
                                modifier = Modifier
                                    .clickable {
                                        ChatEngine.addReaction(roomId, msg.id, emoji)
                                        selectedMsgForActions = null
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))

                    DropdownMenuItem(
                        text = { Text("REPLY TO MESSAGE", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        onClick = {
                            replyTargetMsg = msg
                            selectedMsgForActions = null
                        },
                        leadingIcon = { Icon(Icons.Default.Reply, contentDescription = null, tint = Color(0xFF00FF00)) }
                    )

                    DropdownMenuItem(
                        text = { Text("DELETE FOR ME ONLY", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        onClick = {
                            ChatEngine.deleteMessage(roomId, msg.id, forEveryone = false)
                            selectedMsgForActions = null
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.LightGray) }
                    )

                    DropdownMenuItem(
                        text = { Text("DELETE FOR EVERYONE", color = Color.Red, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        onClick = {
                            ChatEngine.deleteMessage(roomId, msg.id, forEveryone = true)
                            selectedMsgForActions = null
                        },
                        leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.Red) }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMsgForActions = null }) {
                    Text("CLOSE", color = Color.Gray, fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = Color(0xFF111111)
        )
    }

    // Simulated Document attachments upload handler dialog
    if (showAttachmentSheet) {
        AlertDialog(
            onDismissRequest = { showAttachmentSheet = false },
            title = { Text(text = "Secure Payload", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color(0xFF00FF00)) },
            text = {
                Column {
                    Text(text = "Select element to broadcast to node.", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(16.dp))

                    DropdownMenuItem(
                        text = { Text("IMAGE PACKET (.png / .jpg)", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        onClick = {
                            ChatEngine.sendMessage(roomId, "📎 Image attachment broadcast", fileType = FileType.IMAGE, fileUrl = "https://example.com/mock.png")
                            showAttachmentSheet = false
                        },
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF00FF00)) }
                    )

                    DropdownMenuItem(
                        text = { Text("VIDEO PACKET (.mp4)", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        onClick = {
                            ChatEngine.sendMessage(roomId, "📎 Video attachment broadcast", fileType = FileType.VIDEO, fileUrl = "https://example.com/mock.mp4")
                            showAttachmentSheet = false
                        },
                        leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFF00FF00)) }
                    )

                    DropdownMenuItem(
                        text = { Text("DOCUMENT ARCHIVE (.pdf / .json)", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        onClick = {
                            ChatEngine.sendMessage(roomId, "📎 Hacker Manifesto document log", fileType = FileType.DOCUMENT, fileUrl = "https://example.com/manifesto.pdf")
                            showAttachmentSheet = false
                        },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF00FF00)) }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachmentSheet = false }) {
                    Text("CANCEL", color = Color.Gray, fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = Color(0xFF111111)
        )
    }
}

@Composable
fun MessageBubble(message: Message, isMe: Boolean, onClick: () -> Unit) {
    val bubbleShape = if (isMe) {
        RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
    } else {
        RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)
    }

    val backgroundBrush = if (isMe) {
        Color(0xFF00FF00).copy(alpha = 0.12f) 
    } else {
        Color(0xFF111111) 
    }

    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart

    var voiceSimPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(voiceSimPlaying) {
        if (voiceSimPlaying) {
            delay(message.voiceDuration * 1000L)
            voiceSimPlaying = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            // Sender display label if from Group (Ignore if DM)
            if (!isMe && message.senderId != "me") {
                Text(
                    text = message.senderName,
                    color = Color(0xFF00FF00),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            // Bubble body containment
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(backgroundBrush)
                    .border(
                        1.dp,
                        if (isMe) Color(0xFF00FF00).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f),
                        bubbleShape
                    )
                    .padding(10.dp)
            ) {
                Column {
                    // Replied-To message preview block inside bubble
                    if (message.replyToText != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Handshake Reply:",
                                    fontSize = 9.sp,
                                    color = Color(0xFF00FF00),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = message.replyToText,
                                    fontSize = 10.sp,
                                    color = Color.LightGray,
                                    maxLines = 1,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Content Rendering based on type
                    when {
                        message.deletedForEveryone -> {
                            Text(
                                text = "This transmission has been purged globally.",
                                color = Color.Red.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        message.isVoice -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { voiceSimPlaying = !voiceSimPlaying }) {
                                    Icon(
                                        imageVector = if (voiceSimPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Voice playing toggler",
                                        tint = Color(0xFF00FF00)
                                    )
                                }
                                Column(modifier = Modifier.width(110.dp)) {
                                    Text(
                                        text = if (voiceSimPlaying) "TRANSMITTING..." else "🎤 Voice Record",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Custom visual equalizer waveform drawing simulation flat rows
                                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                        listOf(10, 16, 24, 8, 12, 18, 32, 22, 10, 6).forEach { heightVal ->
                                            Box(
                                                modifier = Modifier
                                                    .width(4.dp)
                                                    .height((heightVal * if (voiceSimPlaying) (1..2).random() else 1).dp)
                                                    .background(if (voiceSimPlaying) Color(0xFF00FF00) else Color.DarkGray)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${message.voiceDuration}s",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        message.fileType != FileType.NONE -> {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(115.dp)
                                        .background(Color.Black)
                                        .border(1.dp, Color.DarkGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = when (message.fileType) {
                                                FileType.IMAGE -> Icons.Default.Image
                                                FileType.VIDEO -> Icons.Default.Videocam
                                                else -> Icons.Default.Description
                                            },
                                            contentDescription = "Shared payload icon",
                                            tint = Color(0xFF00FF00),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "SECTOR SECURITY PACKET", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = message.text,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = message.text,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                              )
                        }
                    }

                    // Bottom info metrics (Reactions and Time indicator stamp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Render reactions inside bubble left side
                        if (message.reactions.isNotEmpty() && !message.deletedForEveryone) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                message.reactions.forEach { reaction ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(alpha = 0.4f))
                                            .padding(horizontal = 4.dp)
                                    ) {
                                        Text(text = reaction, fontSize = 9.sp)
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        // Time stamp display
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            if (isMe) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                                    contentDescription = "receipt ticks",
                                    tint = if (message.isRead) Color(0xFF00FF00) else Color.Gray,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
