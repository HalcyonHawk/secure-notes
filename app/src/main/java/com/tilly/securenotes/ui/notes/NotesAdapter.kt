package com.tilly.securenotes.ui.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tilly.securenotes.R
import com.tilly.securenotes.data.model.Note

class NotesAdapter(val noteList: ArrayList<Note>) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {
    class NoteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val titleText: TextView

        init {
            titleText = itemView.findViewById(R.id.note_title)
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

    // Set note view title text
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.titleText.setText(noteList.get(position).title)
    }

    fun updateNotes(newList: ArrayList<Note>){
        noteList.clear()
        noteList.addAll(newList)

        notifyDataSetChanged()
    }
}