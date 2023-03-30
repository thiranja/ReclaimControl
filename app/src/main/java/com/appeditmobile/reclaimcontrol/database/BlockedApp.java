package com.appeditmobile.reclaimcontrol.database;

import java.sql.Date;

public class BlockedApp {

    public static final int BLOCK_TYPE_NONE = 0;
    public static final int BLOCK_TYPE_WARN = 1;
    public static final int BLOCK_TYPE_BLOCK = 2;
    public static final int BLOCK_TYPE_LOCK = 3;


    // Define fields for the BlockedApp table
    private String packageName;
    private int launchCount;
    private Date lastAccessedDate;
    private int blockType;
    private int attemptCount;

    // Constructor
    public BlockedApp(String packageName, int launchCount, Date lastAccessedDate, int blockType, int attemptCount) {
        this.packageName = packageName;
        this.launchCount = launchCount;
        this.lastAccessedDate = lastAccessedDate;
        this.blockType = blockType;
        this.attemptCount = attemptCount;
    }

    // Getters and setters for packageName, launchCount, and lastAccessedDate
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getLaunchCount() {
        return launchCount;
    }

    public void setLaunchCount(int launchCount) {
        this.launchCount = launchCount;
    }

    public Date getLastAccessedDate() {
        return lastAccessedDate;
    }

    public void setLastAccessedDate(Date lastAccessedDate) {
        this.lastAccessedDate = lastAccessedDate;
    }

    // Method to increment the launch count
    public void incrementLaunchCount() {
        launchCount++;
    }

    // Method to log all the data in the database
    public String toString() {
        return "BlockedApp [packageName=" + packageName + ", launchCount=" + launchCount + ", lastAccessedDate="
                + lastAccessedDate + "]";
    }

    public int getBlockType() {
        return blockType;
    }

    public void setBlockType(int blockType) {
        this.blockType = blockType;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    // Method to increment the launch count
    public void incrementAttemptCount() {
        attemptCount++;
    }
}
