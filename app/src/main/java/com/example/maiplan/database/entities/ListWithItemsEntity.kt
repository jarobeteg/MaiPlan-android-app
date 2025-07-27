package com.example.maiplan.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ListWithItemsEntity(
    @Embedded val list: ListEntity,

    @Relation(
        parentColumn = "list_id",
        entityColumn = "list_id"
    )
    val items: List<ListItemEntity>
)
