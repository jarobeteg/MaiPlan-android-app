package com.example.maiplan.category.data


data class CreateCategoryInput(
    val name: String,
    val description: String = "",
    val color: String,
    val icon: String
)

data class UpdateCategoryInput(
    val categoryLocalId: Long,
    val name: String,
    val description: String = "",
    val color: String,
    val icon: String
)

data class CategoryMutationPayload(
    val name: String,
    val description: String = "",
    val color: String,
    val icon: String
)
