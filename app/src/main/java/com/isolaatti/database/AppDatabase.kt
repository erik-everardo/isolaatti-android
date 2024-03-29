package com.isolaatti.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.isolaatti.audio.drafts.data.AudioDraftEntity
import com.isolaatti.audio.drafts.data.AudiosDraftsDao
import com.isolaatti.auth.data.local.UserInfoDao
import com.isolaatti.auth.data.local.UserInfoEntity
import com.isolaatti.search.data.SearchDao
import com.isolaatti.search.data.SearchHistoryEntity
import com.isolaatti.settings.data.KeyValueDao
import com.isolaatti.settings.data.KeyValueEntity

@Database(entities = [KeyValueEntity::class, UserInfoEntity::class, AudioDraftEntity::class, SearchHistoryEntity::class], version = 7)
abstract class AppDatabase : RoomDatabase() {
    abstract fun keyValueDao(): KeyValueDao
    abstract fun userInfoDao(): UserInfoDao
    abstract fun audioDrafts(): AudiosDraftsDao
    abstract fun searchHistoryDao(): SearchDao

}