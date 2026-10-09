# TurboUro Mobile Design System

## 1. Identitas Aplikasi

**Nama:** TurboUro

**Kategori:** Android Gaming Performance & Telemetry Utility

**Karakter desain:**
- Modern
- Clean
- Technical
- Gaming-oriented
- Premium
- Tidak berlebihan
- Fokus pada data nyata
- Tidak menggunakan animasi boost palsu
- Tidak menggunakan indikator performa dekoratif yang tidak memiliki sumber data

TurboUro harus terasa seperti aplikasi utility profesional untuk pengguna yang ingin mengetahui kondisi perangkat dan performa game secara nyata.

---

# 2. Design Direction

## Visual Style

Gunakan pendekatan:

```text
Dark Technical UI
+
Minimal Gaming Accent
+
Data Dashboard
+
Rounded Cards
+
Subtle Glow
```

Hindari:
- Background penuh gambar gaming
- Efek neon berlebihan
- Animasi loading yang menyatakan perangkat sedang "dibost" tanpa operasi nyata
- Progress bar palsu
- Terlalu banyak warna
- Glassmorphism ekstrem yang mengurangi keterbacaan
- Informasi terlalu padat pada satu layar

UI harus mengutamakan keterbacaan data.

---

# 3. Color System

## Dark Theme

```text
Background Primary:   #090B10
Background Secondary: #11141B
Surface:              #171B23
Surface Elevated:     #1D222C
Border:               #282E39

Text Primary:         #F5F7FA
Text Secondary:       #A8AFBA
Text Muted:           #6F7784

Accent Primary:       #7CFF6B
Accent Secondary:     #57C7FF

Success:              #62E294
Warning:              #FFC857
Danger:               #FF5C67
Info:                 #58B8FF
```

Accent utama digunakan untuk:
- FPS normal
- Tombol utama
- Status aktif
- Selected state
- Highlight data penting

Warning digunakan untuk:
- Thermal warm/hot
- Permission belum aktif
- Capability terbatas

Danger digunakan untuk:
- Thermal critical
- Error
- Service stopped karena masalah

---

# 4. Light Theme

TurboUro tetap menyediakan light theme.

```text
Background Primary:   #F5F7FA
Background Secondary: #FFFFFF
Surface:              #FFFFFF
Surface Elevated:     #EEF1F5
Border:               #DCE1E8

Text Primary:         #11151C
Text Secondary:       #5D6673
Text Muted:           #89919D

Accent Primary:       #2E9D45
Accent Secondary:     #168AC2

Success:              #278A48
Warning:              #B47700
Danger:               #D53B48
Info:                 #147DB2
```

---

# 5. Typography

Gunakan font sans-serif modern.

Prioritas:

```text
Roboto
```

atau font sistem Android yang konsisten.

Hierarchy:

```text
Display
32sp / Bold

Screen Title
24sp / Bold

Section Title
18sp / SemiBold

Card Title
16sp / SemiBold

Body
14sp / Regular

Secondary
12sp / Regular

Telemetry Value
24sp / Bold

Telemetry Unit
12sp / Medium
```

Angka telemetry harus menggunakan font dengan karakter angka yang mudah dibaca.

---

# 6. Spacing System

Gunakan kelipatan 4dp.

```text
4dp   Micro
8dp   Small
12dp  Compact
16dp  Standard
20dp  Medium
24dp  Large
32dp  Section
40dp  Major
```

Screen horizontal padding:

```text
16dp
```

Tablet:

```text
24dp
```

---

# 7. Corner Radius

```text
Small Button: 10dp
Card:         16dp
Large Card:   20dp
Bottom Sheet: 24dp
Chip:         999dp
Overlay:      10dp
```

Gunakan radius konsisten.

---

# 8. Elevation

Gunakan elevation rendah.

```text
Card:
2dp - 4dp

Dialog:
8dp

Bottom Sheet:
8dp

Floating Action:
6dp
```

Jangan menggunakan shadow terlalu berat.

---

# 9. Iconography

Gunakan Material Icons atau icon set yang konsisten.

Ikon utama:

```text
Dashboard       dashboard
Games           sports_esports
Performance     speed
Thermal         device_thermostat
History         history
Settings        settings
Diagnostics     troubleshoot
Battery         battery_full
CPU             memory
GPU             developer_board
FPS             monitor_heart
Overlay         layers
Play            play_arrow
Stop            stop
Warning         warning
Success         check_circle
Error           error
```

Ikon tidak boleh digunakan sebagai satu-satunya indikator status. Selalu sertakan teks untuk status penting.

---

# 10. Navigation

Gunakan Bottom Navigation untuk navigasi utama.

```text
┌──────────────────────────────────────┐
│                                      │
│              CONTENT                 │
│                                      │
├──────────────────────────────────────┤
│  Home     Games    Performance  More │
└──────────────────────────────────────┘
```

Tab utama:

```text
Home
Games
Performance
More
```

`More` berisi:

```text
Thermal
History
Diagnostics
Settings
```

Untuk perangkat kecil, tetap gunakan 4 item agar navigation tidak terlalu padat.

---

# 11. Splash Screen

## Layout

```text
        ┌──────────────────┐
        │                  │
        │                  │
        │      [ICON]      │
        │                  │
        │    TurboUro      │
        │                  │
        │  Game Telemetry  │
        │                  │
        └──────────────────┘
```

Tidak menggunakan animasi progress "boost".

Splash hanya digunakan untuk:
- Branding
- Initial capability check
- Loading konfigurasi lokal

---

# 12. Onboarding

Onboarding maksimal 3 halaman.

## Page 1

```text
Real Game Telemetry

Monitor FPS, frame time,
temperature, battery and
device status.
```

Visual:
- Smartphone
- Telemetry cards
- FPS graph sederhana

## Page 2

```text
Game Profiles

Simpan pengaturan berbeda
untuk setiap game.
```

## Page 3

```text
Safe Performance

TurboUro membaca kemampuan
perangkat dan tidak memalsukan
data telemetry.
```

CTA:

```text
Get Started
```

---

# 13. Home Dashboard

Home adalah halaman utama.

## Header

```text
TurboUro                         ⚙

Good evening
Device is ready
```

Jika thermal bermasalah:

```text
TurboUro                         ⚙

Thermal warning
Device temperature is elevated
```

---

# 14. Device Status Card

```text
┌──────────────────────────────────┐
│ DEVICE STATUS              ● OK  │
│                                  │
│ CPU              GPU             │
│ 2.4 GHz          680 MHz         │
│                                  │
│ Temperature      Battery         │
│ 37.8°C           78%             │
│                                  │
│ Refresh Rate                     │
│ 120 Hz                           │
└──────────────────────────────────┘
```

Jika data tidak tersedia:

```text
GPU
N/A
```

Jangan menampilkan angka estimasi sebagai telemetry nyata.

---

# 15. Current Game Card

Jika tidak ada game:

```text
┌──────────────────────────────────┐
│ CURRENT GAME                     │
│                                  │
│ No game running                  │
│                                  │
│ Select a game from your library  │
│                                  │
│ [ Open Game Library ]            │
└──────────────────────────────────┘
```

Jika game aktif:

```text
┌──────────────────────────────────┐
│ CURRENT GAME                     │
│                                  │
│ 🎮 Mobile Legends                │
│                                  │
│ 59 FPS          16.9 ms          │
│                                  │
│ Thermal         Battery          │
│ Normal          72%              │
│                                  │
│ [ View Session ]                 │
└──────────────────────────────────┘
```

---

# 16. Quick Actions

```text
┌──────────────┐ ┌──────────────┐
│ ▶ Open Game  │ │ ◉ FPS Overlay│
└──────────────┘ └──────────────┘

┌──────────────┐ ┌──────────────┐
│ ⚡ Optimize  │ │ 🌡 Thermal   │
└──────────────┘ └──────────────┘
```

Tombol `Optimize` hanya menampilkan operasi yang benar-benar dilakukan.

Contoh:

```text
Preparing game
Applying supported profile
Starting telemetry
```

Bukan:

```text
Boosting RAM 87%
CPU Turbo 200%
```

---

# 17. Game Library

## Header

```text
Games                         ＋
Your Games
```

Search:

```text
🔍 Search games
```

Filter:

```text
All
Recent
Favorites
```

Game card:

```text
┌──────────────────────────────────┐
│ [ICON] Mobile Legends            │
│        com.mobile.legends        │
│                                  │
│        Performance Profile       │
│        ● Ready                   │
│                                  │
│        [ Play ]                  │
└──────────────────────────────────┘
```

Game card tidak boleh menampilkan klaim FPS ketika game belum berjalan.

---

# 18. Game Detail

Header:

```text
← Mobile Legends             ⋮
```

Hero card:

```text
┌──────────────────────────────────┐
│             [ICON]               │
│                                  │
│       Mobile Legends             │
│       Performance Profile        │
│                                  │
│             [ Play ]              │
└──────────────────────────────────┘
```

Sections:

```text
Performance
Overlay
Thermal
Session History
```

---

# 19. Game Profile

```text
Performance Profile

Game Mode
[ Performance ▼ ]

Auto Optimize
[ ON ]

FPS Overlay
[ ON ]

Thermal Protection
[ ON ]

Target FPS
[ 60 ]

Background Cleanup
[ ON ]

[ Save Profile ]
```

Semua option yang tidak didukung perangkat harus disabled dengan alasan.

Contoh:

```text
Game Mode
Unavailable on this device
```

---

# 20. Performance Screen

Header:

```text
Performance
```

Live telemetry:

```text
┌──────────────────────────────────┐
│ FPS                              │
│                                  │
│              59                  │
│             FPS                  │
│                                  │
│ Average      58                  │
│ 1% Low       46                  │
│ 0.1% Low     41                  │
└──────────────────────────────────┘
```

Frame time:

```text
Frame Time

16.9 ms
```

Graph:

```text
FPS
60 ┤╭─╮╭────╮
55 ┤│ ╰╯    ╰─╮
50 ┤│         ╰─
45 ┤
   └────────────────
```

Graph harus berasal dari telemetry aktual.

---

# 21. Performance Profile Selector

Bottom sheet:

```text
Performance Profile

○ Balanced
  Normal performance and battery usage

● Performance
  Prioritize supported performance settings

○ Battery Saver
  Reduce performance load

○ Custom
  User-defined settings

[ Apply ]
```

---

# 22. Thermal Screen

Header:

```text
Thermal
```

Main card:

```text
┌──────────────────────────────────┐
│ THERMAL STATUS                   │
│                                  │
│              37.8°C              │
│               NORMAL             │
│                                  │
│ Battery        CPU        GPU    │
│ 37.8°C         N/A        N/A    │
└──────────────────────────────────┘
```

Thermal history:

```text
Temperature

42°C ┤
40°C ┤      ╭─╮
38°C ┤╭─────╯ ╰──╮
36°C ┤╯          ╰──
    └────────────────
```

Status chip:

```text
● Cool
● Normal
● Warm
● Hot
● Critical
```

---

# 23. Session History

```text
Gaming History

Today

┌──────────────────────────────────┐
│ Mobile Legends                   │
│ 34 min · Today                   │
│ Avg 58 FPS · Max 41.2°C          │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│ Genshin Impact                   │
│ 27 min · Today                   │
│ Avg 52 FPS · Max 43.1°C          │
└──────────────────────────────────┘
```

---

# 24. Session Detail

```text
Mobile Legends

34 min

Performance

Average FPS       58
Minimum FPS       39
Maximum FPS       60
1% Low            46
0.1% Low          41
Frame Time        17.2 ms

Thermal

Max Temperature   41.2°C

Battery

Start             82%
End               71%

[ Delete Session ]
```

---

# 25. Overlay Design

Overlay harus minimal dan tidak mengganggu gameplay.

## Compact

```text
┌────────────────┐
│ 59 FPS  16.9ms │
└────────────────┘
```

## Standard

```text
┌────────────────────┐
│ 59 FPS             │
│ 16.9 ms   38°C     │
└────────────────────┘
```

## Full

```text
┌────────────────────┐
│ FPS       59       │
│ Frame     16.9 ms  │
│ CPU       N/A      │
│ GPU       N/A      │
│ Temp      38°C     │
│ Battery   72%      │
└────────────────────┘
```

Overlay harus:
- Semi-transparent
- Tidak menerima touch secara default
- Bisa dipindahkan
- Bisa dikunci
- Bisa diatur opacity
- Bisa memilih metrik

---

# 26. Overlay Settings

```text
FPS Overlay

Enabled                 [ ON ]

Metrics

FPS                     [ ON ]
Frame Time              [ ON ]
Temperature             [ ON ]
CPU                     [ OFF ]
GPU                     [ OFF ]
Battery                 [ ON ]
Refresh Rate            [ OFF ]

Appearance

Opacity                 ━━━━━●━━
Text Size               ━━━●━━━━
Update Interval         1 sec

Position

[ Reset Position ]

[ Save ]
```

---

# 27. Diagnostics

Halaman diagnostics harus menjadi halaman teknis.

```text
Diagnostics

Device
Android 15
API 35
Samsung / Example

Capabilities

Game Mode              ✓ Available
Thermal API            ✓ Available
Overlay                ✓ Available
Usage Access           ✓ Granted
Notifications          ✓ Granted
Shizuku                ! Not Connected
Game FPS               ! Limited
GPU Telemetry          — N/A
```

Status:

```text
✓ Available
! Limited
! Permission Required
— Unsupported
× Error
```

---

# 28. Permission Center

```text
Permissions

Overlay
Allow TurboUro to display
telemetry over games

[ Open Settings ]

Usage Access
Required for foreground
game detection

[ Open Settings ]

Notifications
Required for foreground
service status

[ Grant ]

Shizuku
Optional advanced telemetry

[ Setup ]
```

Jelaskan alasan permission sebelum membuka system settings.

---

# 29. Settings

Sections:

```text
General
Performance
Overlay
Notifications
Battery
Data
About
```

General:

```text
Theme
○ System
○ Dark
○ Light

Start on Boot
[ OFF ]

Auto Detect Game
[ ON ]
```

Performance:

```text
Default Profile
[ Balanced ]

Thermal Protection
[ ON ]

Background Cleanup
[ ON ]
```

Data:

```text
Save Session History
[ ON ]

Clear Session History
[ Clear ]
```

About:

```text
TurboUro
Android Gaming Performance
& Telemetry Utility

Version 1.0.0

Open Source

Licenses
Privacy
Diagnostics
```

---

# 30. More Screen

```text
More

🌡 Thermal
View thermal telemetry

◷ History
View gaming sessions

🔧 Diagnostics
Check device capabilities

⚙ Settings
Configure TurboUro

ⓘ About
Application information
```

---

# 31. Empty States

Game kosong:

```text
No Games Yet

TurboUro couldn't find any
supported games.

[ Scan Again ]
[ Add Game ]
```

History kosong:

```text
No Gaming Sessions

Your completed gaming sessions
will appear here.
```

Telemetry tidak tersedia:

```text
Data Unavailable

This device does not expose
this telemetry through the
available Android APIs.
```

Shizuku belum aktif:

```text
Advanced Telemetry Unavailable

Connect Shizuku to unlock
supported advanced telemetry.

[ Setup Shizuku ]
```

---

# 32. Error States

Gunakan error yang jelas.

Contoh:

```text
Unable to Start Overlay

Overlay permission is disabled.

[ Open Permission Settings ]
```

Game Mode:

```text
Game Mode Unavailable

Android or the device vendor
does not expose this operation
to TurboUro.
```

Telemetry:

```text
GPU Telemetry Unavailable

The device does not expose a
readable GPU frequency source.
```

Jangan gunakan:

```text
Boost Failed
```

jika tidak ada operasi boost yang sebenarnya gagal.

---

# 33. Thermal Warning

Jika temperature meningkat:

```text
┌──────────────────────────────────┐
│ ⚠ Thermal Warning                │
│                                  │
│ Device temperature is elevated. │
│ Performance may be reduced by   │
│ Android thermal management.     │
│                                  │
│ [ View Thermal ]                 │
└──────────────────────────────────┘
```

Critical:

```text
┌──────────────────────────────────┐
│ ⚠ Critical Temperature          │
│                                  │
│ Device is significantly hot.    │
│ Reduce gaming load and allow    │
│ the device to cool.             │
│                                  │
│ [ Stop Overlay ]                 │
│ [ View Thermal ]                │
└──────────────────────────────────┘
```

---

# 34. Loading States

Loading hanya digunakan ketika benar-benar menunggu data.

Contoh:

```text
Detecting games...
```

```text
Reading device capabilities...
```

```text
Loading session...
```

Hindari:

```text
Boosting CPU...
Optimizing RAM...
Turbo charging GPU...
```

jika proses tersebut tidak benar-benar dilakukan.

---

# 35. Toast dan Snackbar

Gunakan snackbar untuk feedback singkat.

Contoh:

```text
Profile applied
```

```text
Overlay started
```

```text
Overlay stopped
```

```text
Session saved
```

Error:

```text
Unable to start overlay
```

Durasi:

```text
2–4 seconds
```

---

# 36. Confirmation Dialog

Untuk operasi destruktif:

```text
Clear Session History?

All saved gaming session
statistics will be deleted.

[ Cancel ] [ Clear ]
```

Untuk menghentikan service:

```text
Stop FPS Overlay?

Live telemetry overlay will
be removed.

[ Cancel ] [ Stop ]
```

---

# 37. Responsive Design

## Small Phone

Prioritas:

```text
Single column
16dp padding
Compact cards
Bottom navigation
Scrollable content
```

## Large Phone

Gunakan:

```text
16–24dp padding
Larger telemetry cards
2-column quick actions
```

## Tablet

Gunakan layout:

```text
┌──────────────┬──────────────────────┐
│ Navigation   │ Dashboard            │
│              │                      │
│ Home         │ Device Status        │
│ Games        │ Current Game         │
│ Performance  │ Performance          │
│ Thermal      │                      │
│ History      │                      │
│ Settings     │                      │
└──────────────┴──────────────────────┘
```

---

# 38. Accessibility

Minimum:

- Touch target 48dp.
- Kontras teks tinggi.
- Jangan mengandalkan warna saja.
- Status menggunakan icon + text.
- Support system font scaling.
- Semua icon button memiliki content description.
- Jangan membuat telemetry terlalu kecil.
- Jangan menggunakan warna merah/hijau sebagai satu-satunya pembeda.

Contoh:

```text
✓ Available
! Limited
× Error
```

bukan hanya warna.

---

# 39. Animation

Gunakan animasi ringan:

```text
Screen transition
Card expansion
Bottom sheet
Graph update
Toggle
```

Durasi:

```text
150–250ms
```

FPS overlay tidak boleh menggunakan animasi angka yang berlebihan.

Telemetry harus terasa real-time, bukan seperti efek gaming.

---

# 40. Performance UI

UI TurboUro harus hemat resource.

Hindari:
- Animasi infinite yang tidak diperlukan.
- Background blur berat.
- Rendering graph dengan terlalu banyak data.
- Update seluruh dashboard setiap frame.
- Rebuild semua component ketika satu telemetry berubah.

Recommended:

```text
Telemetry update
     ↓
State update
     ↓
Only affected widget rebuilds
```

Untuk overlay:

```text
1 update / second
```

dapat digunakan sebagai default untuk mengurangi overhead, dengan opsi interval yang dapat dikonfigurasi.

---

# 41. Information Hierarchy

Setiap halaman harus mengikuti:

```text
Title
↓
Current Status
↓
Primary Metric
↓
Supporting Metrics
↓
Detailed Information
↓
Actions
```

Contoh Performance:

```text
Performance
↓
59 FPS
↓
Frame Time
↓
1% Low / 0.1% Low
↓
FPS Graph
↓
Session Details
```

---

# 42. Component Library

Buat reusable components:

```text
TurboCard
TelemetryCard
StatusChip
MetricRow
GameCard
ProfileCard
QuickActionCard
SectionHeader
PermissionCard
CapabilityRow
ThermalIndicator
PerformanceGraph
EmptyState
ErrorState
PrimaryButton
SecondaryButton
IconButton
SettingsTile
```

Semua komponen harus menggunakan Design System yang sama.

---

# 43. Primary Button

```text
┌──────────────────────────────┐
│          Start Game          │
└──────────────────────────────┘
```

Height:

```text
48–52dp
```

Radius:

```text
12dp
```

Primary button digunakan untuk satu action utama per section.

---

# 44. Secondary Button

```text
┌──────────────────────────────┐
│        View Details          │
└──────────────────────────────┘
```

Gunakan untuk action sekunder.

---

# 45. Status Chip

```text
● NORMAL
● ACTIVE
● LIMITED
● N/A
● ERROR
```

Chip harus tetap memiliki label teks.

---

# 46. Data Visualization

Gunakan graph sederhana.

Prioritas:

```text
FPS
Temperature
Frame Time
Battery
```

Jangan menampilkan graph jika sample tidak cukup.

Minimum:

```text
5–10 samples
```

Jika data belum tersedia:

```text
Collecting telemetry...
```

---

# 47. Game Running Indicator

Ketika game aktif:

```text
● GAME ACTIVE
```

Home dashboard dapat memiliki accent indicator.

Ketika tidak aktif:

```text
○ NO GAME
```

---

# 48. Foreground Service Indicator

Jika overlay/service aktif:

```text
TurboUro Engine
● Active
```

Jika service berhenti:

```text
TurboUro Engine
○ Inactive
```

Notification sistem menjadi sumber status service yang sebenarnya.

---

# 49. Design Rules

```text
1. Data nyata > dekorasi
2. Keterbacaan > efek
3. Capability aware > asumsi
4. Safety > maximum performance
5. Empty state harus jelas
6. N/A lebih baik daripada data palsu
7. Semua permission harus dijelaskan
8. Semua action harus memberikan feedback
9. UI tidak boleh menjanjikan kemampuan hardware
10. Telemetry harus memiliki sumber yang jelas
```

---

# 50. Final App Flow

```text
Splash
  ↓
Onboarding
  ↓
Permission Center
  ↓
Home
  ├── Current Game
  │      ↓
  │   Game Detail
  │      ↓
  │   Game Profile
  │      ↓
  │   Launch Game
  │      ↓
  │   Live Performance
  │      ↓
  │   Session History
  │
  ├── Games
  │      ↓
  │   Game Library
  │
  ├── Performance
  │      ↓
  │   FPS / Frame Time
  │
  └── More
         ├── Thermal
         ├── History
         ├── Diagnostics
         └── Settings
```

---

# 51. Final Visual Target

TurboUro harus terlihat seperti gabungan:

```text
Gaming Dashboard
+
System Monitor
+
Performance Utility
```

Dengan prioritas visual:

```text
Clean
   ↓
Readable
   ↓
Technical
   ↓
Gaming
   ↓
Premium
```

Bukan:

```text
Neon
   ↓
Heavy Animation
   ↓
Fake Boost
   ↓
Visual Noise
```

Tujuan utama desain adalah membuat pengguna dapat membuka TurboUro dan dalam waktu kurang dari beberapa detik memahami:

```text
Game apa yang aktif?
FPS berapa?
Frame time berapa?
Temperatur berapa?
Battery berapa?
Apakah thermal normal?
Fitur apa yang tersedia?
Apakah overlay aktif?
```

Semua informasi penting harus dapat dipahami tanpa membuka banyak halaman.
