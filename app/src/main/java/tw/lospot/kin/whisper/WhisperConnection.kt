package tw.lospot.kin.whisper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log

class WhisperConnection(outContext: Context, private val onStateChanged: () -> Unit) :
    ServiceConnection,
    Handler.Callback {
    companion object {
        private const val TAG = "WhisperConnection"
    }
    private val context = outContext.applicationContext
    private val intent = Intent(context, WhisperService::class.java)
    private var messenger: Messenger? = null
        private set(value) {
            field = value
            onStateChanged()
        }
    private val replyMessenger = Messenger(Handler(Looper.getMainLooper(), this))
    var status: WhisperStatus = WhisperStatus()
        private set(value) {
            if (field != value) {
                field = value
                onStateChanged()
            }
        }
    var isBound = false
        private set(value) {
            if (field != value) {
                field = value
                onStateChanged()
            }
        }
    val isConnected get() = messenger != null

    fun bind() {
        if (isBound) return
        Log.v(TAG, "bind")
        isBound = context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    fun unbind() {
        if (!isBound) return
        Log.v(TAG, "unbind")
        messenger?.send(Message.obtain().apply {
            what = WhisperService.MSG_STATUS
            arg1 = 0
            replyTo = replyMessenger
        })
        context.unbindService(this)
        messenger = null
        isBound = false
    }

    fun start() {
        Log.v(TAG, "start")
        messenger?.send(Message.obtain().apply {
            what = WhisperService.MSG_START
        })
    }

    fun stop() {
        Log.v(TAG, "stop")
        messenger?.send(Message.obtain().apply {
            what = WhisperService.MSG_STOP
        })
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        Log.v(TAG, "onServiceConnected")
        messenger = Messenger(binder).also {
            it.send(Message.obtain().apply {
                what = WhisperService.MSG_STATUS
                arg1 = 1
                replyTo = replyMessenger
            })
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        Log.v(TAG, "onServiceDisconnected")
        messenger = null
    }

    override fun onBindingDied(name: ComponentName) {
        Log.v(TAG, "onBindingDied")
        unbind()
    }

    override fun onNullBinding(name: ComponentName) {
        Log.v(TAG, "onNullBinding")
        unbind()
    }

    override fun handleMessage(msg: Message): Boolean {
        Log.v(TAG, "handleMessage(${msg.what})")
        when (msg.what) {
            WhisperService.MSG_STATUS -> status = msg.obj as WhisperStatus
        }
        return true
    }
}