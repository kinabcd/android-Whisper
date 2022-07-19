package tw.lospot.kin.whisper.controller

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import kotlin.math.min
import kotlin.properties.Delegates

class AudioController(context: Context) : Controller {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximityListener = ProximityListener()
    private val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    override fun start() {
        sensorManager.registerListener(
            proximityListener,
            proximitySensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private var isFar by Delegates.observable(true) { _, old, new ->
        if (old != new) {
            if (new) {
                audioManager.mode = AudioManager.MODE_NORMAL
            } else {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                val hasExternalDevice = devices.any {
                    it.type !in arrayOf(
                        AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
                        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                        AudioDeviceInfo.TYPE_TELEPHONY
                    )
                }
                if (hasExternalDevice) {
                    audioManager.mode = AudioManager.MODE_NORMAL
                } else {
                    audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                }
            }
        }

    }

    override fun stop() {
        sensorManager.unregisterListener(proximityListener)
        audioManager.mode = AudioManager.MODE_NORMAL
    }

    private fun onProximityChanged(value: Float) {
        isFar = value >= min(proximitySensor.maximumRange, 5f)
    }

    private inner class ProximityListener : SensorEventCallback() {
        override fun onSensorChanged(event: SensorEvent) {
            onProximityChanged(event.values[0])
        }
    }
}