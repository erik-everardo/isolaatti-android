package com.isolaatti.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.isolaatti.images.common.data.dao.ImagesDraftsDao
import com.isolaatti.images.common.data.entity.ImageDraftEntity
import com.isolaatti.profile.data.local.UserProfileDao
import com.isolaatti.profile.data.local.UserProfileEntity
import com.isolaatti.search.data.SearchDao
import com.isolaatti.search.data.SearchHistoryEntity
import com.isolaatti.settings.data.KeyValueDao
import com.isolaatti.settings.data.KeyValueEntity

@Database(entities = [KeyValueEntity::class, SearchHistoryEntity::class, ImageDraftEntity::class, UserProfileEntity::class], version = 10, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun keyValueDao(): KeyValueDao
    abstract fun searchHistoryDao(): SearchDao
    abstract fun imagesDraftsDao(): ImagesDraftsDao
    abstract fun userProfileDao(): UserProfileDao
}