package com.tilly.securenotes.data.model

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

// Class for activities that track if a text field is focused
abstract class EditorToolbarActivity: AppCompatActivity(){
    // Hide keyboard and unfocus view
    fun stopEditingText(){
        var focusedView = currentFocus

        if (focusedView == null){
            focusedView = View(this)
        }
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(focusedView.windowToken, 0)
        focusedView.clearFocus()
    }


    fun getUpdateToolbarIfEditingListener(focusedAction: MenuItem?, vararg unfocusedAction: MenuItem?): View.OnFocusChangeListener {
        return View.OnFocusChangeListener { _, hasFocus ->
            focusedAction?.isVisible = hasFocus
            unfocusedAction.forEach { menuItem ->
                menuItem?.isVisible = !hasFocus
            }
        }
    }

    fun startEditingTextField(editTextView: EditText){
        editTextView.requestFocusFromTouch()
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT)
    }

}
