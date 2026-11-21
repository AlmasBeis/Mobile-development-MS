package kz.alfasoft.mycompany

import android.app.Application

class MyCompanyApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
