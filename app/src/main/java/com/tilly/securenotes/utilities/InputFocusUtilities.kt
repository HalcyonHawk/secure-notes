package com.tilly.securenotes.utilities

import android.app.Activity
import android.content.Context
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import java.lang.ref.WeakReference

// Utility class for activities that track if a text field is focused and handle focus changes
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


    // Change visibility of button for actions according to if a text view is focused or not
    // Focused action is usually a submit button and is shown when a text box is being edited/has focus
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

    // Set focus on text view given and show keyboard.
    // WeakReference to activity is used to prevent a memory leak due to this being a static class
    fun startEditingTextField(activity: WeakReference<Activity>, editTextView: EditText){
        editTextView.requestFocusFromTouch()
        val inputMethodManager = activity.get()?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT)
    }
}
