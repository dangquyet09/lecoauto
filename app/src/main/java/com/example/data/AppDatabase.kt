package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ------------------------------------------------------------------------
// Entities
// ------------------------------------------------------------------------

@Entity(tableName = "app_shortcuts")
data class AppShortcut(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appName: String,
    val packageName: String,
    val iconName: String, // "map", "music", "youtube", "camera", "settings", "chrome", "weather"
    val isSystemApp: Boolean = false,
    val orderIndex: Int
)

@Entity(tableName = "launcher_settings")
data class LauncherSetting(
    @PrimaryKey val configKey: String,
    val configValue: String
)

// ------------------------------------------------------------------------
// DAOs
// ------------------------------------------------------------------------

@Dao
interface LauncherDao {
    // Shortcuts
    @Query("SELECT * FROM app_shortcuts ORDER BY orderIndex ASC")
    fun getShortcutsFlow(): Flow<List<AppShortcut>>

    @Query("SELECT * FROM app_shortcuts ORDER BY orderIndex ASC")
    suspend fun getShortcutsList(): List<AppShortcut>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: AppShortcut)

    @Update
    suspend fun updateShortcut(shortcut: AppShortcut)

    @Query("DELETE FROM app_shortcuts WHERE id = :id")
    suspend fun deleteShortcut(id: Int)

    @Query("DELETE FROM app_shortcuts")
    suspend fun clearShortcuts()

    // Key-Value settings
    @Query("SELECT * FROM launcher_settings WHERE configKey = :key")
    suspend fun getSettingByKey(key: String): LauncherSetting?

    @Query("SELECT * FROM launcher_settings")
    fun getAllSettingsFlow(): Flow<List<LauncherSetting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: LauncherSetting)
}

// ------------------------------------------------------------------------
// Database
// ------------------------------------------------------------------------

@Database(entities = [AppShortcut::class, LauncherSetting::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val dao: LauncherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lecoauto_launcher.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ------------------------------------------------------------------------
// Repository
// ------------------------------------------------------------------------

class LauncherRepository(private val database: AppDatabase) {
    private val dao = database.dao

    // Shortcuts
    val shortcuts: Flow<List<AppShortcut>> = dao.getShortcutsFlow()

    suspend fun getShortcutsList() = dao.getShortcutsList()

    suspend fun addShortcut(shortcut: AppShortcut) = dao.insertShortcut(shortcut)

    suspend fun removeShortcut(id: Int) = dao.deleteShortcut(id)

    suspend fun populateDefaultShortcuts() {
        val current = dao.getShortcutsList()
        if (current.isEmpty()) {
            val defaults = listOf(
                AppShortcut(appName = "Bản Đồ", packageName = "com.google.android.apps.maps", iconName = "map", orderIndex = 0),
                AppShortcut(appName = "Âm Nhạc", packageName = "com.spotify.music", iconName = "music", orderIndex = 1),
                AppShortcut(appName = "YouTube", packageName = "com.google.android.youtube", iconName = "youtube", orderIndex = 2),
                AppShortcut(appName = "Camera", packageName = "com.android.camera", iconName = "camera", orderIndex = 3),
                AppShortcut(appName = "Trình Duyệt", packageName = "com.android.chrome", iconName = "chrome", orderIndex = 4),
                AppShortcut(appName = "Thời Tiết", packageName = "com.example.weather", iconName = "weather", orderIndex = 5),
                AppShortcut(appName = "Cài Đặt", packageName = "com.android.settings", iconName = "settings", orderIndex = 6)
            )
            for (shortcut in defaults) {
                dao.insertShortcut(shortcut)
            }
        }
    }

    // Settings helpers
    val allSettings: Flow<List<LauncherSetting>> = dao.getAllSettingsFlow()

    suspend fun getSetting(key: String, defaultValue: String): String {
        return dao.getSettingByKey(key)?.configValue ?: defaultValue
    }

    suspend fun saveSetting(key: String, value: String) {
        dao.saveSetting(LauncherSetting(key, value))
    }
}
