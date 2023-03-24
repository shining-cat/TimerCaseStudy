package fr.shining_cat.timer_case_study.utils.di

import android.content.Context
import android.content.pm.ApplicationInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.shining_cat.timer_case_study.utils.HiitLogger
import fr.shining_cat.timer_case_study.utils.HiitLoggerImpl

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

    @Provides
    fun provideHiitLogger(@ApplicationContext context: Context): HiitLogger {
        val isDebug: Boolean =
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return HiitLoggerImpl(isDebug)
    }

}