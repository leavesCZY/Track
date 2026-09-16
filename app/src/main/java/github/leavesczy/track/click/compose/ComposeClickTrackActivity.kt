package github.leavesczy.track.click.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.leavesczy.track.BaseActivity
import github.leavesczy.track.R

class ComposeClickTrackActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackTheme {
                ComposeClickTrackScreen()
            }
        }
    }

}

@Composable
private fun ComposeClickTrackScreen() {
    val skipOnClickLabel = "skip"
    var index by remember {
        mutableIntStateOf(value = 0)
    }
    val primary = colorResource(id = R.color.color_primary)
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TrackTopAppBar(title = "ComposeClickTrack")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = index.toString(),
                fontSize = 36.sp,
                color = primary
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClickLabel = skipOnClickLabel) {
                        index++
                    }
                    .padding(vertical = 8.dp),
                text = "clickable（不防抖）"
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClickLabel = skipOnClickLabel,
                        onClick = {
                            index++
                        }
                    )
                    .padding(vertical = 8.dp),
                text = "combinedClickable（不防抖）"
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        index++
                    }
                    .padding(vertical = 8.dp),
                text = "clickable"
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = null,
                        indication = null
                    ) {
                        index++
                    }
                    .padding(vertical = 8.dp),
                text = "clickable（无 indication）"
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = {
                        index++
                    })
                    .padding(vertical = 8.dp),
                text = "combinedClickable"
            )
            TextButton(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    index++
                }
            ) {
                Text(text = "TextButton")
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    index++
                }
            ) {
                Text(text = "Button")
            }
        }
    }
}
