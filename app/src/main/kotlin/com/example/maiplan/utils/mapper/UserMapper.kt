package com.example.maiplan.utils.mapper

import com.example.maiplan.database.entities.UserEntity
import com.example.maiplan.network.api.UserResponse
import java.time.Instant
import java.util.UUID

fun UserResponse.toUserEntity(): UserEntity {
    return UserEntity(
        email = email.trim().lowercase(),
        username = username,
        syncId = UUID.fromString(syncId),
        serverVersion = serverVersion,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
        deletedAt = deletedAt?.let(Instant::parse)
    )
}