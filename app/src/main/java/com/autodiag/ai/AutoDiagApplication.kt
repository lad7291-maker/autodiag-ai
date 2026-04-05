package com.autodiag.ai

import android.app.Application
import com.autodiag.ai.di.aiAgentModule
import com.autodiag.ai.di.appModule
import com.autodiag.ai.di.databaseModule
import com.autodiag.ai.di.domainModule
import com.autodiag.ai.di.networkModule
import com.autodiag.ai.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AutoDiagApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@AutoDiagApplication)
            modules(
                appModule,
                databaseModule,
                networkModule,
                viewModelModule,
                aiAgentModule,
                domainModule
            )
        }
    }
}
