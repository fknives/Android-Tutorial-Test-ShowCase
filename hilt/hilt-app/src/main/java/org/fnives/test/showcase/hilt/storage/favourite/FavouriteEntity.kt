package org.fnives.test.showcase.hilt.storage.favourite

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavouriteEntity(
    @ColumnInfo(name = "content_id")
    @PrimaryKey val contentId: String
)
