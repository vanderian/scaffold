package com.vander.scaffold.ui

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputLayout
import com.vander.scaffold.form.FormInput
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables

fun Boolean.visibility() = if (this) View.VISIBLE else View.GONE

fun Int.dpToPx(context: Context): Float = this * context.resources.displayMetrics.density

fun <T : View> BottomSheetBehavior<T>.show() {
  this.state = BottomSheetBehavior.STATE_EXPANDED
}

fun <T : View> BottomSheetBehavior<T>.close() {
  this.state = BottomSheetBehavior.STATE_HIDDEN
}

fun View.visible() {
  if (visibility != View.VISIBLE) visibility = View.VISIBLE
}

fun View.gone() {
  if (visibility != View.GONE) visibility = View.GONE
}

fun TextInputLayout.gone(form: FormInput) {
  if (visibility != View.GONE) visibility = View.GONE
  form.validationEnabled(this, false)
}

fun View.invisible() {
  if (visibility != View.INVISIBLE) visibility = View.INVISIBLE
}

fun View.isVisible() = visibility == View.VISIBLE

fun TextInputLayout.text(): String = editText!!.text.toString()
fun TextInputLayout.setText(text: String): Unit = editText!!.setText(text)
fun TextInputLayout.setText(text: Int): Unit = editText!!.setText(text)

fun CompositeDisposable.with(vararg ds: Disposable) = apply { addAll(*ds) }

fun Resources.Theme.resourceId(attribute: Int) = TypedValue().apply { resolveAttribute(attribute, this, true) }.resourceId

fun checkMainThread(observer: Observer<*>): Boolean {
  if (Looper.myLooper() != Looper.getMainLooper()) {
    observer.onSubscribe(Disposables.empty())
    observer.onError(IllegalStateException("Expected to be called on the main thread but was " + Thread.currentThread().name))
    return false
  }
  return true
}

fun View.hideKeyboard() = (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)
