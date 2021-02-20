package com.tilly.securenotes.ui.notes

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tilly.securenotes.R
import com.tilly.securenotes.data.model.Note
import com.tilly.securenotes.ui.editor.EditorActivity

class NotesAdapter(val noteList: ArrayList<Note>) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {
    class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val titleText: TextView
        val openNoteButton: ImageButton

        init {
            titleText = itemView.findViewById(R.id.note_title)
            openNoteButton = itemView.findViewById(R.id.open_note)
        }
    }

    // Create note list item view holders and initialise views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)

        return NoteViewHolder(view)
    }

    // Get number of notes
    override fun getItemCount(): Int {
        return noteList.size
    }

    // Set note view title text and button click
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.titleText.setText(noteList.get(position).title)

        // Note button click should open the note editor with the note
        holder.openNoteButton.setOnClickListener {
            // Starting note editing activity and passing note in intent
            val intent = Intent(it.context, EditorActivity::class.java)
            intent.putExtra("note", NotesUtility.noteToString(noteList[position]))
            it.context.startActivity(intent)
        }
    }

    fun updateNotes(newList: ArrayList<Note>){
        noteList.clear()
        noteList.addAll(newList)

        notifyDataSetChanged()
    }
}