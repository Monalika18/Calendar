package com.example.calendar

import android.accounts.AccountManager
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendar.ui.dashboard.DashboardScreen
import com.example.calendar.ui.theme.CalendarTheme
import com.example.calendar.ui.viewmodel.CalendarViewModel
import com.google.android.gms.common.AccountPicker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CalendarViewModel = viewModel()
            
            // Launcher for picking the Google Account
            val pickAccountLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    if (accountName != null) {
                        viewModel.setUserAccount(android.accounts.Account(accountName, "com.google"))
                    }
                }
            }

            // Launcher for Google Consent (the "Allow" button screen)
            val authLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    viewModel.fetchGoogleEvents()
                }
            }

            // Trigger Account Picker on first launch
            LaunchedEffect(Unit) {
                val intent = AccountPicker.newChooseAccountIntent(
                    AccountPicker.AccountChooserOptions.Builder()
                        .setAllowableAccountsTypes(listOf("com.google"))
                        .build()
                )
                pickAccountLauncher.launch(intent)
            }

            // Listen for when Google requires permission (Consent)
            LaunchedEffect(viewModel.authIntent) {
                viewModel.authIntent.collect { intent ->
                    authLauncher.launch(intent)
                }
            }

            CalendarTheme {
                DashboardScreen(viewModel)
            }
        }
    }
}
