package fr.cdudit.mlkit

import android.app.Application
import fr.cdudit.mlkit.features.main.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {
    init {
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(listOf(viewModels))
        }
    }
}

val viewModels = module {
    viewModel {
        MainViewModel()
    }
}