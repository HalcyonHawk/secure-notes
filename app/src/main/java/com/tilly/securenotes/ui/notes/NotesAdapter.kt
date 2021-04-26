package com.tilly.securenotes.ui.notes

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import com.tilly.securenotes.R
import com.tilly.securenotes.data.Note
import com.tilly.securenotes.data.repository.NoteRepository
import com.tilly.securenotes.databinding.NoteItemBinding
import com.tilly.securenotes.ui.editor.EditorActivity
import com.tilly.securenotes.utilities.NotesUtilities

// RecyclerView adapter for populating notes list in NotesActivity
// Takes an initial list of notes to populate the recycler view with
class NotesAdapter(initNoteList: ArrayList<Note>) :
    RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    // Lists for storing full list of notes and displayed notes
    // Separate lists to allow notes to be filtered
    private val fullNoteList = arrayListOf<Note>()
    private val displayedNoteList = arrayListOf<Note>()

    // When adapter is created, add the initial list of notes to the full note list and displayed notes list
    init {
        fullNoteList.addAll(fullNoteList)
        displayedNoteList.addAll(initNoteList)
    }

    // View holder with references to views in each note entry
    class NoteViewHolder(val binding: NoteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val titleText: TextView = binding.noteTitle
        val openNoteButton: ImageButton = binding.openNote
    }

    // Store if alphabetical filter is ascending or descending
    var isFilterAscending: Boolean = false

    // Function for filtering notes list by title using passed filter string
    fun filterNotes(filterText: String){
        val filteredNotes = fullNoteList.filter { it.title.contains(filterText, ignoreCase = true)}
        // Sort remaining notes alphabetically
        sortNotesLastEdited(ArrayList(filteredNotes))
    }


    // Create note list item view holders and initialise views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    // Get number of notes in displayed list
    override fun getItemCount(): Int {
        return displayedNoteList.size
    }

    // Set note view title text and button click listener
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = displayedNoteList.get(position)
        holder.titleText.text = note.title

        // If note is favourite
        if (note.favourite) {
            // Set star icon to visible
            holder.binding.favouritedIcon.visibility = View.VISIBLE
            // Update swipe favourite button drawable based on fav state
            holder.binding.favouriteNoteBtn.setImageResource(R.drawable.ic_star_white_24dp)
        } else {
            holder.binding.favouritedIcon.visibility = View.GONE
            holder.binding.favouriteNoteBtn.setImageResource(R.drawable.ic_star_border_white_24dp)
        }

        // Note button click should open the note editor with the note
        holder.openNoteButton.setOnClickListener {
            // Start note editing activity with the note as a JSON string
            val intent = Intent(it.context, EditorActivity::class.java)
            intent.putExtra("note", NotesUtilities.noteToString(note))
            it.context.startActivity(intent)
        }

        // Initialise note item swipe behavior to reveal hidden action buttons
        holder.binding.noteRoot.showMode = SwipeLayout.ShowMode.LayDown
        holder.binding.noteRoot.addDrag(SwipeLayout.DragEdge.Right, holder.binding.itemMenu)

        // Set action for delete note button in hidden swipe menu
        holder.binding.deleteNoteBtn.setOnClickListener {
            NoteRepository.deleteNote(note.noteId).addOnCompleteListener { task ->
                // Handle result of deleting note from database
                if (task.isSuccessful) {
                    // Remove note from displayed notes list if successful
                    // If note removed on firebase then remove from displayed list and notify adapter about update
                    displayedNoteList.removeIf { item -> item.noteId == note.noteId }
                    notifyDataSetChanged()
                } else {
                    // Show error if failed
                    Toast.makeText(it.context, "Failed to delete note.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Pressing the share button in swipe menu creates and launches intent to
        // share note title and content with another app
        holder.binding.shareNoteBtn.setOnClickListener {
            it.context.startActivity(NotesUtilities.createShareIntent(note.title, note.content))
        }

        // Favourite note button toggles is_favourite field for note document in firestore database
        holder.binding.favouriteNoteBtn.setOnClickListener { view ->
            val newFavState = !note.favourite
            // Toggle note favourite field and update icon to present new state if successful, show error if not
            NoteRepository.favouriteNote(noteId = note.noteId, favourite = newFavState)
                .addOnSuccessListener {
                    note.favourite = newFavState
                    holder.binding.noteRoot.close(true)

                    displayedNoteList.first { it.noteId == note.noteId }.favourite = newFavState
                    notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(view.context, "Note favourite failed", Toast.LENGTH_SHORT).show()
                }

        }
    }

    // Update full notes list with a new list
    fun updateFullNotesList(newList: ArrayList<Note>){
        fullNoteList.clear()
        fullNoteList.addAll(newList)

        sortNotesLastEdited(fullNoteList)
    }

    // Clear and update displayed notes list
    private fun setDisplayedNotes(newList: ArrayList<Note>){
        displayedNoteList.clear()
        displayedNoteList.addAll(newList)
    }

    // Sort displayed notes alphabetically based on if filter is ascending or descending
    fun sortNotesAlphabetically(){
        // Toggle sorting direction
        val sortedNotes = if(isFilterAscending){
            isFilterAscending = false
            displayedNoteList.sortedWith(compareByDescending<Note>{ it.title }.thenBy { it.favourite })
        } else {
            isFilterAscending = true
            displayedNoteList.sortedWith(compareBy(Note::title, Note::favourite))
        }

        // Update displayed notes with sorted ones
        setDisplayedNotes(ArrayList(sortedNotes))

        // Update view with new displayed notes list
        notifyDataSetChanged()
    }


    // Update displayed notes list and sort by last edited date and favourite status
    private fun sortNotesLastEdited(newList: ArrayList<Note>) {
        setDisplayedNotes(newList)

        // Sort notes by last edited date
        displayedNoteList.sortByDescending { it.lastEdited }
        displayedNoteList.sortBy { it.favourite }

        notifyDataSetChanged()
    }

    // Reset displayed notes to display all notes
    fun resetNotes() {
        sortNotesLastEdited(fullNoteList)
    }
}