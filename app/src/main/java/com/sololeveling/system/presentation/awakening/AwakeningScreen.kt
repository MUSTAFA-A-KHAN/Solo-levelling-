package com.sololeveling.system.presentation.awakening

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.semantics.Role
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sololeveling.system.R
import kotlinx.coroutines.delay

@Composable
fun AwakeningScreen(
    onAwakeningComplete: () -> Unit
) {
    var step by remember { mutableStateOf(0) }

    LaunchedEffect(step) {
        if (step == 0) {
            delay(1500)
            step = 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClickLabel = "Next awakening step", role = Role.Button) {
                if (step == 1) step = 2
                else if (step == 2) step = 3
                else if (step == 3) onAwakeningComplete()
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_awakening),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .alpha(0.3f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            AnimatedVisibility(
                visible = step >= 1,
                enter = fadeIn(animationSpec = tween(2000))
            ) {
                Text(
                    text = "YOU HAVE COMPLETED ALL THE NECESSARY REQUIREMENTS OF THE SECRET QUEST",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = step >= 2,
                enter = fadeIn(animationSpec = tween(1500))
            ) {
                Text(
                    text = "DO YOU WISH TO BECOME A PLAYER?",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedVisibility(
                visible = step >= 3,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                Text(
                    text = "[ ACCEPT ]",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
