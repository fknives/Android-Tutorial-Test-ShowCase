package org.fnives.test.showcase.hilt.storage.favourite

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {

    @Query("SELECT * FROM FavouriteEntity")
    fun get(): Flow<List<FavouriteEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavourite(favouriteEntity: FavouriteEntity)

    @Delete
    suspend fun deleteFavourite(favouriteEntity: FavouriteEntity)
}
