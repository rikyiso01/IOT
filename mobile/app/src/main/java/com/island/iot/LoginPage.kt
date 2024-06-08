package com.island.iot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
@Preview
fun LoginPagePreview() {
    Decorations(
        bottomBarVisible = false
    ) {
        LoginPage()
    }
}

@Composable
fun LoginPage(
    register: () -> Unit = {},
    homePage: () -> Unit = {},
    login: (String, String) -> Unit = { _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(-100f) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .matchParentSize()
                .offset {
                    IntOffset(
                        offsetX.value.toInt(),
                        offsetY.value.toInt()
                    )
                }
        )

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                launch {
                    offsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 1000,
                            easing = FastOutSlowInEasing))}
            }
        }

        ScrollableContent {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp),
            ) {
                Spacer(modifier = Modifier.size(30.dp))
                Text(
                    text = "SmartJugs",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Thin,
                    modifier = Modifier.padding(32.dp, 0.dp)
                )
                Text(
                    text = "Login",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(32.dp, 0.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                CredentialCard(
                    operation = login,
                    navigate = register,
                    isRegistration = false,
                    firstButtonMsg = "Login",
                    secondButtonMsg = "Not a user? Register"
                )
                Button(onClick = { homePage() }) { Text("HomePage") }
            }
        }
    }
}
