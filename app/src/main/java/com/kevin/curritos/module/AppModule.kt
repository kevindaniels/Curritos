package com.kevin.curritos.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    const val APP_DIRECTORY_KEY = "APP_DIRECTORY_KEY"
    @Provides
    @Singleton
    @Named(APP_DIRECTORY_KEY)
    fun providesContext(@ApplicationContext context: Context): String {
        var folder = context.getExternalFilesDir(null)?.absolutePath
        folder = if (folder != null) {
            "$folder/"
        }else{
            // should hopefully never get here
            "/storage/emulated/0/Android/data/com.kevin.curritos/files/"
        }
        return folder
    }
}