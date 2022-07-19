package tw.lospot.kin.whisper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private val connection by lazy { WhisperConnection(this, ::updateStatus) }

    private var isConnected by mutableStateOf(false)
    private var isRunning by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row {
                    Button(
                        enabled = isConnected && !isRunning,
                        onClick = { connection.start() },
                    ) {
                        Text("Start")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        enabled = isConnected && isRunning,
                        onClick = { connection.stop() },
                    ) {
                        Text("Stop")
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        connection.bind()
    }

    override fun onStop() {
        super.onStop()
        connection.unbind()
    }

    private fun updateStatus() {
        isConnected = connection.isConnected
        connection.status.let { status ->
            isRunning = status.isRunning
        }
    }

}
