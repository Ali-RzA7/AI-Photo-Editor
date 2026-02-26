package com.example.myapplication.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Room DAO: Geçmiş kayıtları için veri erişim arayüzü.
 */
@Dao
public interface HistoryDao {

    @Insert
    void insertHistory(HistoryEntity history);

    @Delete
    void deleteHistory(HistoryEntity history);

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    LiveData<List<HistoryEntity>> getAllHistory();
}
