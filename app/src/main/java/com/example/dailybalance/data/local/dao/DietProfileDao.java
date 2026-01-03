package com.example.dailybalance.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.dailybalance.data.local.entity.DietProfile;

@Dao
public interface DietProfileDao {

    @Query("SELECT * FROM diet_profile ORDER BY createdDate DESC LIMIT 1")
    LiveData<DietProfile> getCurrentProfile();

    @Query("SELECT * FROM diet_profile ORDER BY createdDate DESC LIMIT 1")
    DietProfile getCurrentProfileSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DietProfile profile);

    @Update
    void update(DietProfile profile);

    @Query("DELETE FROM diet_profile")
    void deleteAll();
}
