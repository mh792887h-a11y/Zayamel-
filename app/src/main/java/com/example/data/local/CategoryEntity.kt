package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val icon: String?,
    val sortOrder: Int
) {
    fun toDomain(): Category = Category(
        id = id,
        name = name,
        description = description,
        icon = icon,
        sortOrder = sortOrder
    )

    companion object {
        fun fromDomain(c: Category): CategoryEntity = CategoryEntity(
            id = c.id,
            name = c.name,
            description = c.description,
            icon = c.icon,
            sortOrder = c.sortOrder
        )
    }
}
