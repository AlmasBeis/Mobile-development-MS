package kz.alfasoft.mycompany

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kz.alfasoft.mycompany.ui.navigation.AppNavHost
import kz.alfasoft.mycompany.ui.theme.MyCompanyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyCompanyTheme {
                AppNavHost()
            }
        }
    }
}
