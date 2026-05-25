package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppShortcut
import com.example.data.LauncherRepository
import com.example.ui.theme.CarThemeStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class LauncherPipType(val title: String, val systemIcon: String) {
    MAPS("Bản Đồ HD", "map"),
    YOUTUBE("YouTube Entertainment", "video_library"),
    REAR_CAMERA("Camera Lùi AHD 1080P", "camera_rear"),
    MUSIC("Premium Music Player", "album")
}

data class FloatingWindow(
    val type: LauncherPipType,
    val title: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isSnapped: Boolean = false,
    val isMaximized: Boolean = false
)

data class SongInfo(
    val title: String,
    val artist: String,
    val source: String, // "Spotify" | "YouTube" | "Bluetooth" | "Local"
    val durationSeconds: Int,
    val lyrics: List<Pair<Int, String>> // Pair (TimeSeconds, ColumnString)
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = LauncherRepository(database)

    // ------------------------------------------------------------------------
    // Persistent Database State
    // ------------------------------------------------------------------------
    val shortcutsList = repository.shortcuts
    
    private val _themeStyle = MutableStateFlow(CarThemeStyle.CYBERPUNK)
    val themeStyle: StateFlow<CarThemeStyle> = _themeStyle.asStateFlow()

    private val _speedUnit = MutableStateFlow("km/h")
    val speedUnit: StateFlow<String> = _speedUnit.asStateFlow()

    private val _showLiveBg = MutableStateFlow(true)
    val showLiveBg: StateFlow<Boolean> = _showLiveBg.asStateFlow()

    // ------------------------------------------------------------------------
    // Engine Telemetry Simulation (Tesla/BMW style metrics)
    // ------------------------------------------------------------------------
    private val _speed = MutableStateFlow(0f)
    val speed: StateFlow<Float> = _speed.asStateFlow()

    private val _rpm = MutableStateFlow(800f)
    val rpm: StateFlow<Float> = _rpm.asStateFlow()

    private val _fuelLevel = MutableStateFlow(78)
    val fuelLevel: StateFlow<Int> = _fuelLevel.asStateFlow()

    private val _engineTemp = MutableStateFlow(90)
    val engineTemp: StateFlow<Int> = _engineTemp.asStateFlow()

    private val _isDrivingEngaged = MutableStateFlow(true)
    val isDrivingEngaged: StateFlow<Boolean> = _isDrivingEngaged.asStateFlow()

    private val _gpsSatellites = MutableStateFlow(12)
    val gpsSatellites: StateFlow<Int> = _gpsSatellites.asStateFlow()

    // Waveform simulation
    private val _audioWaveform = MutableStateFlow(floatArrayOf())
    val audioWaveform: StateFlow<FloatArray> = _audioWaveform.asStateFlow()

    private var simulationJob: Job? = null

    // ------------------------------------------------------------------------
    // Media Player State & Scrolling Lyrics
    // ------------------------------------------------------------------------
    private val songCatalog = listOf(
        SongInfo(
            title = "Hoa Nở Không Màu",
            artist = "Hoài Lâm",
            source = "Spotify Xe Hơi",
            durationSeconds = 210,
            lyrics = listOf(
                0 to "[Dạo nhạc chào mừng LecoAuto]",
                10 to "Chỉ là anh cố chấp... ngỡ đã quên đi",
                18 to "Nơi góc xưa tìm về, nhặt từng chiếc lá",
                26 to "Lòng nghe buốt giá thương cuộc tình tàn hoa",
                34 to "Thời gian đã lấy đi ngàn lời mong ước",
                42 to "Tình yêu như giấc mơ ngọt ngào nay vỡ nát...",
                50 to "Tiếc cho duyên chúng mình ly biệt không một câu",
                58 to "Như đoá hoa kia phai màu tàn úa theo mây sâu",
                66 to "Lời hứa xưa nay còn đâu gieo lòng đau bể dâu..."
            )
        ),
        SongInfo(
            title = "Chúng Ta Của Tương Lai",
            artist = "Sơn Tùng M-TP",
            source = "YouTube Music",
            durationSeconds = 180,
            lyrics = listOf(
                0 to "[Dạo nhạc mở đầu ca khúc]",
                12 to "Từ lâu anh đã thấy điều mập mờ trong tim",
                20 to "Từ lâu anh đã thức trọn từng đêm để kiếm tìm",
                28 to "Một hình bóng đã quá quen quen góc cũ kỉ niệm",
                36 to "Liệu rằng mai sau đôi ta có tìm thấy lối đi về chung đôi?",
                44 to "Cố giấu giọt lệ rơi giữa màn sương lạnh",
                52 to "Liệu tương lai có ấm êm như lúc bên anh?",
                60 to "Chúng ta của tương lai có đi qua bể dâu trùng khơi?"
            )
        ),
        SongInfo(
            title = "Anh Nhà Ở Đâu Thế",
            artist = "AMEE x B Ray",
            source = "Bluetooth Music",
            durationSeconds = 160,
            lyrics = listOf(
                0 to "[Dạo nhạc tươi vui cực chill]",
                8 to "Anh nhà ở đâu thế? Hì hì",
                16 to "Cứ gieo tương tư làm lòng em bối rối",
                24 to "Này anh gì ơi, cho em xin một lối",
                32 to "Nhà em thì xa, mà đường về chông chênh quá",
                40 to "Nếu anh không phiền thì đưa em rẽ lối...",
                48 to "Chỉ một nụ cười mà làm say cả đêm thu",
                56 to "Cứ ngỡ là mơ ai ngờ ngơ ngác đợi chờ."
            )
        )
    )

    private val _currentSongIndex = MutableStateFlow(0)
    val currentSong: StateFlow<SongInfo> = MutableStateFlow(songCatalog[0]).asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _songProgressSeconds = MutableStateFlow(25)
    val songProgressSeconds: StateFlow<Int> = _songProgressSeconds.asStateFlow()

    private val _currentLyricLine = MutableStateFlow("LecoAuto kính chào quý khách, chúc quý khách lái xe an toàn!")
    val currentLyricLine: StateFlow<String> = _currentLyricLine.asStateFlow()

    // ------------------------------------------------------------------------
    // Weather States Simulation (Realtime)
    // ------------------------------------------------------------------------
    private val cityWeatherList = listOf(
        Triple("Hà Nội", "Nắng ấm nhẹ", 29),
        Triple("TP. Hồ Chí Minh", "Mưa rào nhẹ", 33),
        Triple("Đà Nẵng", "Nắng rực rỡ", 31),
        Triple("Hải Phòng", "Nhiều mây gió", 27)
    )
    private val _selectedCityIndex = MutableStateFlow(0)
    val selectedCityIndex: StateFlow<Int> = _selectedCityIndex.asStateFlow()

    private val _weatherState = MutableStateFlow("Nắng ấm nhẹ, 29°C")
    val weatherState: StateFlow<String> = _weatherState.asStateFlow()

    // ------------------------------------------------------------------------
    // Picture in Picture / Floating Windows Custom System Layouts
    // ------------------------------------------------------------------------
    val floatingWindows = mutableStateListOf<FloatingWindow>()

    // ------------------------------------------------------------------------
    // App Drawer Custom Installed/Mock Apps
    // ------------------------------------------------------------------------
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val drawerCategories = listOf("Bản đồ", "Giải trí", "Hệ thống", "Tiện ích")
    private val _selectedCategory = MutableStateFlow("Tất cả")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    data class DrawerApp(
        val name: String,
        val packageName: String,
        val iconName: String,
        val category: String,
        val bannerColor: String
    )

    val drawerApps = listOf(
        DrawerApp("Bản Đồ Vietmap S2", "com.vietmap.s2", "map", "Bản đồ", "#00C853"),
        DrawerApp("Google Maps HD", "com.google.android.apps.maps", "map", "Bản đồ", "#1565C0"),
        DrawerApp("Navitel Navigation", "com.navitel.navigator", "map", "Bản đồ", "#D32F2F"),
        DrawerApp("Spotify Xe Hơi", "com.spotify.music", "music", "Giải trí", "#1DB954"),
        DrawerApp("YouTube Vanced", "com.google.android.youtube", "youtube", "Giải trí", "#FF0000"),
        DrawerApp("TV360 Android Auto", "com.tv360.auto", "video_library", "Giải trí", "#FFD600"),
        DrawerApp("Áp Suất Lốp TPMS", "com.tpms.car", "tire_pressure", "Tiện ích", "#0066CC"),
        DrawerApp("Chẩn Đoán OBD2", "com.obd2.torque", "obd", "Tiện ích", "#880E4F"),
        DrawerApp("Thời Tiết Live", "com.example.weather", "weather", "Tiện ích", "#00B0FF"),
        DrawerApp("Hệ Thống Camera 360", "com.car.camera360", "camera", "Hệ thống", "#37474F"),
        DrawerApp("Chrome Browser", "com.android.chrome", "chrome", "Hệ thống", "#FF9800"),
        DrawerApp("Cài đặt LecoAuto", "com.example.settings", "settings", "Hệ thống", "#607D8B"),
        DrawerApp("Trợ Lý Giọng Nói AI", "com.google.android.googlequicksearchbox", "voice", "Hệ thống", "#00E5FF")
    )

    init {
        // Load settings from db
        viewModelScope.launch {
            repository.populateDefaultShortcuts()
            
            val savedTheme = repository.getSetting("theme_style", CarThemeStyle.CYBERPUNK.name)
            _themeStyle.value = CarThemeStyle.valueOf(savedTheme)

            val savedUnit = repository.getSetting("speed_unit", "km/h")
            _speedUnit.value = savedUnit

            val savedBg = repository.getSetting("show_live_bg", "true")
            _showLiveBg.value = savedBg.toBoolean()
        }

        // Initialize background simulation
        startSimulation()
    }

    private fun startSimulation() {
        simulationJob = viewModelScope.launch {
            var time = 0.0
            
            // Initial mock waveform
            _audioWaveform.value = FloatArray(16) { 0.2f }

            while (true) {
                delay(100)
                time += 0.1

                // Simulate Telemetry if driving is engaged
                if (_isDrivingEngaged.value) {
                    // Cyclic speed simulation: goes from 30 to 110 beautifully
                    val simulatedSpeed = 60f + 30f * sin(time * 0.05).toFloat() + (sin(time * 0.3).toFloat() * 3f)
                    _speed.value = simulatedSpeed.coerceIn(0f, 220f)

                    // Target RPM follows speed dynamically
                    val gearFactor = (simulatedSpeed % 40) / 40f
                    val baseRpm = 1200f + (simulatedSpeed * 35) + (gearFactor * 800f)
                    _rpm.value = baseRpm.coerceIn(800f, 7500f)

                    // Slow temperature swings
                    _engineTemp.value = (88 + (2f * sin(time * 0.01)).toInt()).coerceIn(70, 115)

                    // Satellites slight signal noise
                    if (time.toInt() % 10 == 0) {
                        _gpsSatellites.value = (10 + (time.toInt() % 4)).coerceIn(6, 18)
                    }
                } else {
                    // Idle state simulation
                    _speed.value = 0f
                    _rpm.value = 750f + (sin(time * 2).toFloat() * 20f)
                }

                // Simulate audio visualizer peaks if music is playing
                if (_isPlaying.value) {
                    _audioWaveform.value = FloatArray(16) { index ->
                        val baseVal = 0.1f + 0.9f * sin(time * (index + 1) * 0.5).toFloat().coerceIn(0f, 1f)
                        baseVal * (0.8f + 0.2f * sin(time).toFloat())
                    }

                    // Increment song elapsed time
                    if (time.toInt() % 10 == 0) {
                        val currentProg = _songProgressSeconds.value
                        val maxProg = currentSong.value.durationSeconds
                        if (currentProg >= maxProg) {
                            nextSong()
                        } else {
                            _songProgressSeconds.value = currentProg + 1
                        }
                    }

                    // Sync real-time Scrolling Lyrics in Vietnamese
                    val song = currentSong.value
                    val currentSecs = _songProgressSeconds.value
                    var matchedLyric = song.lyrics.firstOrNull()?.second ?: ""
                    for (lyric in song.lyrics) {
                        if (currentSecs >= lyric.first) {
                            matchedLyric = lyric.second
                        }
                    }
                    _currentLyricLine.value = matchedLyric
                } else {
                    // Static visualizer
                    _audioWaveform.value = FloatArray(16) { 0.05f + 0.05f * sin(time * (it + 1) * 0.1).toFloat().coerceIn(0f, 1f) }
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // State Modification Methods (UI Event callbacks)
    // ------------------------------------------------------------------------
    fun setThemeStyle(style: CarThemeStyle) {
        _themeStyle.value = style
        viewModelScope.launch {
            repository.saveSetting("theme_style", style.name)
        }
    }

    fun setSpeedUnit(unit: String) {
        _speedUnit.value = unit
        viewModelScope.launch {
            repository.saveSetting("speed_unit", unit)
        }
    }

    fun setLiveBg(show: Boolean) {
        _showLiveBg.value = show
        viewModelScope.launch {
            repository.saveSetting("show_live_bg", show.toString())
        }
    }

    fun toggleDrivingSimulation() {
        _isDrivingEngaged.value = !_isDrivingEngaged.value
    }

    fun changeCity() {
        val nextIndex = (_selectedCityIndex.value + 1) % cityWeatherList.size
        _selectedCityIndex.value = nextIndex
        val info = cityWeatherList[nextIndex]
        _weatherState.value = "${info.second}, ${info.third}°C"
    }

    // ------------------------------------------------------------------------
    // Music controls
    // ------------------------------------------------------------------------
    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun nextSong() {
        _currentSongIndex.value = (_currentSongIndex.value + 1) % songCatalog.size
        updateSongSelection()
    }

    fun prevSong() {
        var prevIndex = _currentSongIndex.value - 1
        if (prevIndex < 0) prevIndex = songCatalog.size - 1
        _currentSongIndex.value = prevIndex
        updateSongSelection()
    }

    private fun updateSongSelection() {
        val nextSong = songCatalog[_currentSongIndex.value]
        (currentSong as MutableStateFlow<SongInfo>).value = nextSong
        _songProgressSeconds.value = 0
        _currentLyricLine.value = nextSong.lyrics.firstOrNull()?.second ?: ""
    }

    fun seekProgress(progress: Int) {
        _songProgressSeconds.value = progress.coerceIn(0, currentSong.value.durationSeconds)
    }

    // ------------------------------------------------------------------------
    // Picture in Picture / Window System Actions
    // ------------------------------------------------------------------------
    fun openPipWindow(type: LauncherPipType) {
        // If window already open, bring to front, otherwise add it
        val existing = floatingWindows.indexOfFirst { it.type == type }
        if (existing != -1) {
            val element = floatingWindows.removeAt(existing)
            floatingWindows.add(element) // Move to last position (top of painter stack)
        } else {
            // Determine custom coordinates avoiding stacking
            val offsetMultiplier = floatingWindows.size
            floatingWindows.add(
                FloatingWindow(
                    type = type,
                    title = type.title,
                    x = 80f + (offsetMultiplier * 40f),
                    y = 60f + (offsetMultiplier * 30f),
                    width = 380f,
                    height = 250f
                )
            )
        }
    }

    fun closePipWindow(type: LauncherPipType) {
        floatingWindows.removeAll { it.type == type }
    }

    fun minimizePipWindow(type: LauncherPipType) {
        closePipWindow(type)
    }

    fun updatePipPosition(type: LauncherPipType, newX: Float, newY: Float) {
        val index = floatingWindows.indexOfFirst { it.type == type }
        if (index != -1) {
            val win = floatingWindows[index]
            // Constraint check boundaries (limit within typical landscape space, roughly 1280x720)
            val boundedX = newX.coerceIn(0f, 1000f)
            val boundedY = newY.coerceIn(0f, 500f)
            floatingWindows[index] = win.copy(x = boundedX, y = boundedY)
        }
    }

    fun resizePipWindow(type: LauncherPipType, widthDelta: Float, heightDelta: Float) {
        val index = floatingWindows.indexOfFirst { it.type == type }
        if (index != -1) {
            val win = floatingWindows[index]
            val newWidth = (win.width + widthDelta).coerceIn(240f, 800f)
            val newHeight = (win.height + heightDelta).coerceIn(160f, 600f)
            floatingWindows[index] = win.copy(width = newWidth, height = newHeight)
        }
    }

    fun toggleMaximizePip(type: LauncherPipType) {
        val index = floatingWindows.indexOfFirst { it.type == type }
        if (index != -1) {
            val win = floatingWindows[index]
            floatingWindows[index] = win.copy(
                isMaximized = !win.isMaximized,
                // If maximizing, snap coordinate, else restore offsets
                x = if (!win.isMaximized) 20f else win.x,
                y = if (!win.isMaximized) 20f else win.y,
                width = if (!win.isMaximized) 1160f else 380f,
                height = if (!win.isMaximized) 530f else 250f
            )
        }
    }

    // ------------------------------------------------------------------------
    // Apps & Favorites actions
    // ------------------------------------------------------------------------
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleAppOnShortcutDock(drawerApp: DrawerApp) {
        viewModelScope.launch {
            val currentList = repository.getShortcutsList()
            val existing = currentList.firstOrNull { it.packageName == drawerApp.packageName }
            if (existing != null) {
                // Delete from favorites
                repository.removeShortcut(existing.id)
            } else {
                // Add to favorites
                val maxIndex = currentList.maxOfOrNull { it.orderIndex } ?: -1
                repository.addShortcut(
                    AppShortcut(
                        appName = drawerApp.name,
                        packageName = drawerApp.packageName,
                        iconName = drawerApp.iconName,
                        orderIndex = maxIndex + 1
                    )
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
