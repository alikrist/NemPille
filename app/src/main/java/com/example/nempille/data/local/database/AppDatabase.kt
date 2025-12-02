package com.example.nempille.data.local.database
//main database class
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nempille.data.local.dao.MedicationDao
import com.example.nempille.data.local.entity.MedicationEntity

// Version = database schema version. Change it when structure changes.
// exportSchema = false disables saving schema files.
@Database(
    entities = [MedicationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Provide access to DAOs.
    abstract fun medicationDao(): MedicationDao
}