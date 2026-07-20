package com.jomebe.harmoniq

import android.app.Application

class HarmoniqApplication : Application() {
    val container by lazy { AppContainer(this) }
}
