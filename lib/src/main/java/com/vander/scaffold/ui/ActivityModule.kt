package com.vander.scaffold.ui

import android.app.Activity
import android.content.Context
import dagger.Module
import dagger.Provides
import com.vander.scaffold.annotations.ActivityScope

@Module
@ActivityScope
class ActivityModule(private val activity: Activity) {

  @Provides fun provideContext(): Context = activity
}
