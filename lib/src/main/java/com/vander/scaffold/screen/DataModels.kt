package com.vander.scaffold.screen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jakewharton.rxbinding3.appcompat.itemClicks
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.vander.scaffold.ui.widget.adapter.AdapterItemEventObservable
import io.reactivex.Observable
import kotlinx.android.parcel.Parcelize
import kotlin.reflect.KClass

/**
 * @author marian on 24.9.2017.
 */
interface Event

interface Dialog : Event
sealed class NavEvent : Event

object None : NavEvent()
object GoBack : NavEvent()
data class GoUp(val childNavHostId: Int? = null) : NavEvent()
data class PopStack(val childNavHostId: Int? = null) : NavEvent()
data class PopWithResult(val extras: Bundle = Bundle.EMPTY, val success: Boolean = !extras.isEmpty) : NavEvent()
data class NextActivity(val intent: Intent, val finish: Boolean = false) : NavEvent()
data class NextActivityExplicit(val clazz: KClass<out Activity>, val finish: Boolean = false, val intentBuilder: Intent.() -> Unit = {}) : NavEvent()
data class WithResult(val intent: Intent, val requestCode: Int = 0) : NavEvent()
data class WithResultExplicit(val clazz: KClass<out Activity>, val requestCode: Int = 0, val intentBuilder: Intent.() -> Unit = {}) : NavEvent()
data class NavDirection(
    val action: Int,
    val args: Bundle? = null,
    val navOptions: NavOptions? = null,
    val extras: Navigator.Extras? = null,
    val childNavHostId: Int? = null) : NavEvent()

data class ToastEvent(val msgRes: Int = -1, val msg: String = "", val length: Int = Toast.LENGTH_SHORT) : Event
interface Notification : Event

@Parcelize
data class Result(
    val request: Int,
    val success: Boolean = false,
    val extras: Bundle? = null
) : Parcelable

object Empty : Screen.State, Screen.Intents

interface MenuIntent : Screen.Intents {
  val toolbar: Toolbar
  fun menu(): Observable<MenuItem> = toolbar.itemClicks()
}

interface ListIntent<R> : Screen.Intents {
  val adapter: AdapterItemEventObservable<R>

  fun onItem(): Observable<R> = adapter.itemEventSource.toObservable()
}

interface RefreshIntent : Screen.Intents {
  val refresh: SwipeRefreshLayout

  fun onRefresh(): Observable<Unit> = refresh.refreshes()
}

interface PageIntents {
  fun loadMore(): Observable<Unit>
  fun retryPage(): Observable<Unit>
}

interface NavigationIntent : Screen.Intents {
  val toolbar: Toolbar
  fun navigation(): Observable<Unit> = toolbar.navigationClicks()
}

