package com.example.envirosense.data;

public class Achievement {
    public int id;
    public String title;
    public String description;
    public String longDescription;
    public int targetHours;
    public int iconResId;


    public boolean isUnlocked;
    public int currentProgressHrs;

    public Achievement(int id, String title, String description, String longDescription, int targetHours, int iconResId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.longDescription = longDescription;
        this.targetHours = targetHours;
        this.iconResId = iconResId;
        this.isUnlocked = false;
        this.currentProgressHrs = 0;
    }
}