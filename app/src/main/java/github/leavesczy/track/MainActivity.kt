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
import github.leavesczy.track.click.view.ViewClickTrackActivity
import github.leavesczy.track.member.MemberTrackActivity
import github.leavesczy.track.superclass.SuperclassTrackActivity
import github.leavesczy.track.ui.TrackTheme
import github.leavesczy.track.ui.TrackTopAppBar

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackTheme {
                MainScreen(
                    onViewClickTrack = { startActivity<ViewClickTrackActivity>() },
                    onComposeClickTrack = { startActivity<ComposeClickTrackActivity>() },
                    onSuperclassTrack = { startActivity<SuperclassTrackActivity>() },
                    onMemberTrack = { startActivity<MemberTrackActivity>() }
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
    onSuperclassTrack: () -> Unit,
    onMemberTrack: () -> Unit
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
                onClick = onSuperclassTrack
            ) {
                Text(text = "SuperclassTrack")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onMemberTrack
            ) {
                Text(text = "MemberTrack")
            }
        }
    }
}
