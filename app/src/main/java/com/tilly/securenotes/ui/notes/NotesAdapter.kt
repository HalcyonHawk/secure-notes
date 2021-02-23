package com.tilly.securenotes.ui.notes

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tilly.securenotes.data.model.Note
import com.tilly.securenotes.databinding.NoteItemBinding
import com.tilly.securenotes.ui.editor.EditorActivity
import com.tilly.securenotes.utilities.NotesUtility

class NotesAdapter(val noteList: ArrayList<Note>) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    class NoteViewHolder(val binding: NoteItemBinding): RecyclerView.ViewHolder(binding.root){
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

        // Note button click should open the note editor with the note
        holder.openNoteButton.setOnClickListener {
            // Starting note editing activity and passing note in intent
            val intent = Intent(it.context, EditorActivity::class.java)
            intent.putExtra("note", NotesUtility.noteToString(note))
            it.context.startActivity(intent)
        }
    }

    fun updateNotes(newList: ArrayList<Note>){
        // TODO: dont clear, add notes already there
        noteList.clear()
        noteList.addAll(newList)

        notifyDataSetChanged()
    }
}