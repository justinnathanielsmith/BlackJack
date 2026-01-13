package io.github.smithjustinn.components.game

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.cancel
import memory_match.sharedui.generated.resources.quit_confirm
import memory_match.sharedui.generated.resources.quit_game_message
import memory_match.sharedui.generated.resources.quit_game_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExitGameDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.quit_game_title)) },
        text = { Text(stringResource(Res.string.quit_game_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.quit_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}
