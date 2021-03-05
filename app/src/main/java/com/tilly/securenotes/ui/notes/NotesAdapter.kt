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
import com.squareup.picasso.Picasso
import com.tilly.securenotes.R
import com.tilly.securenotes.data.model.Note
import com.tilly.securenotes.data.repository.NoteRepository
import com.tilly.securenotes.databinding.NoteItemBinding
import com.tilly.securenotes.ui.editor.EditorActivity
import com.tilly.securenotes.utilities.NotesUtility

class NotesAdapter(val noteList: ArrayList<Note>) :
    RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    class NoteViewHolder(val binding: NoteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val titleText: TextView = binding.noteTitle
        val openNoteButton: ImageButton = binding.openNote

    }

    // Create note list item view holders and initialise views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    // Get number of notes
    override fun getItemCount(): Int {
        return noteList.size
    }

    // Set note view title text and button click
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList.get(position)
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
                    noteList.removeIf { item -> item.noteId == note.noteId }
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

                    noteList.first { it.noteId == note.noteId }.favorite = newFavState
                    notifyDataSetChanged()
                }
                .addOnFailureListener {
//                        Picasso.get().load(R.drawable.ic_star_border_white_24dp)
                    Toast.makeText(view.context, "Note favorite failed", Toast.LENGTH_SHORT).show()
                }

        }
    }

    // Update displayed notes list and sort by last edited date and favorite status
    fun updateNotes(newList: ArrayList<Note>) {
        noteList.clear()
        noteList.addAll(newList)
        // Sort notes by last edited date
        noteList.sortBy { it.lastEdited }
        noteList.sortBy { it.favorite }


        notifyDataSetChanged()
    }
}