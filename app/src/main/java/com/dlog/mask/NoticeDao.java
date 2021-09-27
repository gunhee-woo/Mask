package com.dlog.mask;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoticeDao {
    @Query("SELECT * FROM notice ORDER BY id ASC")
    List<Notice> getAllNotice();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNotices(Notice... notices);
    @Update
    void updateNotices(Notice... notices);

    @Query("DELETE FROM Notice")
    void nukeTable();
}
