package com.tilly.securenotes.ui.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.tilly.securenotes.data.model.Note
import com.tilly.securenotes.data.repository.LoginRepository
import com.tilly.securenotes.data.repository.NotesRepository
import java.util.*
import kotlin.collections.ArrayList

class EditorViewModel: ViewModel() {
    lateinit var currentNote: Note

}