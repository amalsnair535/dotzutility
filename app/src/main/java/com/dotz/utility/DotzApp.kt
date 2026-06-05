package com.dotz.utility

import android.app.Application

/**
 * Application class — entry point for initialising app-wide singletons.
 * Kept intentionally minimal to reduce startup overhead.
 */
class DotzApp : Application() {
    // Database instance is lazily created in AppDatabase companion object
}
