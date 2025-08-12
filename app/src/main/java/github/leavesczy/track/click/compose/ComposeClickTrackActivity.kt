package github.leavesczy.track.click.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * @Author: leavesCZY
 * @Date: 2025/5/16 11:43
 * @Desc:
 */
class ComposeClickTrackActivity : AppCompatActivity() {

    private val ontClickWhiteList = "notCheck"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = "ComposeClickTrack"
        setContent {
            TrackTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentWindowInsets = WindowInsets.navigationBars
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues = innerPadding)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(
                                space = 20.dp,
                                alignment = Alignment.CenterVertically
                            )
                        ) {
                            var index by remember {
                                mutableIntStateOf(0)
                            }
                            Text(
                                modifier = Modifier,
                                text = index.toString(),
                                fontSize = 25.sp
                            )
                            Text(
                                modifier = Modifier
                                    .clickable(onClickLabel = ontClickWhiteList) {
                                        index++
                                    },
                                text = "Text clickable 不防抖"
                            )
                            Text(
                                modifier = Modifier
                                    .combinedClickable(
                                        onClickLabel = ontClickWhiteList,
                                        onClick = {
                                            index++
                                        }
                                    ),
                                text = "Text combinedClickable 不防抖"
                            )
                            Text(
                                modifier = Modifier
                                    .clickable {
                                        index++
                                    },
                                text = "Text clickable"
                            )
                            Text(
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = null,
                                        indication = null
                                    ) {
                                        index++
                                    },
                                text = "Text clickable"
                            )
                            Text(
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            index++
                                        }
                                    ),
                                text = "Text combinedClickable"
                            )
                            TextButton(
                                modifier = Modifier,
                                onClick = {
                                    index++
                                }
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = "TextButton"
                                )
                            }
                            Button(
                                modifier = Modifier,
                                onClick = {
                                    index++
                                }
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = "Button"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}