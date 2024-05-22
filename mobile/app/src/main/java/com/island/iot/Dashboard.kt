package com.island.iot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.island.iot.ui.theme.IOTTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.platform.LocalContext

class Dashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Root()
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
    fun Metric(title:String, value: String){
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Text(title, modifier = Modifier.padding(8.dp, 16.dp))
            Text(value, modifier = Modifier.padding(8.dp, 16.dp))
        }
    }

    @Composable
    fun Grid(){
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(16.dp, 0.dp)
            ){
                Metric("Total consumption", "LLL")
                HorizontalDivider(thickness = 2.dp, modifier = Modifier.fillMaxWidth(.3f))
                Metric("Daily consumption", "LLL")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(16.dp, 0.dp)
            ){
                Metric("Filter capacity", "LLL")
                HorizontalDivider(thickness = 2.dp, modifier = Modifier.fillMaxWidth(.6f))
                Metric("Filter life", "LLL")
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(0.dp, 8.dp)
        ) {
            Column{
                HorizontalDivider(thickness = 2.dp, modifier = Modifier.fillMaxWidth(.5f))
                Metric("Quantity of plastic save", "LLL")
            }
            }
        Row(){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ){
                Text("Filter status", modifier = Modifier.padding(4.dp))
                Text("%%%")
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ){
            ExtendedFloatingActionButton(
                onClick = { /* TODO */ },
                icon = { Icon(Icons.Filled.ShoppingCart, "Buy filter") },
                text = { Text(text = "Buy filter") },
            )
        }
    }

    @Composable
    fun Layout(onRegister: (String, String) -> Unit) {
        val mContext = LocalContext.current
        var selectedItem by remember { mutableIntStateOf(0) }
        val items = listOf("Dashboard", "Charts", "Jugs", "Account")
        val icons = listOf(Icons.Filled.Home, Icons.Filled.Menu, Icons.Filled.Create, Icons.Filled.Person)
        val classes = listOf(Dashboard::class.java, Charts::class.java, Jugs::class.java, Account::class.java)
        IOTTheme {
            Scaffold(
                topBar = {
                    Text("Dashboard", modifier = Modifier.padding(16.dp))
                },
                bottomBar = {
                    NavigationBar {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                icon = { Icon(icons[index], contentDescription = item) },
                                label = { Text(item) },
                                selected = selectedItem == index,
                                onClick = { selectedItem = index; mContext.startActivity(
                                    android.content.Intent(mContext, classes[index])
                                ) }
                            )
                        }
                    }
                }
            ){
                System.out.println(it)
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(0.dp, 32.dp)
                    ) {
                        Grid()
                    }
                }
            }
        }
    }
}