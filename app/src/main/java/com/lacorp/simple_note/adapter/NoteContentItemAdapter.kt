package com.lacorp.simple_note.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lacorp.simple_note.R
import com.lacorp.simple_note.data.NoteData
import kotlinx.android.synthetic.main.item_note_content.view.*

class NoteContentItemAdapter (
    private val noteData: ArrayList<NoteData>,
    ): RecyclerView.Adapter<NoteContentItemAdapter.NoteContentItemViewHolder>() {
    private lateinit var listener: AdapterListener

    interface AdapterListener {
        fun onClick(position: Int)
    }

    class NoteContentItemViewHolder(parent: View, listener: AdapterListener): RecyclerView.ViewHolder(parent) {
        var noteTitle: TextView = itemView.tvNoteTitle
        var noteTextContent: TextView = itemView.tvNoteTextContent
        var noteLastUpdated: TextView = itemView.tvNoteLastUpdated

        init {
            itemView.setOnClickListener {
                listener.onClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteContentItemViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.item_note_content, parent, false)
        return NoteContentItemViewHolder(inflater, listener)
    }

    override fun onBindViewHolder(holder: NoteContentItemViewHolder, position: Int) {
        val item = noteData[position]
        holder.noteTitle.text = item.noteTitle
        holder.noteTextContent.text = item.noteTextContent
        holder.noteLastUpdated.text = item.noteLastUpdated
    }

    override fun getItemCount() = noteData.size

    fun setData(data: ArrayList<NoteData>) {
        noteData.run {
            clear()
            addAll(data)
        }
    }

    fun setItemClick(listener: AdapterListener) {
        this.listener = listener
    }
}
