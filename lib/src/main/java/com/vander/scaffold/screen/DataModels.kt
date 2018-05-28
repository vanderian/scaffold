package com.vander.scaffold.screen

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.vander.scaffold.R
import kotlin.reflect.KClass

/**
 * @author marian on 24.9.2017.
 */
interface Event

interface Dialog : Event
sealed class Navigation : Event

object None: Navigation()
object GoBack : Navigation()
class NextScreen(val screen: Screen<*, *>, val id: Int = R.id.container_id, val fragmentsManager: Boolean = false) : Navigation()
class NextScreenResult(val screen: Screen<*, *>, val requestCode: Int, val id: Int = R.id.container_id, val fragmentsManager: Boolean = false) : Navigation()
class NextActivity(val intent: Intent, val finish: Boolean = false) : Navigation()
class NextActivityExplicit(val clazz: KClass<out Activity>, val finish: Boolean = false) : Navigation()
class WithResult(val intent: Intent, val requestCode: Int = 0) : Navigation()
class WithResultExplicit(val clazz: KClass<out Activity>, val requestCode: Int = 0g) : Navigation()

data class ToastEvent(val msgRes: Int = -1, val msg: String = "", val length: Int = Toast.LENGTH_SHORT) : Event
interface Notification : Event

data class Result(
    val request: Int,
    val success: Boolean = false,
    val jsonData: String = ""
)

object Empty : Screen.State, Screen.Intents
