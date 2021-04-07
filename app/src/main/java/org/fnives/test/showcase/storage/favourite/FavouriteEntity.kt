package org.fnives.test.showcase.storage.favourite

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavouriteEntity(@PrimaryKey val contentId: String)
