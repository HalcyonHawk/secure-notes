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
import com.tilly.securenotes.data.model.Note
import com.tilly.securenotes.data.repository.NoteRepository
import com.tilly.securenotes.databinding.NoteItemBinding
import com.tilly.securenotes.ui.editor.EditorActivity
import com.tilly.securenotes.utilities.NotesUtility

class NotesAdapter(initNoteList: ArrayList<Note>) :
    RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    val fullNoteList = arrayListOf<Note>()
    val displayedNoteList = arrayListOf<Note>()

    init {
        fullNoteList.addAll(fullNoteList)
        displayedNoteList.addAll(initNoteList)
    }

    class NoteViewHolder(val binding: NoteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val titleText: TextView = binding.noteTitle
        val openNoteButton: ImageButton = binding.openNote

    }

    var isFilterAscending: Boolean = false

    fun filterNotes(filterText: String){
        val filteredNotes = fullNoteList.filter { it.title.contains(filterText, ignoreCase = true)}
        sortNotesLastEdited(ArrayList(filteredNotes))
    }


    // Create note list item view holders and initialise views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    // Get number of notes
    override fun getItemCount(): Int {
        return displayedNoteList.size
    }

    // Set note view title text and button click
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = displayedNoteList.get(position)
        holder.titleText.setText(note.title)

        // If note is favorite then set yellow star icon to visable
        if (note.favorite) {
            holder.binding.favoritedIcon.visibility = View.VISIBLE
            // Update swipe favorite button drawable based on fav state
            holder.binding.favoriteNoteBtn.setImageResource(R.drawable.ic_star_white_24dp)

        } else {
            holder.binding.favoritedIcon.visibility = View.GONE
            holder.binding.favoriteNoteBtn.setImageResource(R.drawable.ic_star_border_white_24dp)
        }

        // Note button click should open the note editor with the note
        holder.openNoteButton.setOnClickListener {
            // Starting note editing activity and passing note in intent
            val intent = Intent(it.context, EditorActivity::class.java)
            intent.putExtra("note", NotesUtility.noteToString(note))
            it.context.startActivity(intent)
        }

        holder.binding.noteRoot.showMode = SwipeLayout.ShowMode.LayDown
        holder.binding.noteRoot.addDrag(SwipeLayout.DragEdge.Right, holder.binding.itemMenu)

        holder.binding.deleteNoteBtn.setOnClickListener {
            NoteRepository.deleteNote(note.noteId).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // If note removed on firebase then remove from displayed list and notify adapter about update
                    displayedNoteList.removeIf { item -> item.noteId == note.noteId }
                    notifyDataSetChanged()
                } else {
                    Toast.makeText(it.context, "Failed to delete note.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Clicking share button in swipe menu creates and launches intent to share note title and content with another app
        holder.binding.shareNoteBtn.setOnClickListener {
            it.context.startActivity(NotesUtility.createShareIntent(note.title, note.content))
        }

        holder.binding.favoriteNoteBtn.setOnClickListener { view ->
            val newFavState = !note.favorite
            // If not favourite then change icon and update in firebase
            // Toggle note favorite field and update icon to present new state
            NoteRepository.favoriteNote(noteId = note.noteId, favorite = newFavState)
                .addOnSuccessListener {
                    note.favorite = newFavState
                    holder.binding.noteRoot.close(true)

                    displayedNoteList.first { it.noteId == note.noteId }.favorite = newFavState
                    notifyDataSetChanged()
                }
                .addOnFailureListener {
//                        Picasso.get().load(R.drawable.ic_star_border_white_24dp)
                    Toast.makeText(view.context, "Note favorite failed", Toast.LENGTH_SHORT).show()
                }

        }
    }

    // Update full list
    fun updateFullNotesList(newList: ArrayList<Note>){
        fullNoteList.clear()
        fullNoteList.addAll(newList)

        sortNotesLastEdited(fullNoteList)
    }

    private fun setDisplayedNotes(newList: ArrayList<Note>){
        displayedNoteList.clear()
        displayedNoteList.addAll(newList)
    }

    fun sortNotesAlphabetically(){
        // Toggle sorting direction each call using isFilterDescending boolean
        val sortedNotes = if(isFilterAscending){
            isFilterAscending = false
            displayedNoteList.sortedWith(compareByDescending<Note>{ it.title }.thenBy { it.favorite })
        } else {
            isFilterAscending = true
            displayedNoteList.sortedWith(compareBy(Note::title, Note::favorite))
        }

        // Updating displayed notes with sorted ones
        setDisplayedNotes(ArrayList(sortedNotes))

        notifyDataSetChanged()
    }


    // Update displayed notes list and sort by last edited date and favorite status
    fun sortNotesLastEdited(newList: ArrayList<Note>) {
        setDisplayedNotes(newList)

        // Sort notes by last edited date
        displayedNoteList.sortByDescending { it.lastEdited }
        displayedNoteList.sortBy { it.favorite }

        notifyDataSetChanged()
    }

    fun resetNotes() {
        sortNotesLastEdited(fullNoteList)
    }
}