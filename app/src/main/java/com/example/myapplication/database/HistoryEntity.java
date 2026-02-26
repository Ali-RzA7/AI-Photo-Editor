package com.example.myapplication.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity: Çalışma geçmişi kaydı.
 * Görseller dosya yolu (String) olarak saklanır — BLOB kullanılmaz.
 */
@Entity(tableName = "history")
public class HistoryEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String moduleType;
    private String originalImagePath;
    private String resultImagePath;
    private long timestamp;

    public HistoryEntity(String moduleType, String originalImagePath, String resultImagePath, long timestamp) {
        this.moduleType = moduleType;
        this.originalImagePath = originalImagePath;
        this.resultImagePath = resultImagePath;
        this.timestamp = timestamp;
    }

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getModuleType() { return moduleType; }
    public void setModuleType(String moduleType) { this.moduleType = moduleType; }

    public String getOriginalImagePath() { return originalImagePath; }
    public void setOriginalImagePath(String originalImagePath) { this.originalImagePath = originalImagePath; }

    public String getResultImagePath() { return resultImagePath; }
    public void setResultImagePath(String resultImagePath) { this.resultImagePath = resultImagePath; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
