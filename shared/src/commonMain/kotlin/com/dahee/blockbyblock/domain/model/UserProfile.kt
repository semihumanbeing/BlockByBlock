package com.dahee.blockbyblock.domain.model

/**
 * User profile information including nickname, avatar, and linked email.
 */
data class UserProfile(
    val nickname: String = "블록 쉐프",
    val avatarType: ProfileAvatarType = ProfileAvatarType.PERSON,
    val email: String = "user@blockbyblock.com"
)
