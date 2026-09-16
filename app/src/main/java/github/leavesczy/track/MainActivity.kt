package github.leavesczy.track

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import github.leavesczy.track.click.compose.ComposeClickTrackActivity
import github.leavesczy.track.click.compose.TrackTheme
import github.leavesczy.track.click.compose.TrackTopAppBar
import github.leavesczy.track.click.view.ViewClickTrackActivity
import github.leavesczy.track.replace.inheritance.ReplaceClassTrackActivity
import github.leavesczy.track.replace.rule.ReplaceRuleTrackActivity
import github.leavesczy.track.thread.OptimizedThreadTrackActivity
import github.leavesczy.track.toast.ToastTrackActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackTheme {
                MainScreen(
                    onViewClickTrack = { startActivity<ViewClickTrackActivity>() },
                    onComposeClickTrack = { startActivity<ComposeClickTrackActivity>() },
                    onToastTrack = { startActivity<ToastTrackActivity>() },
                    onOptimizedThreadTrack = { startActivity<OptimizedThreadTrackActivity>() },
                    onReplaceClassTrack = { startActivity<ReplaceClassTrackActivity>() },
                    onReplaceRuleTrack = { startActivity<ReplaceRuleTrackActivity>() }
                )
            }
        }
    }

    private inline fun <reified T : Activity> startActivity() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }

}

@Composable
private fun MainScreen(
    onViewClickTrack: () -> Unit,
    onComposeClickTrack: () -> Unit,
    onToastTrack: () -> Unit,
    onOptimizedThreadTrack: () -> Unit,
    onReplaceClassTrack: () -> Unit,
    onReplaceRuleTrack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TrackTopAppBar(title = stringResource(id = R.string.app_name))
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
                modifier = Modifier.fillMaxWidth(),
                onClick = onViewClickTrack
            ) {
                Text(text = "ViewClickTrack")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onComposeClickTrack
            ) {
                Text(text = "ComposeClickTrack")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onToastTrack
            ) {
                Text(text = "ToastTrack")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOptimizedThreadTrack
            ) {
                Text(text = "OptimizedThreadTrack")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onReplaceClassTrack
            ) {
                Text(text = "ReplaceClassTrack")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onReplaceRuleTrack
            ) {
                Text(text = "ReplaceRuleTrack")
            }
        }
    }
}
