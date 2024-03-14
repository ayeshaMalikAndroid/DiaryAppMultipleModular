package com.example.diaryapp.presentation.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.diaryapp.R

@Composable
fun HomeScreen(
    drawerState: DrawerState,
    onMenuClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    navigateToWrite: () -> Unit
) {
    NavigationDrawer(
        drawerState = drawerState,
        onSignOutClicked = onSignOutClicked
    ) {
        Scaffold(topBar = {
            HomeTopBar(onMenuClicked = onMenuClicked)
        },
            floatingActionButton = {
                FloatingActionButton(onClick = { navigateToWrite }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "New Diary Icon.")
                }
            },
            content = {

            }
        )
    }
}

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    onSignOutClicked: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(content = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier.size(250.dp),
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo image"
                    )
                }
                NavigationDrawerItem(label = {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.google_logo),
                            contentDescription = "Google Logo",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Sign Out", color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                    selected = false,
                    onClick = { onSignOutClicked })
            })
        },
        content = content
    )
}