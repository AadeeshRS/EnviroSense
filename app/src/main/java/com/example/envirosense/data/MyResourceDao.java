package com.example.envirosense.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.envirosense.data.models.MyResource;

import java.util.List;

/**
 * Data Access Object for personal resources stored locally.
 */
@Dao
public interface MyResourceDao {

    @Query("SELECT * FROM my_resources ORDER BY addedAt DESC")
    List<MyResource> getAllResources();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MyResource resource);

    @Delete
    void delete(MyResource resource);

    @Query("DELETE FROM my_resources WHERE id = :resourceId")
    void deleteById(String resourceId);
}
