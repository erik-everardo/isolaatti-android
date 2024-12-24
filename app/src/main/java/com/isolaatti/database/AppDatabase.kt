package com.isolaatti.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.isolaatti.auth.data.local.UserInfoDao
import com.isolaatti.auth.data.local.UserInfoEntity
import com.isolaatti.images.common.data.dao.ImagesDraftsDao
import com.isolaatti.images.common.data.entity.ImageDraftEntity
import com.isolaatti.search.data.SearchDao
import com.isolaatti.search.data.SearchHistoryEntity
import com.isolaatti.settings.data.KeyValueDao
import com.isolaatti.settings.data.KeyValueEntity

@Database(entities = [KeyValueEntity::class, UserInfoEntity::class, SearchHistoryEntity::class, ImageDraftEntity::class], version = 9)
abstract class AppDatabase : RoomDatabase() {
    abstract fun keyValueDao(): KeyValueDao
    abstract fun userInfoDao(): UserInfoDao
    abstract fun searchHistoryDao(): SearchDao
    abstract fun imagesDraftsDao(): ImagesDraftsDao
}