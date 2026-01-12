package com.isolaatti.profile.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userProfile: UserProfileEntity)

    @Update
    suspend fun update(userProfile: UserProfileEntity)

    @Delete
    suspend fun delete(userProfile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getById(id: Long): UserProfileEntity?

    @Query("SELECT profileImageId FROM user_profiles WHERE id = :userId")
    fun getProfileImageId(userId: Long): Flow<String?>

    @Query("SELECT * FROM user_profiles WHERE isUserItself = 1 LIMIT 1")
    fun getMyUser(): Flow<UserProfileEntity?>

}
