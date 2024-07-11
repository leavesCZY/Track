package github.leavesczy.track.click.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
@OptIn(ExperimentalFoundationApi::class)
class ComposeClickTrackActivity : AppCompatActivity() {

    private val ontClickWhiteList = "notCheck"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = "ComposeClickTrack"
        setContent {
            TransformTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.navigationBars
                ) { innerPadding ->
                    var index by remember {
                        mutableIntStateOf(0)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues = innerPadding)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(all = 10.dp),
                                text = index.toString(),
                                fontSize = 22.sp
                            )
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(height = 60.dp)
                            )
                            Text(
                                modifier = Modifier
                                    .clickable(onClickLabel = ontClickWhiteList) {
                                        index++
                                    }
                                    .padding(all = 15.dp),
                                text = "Text clickable 不防抖"
                            )
                            Text(
                                modifier = Modifier
                                    .combinedClickable(
                                        onClickLabel = ontClickWhiteList,
                                        onClick = {
                                            index++
                                        }
                                    )
                                    .padding(all = 15.dp),
                                text = "Text combinedClickable 不防抖"
                            )
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(height = 60.dp)
                            )
                            Text(
                                modifier = Modifier
                                    .clickable {
                                        index++
                                    }
                                    .padding(all = 15.dp),
                                text = "Text clickable"
                            )
                            Text(
                                modifier = Modifier
                                    .combinedClickable(
                                        onClick = {
                                            index++
                                        }
                                    )
                                    .padding(all = 15.dp),
                                text = "Text combinedClickable"
                            )
                            TextButton(
                                onClick = {
                                    index++
                                }
                            ) {
                                Text(
                                    modifier = Modifier
                                        .padding(all = 15.dp),
                                    text = "TextButton"
                                )
                            }
                            Button(
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