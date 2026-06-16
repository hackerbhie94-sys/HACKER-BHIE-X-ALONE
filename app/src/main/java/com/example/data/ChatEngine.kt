package com.example.data

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

object ChatEngine {

    // --- Firebase Ready Bindings ---
    // In a production environment, toggle this flag when Google Firebase is configured.
    // Our UI and flow are built dual-ready: local secure storage + live synchronization callbacks!
    var isFirebaseSyncEnabled = false
    
    // --- State Management ---
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _rooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val rooms: StateFlow<List<ChatRoom>> = _rooms.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    private val _contacts = MutableStateFlow<List<UserProfile>>(emptyList())
    val contacts: StateFlow<List<UserProfile>> = _contacts.asStateFlow()

    private val _blockedUsersIds = MutableStateFlow<Set<String>>(emptySet())
    val blockedUsersIds: StateFlow<Set<String>> = _blockedUsersIds.asStateFlow()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _typingIndicator = MutableStateFlow<Map<String, String?>>(emptyMap()) // RoomId -> SenderName?
    val typingIndicator: StateFlow<Map<String, String?>> = _typingIndicator.asStateFlow()

    private val _analytics = MutableStateFlow(AnalyticsData())
    val analytics: StateFlow<AnalyticsData> = _analytics.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true) // Hackers prefer Dark Mode by default!
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    init {
        setupMockData()
    }

    // --- Core Operations ---

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    private fun setupMockData() {
        val guru = UserProfile("guru", "+919999911111", "guru@hacker.org", "Hacker Guru", "Root is my home. Hack the planet.", "https://example.com/sec.png", true, UserRole.ADMIN)
        val wanderer = UserProfile("wanderer", "+10002223333", "wanderer@alone.net", "Alone Wanderer", "Sometimes lonely, always coding.", "https://example.com/lonely.png", true, UserRole.USER)
        val spy = UserProfile("spy", "+4477889988", "spy@classified.net", "Anonymous Spy", "I know things.", "https://example.com/spy.png", false, UserRole.USER)
        val adminUser = UserProfile("admin", "+919999900000", "admin@aistudio.com", "Hacker Bhai", "System Administrator.", "", true, UserRole.ADMIN)

        _contacts.value = listOf(guru, wanderer, spy, adminUser)

        val direct1 = ChatRoom("room_guru", RoomType.DIRECT, "Hacker Guru", "Direct Chat", "", "guru", listOf("guru"), listOf("guru", "me"), "System operational.", System.currentTimeMillis() - 60000, 1)
        val direct2 = ChatRoom("room_wanderer", RoomType.DIRECT, "Alone Wanderer", "Direct Chat", "", "wanderer", listOf("wanderer"), listOf("wanderer", "me"), "I'm looking for a compiler buddy.", System.currentTimeMillis() - 120000, 0)
        
        val group1 = ChatRoom(
            id = "room_deep_web",
            type = RoomType.GROUP,
            name = "Deep Web Coders",
            description = "High stakes coding, algorithms, and secure network discussions.",
            creatorId = "guru",
            adminList = listOf("guru", "me"),
            memberList = listOf("guru", "wanderer", "spy", "me"),
            lastMessageText = "Admin updated room configuration.",
            lastMessageTime = System.currentTimeMillis() - 180000,
            unreadCount = 2
        )

        val channel1 = ChatRoom(
            id = "room_alerts",
            type = RoomType.CHANNEL,
            name = "Cyber Alerts Channel",
            description = "Broadcasting critical digital vulnerabilities and security advice.",
            creatorId = "guru",
            adminList = listOf("guru"),
            memberList = listOf("guru", "wanderer", "spy", "me"),
            lastMessageText = "CRITICAL UPDATE: Log4j patched versions are out.",
            lastMessageTime = System.currentTimeMillis() - 360000,
            unreadCount = 0
        )

        _rooms.value = listOf(direct1, direct2, group1, channel1)

        val m1 = Message("m1_1", "room_guru", "guru", "Hacker Guru", "Hey there, ready to bypass major firewalls?", FileType.NONE, "", null, null, listOf("🔥"), System.currentTimeMillis() - 300000, false, 0, false, false, true)
        val m1_r = Message("m1_2", "room_guru", "guru", "Hacker Guru", "System operational. Ping me when ready.", FileType.NONE, "", null, "Hey there, ready to bypass...", emptyList(), System.currentTimeMillis() - 60000, false, 0, false, false, false)
        val m2 = Message("m2_1", "room_wanderer", "wanderer", "Alone Wanderer", "The desert nights are silent, coder.", FileType.NONE, "", null, null, emptyList(), System.currentTimeMillis() - 500000, false, 0, false, false, true)
        val m2_r = Message("m2_2", "room_wanderer", "wanderer", "Alone Wanderer", "I'm looking for a compiler buddy.", FileType.NONE, "", null, null, listOf("❤️", "💻"), System.currentTimeMillis() - 120000, false, 0, false, false, true)
        
        val gm1 = Message("gm_1", "room_deep_web", "spy", "Anonymous Spy", "I leaks the kernel patch tonight.", FileType.NONE, "", null, null, listOf("😮"), System.currentTimeMillis() - 600000, false, 0, false, false, true)
        val gm2 = Message("gm_2", "room_deep_web", "guru", "Hacker Guru", "Quiet. Use end-to-end routing only.", FileType.NONE, "", null, "I leaks the kernel patch...", emptyList(), System.currentTimeMillis() - 300000, false, 0, false, false, true)
        val gm3 = Message("gm_3", "room_deep_web", "guru", "Hacker Guru", "Admin updated room configuration.", FileType.NONE, "", null, null, emptyList(), System.currentTimeMillis() - 180000, false, 0, false, false, false)

        val ch1 = Message("ch_1", "room_alerts", "guru", "Cyber Alerts Channel", "Broadcasting critical vulnerabilities today.", FileType.NONE, "", null, null, emptyList(), System.currentTimeMillis() - 720000, false, 0, false, false, true)
        val ch2 = Message("ch_2", "room_alerts", "guru", "Cyber Alerts Channel", "CRITICAL UPDATE: Log4j patched versions are out.", FileType.NONE, "", null, null, emptyList(), System.currentTimeMillis() - 360000, false, 0, false, false, true)

        _messages.value = mapOf(
            "room_guru" to listOf(m1, m1_r),
            "room_wanderer" to listOf(m2, m2_r),
            "room_deep_web" to listOf(gm1, gm2, gm3),
            "room_alerts" to listOf(ch1, ch2)
        )

        _reports.value = listOf(
            Report("rep_1", "Alone Wanderer", "spy", "Anonymous Spy", "Spamming shady kernel exploit links.", System.currentTimeMillis() - 86400000)
        )
    }

    // --- Authentication ---

    fun login(authType: String, loginInput: String, loginEmail: String = "", role: UserRole = UserRole.USER): Boolean {
        // Mock secure login processing that matches real Firebase auth triggers
        val identifier = if (authType == "Email") loginEmail else loginInput
        val baseName = if (identifier.contains("@")) {
            identifier.substringBefore("@")
        } else if (identifier.isEmpty()) {
            "GuestBhai"
        } else {
            identifier
        }

        _currentUser.value = UserProfile(
            id = "me",
            phone = if (authType == "Phone") loginInput else "+919999900000",
            email = if (authType == "Email") loginEmail else "hacker@example.com",
            username = baseName.take(15).ifEmpty { "Anon_Hacker" },
            bio = "Cyber security researcher. Hacker Bhai X Alone member.",
            avatarUrl = "",
            isOnline = true,
            role = if (baseName.lowercase().contains("admin") || baseName.lowercase().contains("alone")) UserRole.ADMIN else role
        )
        return true
    }

    fun logout() {
        _currentUser.value = null
    }

    fun updateProfile(username: String, bio: String, avatarUrl: String): Boolean {
        val curr = _currentUser.value ?: return false
        _currentUser.value = curr.copy(
            username = username,
            bio = bio,
            avatarUrl = avatarUrl
        )
        return true
    }

    // --- Message Controls ---

    fun sendMessage(
        roomId: String,
        text: String,
        fileType: FileType = FileType.NONE,
        fileUrl: String = "",
        replyToId: String? = null,
        replyToText: String? = null,
        isVoice: Boolean = false,
        voiceDuration: Int = 0
    ) {
        val currUser = _currentUser.value ?: return
        val msgId = "m_" + System.currentTimeMillis() + "_" + Random.nextInt(1000)
        val newMsg = Message(
            id = msgId,
            roomId = roomId,
            senderId = currUser.id,
            senderName = currUser.username,
            text = text,
            fileType = fileType,
            fileUrl = fileUrl,
            replyToId = replyToId,
            replyToText = replyToText,
            reactions = emptyList(),
            timestamp = System.currentTimeMillis(),
            isVoice = isVoice,
            voiceDuration = voiceDuration,
            isRead = false
        )

        // Append message
        val currentRoomMsgs = _messages.value[roomId]?.toMutableList() ?: mutableListOf()
        currentRoomMsgs.add(newMsg)
        val updatedMap = _messages.value.toMutableMap()
        updatedMap[roomId] = currentRoomMsgs
        _messages.value = updatedMap

        // Update room's last message
        _rooms.value = _rooms.value.map { room ->
            if (room.id == roomId) {
                room.copy(
                    lastMessageText = if (isVoice) "🎤 Voice Message (${voiceDuration}s)" else if (fileType != FileType.NONE) "📎 File: ${fileType.name}" else text,
                    lastMessageTime = System.currentTimeMillis()
                )
            } else {
                room
            }
        }

        // Trigger typing and automated bot response
        if (roomId.startsWith("room_")) {
            val contactId = roomId.substringAfter("room_")
            if (contactId == "guru" || contactId == "wanderer" || contactId == "deep_web") {
                simulateContactReaction(roomId, contactId, text)
            }
        }
    }

    private fun simulateContactReaction(roomId: String, responderId: String, userText: String) {
        val handler = Handler(Looper.getMainLooper())
        
        // Typing Indicator delay
        handler.postDelayed({
            val name = if (responderId == "deep_web") "Hacker Guru" else if (responderId == "guru") "Hacker Guru" else "Alone Wanderer"
            val indicators = _typingIndicator.value.toMutableMap()
            indicators[roomId] = name
            _typingIndicator.value = indicators
        }, 1000)

        // Response Delay
        handler.postDelayed({
            // Clear typing
            val indicators = _typingIndicator.value.toMutableMap()
            indicators.remove(roomId)
            _typingIndicator.value = indicators

            // Prepare Auto Reply Text
            val responseText = when {
                userText.contains("exploit", true) || userText.contains("hack", true) -> {
                    "Analysis complete. Decrypting target nodes... Hacker Bhai network secured."
                }
                userText.contains("alone", true) || userText.contains("lonely", true) -> {
                    "Being alone breeds clarity. Let's build something secure."
                }
                userText.contains("hello", true) || userText.contains("hi", true) -> {
                    "Connection established. Terminal handshake authorized."
                }
                else -> {
                    "Inbound transmission received. Query logged to local database secure sector."
                }
            }

            val responderName = if (responderId == "deep_web") "Hacker Guru" else if (responderId == "guru") "Hacker Guru" else "Alone Wanderer"
            val respMsgId = "m_resp_" + System.currentTimeMillis()
            val respMsg = Message(
                id = respMsgId,
                roomId = roomId,
                senderId = if (responderId == "deep_web") "guru" else responderId,
                senderName = responderName,
                text = responseText,
                timestamp = System.currentTimeMillis()
            )

            val currentRoomMsgs = _messages.value[roomId]?.toMutableList() ?: mutableListOf()
            currentRoomMsgs.add(respMsg)
            val updatedMap = _messages.value.toMutableMap()
            updatedMap[roomId] = currentRoomMsgs
            _messages.value = updatedMap

            // Update room's last message and trigger simulated push notification toast indicator
            _rooms.value = _rooms.value.map { room ->
                if (room.id == roomId) {
                    room.copy(
                        lastMessageText = responseText,
                        lastMessageTime = System.currentTimeMillis(),
                        unreadCount = room.unreadCount + 1
                    )
                } else {
                    room
                }
            }
        }, 3000)
    }

    fun deleteMessage(roomId: String, messageId: String, forEveryone: Boolean) {
        val currentRoomMsgs = _messages.value[roomId]?.toMutableList() ?: return
        val index = currentRoomMsgs.indexOfFirst { it.id == messageId }
        if (index != -1) {
            val original = currentRoomMsgs[index]
            if (forEveryone) {
                currentRoomMsgs[index] = original.copy(
                    text = "☣️ This message was deleted for everyone.",
                    deletedForEveryone = true,
                    fileType = FileType.NONE,
                    fileUrl = ""
                )
            } else {
                currentRoomMsgs[index] = original.copy(
                    deletedForMe = true
                )
            }
            val updatedMap = _messages.value.toMutableMap()
            updatedMap[roomId] = currentRoomMsgs
            _messages.value = updatedMap
        }
    }

    fun addReaction(roomId: String, messageId: String, reactionEmoji: String) {
        val currentRoomMsgs = _messages.value[roomId]?.toMutableList() ?: return
        val index = currentRoomMsgs.indexOfFirst { it.id == messageId }
        if (index != -1) {
            val original = currentRoomMsgs[index]
            val hasReaction = original.reactions.contains(reactionEmoji)
            val updatedReactions = if (hasReaction) {
                original.reactions.filterNot { it == reactionEmoji }
            } else {
                original.reactions + reactionEmoji
            }
            currentRoomMsgs[index] = original.copy(reactions = updatedReactions)
            val updatedMap = _messages.value.toMutableMap()
            updatedMap[roomId] = currentRoomMsgs
            _messages.value = updatedMap
        }
    }

    fun markRoomRead(roomId: String) {
        _rooms.value = _rooms.value.map { room ->
            if (room.id == roomId) {
                room.copy(unreadCount = 0)
            } else {
                room
            }
        }
    }

    // --- Channel & Group Operations ---

    fun subscribeToChannel(roomId: String) {
        _rooms.value = _rooms.value.map { room ->
            if (room.id == roomId) {
                val updatedMembers = if (room.memberList.contains("me")) room.memberList else room.memberList + "me"
                room.copy(memberList = updatedMembers)
            } else {
                room
            }
        }
    }

    fun unsubscribeFromChannel(roomId: String) {
        _rooms.value = _rooms.value.map { room ->
            if (room.id == roomId) {
                room.copy(memberList = room.memberList.filterNot { it == "me" })
            } else {
                room
            }
        }
    }

    fun createChatRoom(name: String, description: String, type: RoomType): String {
        val roomId = "room_" + Random.nextInt(10000)
        val newRoom = ChatRoom(
            id = roomId,
            type = type,
            name = name,
            description = description,
            creatorId = "me",
            adminList = listOf("me"),
            memberList = if (type == RoomType.DIRECT) listOf("me") else listOf("me", "guru", "wanderer"),
            lastMessageText = "Room initiated securely.",
            lastMessageTime = System.currentTimeMillis()
        )
        _rooms.value = _rooms.value + newRoom
        _messages.value = _messages.value + (roomId to listOf(
            Message("m_init_$roomId", roomId, "me", "System", "Welcome to $name room. Encrypted channel opened.", FileType.NONE, "", null, null, emptyList(), System.currentTimeMillis(), false, 0, false, false, false)
        ))
        return roomId
    }

    fun updateGroupControls(roomId: String, updatedName: String, updatedDesc: String) {
        _rooms.value = _rooms.value.map { room ->
            if (room.id == roomId) {
                room.copy(name = updatedName, description = updatedDesc)
            } else {
                room
            }
        }
    }

    // --- Moderation & Administration ---

    fun blockUser(userId: String) {
        _blockedUsersIds.value = _blockedUsersIds.value + userId
    }

    fun unblockUser(userId: String) {
        _blockedUsersIds.value = _blockedUsersIds.value - userId
    }

    fun fileReport(reportedUserId: String, reason: String) {
        val refName = _contacts.value.find { it.id == reportedUserId }?.username ?: "Unknown Cyber User"
        val newReport = Report(
            id = "rep_" + System.currentTimeMillis(),
            reporterName = _currentUser.value?.username ?: "Anonymous",
            reportedUserId = reportedUserId,
            reportedUserName = refName,
            reason = reason,
            timestamp = System.currentTimeMillis()
        )
        _reports.value = _reports.value + newReport
    }

    fun adminDeleteUser(userId: String) {
        _contacts.value = _contacts.value.filterNot { it.id == userId }
        _rooms.value = _rooms.value.filterNot { it.creatorId == userId }
    }

    fun adminDeleteChannel(roomId: String) {
        _rooms.value = _rooms.value.filterNot { it.id == roomId }
    }

    fun adminDismissReport(reportId: String) {
        _reports.value = _reports.value.filterNot { it.id == reportId }
    }
}
