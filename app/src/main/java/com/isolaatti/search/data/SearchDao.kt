package com.isolaatti.search.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SearchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTerm(searchTerm: SearchHistoryEntity)

    @Query("SELECT COUNT(*) FROM search_history WHERE `query` = :query")
    fun itemCount(query: String): Int

    @Transaction
    fun insertTermTransaction(searchTerm: SearchHistoryEntity) {
        if(itemCount(searchTerm.query) > 0) {
            deleteTerm(searchTerm.query)
        }
        insertTerm(searchTerm)
    }

    @Query("DELETE FROM search_history WHERE `query` = :searchTerm")
    fun deleteTerm(searchTerm: String)

    @Query("SELECT *, `rowid` FROM search_history ORDER BY time DESC")
    fun getTerms(): List<SearchHistoryEntity>

    @Query("""
        SELECT *, `rowid` FROM search_history WHERE `query` MATCH :searchQuery
    """)
    fun getTermsForQuery(searchQuery: String): List<SearchHistoryEntity>
}