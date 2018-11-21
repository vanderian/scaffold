package com.vander.scaffold.screen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.Toast
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import com.jakewharton.rxbinding2.support.v7.widget.itemClicks
import com.jakewharton.rxbinding2.support.v7.widget.navigationClicks
import com.vander.scaffold.R
import com.vander.scaffold.ui.widget.adapter.AdapterModel
import com.vander.scaffold.ui.widget.adapter.RecyclerAdapter
import io.reactivex.Observable
import kotlin.reflect.KClass

/**
 * @author marian on 24.9.2017.
 */
interface Event

interface Dialog : Event
sealed class NavEvent : Event

object None : NavEvent()
object GoBack : NavEvent()
data class GoUp(val navHostId: Int? = null) : NavEvent()
data class PopStack(val navHostId: Int? = null) : NavEvent()
data class NextScreen(val screen: Screen<*, *>, val id: Int = R.id.container_id, val fragmentsManager: Boolean = false) : NavEvent()
data class NextChildScreen(val screen: Screen<*, *>, val id: Int = R.id.child_container_id) : NavEvent()
data class NextScreenResult(val screen: Screen<*, *>, val requestCode: Int, val id: Int = R.id.container_id, val fragmentsManager: Boolean = false) : NavEvent()
data class NextActivity(val intent: Intent, val finish: Boolean = false) : NavEvent()
data class NextActivityExplicit(val clazz: KClass<out Activity>, val finish: Boolean = false, val intentBuilder: Intent.() -> Unit = {}) : NavEvent()
data class WithResult(val intent: Intent, val requestCode: Int = 0) : NavEvent()
data class WithResultExplicit(val clazz: KClass<out Activity>, val requestCode: Int = 0, val intentBuilder: Intent.() -> Unit = {}) : NavEvent()
data class NavDirection(
    val action: Int,
    val args: Bundle? = null,
    val navOptions: NavOptions? = null,
    val extras: Navigator.Extras? = null,
    val navHostId: Int? = null) : NavEvent()

data class ToastEvent(val msgRes: Int = -1, val msg: String = "", val length: Int = Toast.LENGTH_SHORT) : Event
interface Notification : Event

data class Result(
    val request: Int,
    val success: Boolean = false,
    val intent: Intent? = null
)

object Empty : Screen.State, Screen.Intents

interface MenuIntent : Screen.Intents {
  val toolbar: Toolbar
  fun menu(): Observable<MenuItem> = toolbar.itemClicks()
}

interface ListIntents<T : AdapterModel, R> : Screen.Intents {
  val adapter: RecyclerAdapter<T, R>
  val refresh: SwipeRefreshLayout

  fun onRefresh(): Observable<Unit> = refresh.refreshes()
  fun onItem(): Observable<R> = adapter.itemEventSource.toObservable()
}

interface PageListIntents<T : AdapterModel, R> : ListIntents<T, R> {
  fun loadMore(): Observable<Unit>
  fun retryPage(): Observable<Unit>
}

interface NavigationIntent : Screen.Intents {
  val toolbar: Toolbar
  fun navigation(): Observable<Unit> = toolbar.navigationClicks()
}

