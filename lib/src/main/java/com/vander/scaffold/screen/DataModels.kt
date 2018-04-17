package com.vander.scaffold.screen

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.vander.scaffold.ui.widget.adapter.AdapterModel
import kotlin.reflect.KClass

/**
 * @author marian on 24.9.2017.
 */
interface Event

interface Dialog : Event
sealed class Navigation : Event

object None: Navigation()
object GoBack : Navigation()
class NextScreen(val screen: Screen<*, *, *>) : Navigation()
class NextActivity(val clazz: KClass<out Activity>, val finish: Boolean = false) : Navigation()
class ExtActivity(val intent: Intent) : Navigation()
class WithResult(val intent: Intent, val requestCode: Int) : Navigation()
class NextScreenResult(val screen: Screen<*, *, *>, val requestCode: Int) : Navigation()

data class ToastEvent(val msgRes: Int = -1, val msg: String = "", val length: Int = Toast.LENGTH_SHORT) : Event
interface Notification : Event

data class Result(
    val request: Int,
    val success: Boolean = false,
    val jsonData: String = ""
)

object Empty : Screen.State, Screen.Intents

data class ListState<out T : AdapterModel>(
    val items: List<T> = emptyList(),
    val loading: Boolean = true,
    val empty: Boolean = false
) : Screen.State

