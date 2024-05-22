package com.island.iot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.island.iot.ui.theme.IOTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Root()
        }

    }
}

val maxWidth = Modifier
    .fillMaxWidth()
    .padding(16.dp)

@Composable
fun TextFieldState(
    label: String,
    password: Boolean = false,
    text: String,
    onChange: (String) -> Unit
) {

    TextField(
        value = text,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = maxWidth,
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (password) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default
    )
}

@Composable
fun Register(onRegister: (String, String) -> Unit) {
    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var confPassword by remember {
        mutableStateOf("")
    }
    OutlinedCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.Black),
        modifier = maxWidth
    ) {
        Text(text = "Register", modifier = Modifier.padding(16.dp))
        TextFieldState(label = "Email", text = email, onChange = { email = it })
        TextFieldState(
            label = "Password",
            password = true,
            text = password,
            onChange = { password = it })
        TextFieldState(
            label = "Confirm password",
            password = true,
            text = confPassword,
            onChange = { confPassword = it })
        Button(
            onClick = { onRegister(email,password) },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Register")
        }
    }
}

@Composable
fun Login() {
    OutlinedCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Register", modifier = Modifier.padding(16.dp))
        TextField(
            label = { Text("Email") },
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        TextField(
            label = { Text("Password") },
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        Button(onClick = { /*TODO*/ }, modifier = Modifier.padding(16.dp)) {
            Text(text = "Login")
        }
    }
}

@Composable
fun Root(viewModel: StateViewModel = viewModel()) {
    Layout { username, password -> viewModel.register(username, password) }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    Layout { _, _ -> }
}

@Composable
fun Layout(onRegister: (String, String) -> Unit) {
    IOTTheme {
        Scaffold {
            System.out.println(it)
            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Android2", modifier = Modifier.padding(16.dp))
                    Register(onRegister)
                    Login()
                }
            }
        }
    }
}