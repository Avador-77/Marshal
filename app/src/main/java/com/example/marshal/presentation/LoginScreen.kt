package com.example.marshal.presentation


import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.marshal.R
import com.example.marshal.presentation.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel, onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.ic_marshal_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )


            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Welcome to Marshal", style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        authViewModel.signInWithGoogle(context) { success, errorMessage ->
                            isLoading = false
                            if (success) {
                                onLoginSuccess()
                            } else {
                                Toast.makeText(
                                    context, "Sign-in failed: $errorMessage", Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }, modifier = Modifier.padding(16.dp)
                ) {
                    Text("Sign in with Google")
                }
            }
        }
    }
}