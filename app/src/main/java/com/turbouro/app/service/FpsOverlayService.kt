package com.turbouro.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.turbouro.app.MainActivity
import com.turbouro.app.R
import com.turbouro.app.manager.CpuTelemetry
import com.turbouro.app.manager.FpsCalculator
import com.turbouro.app.manager.FpsMetrics
import com.turbouro.app.manager.GpuTelemetry
import com.turbouro.app.manager.PackageMonitor
import com.turbouro.app.manager.SystemGameOptimizer
import com.turbouro.app.manager.ThermalTracker
import com.turbouro.app.model.OverlayMode
import com.turbouro.app.storage.TurboUroPreferences
import com.turbouro.app.ui.view.CrosshairView
import com.turbouro.app.ui.view.FrameTimeGraphView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class FpsOverlayService : Service() {

    companion object {
        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
        const val EXTRA_CROSSHAIR_ENABLED = "extra_crosshair_enabled"
        const val EXTRA_CROSSHAIR_STYLE = "extra_crosshair_style"
        const val EXTRA_CROSSHAIR_COLOR = "extra_crosshair_color"
        const val EXTRA_DND_ENABLED = "extra_dnd_enabled"
        const val EXTRA_LOCK_BRIGHTNESS = "extra_lock_brightness"
        const val EXTRA_SHOW_MINI_GRAPH = "extra_show_mini_graph"
        const val EXTRA_THERMAL_ALERT = "extra_thermal_alert"

        const val ACTION_STOP_OVERLAY = "com.turbouro.app.ACTION_STOP_OVERLAY"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "TurboUro_Overlay_Channel"
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var windowManager: WindowManager
    private lateinit var prefs: TurboUroPreferences
    private lateinit var fpsCalculator: FpsCalculator
    private lateinit var thermalTracker: ThermalTracker
    private lateinit var packageMonitor: PackageMonitor
    private lateinit var optimizer: SystemGameOptimizer
    private val cpuTelemetry = CpuTelemetry()
    private val gpuTelemetry = GpuTelemetry()

    private var overlayView: View? = null
    private var windowParams: WindowManager.LayoutParams? = null

    private var crosshairView: CrosshairView? = null
    private var crosshairParams: WindowManager.LayoutParams? = null
    private var isCrosshairAttached = false
    private var isCrosshairActive = false

    private var sidebarHandle: View? = null
    private var telemetryPanel: View? = null
    private var btnCollapseOverlay: View? = null

    private var frameTimeGraphView: FrameTimeGraphView? = null
    private var btnActionCleanRam: TextView? = null
    private var btnActionDnd: TextView? = null
    private var btnActionCrosshair: TextView? = null
    private var btnActionBrightness: TextView? = null

    private var tvFpsValue: TextView? = null
    private var tvFrameTimeValue: TextView? = null
    private var tvTempValue: TextView? = null
    private var tvBatteryValue: TextView? = null
    private var tvCpuValue: TextView? = null
    private var tvGpuValue: TextView? = null
    private var tvRefreshRateValue: TextView? = null
    private var containerExtraStandard: View? = null
    private var containerExtraFull: View? = null

    private var isExpanded = true
    private var isDndActive = false
    private var isBrightnessLockActive = false
    private var isThermalAlertEnabled = true
    private var showMiniGraph = true
    private var lastVibrateTime = 0L

    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private var isOverlayAttached = false
    private var targetPackage: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = TurboUroPreferences(this)
        thermalTracker = ThermalTracker(this)
        packageMonitor = PackageMonitor(this)
        optimizer = SystemGameOptimizer(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        fpsCalculator = FpsCalculator { metrics ->
            handler.post {
                updateFpsViews(metrics)
            }
        }

        startForegroundNotification()
        setupOverlayView()
        setupCrosshairView()
        startTelemetryLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_OVERLAY) {
            stopSelf()
            return START_NOT_STICKY
        }

        val pkg = intent?.getStringExtra(EXTRA_TARGET_PACKAGE)
        if (pkg != null) {
            targetPackage = pkg
        }

        isCrosshairActive = intent?.getBooleanExtra(EXTRA_CROSSHAIR_ENABLED, false) ?: false
        val crosshairStyle = intent?.getStringExtra(EXTRA_CROSSHAIR_STYLE) ?: "CROSS"
        val crosshairColor = intent?.getStringExtra(EXTRA_CROSSHAIR_COLOR) ?: "#00E5FF"
        crosshairView?.style = crosshairStyle
        crosshairView?.crosshairColor = try {
            Color.parseColor(crosshairColor)
        } catch (e: Exception) {
            Color.parseColor("#00E5FF")
        }
        updateCrosshairVisibility(isCrosshairActive)

        isDndActive = intent?.getBooleanExtra(EXTRA_DND_ENABLED, false) ?: false
        updateDndButtonState()

        isBrightnessLockActive = intent?.getBooleanExtra(EXTRA_LOCK_BRIGHTNESS, false) ?: false
        updateBrightnessButtonState()

        showMiniGraph = intent?.getBooleanExtra(EXTRA_SHOW_MINI_GRAPH, true) ?: true
        frameTimeGraphView?.visibility = if (showMiniGraph) View.VISIBLE else View.GONE

        isThermalAlertEnabled = intent?.getBooleanExtra(EXTRA_THERMAL_ALERT, true) ?: true

        fpsCalculator.stop()
        fpsCalculator.start(targetPackage)

        return START_STICKY
    }

    private fun startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_description)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, FpsOverlayService::class.java).apply {
            action = ACTION_STOP_OVERLAY
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_title))
            .setContentText(getString(R.string.service_desc))
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Hentikan", stopPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun setupOverlayView() {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        val overlaySettings = prefs.loadOverlaySettings()

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val flagTouch = if (overlaySettings.isLocked) {
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        } else {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            flagTouch,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = overlaySettings.posY.coerceAtLeast(80)
        }
        windowParams = params

        try {
            val inflater = LayoutInflater.from(this)
            val view = inflater.inflate(R.layout.overlay_fps, null)
            overlayView = view

            sidebarHandle = view.findViewById(R.id.sidebarHandle)
            telemetryPanel = view.findViewById(R.id.telemetryPanel)
            btnCollapseOverlay = view.findViewById(R.id.btnCollapseOverlay)

            frameTimeGraphView = view.findViewById(R.id.frameTimeGraphView)
            btnActionCleanRam = view.findViewById(R.id.btnActionCleanRam)
            btnActionDnd = view.findViewById(R.id.btnActionDnd)
            btnActionCrosshair = view.findViewById(R.id.btnActionCrosshair)
            btnActionBrightness = view.findViewById(R.id.btnActionBrightness)

            tvFpsValue = view.findViewById(R.id.tvFpsValue)
            tvFrameTimeValue = view.findViewById(R.id.tvFrameTimeValue)
            tvTempValue = view.findViewById(R.id.tvTempValue)
            tvBatteryValue = view.findViewById(R.id.tvBatteryValue)
            tvCpuValue = view.findViewById(R.id.tvCpuValue)
            tvGpuValue = view.findViewById(R.id.tvGpuValue)
            tvRefreshRateValue = view.findViewById(R.id.tvRefreshRateValue)
            containerExtraStandard = view.findViewById(R.id.overlayExtraStandard)
            containerExtraFull = view.findViewById(R.id.overlayExtraFull)

            applyOverlayAppearance(overlaySettings)

            btnCollapseOverlay?.setOnClickListener {
                setExpanded(false)
            }

            setupToolboxListeners()

            if (!overlaySettings.isLocked) {
                setupDragListener(view, params)
            }

            windowManager.addView(view, params)
            isOverlayAttached = true
        } catch (e: Exception) {
            isOverlayAttached = false
        }
    }

    private fun setupToolboxListeners() {
        btnActionCleanRam?.setOnClickListener {
            btnActionCleanRam?.text = "⏳ ..."
            btnActionCleanRam?.setTextColor(Color.parseColor("#FFC857"))
            serviceScope.launch {
                withContext(Dispatchers.IO) {
                    optimizer.cleanBackgroundProcesses(targetPackage)
                }
                btnActionCleanRam?.text = "✓ OK"
                btnActionCleanRam?.setTextColor(Color.parseColor("#00E5FF"))
                delay(1500L)
                btnActionCleanRam?.text = "🧹 RAM"
                btnActionCleanRam?.setTextColor(Color.parseColor("#CBD5E1"))
            }
        }

        btnActionDnd?.setOnClickListener {
            isDndActive = !isDndActive
            optimizer.setDndMode(isDndActive)
            updateDndButtonState()
        }

        btnActionCrosshair?.setOnClickListener {
            isCrosshairActive = !isCrosshairActive
            updateCrosshairVisibility(isCrosshairActive)
        }

        btnActionBrightness?.setOnClickListener {
            isBrightnessLockActive = !isBrightnessLockActive
            optimizer.setBrightnessLock(isBrightnessLockActive)
            updateBrightnessButtonState()
        }
    }

    private fun updateDndButtonState() {
        if (isDndActive) {
            btnActionDnd?.setBackgroundResource(R.drawable.bg_overlay_action_btn_active)
            btnActionDnd?.setTextColor(Color.parseColor("#00E5FF"))
        } else {
            btnActionDnd?.setBackgroundResource(R.drawable.bg_overlay_action_btn)
            btnActionDnd?.setTextColor(Color.parseColor("#CBD5E1"))
        }
    }

    private fun updateBrightnessButtonState() {
        if (isBrightnessLockActive) {
            btnActionBrightness?.setBackgroundResource(R.drawable.bg_overlay_action_btn_active)
            btnActionBrightness?.setTextColor(Color.parseColor("#00E5FF"))
        } else {
            btnActionBrightness?.setBackgroundResource(R.drawable.bg_overlay_action_btn)
            btnActionBrightness?.setTextColor(Color.parseColor("#CBD5E1"))
        }
    }

    private fun setupCrosshairView() {
        if (!Settings.canDrawOverlays(this)) return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val sizePx = (48 * resources.displayMetrics.density).toInt()
        val params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }
        crosshairParams = params

        crosshairView = CrosshairView(this)
    }

    private fun updateCrosshairVisibility(visible: Boolean) {
        isCrosshairActive = visible
        if (visible) {
            btnActionCrosshair?.setBackgroundResource(R.drawable.bg_overlay_action_btn_active)
            btnActionCrosshair?.setTextColor(Color.parseColor("#00E5FF"))
            if (!isCrosshairAttached && crosshairView != null && crosshairParams != null) {
                try {
                    windowManager.addView(crosshairView, crosshairParams)
                    isCrosshairAttached = true
                } catch (e: Exception) {
                }
            }
        } else {
            btnActionCrosshair?.setBackgroundResource(R.drawable.bg_overlay_action_btn)
            btnActionCrosshair?.setTextColor(Color.parseColor("#CBD5E1"))
            if (isCrosshairAttached && crosshairView != null) {
                try {
                    windowManager.removeView(crosshairView)
                } catch (e: Exception) {
                }
                isCrosshairAttached = false
            }
        }
    }

    private fun setExpanded(expanded: Boolean) {
        isExpanded = expanded
        telemetryPanel?.visibility = if (expanded) View.VISIBLE else View.GONE
        val params = windowParams
        if (isOverlayAttached && overlayView != null && params != null) {
            try {
                params.x = 0
                params.width = WindowManager.LayoutParams.WRAP_CONTENT
                params.height = WindowManager.LayoutParams.WRAP_CONTENT
                windowManager.updateViewLayout(overlayView, params)
            } catch (e: Exception) {
            }
        }
    }

    private fun setupDragListener(view: View, params: WindowManager.LayoutParams) {
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        val handleTouchListener = View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = abs(event.rawX - initialTouchX)
                    val deltaY = event.rawY - initialTouchY
                    if (abs(deltaY) > 8 || deltaX > 8) {
                        isDragging = true
                        params.x = 0
                        params.y = (initialY + deltaY).toInt().coerceAtLeast(0)
                        if (isOverlayAttached && overlayView != null) {
                            try {
                                windowManager.updateViewLayout(overlayView, params)
                            } catch (e: Exception) {
                            }
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        setExpanded(!isExpanded)
                    } else {
                        val currentSettings = prefs.loadOverlaySettings()
                        prefs.updateOverlaySettings(currentSettings.copy(posX = 0, posY = params.y))
                    }
                    true
                }
                else -> false
            }
        }

        sidebarHandle?.setOnTouchListener(handleTouchListener)

        val headerTouchListener = View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = params.y
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    params.x = 0
                    params.y = (initialY + deltaY).coerceAtLeast(0)
                    if (isOverlayAttached && overlayView != null) {
                        try {
                            windowManager.updateViewLayout(overlayView, params)
                        } catch (e: Exception) {
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val currentSettings = prefs.loadOverlaySettings()
                    prefs.updateOverlaySettings(currentSettings.copy(posX = 0, posY = params.y))
                    true
                }
                else -> false
            }
        }

        view.findViewById<View>(R.id.overlayHeader)?.setOnTouchListener(headerTouchListener)
    }

    private fun applyOverlayAppearance(settings: com.turbouro.app.model.OverlaySettings) {
        try {
            val root = overlayView ?: return
            val alphaVal = settings.opacityPercent / 100f
            root.alpha = alphaVal.coerceIn(0.2f, 1f)

            tvFpsValue?.textSize = (settings.textSizeSp + 5).toFloat()
            tvFrameTimeValue?.textSize = (settings.textSizeSp - 2).coerceAtLeast(10).toFloat()

            when (settings.mode) {
                OverlayMode.COMPACT -> {
                    containerExtraStandard?.visibility = View.GONE
                    containerExtraFull?.visibility = View.GONE
                }
                OverlayMode.STANDARD -> {
                    containerExtraStandard?.visibility = View.VISIBLE
                    containerExtraFull?.visibility = View.GONE
                }
                OverlayMode.FULL -> {
                    containerExtraStandard?.visibility = View.VISIBLE
                    containerExtraFull?.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun updateFpsViews(metrics: FpsMetrics) {
        try {
            val fps = metrics.currentFps
            if (fps != null) {
                tvFpsValue?.text = "$fps"
                val fpsColor = when {
                    fps >= 55 -> Color.parseColor("#00E5FF")
                    fps in 30..54 -> Color.parseColor("#FFC857")
                    else -> Color.parseColor("#FF5C67")
                }
                tvFpsValue?.setTextColor(fpsColor)
            } else {
                tvFpsValue?.text = "--"
                tvFpsValue?.setTextColor(Color.parseColor("#64748B"))
            }

            val frameTime = metrics.frameTimeMs
            if (frameTime != null) {
                tvFrameTimeValue?.text = String.format("%.1f ms", frameTime)
                if (showMiniGraph) {
                    frameTimeGraphView?.addSample(frameTime.toFloat())
                }
            } else {
                tvFrameTimeValue?.text = "-- ms"
            }
        } catch (e: Exception) {
        }
    }

    private fun startTelemetryLoop() {
        val settings = prefs.loadOverlaySettings()
        val interval = settings.updateIntervalMillis.coerceAtLeast(500L)

        val runnable = object : Runnable {
            override fun run() {
                updatePeriodicTelemetry()
                handler.postDelayed(this, interval)
            }
        }
        updateRunnable = runnable
        handler.post(runnable)
    }

    private fun updatePeriodicTelemetry() {
        if (!isOverlayAttached) return

        try {
            val (tempC, batteryPct, _) = thermalTracker.getBatteryTelemetry()
            tvTempValue?.text = tempC?.let { String.format("%.1f°C", it) } ?: "N/A"
            tvBatteryValue?.text = "$batteryPct%"

            if (tempC != null && tempC >= 43.0 && isThermalAlertEnabled) {
                sidebarHandle?.setBackgroundResource(R.drawable.bg_sidebar_warning)
                triggerThermalHaptic()
            } else {
                sidebarHandle?.setBackgroundResource(R.drawable.bg_sidebar_handle)
            }

            if (targetPackage.isNullOrBlank()) {
                val fgPkg = packageMonitor.getForegroundPackageName()
                if (!fgPkg.isNullOrBlank() && fgPkg != packageName) {
                    targetPackage = fgPkg
                    fpsCalculator.stop()
                    fpsCalculator.start(fgPkg)
                }
            }

            val currentSettings = prefs.loadOverlaySettings()
            if (currentSettings.mode == OverlayMode.FULL) {
                val cpuFreq = try {
                    cpuTelemetry.getScalingCurrentFreqKHz()
                } catch (e: Exception) {
                    null
                }
                tvCpuValue?.text = cpuFreq?.let {
                    if (it > 1_000_000) String.format("CPU: %.2f GHz", it / 1_000_000.0)
                    else String.format("CPU: %d MHz", it / 1_000)
                } ?: "CPU: N/A"

                val gpuFreq = try {
                    gpuTelemetry.getGpuCurrentFreqKHz()
                } catch (e: Exception) {
                    null
                }
                tvGpuValue?.text = gpuFreq?.let {
                    if (it > 1_000_000) String.format("GPU: %.2f GHz", it / 1_000_000.0)
                    else String.format("GPU: %d MHz", it / 1_000)
                } ?: "GPU: N/A"

                val targetDisplay = try {
                    overlayView?.display ?: run {
                        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as? android.hardware.display.DisplayManager
                        displayManager?.getDisplay(android.view.Display.DEFAULT_DISPLAY)
                    } ?: run {
                        @Suppress("DEPRECATION")
                        windowManager.defaultDisplay
                    }
                } catch (e: Throwable) {
                    null
                }
                val hz = targetDisplay?.refreshRate ?: 60f
                tvRefreshRateValue?.text = String.format("Hz: %.0f", hz)
            }
        } catch (e: Exception) {
        }
    }

    private fun triggerThermalHaptic() {
        val now = System.currentTimeMillis()
        if (now - lastVibrateTime < 15_000L) return
        lastVibrateTime = now

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(180L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(180L)
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        updateRunnable?.let { handler.removeCallbacks(it) }
        fpsCalculator.stop()

        if (isCrosshairAttached && crosshairView != null) {
            try {
                windowManager.removeView(crosshairView)
            } catch (e: Exception) {
            }
            isCrosshairAttached = false
        }

        if (isOverlayAttached && overlayView != null) {
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
            }
            overlayView = null
            isOverlayAttached = false
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }
}
