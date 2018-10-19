package com.vander.scaffold.ui

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.NavigationRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.vander.scaffold.Injectable
import com.vander.scaffold.R
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

abstract class NavigationActivity : AppCompatActivity(), HasSupportFragmentInjector, Injectable {

  @Inject lateinit var viewContainer: ViewContainer
  @Inject lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
  private lateinit var container: ViewGroup

  @LayoutRes open val layoutId = 0
  @NavigationRes open val graphId = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // calls setContentView()
    container = viewContainer[this]
    val inflater = LayoutInflater.from(this)
    if (layoutId == 0) {
      check(graphId != 0) { "graphId must be set if no custom layout is provided" }
      inflater.inflate(R.layout.layout_nav_host, container, true)
      val navController = findNavController(R.id.navHostDefault)
      navController.graph = navController.navInflater.inflate(graphId)
    } else {
      inflater.inflate(layoutId, container, true)
    }
  }

  override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector

  override fun onSupportNavigateUp(): Boolean =
      (supportFragmentManager.findFragmentById(R.id.navHostDefault)?.findNavController()?.navigateUp() ?: false) or super.onSupportNavigateUp()

  override fun onBackPressed() {
    supportFragmentManager.findFragmentById(R.id.navHostDefault)?.childFragmentManager?.primaryNavigationFragment?.let {
      if (BackSupport.handlesBack(it)) return
    }
    super.onBackPressed()
  }
}