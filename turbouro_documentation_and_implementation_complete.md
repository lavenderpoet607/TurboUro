# TurboUro

Aplikasi utilitas performa game Android open-source yang nyata tanpa efek placebo atau animasi palsu. TurboUro memanfaatkan API sistem Android native, Game Manager API, SurfaceFlinger dumpsys untuk pembacaan real-time FPS, serta pemantauan thermal hardware secara riil.

---

## 1. Fitur Utama

- **Real FPS Meter Overlay**: Mengambil data frame rate langsung dari Choreographer atau parsing statistik SurfaceFlinger via Shizuku/ADB shell, bukan kalkulasi tiruan timer loop.
- **Android Game Mode API (Android 12+)**: Menerapkan mode performa resmi sistem (`GameManager.GAME_MODE_PERFORMANCE` dan `GameManager.GAME_MODE_BATTERY`).
- **Hardware & Thermal Telemetry**: Membaca throttling state, frekuensi CPU/GPU, dan temperatur baterai langsung dari HardwarePropertiesManager dan BatteryManager.
- **Trimming Background Memory**: Meminta sistem membebaskan memory non-kritis sebelum game dimulai menggunakan standard Memory TRIM trigger.

---

## 2. Struktur Proyek

```text
app/
├── src/
│   └── main/
│       ├── AndroidManifest.xml
│       ├── java/com/turbouro/app/
│       │   ├── MainActivity.kt
│       │   ├── service/
│       │   │   ├── FpsOverlayService.kt
│       │   │   └── PerformanceDaemonService.kt
│       │   ├── manager/
│       │   │   ├── FpsCalculator.kt
│       │   │   ├── SystemGameOptimizer.kt
│       │   │   └── ThermalTracker.kt
│       │   └── ui/
│       │       └── DashboardScreen.kt
│       └── res/
│           ├── layout/
│           │   └── overlay_fps.xml
│           └── values/
│               ├── strings.xml
│               └── themes.xml
```

---

## 3. Konfigurasi AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
    <uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="TurboUro"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.TurboUro">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".service.FpsOverlayService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="specialUse" />

    </application>
</manifest>
```

---

## 4. Implementasi Pengukur FPS Nyata (FpsCalculator.kt)

Menggunakan callback native Choreographer untuk menghitung interval rendering antar-frame:

```kotlin
package com.turbouro.app.manager

import android.view.Choreographer

class FpsCalculator(private val onFpsCalculated: (Int) -> Unit) : Choreographer.FrameCallback {

    private var frameCount = 0
    private var lastFpsTimestamp = 0L
    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        frameCount = 0
        lastFpsTimestamp = System.nanoTime()
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun stop() {
        isRunning = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!isRunning) return

        frameCount++
        val elapsed = frameTimeNanos - lastFpsTimestamp

        if (elapsed >= 1_000_000_000L) {
            val fps = (frameCount * 1_000_000_000.0 / elapsed).toInt()
            onFpsCalculated(fps)
            frameCount = 0
            lastFpsTimestamp = frameTimeNanos
        }

        Choreographer.getInstance().postFrameCallback(this)
    }
}
```

---

## 5. Implementasi Game Mode API Resmi (SystemGameOptimizer.kt)

```kotlin
package com.turbouro.app.manager

import android.app.ActivityManager
import android.app.GameManager
import android.content.Context
import android.os.Build

class SystemGameOptimizer(private val context: Context) {

    fun applySystemGameMode(packageName: String, enableHighPerformance: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val gameManager = context.getSystemService(Context.GAME_SERVICE) as? GameManager
            val targetMode = if (enableHighPerformance) {
                GameManager.GAME_MODE_PERFORMANCE
            } else {
                GameManager.GAME_MODE_STANDARD
            }
            try {
                gameManager?.setGameMode(packageName, targetMode)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun cleanMemoryBudget() {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        activityManager?.let { am ->
            val runningProcesses = am.runningAppProcesses ?: return
            for (process in runningProcesses) {
                if (process.pkgList != null && process.pkgList.isNotEmpty()) {
                    for (pkg in process.pkgList) {
                        if (pkg != context.packageName) {
                            am.killBackgroundProcesses(pkg)
                        }
                    }
                }
            }
        }
        System.runFinalization()
        Runtime.getRuntime().gc()
    }
}
```

---

## 6. Implementasi Layanan Overlay FPS (FpsOverlayService.kt)

```kotlin
package com.turbouro.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.turbouro.app.R
import com.turbouro.app.manager.FpsCalculator

class FpsOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var fpsTextView: TextView? = null
    private lateinit var fpsCalculator: FpsCalculator

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        setupOverlayView()
        setupFpsCounter()
    }

    private fun startForegroundNotification() {
        val channelId = "TurboUro_Overlay_Channel"
        val channelName = "TurboUro Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("TurboUro Engine Aktif")
            .setContentText("Memantau telemetry rendering dan status frame rate.")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()

        startForeground(1001, notification)
    }

    private fun setupOverlayView() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 50
        }

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_fps, null)
        fpsTextView = overlayView?.findViewById(R.id.tvFpsValue)
        windowManager.addView(overlayView, params)
    }

    private fun setupFpsCounter() {
        fpsCalculator = FpsCalculator { currentFps ->
            fpsTextView?.post {
                fpsTextView?.text = "${currentFps} FPS"
                when {
                    currentFps >= 55 -> fpsTextView?.setTextColor(Color.GREEN)
                    currentFps in 30..54 -> fpsTextView?.setTextColor(Color.YELLOW)
                    else -> fpsTextView?.setTextColor(Color.RED)
                }
            }
        }
        fpsCalculator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        fpsCalculator.stop()
        if (overlayView != null) {
            windowManager.removeView(overlayView)
        }
    }
}
```

---

## 7. Layout Overlay (res/layout/overlay_fps.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:background="#99000000"
    android:padding="8dp"
    android:orientation="horizontal">

    <TextView
        android:id="@+id/tvFpsValue"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="-- FPS"
        android:textColor="#00FF00"
        android:textStyle="bold"
        android:textSize="14sp"
        android:fontFamily="monospace" />

</LinearLayout>
```

---

## 8. Kontrol Utama (MainActivity.kt)

```kotlin
package com.turbouro.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.turbouro.app.manager.SystemGameOptimizer
import com.turbouro.app.service.FpsOverlayService

class MainActivity : AppCompatActivity() {

    private val overlayPermissionReqCode = 2001
    private lateinit var optimizer: SystemGameOptimizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        optimizer = SystemGameOptimizer(this)

        val btnStart = findViewById<Button>(R.id.btnStartOverlay)
        val btnStop = findViewById<Button>(R.id.btnStopOverlay)
        val btnBoost = findViewById<Button>(R.id.btnRealTrim)

        btnStart.setOnClickListener {
            if (checkOverlayPermission()) {
                startService(Intent(this, FpsOverlayService::class.java))
            } else {
                requestOverlayPermission()
            }
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, FpsOverlayService::class.java))
        }

        btnBoost.setOnClickListener {
            optimizer.cleanMemoryBudget()
            Toast.makeText(this, "Memory background berhasil di-trim.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, overlayPermissionReqCode)
        }
    }
}
```

---

## 9. Penjelasan Teknis Integritas Performa

1. **Non-Placebo FPS Counter**: Menggunakan native callback loop Choreographer yang sinkron langsung dengan vertical refresh display perangkat.
2. **Standard API Memory Eviction**: Tidak menggunakan animasi progress bar palsu, melainkan invocation langsung ke `ActivityManager.killBackgroundProcesses()` dan runtime GC clearing.
3. **Android Standard Game Mode Integration**: Mengoperasikan konfigurasi downstream langsung ke kernel scheduler dan driver GPU via implementasi Android 12 Game Mode System.

---

## 10. Komponen yang Masih Kurang dan Wajib Ditambahkan

Dokumentasi dasar TurboUro sudah mencakup overlay FPS, Game Mode, memory trimming, dan telemetry. Namun agar aplikasi menjadi utilitas gaming yang benar-benar lengkap dan dapat digunakan lintas perangkat Android modern, komponen berikut perlu ditambahkan.

### 10.1 Game Library dan Pemilihan Game

TurboUro perlu memiliki daftar aplikasi/game yang terdeteksi dari perangkat.

Struktur tambahan:

```text
manager/
├── GameScanner.kt
├── GameProfileManager.kt
├── PackageMonitor.kt
└── AppLaunchManager.kt
```

Fungsi:

- Mendeteksi aplikasi yang terpasang.
- Memfilter aplikasi berdasarkan kategori game bila metadata tersedia.
- Menampilkan nama, icon, package name, dan status game mode.
- Menambahkan game secara manual.
- Menghapus game dari daftar TurboUro.
- Menjalankan game langsung dari TurboUro.
- Menyimpan profil optimasi berbeda untuk setiap game.

Model data:

```kotlin
data class GameProfile(
    val packageName: String,
    val gameName: String,
    val performanceMode: Boolean,
    val fpsOverlay: Boolean,
    val targetFps: Int,
    val autoOptimize: Boolean,
    val thermalProtection: Boolean
)
```

### 10.2 Auto Game Detection

Tambahkan mekanisme untuk mengetahui aplikasi yang sedang berada di foreground.

```text
PackageMonitor.kt
        ↓
Foreground package
        ↓
GameProfileManager
        ↓
Apply profile
        ↓
Start/stop telemetry
```

Untuk Android modern, gunakan API yang sesuai dengan kemampuan perangkat dan izin yang tersedia. Jangan mengandalkan `PACKAGE_USAGE_STATS` sebagai satu-satunya mekanisme tanpa menjelaskan bahwa akses Usage Access harus diberikan pengguna.

### 10.3 Profil Performa

Tambahkan tiga profil bawaan:

```text
Balanced
Performance
Battery Saver
```

Setiap profil harus dapat mengatur:

- Game Mode.
- FPS overlay.
- Refresh-rate preference jika tersedia melalui API yang sah.
- Frekuensi telemetry.
- Thermal monitoring.
- Background optimization.
- Notifikasi.
- Target FPS.

Tambahkan profil custom agar pengguna dapat membuat konfigurasi sendiri.

### 10.4 Thermal Protection

File `ThermalTracker.kt` yang disebutkan di struktur proyek belum memiliki implementasi.

Tambahkan:

```text
manager/
└── ThermalTracker.kt
```

Data yang perlu dipantau:

- Thermal status.
- Battery temperature.
- CPU/GPU temperature jika tersedia.
- Thermal throttling state.
- Perubahan temperatur dari waktu ke waktu.

Gunakan `PowerManager` thermal API pada versi Android yang mendukungnya dan `BatteryManager` untuk temperatur baterai.

Status:

```text
COOL
NORMAL
WARM
HOT
CRITICAL
```

Ketika perangkat masuk kondisi kritis, TurboUro harus mengutamakan perlindungan perangkat daripada memaksa performa maksimum.

### 10.5 CPU dan GPU Telemetry

Tambahkan:

```text
manager/
├── CpuTelemetry.kt
└── GpuTelemetry.kt
```

Telemetry harus memiliki fallback.

```text
Native Android API
        ↓
Available vendor API
        ↓
Readable system information
        ↓
Unavailable
```

Jangan menganggap `/sys` path CPU/GPU tertentu tersedia pada semua perangkat. Vendor Qualcomm, MediaTek, Samsung, Google, dan perangkat lain dapat menggunakan struktur berbeda.

UI harus menampilkan:

```text
CPU
GPU
Temperature
Thermal Status
Battery
Refresh Rate
FPS
Frame Time
```

Jika sebuah metrik tidak tersedia, tampilkan:

```text
N/A
```

bukan nilai palsu.

### 10.6 Frame Time dan FPS Stability

FPS saja belum cukup untuk mengetahui kelancaran game.

Tambahkan:

```text
Frame Time
Average FPS
Minimum FPS
Maximum FPS
1% Low
0.1% Low
Frame Drop
```

Gunakan window sampling yang jelas, misalnya 1 detik untuk overlay dan sesi yang lebih panjang untuk statistik.

Contoh:

```text
FPS       59
Frame     16.9 ms
1% Low    48
0.1% Low  41
```

### 10.7 Catatan Penting Mengenai FPS Choreographer

Implementasi `FpsCalculator` saat ini menggunakan `Choreographer` dari proses TurboUro sendiri. Ini berarti callback tersebut tidak otomatis mengukur rendering pipeline aplikasi game lain.

Karena itu, dokumentasi tidak boleh menyatakan bahwa implementasi tersebut selalu mengukur FPS game eksternal.

Pisahkan dua mode:

```text
App FPS
    ↓
Choreographer callback TurboUro

Game FPS
    ↓
SurfaceFlinger / dumpsys / Shizuku / metode sistem
    ↓
Tergantung versi Android dan akses yang tersedia
```

Jika sumber game FPS tidak tersedia, UI harus menyatakan bahwa data game FPS tidak tersedia daripada mengklaim angka tersebut sebagai FPS game.

### 10.8 Shizuku Integration

Tambahkan dukungan opsional:

```text
integration/
└── ShizukuManager.kt
```

Fungsi:

- Mengecek apakah Shizuku tersedia.
- Mengecek permission Shizuku.
- Menjalankan operasi shell yang memang diizinkan.
- Membaca informasi SurfaceFlinger jika perangkat dan permission mendukung.
- Menampilkan status koneksi Shizuku.

Status:

```text
Shizuku
Connected
Permission Granted
```

atau:

```text
Shizuku
Unavailable
```

TurboUro tetap harus dapat berjalan tanpa Shizuku.

### 10.9 ADB Mode

Tambahkan dokumentasi mode ADB sebagai fitur opsional untuk debugging dan telemetry lanjutan.

```text
ADB Available
ADB Permission
Shell Capability
SurfaceFlinger Capability
```

Jangan menjadikan ADB sebagai dependency wajib untuk fungsi dasar aplikasi.

### 10.10 Game Session

Setiap kali pengguna menjalankan game melalui TurboUro, buat session.

Model:

```kotlin
data class GameSession(
    val packageName: String,
    val startTime: Long,
    val endTime: Long?,
    val averageFps: Double,
    val minFps: Int,
    val maxFps: Int,
    val averageFrameTime: Double,
    val maxTemperature: Double,
    val batteryStart: Int,
    val batteryEnd: Int
)
```

Data session dapat digunakan untuk halaman:

```text
Gaming History
```

Contoh:

```text
Mobile Legends
34 min

Average FPS
58

1% Low
46

Max Temperature
41.2°C

Battery
82% → 71%
```

### 10.11 Dashboard Statistik

Tambahkan:

```text
ui/
├── DashboardScreen.kt
├── GameLibraryScreen.kt
├── GameDetailScreen.kt
├── PerformanceScreen.kt
├── ThermalScreen.kt
├── SessionHistoryScreen.kt
└── SettingsScreen.kt
```

Dashboard minimum:

```text
TurboUro

Device
CPU
GPU
RAM
Temperature
Refresh Rate

Current Game
FPS
Frame Time
Thermal Status
Battery

[Start Boost]
[Open Game]
[Overlay]
```

### 10.12 Pengaturan Overlay

Overlay jangan hanya menampilkan FPS.

Pengguna harus dapat memilih:

```text
[x] FPS
[x] Frame Time
[x] Temperature
[x] CPU
[x] GPU
[x] Battery
[x] Refresh Rate
[ ] RAM
```

Tambahkan pengaturan:

- Posisi.
- Ukuran.
- Opacity.
- Ukuran teks.
- Update interval.
- Orientation.
- Lock position.

### 10.13 Notifikasi Foreground Service

Foreground service harus memiliki lifecycle yang jelas.

Tambahkan:

```text
TurboUro Engine
FPS Overlay
Thermal Monitor
```

Notification action:

```text
Pause
Resume
Stop Overlay
Open TurboUro
```

Service juga harus menangani:

- `START_STICKY` atau strategi lifecycle yang sesuai.
- `onTaskRemoved()`.
- `onDestroy()`.
- Re-creation setelah proses dihentikan sistem.
- Null-safe cleanup terhadap overlay.
- Exception handling ketika window sudah tidak tersedia.

### 10.14 Android Modern Compatibility

Dokumentasi perlu membedakan:

```text
Android 8+
Android 12+
Android 13+
Android 14+
Android 15+
Android 16+
```

Periksa setiap fitur berdasarkan API level.

Perhatian khusus:

- Foreground Service restrictions.
- Notification permission.
- Overlay permission.
- Package visibility.
- Game Mode API.
- Thermal API.
- Battery optimization.
- Background execution restrictions.
- Exact behavior setiap vendor.

Jangan menjanjikan bahwa satu API akan memberikan kemampuan identik pada semua perangkat.

### 10.15 Runtime Permission Manager

Tambahkan:

```text
manager/
└── PermissionManager.kt
```

Kelola status:

```text
SYSTEM_ALERT_WINDOW
POST_NOTIFICATIONS
PACKAGE_USAGE_STATS
Shizuku
Battery Optimization
```

UI harus menjelaskan mengapa setiap permission diperlukan.

### 10.16 Persistent Settings

Gunakan penyimpanan lokal seperti DataStore untuk:

- Profil aktif.
- Overlay enabled.
- Overlay position.
- Overlay metrics.
- Target FPS.
- Theme.
- Selected games.
- Auto optimization.
- Thermal protection.
- Notification preferences.

Struktur:

```text
storage/
├── TurboUroPreferences.kt
└── GameProfileStore.kt
```

### 10.17 Boot dan Process Recovery

Jika pengguna mengaktifkan fitur auto-start, tambahkan mekanisme boot yang mengikuti batasan Android modern.

```text
receiver/
└── BootReceiver.kt
```

Jangan memulai foreground service secara agresif dari background tanpa mematuhi aturan Android versi target.

### 10.18 Battery Optimization Awareness

Tambahkan pemeriksaan apakah TurboUro dibatasi oleh battery optimization.

UI:

```text
Background Activity
Allowed / Restricted

Battery Optimization
Optimized / Exempted
```

Berikan shortcut menuju halaman pengaturan sistem bila diperlukan.

### 10.19 Error dan Capability System

Semua fitur harus memiliki status kemampuan:

```kotlin
enum class CapabilityState {
    AVAILABLE,
    LIMITED,
    REQUIRES_PERMISSION,
    REQUIRES_SHIZUKU,
    UNSUPPORTED,
    ERROR
}
```

Dengan demikian aplikasi tidak menampilkan tombol seolah-olah fitur tersedia ketika perangkat tidak mendukungnya.

### 10.20 Device Compatibility Matrix

Tambahkan halaman diagnosa:

```text
Device Compatibility

Game Mode          AVAILABLE
Thermal API        AVAILABLE
Overlay            AVAILABLE
Game FPS           LIMITED
CPU Frequency      AVAILABLE
GPU Frequency      N/A
Shizuku            NOT CONNECTED
Usage Access       GRANTED
Notifications      GRANTED
```

Ini penting karena kemampuan telemetry Android sangat bergantung pada API level dan vendor.

---

## 11. Struktur Proyek yang Disarankan

Struktur proyek diperluas menjadi:

```text
app/
└── src/
    └── main/
        ├── AndroidManifest.xml
        ├── java/com/turbouro/app/
        │   ├── MainActivity.kt
        │   │
        │   ├── manager/
        │   │   ├── FpsCalculator.kt
        │   │   ├── SystemGameOptimizer.kt
        │   │   ├── ThermalTracker.kt
        │   │   ├── CpuTelemetry.kt
        │   │   ├── GpuTelemetry.kt
        │   │   ├── GameScanner.kt
        │   │   ├── GameProfileManager.kt
        │   │   ├── AppLaunchManager.kt
        │   │   ├── PermissionManager.kt
        │   │   └── CapabilityManager.kt
        │   │
        │   ├── service/
        │   │   ├── FpsOverlayService.kt
        │   │   └── PerformanceDaemonService.kt
        │   │
        │   ├── integration/
        │   │   ├── ShizukuManager.kt
        │   │   └── AdbManager.kt
        │   │
        │   ├── storage/
        │   │   ├── TurboUroPreferences.kt
        │   │   └── GameProfileStore.kt
        │   │
        │   ├── receiver/
        │   │   └── BootReceiver.kt
        │   │
        │   ├── model/
        │   │   ├── GameProfile.kt
        │   │   ├── GameSession.kt
        │   │   └── DeviceTelemetry.kt
        │   │
        │   └── ui/
        │       ├── DashboardScreen.kt
        │       ├── GameLibraryScreen.kt
        │       ├── GameDetailScreen.kt
        │       ├── PerformanceScreen.kt
        │       ├── ThermalScreen.kt
        │       ├── SessionHistoryScreen.kt
        │       └── SettingsScreen.kt
        │
        └── res/
            ├── layout/
            │   ├── activity_main.xml
            │   └── overlay_fps.xml
            ├── drawable/
            ├── mipmap/
            └── values/
                ├── strings.xml
                ├── colors.xml
                └── themes.xml
```

---

## 12. Arsitektur Runtime

Arsitektur utama yang disarankan:

```text
                    TurboUro
                       │
             ┌─────────┴─────────┐
             │                   │
         Dashboard          Game Library
             │                   │
             └─────────┬─────────┘
                       │
                 Game Profile
                       │
          ┌────────────┼────────────┐
          │            │            │
      Game Mode     Telemetry    Thermal
          │            │            │
          │       ┌────┴────┐       │
          │       │         │       │
          │      FPS       Device   │
          │       │       Sensors   │
          │       └────┬────┘       │
          │            │            │
          └────────────┼────────────┘
                       │
                 Session Engine
                       │
                 Statistics DB
                       │
                 History Screen
```

---

## 13. Prinsip Anti-Placebo

TurboUro harus mengikuti aturan berikut:

```text
Tidak ada angka FPS yang dibuat-buat.
Tidak ada temperatur yang diestimasi lalu ditampilkan sebagai sensor.
Tidak ada CPU/GPU frequency palsu.
Tidak ada progress bar "boost" tanpa operasi sistem nyata.
Tidak ada klaim RAM bertambah.
Tidak ada klaim kernel diubah tanpa akses yang benar-benar tersedia.
```

Setiap telemetry harus memiliki sumber:

```text
Metric
  ↓
Source
  ↓
Validation
  ↓
Value
  ↓
UI
```

Jika source tidak tersedia:

```text
N/A
```

bukan:

```text
0
```

dan bukan angka estimasi yang dipresentasikan sebagai data hardware.

---

## 14. Memory Optimization yang Lebih Aman

Implementasi `killBackgroundProcesses()` tidak boleh diposisikan sebagai "menambah RAM".

Gunakan istilah:

```text
Background Process Cleanup
```

bukan:

```text
RAM Booster
```

TurboUro tidak boleh menghentikan aplikasi secara agresif tanpa alasan karena Android dapat menjalankan kembali proses tersebut dan vendor dapat memiliki kebijakan memory management sendiri.

Prioritas:

```text
Game
    ↓
TurboUro
    ↓
Sistem Android
    ↓
Aplikasi background
```

Biarkan Android menentukan proses mana yang aman dihentikan.

---

## 15. Game Launch Flow

Flow yang direkomendasikan:

```text
User memilih game
        ↓
Load GameProfile
        ↓
Validate capabilities
        ↓
Apply supported settings
        ↓
Start telemetry
        ↓
Start overlay jika aktif
        ↓
Launch game
        ↓
Monitor session
        ↓
Detect game exit
        ↓
Stop telemetry
        ↓
Save session
        ↓
Restore TurboUro state
```

Jika suatu konfigurasi gagal:

```text
Game tetap dapat dijalankan
```

Jangan membuat seluruh proses launch gagal hanya karena satu fitur optional tidak tersedia.

---

## 16. Thermal Safety Policy

Gunakan kebijakan konservatif:

```text
NORMAL
    → tidak ada perubahan

WARM
    → tampilkan peringatan

HOT
    → rekomendasikan menurunkan beban

CRITICAL
    → hentikan boost/telemetry yang tidak diperlukan
    → tampilkan peringatan
    → prioritaskan pendinginan perangkat
```

TurboUro tidak boleh memaksa perangkat tetap berada pada performa maksimum ketika sistem Android telah memasuki thermal throttling atau kondisi kritis.

---

## 17. Hal yang Harus Diperbaiki dari Implementasi Saat Ini

### FpsCalculator

Implementasi saat ini cocok sebagai pengukuran frame callback milik proses TurboUro, tetapi tidak boleh dianggap sebagai pengukur FPS aplikasi game lain.

Untuk game FPS eksternal, gunakan jalur telemetry yang memang dapat mengamati SurfaceFlinger atau sumber sistem lain yang tersedia.

### SystemGameOptimizer

Pemanggilan `GameManager.setGameMode()` harus diperlakukan sebagai capability yang bergantung pada API level, target package, permission, dan implementasi vendor.

Jangan menganggap semua perangkat mengizinkan TurboUro mengubah Game Mode aplikasi lain.

### cleanMemoryBudget

`killBackgroundProcesses()` tidak menjamin seluruh RAM menjadi bebas dan tidak boleh dijelaskan sebagai peningkatan RAM permanen.

`Runtime.gc()` juga bukan mekanisme untuk membersihkan RAM sistem secara keseluruhan.

### FpsOverlayService

Overlay membutuhkan:

```text
Overlay Permission
Foreground Service
Notification
Lifecycle Handling
Android Version Compatibility
```

Service harus menangani kegagalan `WindowManager.addView()` dan `removeView()` agar aplikasi tidak crash ketika permission dicabut atau window sudah tidak valid.

---

## 18. Database Lokal Session

Untuk history yang lebih lengkap, gunakan database lokal.

Struktur minimal:

```text
Game
├── packageName
├── name
├── icon
└── profile

GameSession
├── id
├── packageName
├── startTime
├── endTime
├── averageFps
├── minFps
├── maxFps
├── onePercentLow
├── zeroPointOnePercentLow
├── averageFrameTime
├── maxTemperature
├── batteryStart
└── batteryEnd
```

Room dapat digunakan jika kebutuhan penyimpanan berkembang.

---

## 19. Halaman Diagnostik

Tambahkan halaman:

```text
Diagnostics
```

Isi:

```text
Android Version
API Level
Device
Manufacturer
Model

Game Mode API
Thermal API
Overlay
Usage Access
Notification Permission
Battery Optimization
Shizuku
ADB

CPU Telemetry
GPU Telemetry
Game FPS Telemetry
```

Setiap item memiliki status:

```text
Available
Limited
Permission Required
Unsupported
```

---

## 20. Checklist Implementasi Final

```text
[ ] Game Library
[ ] Game Scanner
[ ] Manual Game Add
[ ] Game Profiles
[ ] Auto Game Detection
[ ] Game Launch
[ ] Game Session
[ ] Session History
[ ] Average FPS
[ ] Min FPS
[ ] Max FPS
[ ] 1% Low
[ ] 0.1% Low
[ ] Frame Time
[ ] FPS Overlay
[ ] Overlay Customization
[ ] CPU Telemetry
[ ] GPU Telemetry
[ ] Battery Temperature
[ ] Thermal Status
[ ] Refresh Rate
[ ] Game Mode
[ ] Background Cleanup
[ ] Shizuku Integration
[ ] Optional ADB Integration
[ ] Permission Manager
[ ] Capability Manager
[ ] Persistent Settings
[ ] Battery Optimization Status
[ ] Foreground Service Lifecycle
[ ] Boot Recovery
[ ] Diagnostics
[ ] Android API Compatibility
[ ] Error Handling
[ ] N/A Fallback
[ ] Anti-Placebo Validation
```

## 21. Target Akhir TurboUro

TurboUro sebaiknya diposisikan sebagai:

```text
Android Gaming Performance & Telemetry Utility
```

bukan sekadar:

```text
RAM Booster
```

Fokus utama:

```text
1. Real telemetry
2. Game management
3. Performance profiles
4. Thermal awareness
5. FPS/frame-time monitoring
6. Session statistics
7. Android capability detection
8. Optional Shizuku/ADB integration
9. Safe system integration
10. Zero fake performance claims
```

Dengan struktur ini, TurboUro memiliki fondasi untuk menjadi aplikasi utilitas gaming yang lebih lengkap, transparan, dan kompatibel dengan perbedaan kemampuan perangkat Android.
