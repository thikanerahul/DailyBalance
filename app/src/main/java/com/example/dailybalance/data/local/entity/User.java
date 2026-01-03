package com.example.dailybalance.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    public String uid; // From Firebase Auth

    public String name;
    public String email;
    public String photoUrl;

    public User(@NonNull String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
    }
}
