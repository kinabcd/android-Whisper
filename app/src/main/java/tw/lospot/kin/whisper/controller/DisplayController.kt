package tw.lospot.kin.whisper.controller

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager

class DisplayController(context: Context): Controller {
    companion object {
        private const val TAG = "DisplayController"
    }

    private val powerManager by lazy { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    private val wakeLock by lazy {
        powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "whisper:$TAG"
        )
    }

    @SuppressLint("WakelockTimeout")
    override fun start() {
        wakeLock.acquire()
    }

    override fun stop() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}