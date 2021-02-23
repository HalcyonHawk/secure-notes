package com.tilly.securenotes.utilities

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import java.lang.ref.WeakReference

// Class for activities that track if a text field is focused
object InputFocusUtilities {
    // Hide keyboard and unfocus view
    fun stopEditingText(context: Context, currentFocus: View?){
        var focusedView = currentFocus

        if (focusedView == null){
            focusedView = View(context)
        }
        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(focusedView.windowToken, 0)
        focusedView.clearFocus()
    }


    fun getUpdateMenuIfEditingListener(focusedAction: MenuItem?, vararg unfocusedAction: MenuItem?): View.OnFocusChangeListener {
        return object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                focusedAction?.isVisible = hasFocus
                unfocusedAction.forEach { menuItem ->
                    menuItem?.isVisible = !hasFocus
                }
            }

        }
    }

    fun startEditingTextField(activity: WeakReference<Activity>, editTextView: EditText){
        editTextView.requestFocusFromTouch()
        val inputMethodManager = activity.get()?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT)
    }
}
