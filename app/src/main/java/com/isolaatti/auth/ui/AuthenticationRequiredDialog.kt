package com.isolaatti.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isolaatti.R
import com.isolaatti.common.IsolaattiTheme

@Composable
fun AuthenticationRequiredDialog(
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = stringResource(R.string.authentication_required),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.this_feature_requires_an_account),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.dont_have_an_account_create_one),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TextButton(
            onClick = onCreateAccount,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(text = stringResource(R.string.sign_up))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onContinue) {
                Text(text = stringResource(R.string.continuar))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthenticateDialogContentPreview() {
    IsolaattiTheme {
        AuthenticationRequiredDialog(
            onContinue = {},
            onDismiss = {},
            onCreateAccount = {}
        )
    }
}
