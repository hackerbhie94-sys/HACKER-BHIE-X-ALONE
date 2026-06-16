package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardView(
    onRoomSelected: (String) -> Unit,
    onAdminTerminalTriggered: () -> Unit,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Chats, 1: Channels, 2: Contacts, 3: Settings

    val currentUserState = ChatEngine.currentUser.collectAsState()
    val roomsListState = ChatEngine.rooms.collectAsState()
    val blockedUsersState = ChatEngine.blockedUsersIds.collectAsState()

    val currentMainUser = currentUserState.value ?: return

    // Create custom Group or Channel dialog state
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var newRoomName by remember { mutableStateOf("") }
    var newRoomDesc by remember { mutableStateOf("") }
    var newRoomType by remember { mutableStateOf(RoomType.GROUP) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF00FF00)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "X",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HACKER BHAI X ALONE",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "NETWORK SECURE",
                                color = Color(0xFF00FF00),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                actions = {
                    if (currentMainUser.role == UserRole.ADMIN) {
                        IconButton(
                            onClick = onAdminTerminalTriggered,
                            modifier = Modifier.testTag("terminal_badge_btn")
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF00FF00).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFF00FF00), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "OPERATOR", color = Color(0xFF00FF00), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    IconButton(onClick = onLogout) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Log Out terminal", tint = Color.LightGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF050505))
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                NavigationBar(
                    containerColor = Color(0xFF111111),
                    tonalElevation = 8.dp
                ) {
                    listOf(
                        Triple("Chats", Icons.Default.Chat, 0),
                        Triple("Broadcasts", Icons.Default.RssFeed, 1),
                        Triple("Contact", Icons.Default.Search, 2),
                        Triple("Settings", Icons.Default.Settings, 3)
                    ).forEach { (label, icon, tabIdx) ->
                        val isSel = activeTab == tabIdx
                        NavigationBarItem(
                            selected = isSel,
                            onClick = { activeTab = tabIdx },
                            icon = { Icon(imageVector = icon, contentDescription = label) },
                            label = { Text(text = label, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = Color(0xFF00FF00),
                                indicatorColor = Color(0xFF00FF00),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (activeTab == 0 || activeTab == 1) {
                FloatingActionButton(
                    onClick = {
                        newRoomType = if (activeTab == 0) RoomType.GROUP else RoomType.CHANNEL
                        showCreateRoomDialog = true
                    },
                    containerColor = Color(0xFF00FF00),
                    contentColor = Color.Black,
                    modifier = Modifier.testTag("add_room_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Initiate custom room")
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
                targetState = activeTab,
                label = "navigationTabTransition",
                modifier = Modifier.fillMaxSize()
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> { // Chats List (Group & DMs)
                        val activeRooms = roomsListState.value.filter { it.type != RoomType.CHANNEL }
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                                Icon(imageVector = Icons.Default.ChatBubble, contentDescription = null, tint = Color(0xFF00FF00), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ACTIVE SECURED CHATS (${activeRooms.size})",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (activeRooms.isEmpty()) {
                                DashboardEmptyState("NO ACTIVE HANDSHAKES", "Use the positive action trigger at the bottom to initiate encrypt lists.")
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(activeRooms) { room ->
                                        RoomRowItem(room = room, onClick = { onRoomSelected(room.id) })
                                    }
                                }
                            }
                        }
                    }
                    1 -> { // Broadcast Channels
                        val activeChannels = roomsListState.value.filter { it.type == RoomType.CHANNEL }
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                                Icon(imageVector = Icons.Default.RssFeed, contentDescription = null, tint = Color(0xFF00FF00), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AVAILABLE BROADCAST SEGMENTS (${activeChannels.size})",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (activeChannels.isEmpty()) {
                                DashboardEmptyState("STANDBY: ZERO FREQUENENCES", "No available broadcast lines initialized. Run Operator Station to establish channels.")
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(activeChannels) { channel ->
                                        ChannelRowItem(channel = channel, onClick = { onRoomSelected(channel.id) })
                                    }
                                }
                            }
                        }
                    }
                    2 -> { // Contacts & User Discovery search segment
                        var searchQuery by remember { mutableStateOf("") }
                        val contactsState = ChatEngine.contacts.collectAsState()
                        val filteredContacts = contactsState.value.filter {
                            it.id != "me" && (searchQuery.isEmpty() || it.username.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery))
                        }

                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                                placeholder = { Text("Query network usernames...", fontFamily = FontFamily.Monospace, color = Color.Gray, fontSize = 13.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .testTag("contact_search"),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00FF00),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                                Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF00FF00), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DISCOVERED CODER NODES (${filteredContacts.size})",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (filteredContacts.isEmpty()) {
                                DashboardEmptyState("ZERO Handshake nodes", "No matches found on the local search queries.")
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(filteredContacts) { contact ->
                                        val isBlocked = blockedUsersState.value.contains(contact.id)
                                        ContactRowItem(
                                            user = contact,
                                            isBlocked = isBlocked,
                                            onToggleBlock = {
                                                if (isBlocked) ChatEngine.unblockUser(contact.id) else ChatEngine.blockUser(contact.id)
                                            },
                                            onStartChat = {
                                                val existingRoom = roomsListState.value.find { it.type == RoomType.DIRECT && (it.creatorId == contact.id || it.id == "room_${contact.id}") }
                                                if (existingRoom != null) {
                                                    onRoomSelected(existingRoom.id)
                                                } else {
                                                    val newId = ChatEngine.createChatRoom("Handshake: ${contact.username}", "Direct confidential DM lines.", RoomType.DIRECT)
                                                    onRoomSelected(newId)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    3 -> { // Hacker Settings & Appearance Pane
                        SettingsPane(
                            userProfile = currentMainUser,
                            onThemeToggled = { ChatEngine.toggleTheme() },
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }

    if (showCreateRoomDialog) {
        AlertDialog(
            onDismissRequest = { showCreateRoomDialog = false },
            title = {
                Text(
                    text = if (newRoomType == RoomType.GROUP) "INITIATE SECURE CODER GROUP" else "INITIALIZE BROADCAST SEGMENT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = Color(0xFF00FF00),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(text = "Establish a cryptographic network vector segment on local DB routing table.", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newRoomName,
                        onValueChange = { newRoomName = it },
                        label = { Text("Segment Alias / Title", fontFamily = FontFamily.Monospace, color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("add_room_title"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FF00),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newRoomDesc,
                        onValueChange = { newRoomDesc = it },
                        label = { Text("Security Description / Rules", fontFamily = FontFamily.Monospace, color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FF00),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newRoomName.isNotBlank()) {
                            val newId = ChatEngine.createChatRoom(newRoomName, newRoomDesc, newRoomType)
                            showCreateRoomDialog = false
                            newRoomName = ""
                            newRoomDesc = ""
                            onRoomSelected(newId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00))
                ) {
                    Text("COMPILE VECTOR", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRoomDialog = false }) {
                    Text("CANCEL", color = Color.Gray, fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = Color(0xFF111111)
        )
    }
}

@Composable
fun RoomRowItem(room: ChatRoom, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF00FF00).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (room.type == RoomType.GROUP) Icons.Default.Groups else Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF00FF00)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = room.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(room.lastMessageTime)),
                        color = Color.Gray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = room.lastMessageText,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    if (room.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF00FF00))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = room.unreadCount.toString(),
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelRowItem(channel: ChatRoom, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF00FF00).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Campaign, contentDescription = null, tint = Color(0xFF00FF00))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📣 ${channel.name}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = channel.description,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = Color(0xFF00FF00), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${channel.memberList.size} secure listeners followed",
                        color = Color(0xFF00FF00),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun ContactRowItem(user: UserProfile, isBlocked: Boolean, onToggleBlock: () -> Unit, onStartChat: () -> Unit) {
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (user.isOnline) Color(0xFF00FF00).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = user.username.take(2).uppercase(), color = Color(0xFF00FF00), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = user.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        if (isBlocked) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "BLOCKED", color = Color.Red, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(text = user.bio, color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                }
            }

            Row {
                IconButton(onClick = onStartChat) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color(0xFF00FF00))
                }
                IconButton(onClick = { showReportDialog = true }) {
                    Icon(imageVector = Icons.Default.Report, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f))
                }
                IconButton(onClick = onToggleBlock) {
                    Icon(
                        imageVector = if (isBlocked) Icons.Default.LockOpen else Icons.Default.Block,
                        contentDescription = null,
                        tint = if (isBlocked) Color(0xFF00FF00) else Color.Red
                    )
                }
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("FILE INFRACTION INCIDENT", fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color.Red, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Report node behavior to central administration operator.", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("Infraction Reason", fontFamily = FontFamily.Monospace, color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FF00),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportReason.isNotBlank()) {
                            ChatEngine.fileReport(user.id, reportReason)
                            showReportDialog = false
                            reportReason = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("FILE REPORT", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("CANCEL", color = Color.Gray, fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = Color(0xFF111111)
        )
    }
}

@Composable
fun SettingsPane(
    userProfile: UserProfile,
    onThemeToggled: () -> Unit,
    onLogout: () -> Unit
) {
    var editUsername by remember { mutableStateOf(userProfile.username) }
    var editBio by remember { mutableStateOf(userProfile.bio) }
    var isPushEnabled by remember { mutableStateOf(true) }
    var isPrivacyLocked by remember { mutableStateOf(false) }

    var isFirebaseSyncToggled by remember { mutableStateOf(ChatEngine.isFirebaseSyncEnabled) }
    var updateSuccess by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111111))
                    .border(1.dp, Color(0xFF00FF00).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "📡 CLOUD HANDSHAKE DUAL SYNC MODE",
                        color = Color(0xFF00FF00),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dual sync allows you to connect native Android Firestore, Storage and Cloud Messaging protocols. Default local handshakes persist perfectly offline-first in our reactive state engine.",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = isFirebaseSyncToggled,
                            onCheckedChange = {
                                isFirebaseSyncToggled = it
                                ChatEngine.isFirebaseSyncEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00FF00))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFirebaseSyncToggled) "CLOUD SYNC: ACTIVE HANDSHAKES" else "LOCAL OFFLINE-FIRST HANDSHAKE CACHE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111111), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "ACCOUNT INFOMETRY CONFIG",
                        color = Color(0xFF00FF00),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Network Username", fontFamily = FontFamily.Monospace, color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().testTag("settings_username"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FF00),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Identity Bio", fontFamily = FontFamily.Monospace, color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FF00),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (editUsername.isNotBlank()) {
                                ChatEngine.updateProfile(editUsername, editBio, "")
                                updateSuccess = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("SAVE IDENTITY HASH", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }

                    if (updateSuccess) {
                        LaunchedEffect(Unit) {
                            delay(2000)
                            updateSuccess = false
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("✔️ IDENTITY HASH SAVED SECURELY", color = Color(0xFF00FF00), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111111), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "ENVIRONMENT SETTINGS",
                        color = Color(0xFF00FF00),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPushEnabled = !isPushEnabled }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Push Notification Dispatcher", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text("Dispatch instant local ring indicators", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Switch(
                            checked = isPushEnabled,
                            onCheckedChange = { isPushEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00FF00))
                        )
                    }

                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeToggled() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            Icon(imageVector = Icons.Default.Brightness6, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Toggle Theme Appearance", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text("Shift between Dark Matrix / Light terminal", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }

                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPrivacyLocked = !isPrivacyLocked }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Privacy Lock Handshake", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text("Request fingerprint verification on launching", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Switch(
                            checked = isPrivacyLocked,
                            onCheckedChange = { isPrivacyLocked = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00FF00))
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DESTROY TERMINAL SESSION", color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DashboardEmptyState(title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF00FF00), modifier = Modifier.size(54.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
        Text(text = desc, color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 8.dp))
    }
}
