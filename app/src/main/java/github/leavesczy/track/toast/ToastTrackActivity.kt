package github.leavesczy.track.toast

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import github.leavesczy.track.BaseActivity
import github.leavesczy.track.click.compose.TrackTheme
import github.leavesczy.track.click.compose.TrackTopAppBar

class ToastTrackActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackTheme {
                ToastTrackScreen(
                    onShowToast = {
                        Toast.makeText(this, "ToastTrack", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

}

@Composable
private fun ToastTrackScreen(onShowToast: () -> Unit) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TrackTopAppBar(title = "ToastTrack")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = onShowToast
            ) {
                Text(text = "showToast")
            }
        }
    }
}
