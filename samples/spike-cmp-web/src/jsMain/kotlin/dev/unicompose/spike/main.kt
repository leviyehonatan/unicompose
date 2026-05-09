package dev.unicompose.spike

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
public fun main() {
    ComposeViewport(viewportContainerId = "ComposeTarget") {
        SpikeContent()
    }
}

@Composable
private fun SpikeContent() {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Hello from CMP-for-Web")
        Text("If you can see this, the canvas target works.")
    }
}
