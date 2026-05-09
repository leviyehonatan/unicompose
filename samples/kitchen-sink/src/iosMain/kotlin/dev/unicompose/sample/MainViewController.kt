package dev.unicompose.sample

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("FunctionName")
public fun MainViewController(): UIViewController = ComposeUIViewController { App() }
