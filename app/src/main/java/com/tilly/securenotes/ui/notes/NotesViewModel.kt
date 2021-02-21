package com.tilly.securenotes.ui.notes

import androidx.lifecycle.*
import com.tilly.securenotes.data.model.ResultStatusWrapper
import com.tilly.securenotes.data.model.Note
import com.tilly.securenotes.data.repository.NotesRepository
import com.tilly.securenotes.ui.notes.NotesUtility.observeOnce
import kotlin.collections.ArrayList

class NotesViewModel: ViewModel() {

    // Notes list live data to post updates to observers
    private val _notesList: MutableLiveData<ArrayList<Note>> = MutableLiveData(arrayListOf())
    // Getter to only expose on-mutable live data object to view
    val notesList: LiveData<ArrayList<Note>> get() = _notesList

    // TODO: Write javadoc comments & write reload notes function
    // Load new notes into live data ArrayList  from firebase using repository if successful
    // return firebase Task for error handling or success handling in view
    fun loadNotes(): LiveData<ResultStatusWrapper<ArrayList<Note>>> {
        val resultLiveData: MutableLiveData<ResultStatusWrapper<ArrayList<Note>>> = MutableLiveData()
        // Loading notes
        // Using observeOnce extension function defined in NotesUtils to automatically remove observer after Observer.onChanged executed
        // NOTE: using observeForever not observe in observeOnce function
        NotesRepository.loadNotes().observeOnce(Observer { response ->
            when(response){
                // If response from firebase successful then post newly loaded note list into _notesList
                is ResultStatusWrapper.Success -> {
                    // Posting newly loaded list to observers
                    _notesList.postValue(response.data)
                    // Posting response success for handling in view
                    resultLiveData.postValue(response)
                }
                // If response from firebase unsuccessful then post error for handling in view
                is ResultStatusWrapper.Error -> {
                    resultLiveData.postValue(response)
                }
            }
        })
        return resultLiveData
    }


}