package tw.lospot.kin.whisper

import android.service.quicksettings.Tile.STATE_ACTIVE
import android.service.quicksettings.Tile.STATE_INACTIVE
import android.service.quicksettings.TileService
import kotlin.properties.Delegates

class WhisperTileService : TileService() {
    private val connection by lazy { WhisperConnection(this, ::updateStatus) }
    private var isConnected by Delegates.observable(false) { _, _, _ ->
        updateTile()
    }
    private var isRunning by Delegates.observable(false) { _, _, _ ->
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (isRunning) connection.stop() else connection.start()
    }

    override fun onStartListening() {
        super.onStartListening()
        connection.bind()
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        connection.unbind()
    }

    private fun updateTile() {
        qsTile.apply {
            state = if (isRunning) STATE_ACTIVE else STATE_INACTIVE
        }.updateTile()
    }

    private fun updateStatus() {
        isConnected = connection.isConnected
        connection.status.let { status ->
            isRunning = status.isRunning
        }
        updateTile()
    }
}