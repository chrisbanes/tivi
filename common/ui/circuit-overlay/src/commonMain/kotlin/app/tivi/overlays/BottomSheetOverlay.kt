// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0

package app.tivi.overlays

// @OptIn(ExperimentalMaterial3Api::class)
// class BottomSheetOverlay<Model : Any, Result : Any>(
//    private val model: Model,
//    private val onDismiss: () -> Result,
//    private val tonalElevation: Dp = BottomSheetDefaults.Elevation,
//    private val scrimColor: Color = Color.Unspecified,
//    private val content: @Composable (Model, OverlayNavigator<Result>) -> Unit,
// ) : Overlay<Result> {
//    @Composable
//    override fun Content(navigator: OverlayNavigator<Result>) {
//        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//
//        val coroutineScope = rememberCoroutineScope()
//        BackHandler(enabled = sheetState.isVisible) {
//            coroutineScope
//                .launch { sheetState.hide() }
//                .invokeOnCompletion {
//                    if (!sheetState.isVisible) {
//                        navigator.finish(onDismiss())
//                    }
//                }
//        }
//
//        ModalBottomSheet(
//            modifier = Modifier.fillMaxWidth(),
//            content = {
//                // Delay setting the result until we've finished dismissing
//                content(model) { result ->
//                    // This is the OverlayNavigator.finish() callback
//                    coroutineScope.launch {
//                        try {
//                            sheetState.hide()
//                        } finally {
//                            navigator.finish(result)
//                        }
//                    }
//                }
//            },
//            tonalElevation = tonalElevation,
//            scrimColor = if (scrimColor.isSpecified) scrimColor else BottomSheetDefaults.ScrimColor,
//            sheetState = sheetState,
//            onDismissRequest = { navigator.finish(onDismiss()) },
//        )
//
//        LaunchedEffect(Unit) { sheetState.show() }
//    }
// }
//
// suspend fun OverlayHost.showInBottomSheet(
//    screen: Screen,
// ): Unit = show(
//    BottomSheetOverlay(Unit, {}) { _, _ ->
//        // We want to use `onNavEvent` here to finish the overlay but we're blocked by
//        // https://github.com/slackhq/circuit/issues/653
//        CircuitContent(screen = screen)
//    },
// )
