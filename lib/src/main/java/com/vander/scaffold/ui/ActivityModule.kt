package com.vander.scaffold.ui

import android.app.Activity
import android.content.Context
import com.vander.scaffold.annotations.ActivityScope
import dagger.Module
import dagger.Provides

@Module
@ActivityScope
class ActivityModule(private val activity: Activity) {

  @Provides fun provideContext(): Context = activity
}
