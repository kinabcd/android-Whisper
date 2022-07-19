package tw.lospot.kin.whisper

import android.app.*
import android.content.Intent
import android.os.*
import android.util.Log
import tw.lospot.kin.whisper.controller.AudioController
import tw.lospot.kin.whisper.controller.DisplayController
import tw.lospot.kin.whisper.controller.NotificationController
import java.lang.Exception
import kotlin.properties.Delegates

class WhisperService : Service(), NotificationController.Listener {
    companion object {
        private const val TAG = "WhisperService"
        const val MSG_START = 0
        const val MSG_STOP = 1
        const val MSG_STATUS = 2
    }

    private val handler = Handler(Looper.getMainLooper(), CommandProcessor())
    private val messenger = Messenger(handler)

    private val controllers by lazy {
        arrayOf(
            DisplayController(applicationContext),
            NotificationController(this, this),
            AudioController(applicationContext)
        )
    }

    private var isStarted by Delegates.observable(false) { _, old, new ->
        if (old != new) {
            Log.v(TAG, "isStarted $old -> $new")
            if (new) {
                applicationContext.startForegroundService(selfIntent)
                controllers.forEach { it.start() }
            } else {
                controllers.reversedArray().forEach { it.stop() }
                applicationContext.stopService(selfIntent)
            }
            val failedMessenger = LinkedHashSet<Messenger>()
            val newStatus = WhisperStatus(new)
            listeners.forEach {
                try {
                    it.send(Message.obtain().apply {
                        what = MSG_STATUS
                        obj = newStatus
                    })
                    Log.v(TAG, "notify $it")
                } catch (e: Exception) {
                    Log.w(TAG, "send to $it failed, $e")
                    failedMessenger += it
                }
            }
            failedMessenger.forEach {
                listeners.remove(it)
                Log.w(TAG, "clear failed messenger $it")
            }
        }
    }
    private val listeners = HashSet<Messenger>()

    private val selfIntent by lazy { Intent(applicationContext, WhisperService::class.java) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return messenger.binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        isStarted = false
    }

    override fun onStopClicked() {
        handler.sendEmptyMessage(MSG_STOP)
    }

    private fun handleStatus(listen: Boolean, replyTo: Messenger) {
        if (listen) {
            try {
                replyTo.send(Message.obtain().apply {
                    what = MSG_STATUS
                    obj = WhisperStatus(isStarted)
                })
                listeners.add(replyTo)
                Log.v(TAG, "listener $replyTo is added")
            } catch (e: Exception) {
                Log.w(TAG, "send to $replyTo failed, $e")
            }
        } else {
            val remove = listeners.remove(replyTo)
            if (remove) {
                Log.v(TAG, "listener $replyTo is removed")
            }
        }
    }

    private inner class CommandProcessor : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            Log.v(TAG, "handleMessage(${msg.what})")
            when (msg.what) {
                MSG_START -> isStarted = true
                MSG_STOP -> isStarted = false
                MSG_STATUS -> handleStatus(msg.arg1 == 1, msg.replyTo)
            }

            return true
        }
    }
}
