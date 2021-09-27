package com.dlog.mask;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Notice {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String mNoticeInfo;
    public Notice(String noticeInfo){
        this.mNoticeInfo = noticeInfo;
    }
}
