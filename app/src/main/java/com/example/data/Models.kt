package com.example.data

import java.io.Serializable

enum class RoomType {
    DIRECT, GROUP, CHANNEL
}

enum class FileType {
    NONE, IMAGE, VIDEO, DOCUMENT, VOICE
}

enum class UserRole {
    USER, ADMIN
}

data class UserProfile(
    val id: String = "",
    val phone: String = "",
    val email: String = "",
    val username: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val isOnline: Boolean = false,
    val role: UserRole = UserRole.USER,
    val isBlocked: Boolean = false
) : Serializable

data class Message(
    val id: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val fileType: FileType = FileType.NONE,
    val fileUrl: String = "",
    val replyToId: String? = null,
    val replyToText: String? = null,
    // Store reactions as list of string keys
    val reactions: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isVoice: Boolean = false,
    val voiceDuration: Int = 0,
    val deletedForMe: Boolean = false,
    val deletedForEveryone: Boolean = false,
    val isRead: Boolean = false
) : Serializable

data class ChatRoom(
    val id: String = "",
    val type: RoomType = RoomType.DIRECT,
    val name: String = "",
    val description: String = "",
    val avatarUrl: String = "",
    val creatorId: String = "",
    val adminList: List<String> = emptyList(),
    val memberList: List<String> = emptyList(),
    val lastMessageText: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
) : Serializable

data class Report(
    val id: String = "",
    val reporterName: String = "",
    val reportedUserId: String = "",
    val reportedUserName: String = "",
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

data class AnalyticsData(
    val activeUsers: Int = 312,
    val totalChats: Int = 1245,
    val totalChannels: Int = 18,
    val messageVolume: List<Int> = listOf(450, 680, 890, 1100, 950, 1500, 1720)
) : Serializable
