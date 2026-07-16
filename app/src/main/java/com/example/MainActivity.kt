package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GasGuardViewModel
import com.example.ui.SignInScreen
import com.example.ui.HomeownerDashboard
import com.example.ui.SupplierDashboard
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: GasGuardViewModel = viewModel()
        val screen by viewModel.currentScreen.collectAsState()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          when (screen) {
            "SIGN_IN" -> SignInScreen(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
            )
            "HOMEOWNER" -> HomeownerDashboard(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
            )
            "SUPPLIER" -> SupplierDashboard(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
            )
          }
        }
      }
    }
  }
}
