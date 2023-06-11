// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

// @OptIn(ExperimentalMaterial3Api::class)
// @Composable
// fun TimePickerDialog(
//    onDismissRequest: () -> Unit,
//    confirmButton: @Composable () -> Unit,
//    modifier: Modifier = Modifier,
//    dismissButton: @Composable (() -> Unit)? = null,
//    shape: Shape = MaterialTheme.shapes.extraLarge,
//    tonalElevation: Dp = DatePickerDefaults.TonalElevation,
//    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
//    content: @Composable () -> Unit,
// ) {
//    AlertDialog(
//        onDismissRequest = onDismissRequest,
//        properties = properties,
//        modifier = modifier,
//    ) {
//        Surface(
//            shape = shape,
//            tonalElevation = tonalElevation,
//        ) {
//            Column {
//                content()
//
//                Row(
//                    modifier = Modifier
//                        .align(Alignment.End)
//                        .padding(bottom = 8.dp, end = 6.dp),
//                ) {
//                    dismissButton?.invoke()
//                    confirmButton()
//                }
//            }
//        }
//    }
// }
