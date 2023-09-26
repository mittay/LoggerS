package com.mittay.loggers

import android.app.Application
import android.util.Log
import com.mittay.loggersio.FileLogTree
import com.mittay.loggersio.LoggerSio
import timber.log.Timber
import java.io.File

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        val pathe =  "${this.getExternalFilesDir(null)?.absolutePath
        }${File.separator}${"DebugTimber.txt"}"
        val builder = LoggerSio.Param()
        builder.maxLogSize = 4 //Mb
        builder.path = pathe
        Timber.plant(
            FileLogTree(builder.build())
        )

        Timber.i( "App test")
        repeat(3300){
            Timber.i( "App test$it")
        }
    }
}