package com.vander.scaffold.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.vander.scaffold.R
import com.vander.scaffold.ui.dpToPx
import com.vander.scaffold.ui.resourceId
import io.reactivex.Observable

class InfiniteScrollDecoration(
    context: Context,
    private val recyclerView: RecyclerView
) : RecyclerView.ItemDecoration() {
  private var state: State = State.LOADING
  private val retryText = context.getString(R.string.action_retry)
  private val touchTargetSize = 48.dpToPx(context)
  private val enabled = intArrayOf(android.R.attr.state_enabled)
  private val pressed = intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled, android.R.attr.state_focused)
  private val textPaint: Paint
  private val drawableProgress: Drawable
  private val drawableBackground: Drawable
  private var touchRect = Rect()

  init {
    val appearance = context.theme.resourceId(R.attr.textAppearanceCaption)
    textPaint = TextView(context).apply { TextViewCompat.setTextAppearance(this, appearance) }.paint.apply { textAlign = Paint.Align.CENTER }
    drawableBackground = ContextCompat.getDrawable(context, context.theme.resourceId(R.attr.selectableItemBackgroundBorderless))!!
    drawableProgress = context.obtainStyledAttributes(R.style.Widget_AppCompat_ProgressBar, intArrayOf(android.R.attr.indeterminateDrawable))
        .let { arr -> arr.getDrawable(0).also { arr.recycle() } }!!
        .also { (it as? Animatable)?.start() }
  }

  @SuppressLint("ClickableViewAccessibility")
  val retryObservable: Observable<Unit> = Observable.create<Unit> { emitter ->
    recyclerView.setOnTouchListener { _, event ->
      if (state == State.ERROR) {
        if (touchRect.contains(event.x.toInt(), event.y.toInt())) {
          if (event.action == MotionEvent.ACTION_DOWN) drawableBackground.state = pressed
          if (event.action == MotionEvent.ACTION_UP) emitter.onNext(Unit)
        } else {
          drawableBackground.state = enabled
        }
        recyclerView.invalidateItemDecorations()
      }
      false
    }
    emitter.setCancellable { recyclerView.setOnTouchListener(null) }
  }

  override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
    super.onDrawOver(c, parent, state)
    if (parent.childCount > 0) {
      val child = parent.getChildAt(parent.childCount - 1)
      val bounds = Rect().apply { parent.getDecoratedBoundsWithMargins(child, this) }
          .apply {
            top = bottom
            bottom = top + parent.paddingBottom
          }

      when (this.state) {
        State.LOADING -> {
          val size = drawableProgress.intrinsicHeight / 4
          drawableProgress.bounds = Rect(
              bounds.centerX() - size,
              bounds.centerY() - size,
              bounds.centerX() + size,
              bounds.centerY() + size
          )
          drawableProgress.draw(c)
        }
        State.ERROR -> {
          val textWidth = textPaint.measureText(retryText)
          touchRect = Rect(
              bounds.centerX() - textWidth.toInt(),
              (bounds.centerY() - touchTargetSize / 2).toInt(),
              bounds.centerX() + textWidth.toInt(),
              (bounds.centerY() + touchTargetSize / 2).toInt()
          )
          drawableBackground.bounds = touchRect
          drawableBackground.draw(c)
          c.drawText(retryText, bounds.exactCenterX(), bounds.centerY() + textPaint.textSize / 2, textPaint)
        }
        else -> {
        }
      }
    }
  }

  fun changeState(state: State) {
    this.state = state
    recyclerView.invalidateItemDecorations()
  }

  enum class State {
    LOADING, END, ERROR, IDLE
  }
}