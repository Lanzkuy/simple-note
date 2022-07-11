package com.lacorp.simple_note.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lacorp.simple_note.R
import com.lacorp.simple_note.data.NoteData
import com.lacorp.simple_note.ui.MenuActivity.Companion.noteContentItemAdapter
import com.lacorp.simple_note.ui.MenuActivity.Companion.noteData
import com.lacorp.simple_note.ui.MenuActivity.Companion.rvNotes
import kotlinx.android.synthetic.main.activity_content.*
import kotlinx.android.synthetic.main.activity_content.contentToolbar
import java.text.SimpleDateFormat
import java.util.*


class ContentActivity : AppCompatActivity() {
    private var noteId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)
        setSupportActionBar(contentToolbar)

        loadData()
        onClick()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.content_toolbar, menu)
        if (menu != null) {
            try {
                val extras: Bundle? = intent.extras
                val itemDelete = menu.findItem(R.id.appBarContentItemDelete)
                itemDelete.isVisible = false
                if (extras != null) {
                    itemDelete.isVisible = true
                }
            }
            catch (e: Exception) {
                Log.e("ContentActivity", "onCreateOptionsMenu: " + e.stackTraceToString())
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            saveValidate()
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun onClick() {
        contentToolbar.setNavigationOnClickListener {
            saveValidate()
        }

        contentToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.appBarContentItemDelete -> {
                    try {
                        val dialogClickListener = DialogInterface.OnClickListener { _, i ->
                            when (i) {
                                DialogInterface.BUTTON_POSITIVE -> {
                                    saveData("delete")
                                    finish()
                                }
                                DialogInterface.BUTTON_NEGATIVE -> { }
                            }
                        }

                        val builder = AlertDialog.Builder(this)
                        builder.setMessage("Are you sure want to delete this?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show()
                    }
                    catch (e: Exception) {
                        Log.e("ContentActivity", "onClick: " + e.stackTraceToString())
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun insertData(date: String) {
        try {
            var id = 1
            if(noteData.size > 0) {
                val lastData: NoteData = noteData[noteData.lastIndex]
                id = (lastData.noteId+1)
            }
            noteData.add(NoteData(id, etTitleContent.text.toString(),
                etTextContent.text.toString(), date))
        }
        catch (e: Exception) {
            Log.e("ContentActivity", "insertData: " + e.stackTraceToString())
        }
    }

    private fun updateData(date: String) {
        try {
            val index = noteData.indexOfFirst { it.noteId == noteId }
            noteData[index] = NoteData(
                noteId!!, etTitleContent.text.toString(),
                etTextContent.text.toString(), date)
        }
        catch (e: Exception) {
            Log.e("ContentActivity", "updateData: " + e.stackTraceToString())
        }
    }

    private fun deleteData() {
        try {
            val index = noteData.indexOfFirst { it.noteId == noteId }
            noteData.removeAt(index)
        }
        catch (e: Exception) {
            Log.e("ContentActivity", "deleteData: " + e.stackTraceToString())
        }
    }

    private fun loadData() {
        try {
            val position: Int = intent.getIntExtra("POSITION", -1)
            if (position > -1) {
                val item = noteData[position]
                noteId = item.noteId
                val noteTitle = item.noteTitle
                val noteContent = item.noteTextContent

                etTitleContent.setText(noteTitle)
                etTextContent.setText(noteContent)
            }
        }
        catch (e: Exception) {
            Log.e("ContentActivity", "loadData: " + e.stackTraceToString())
        }
    }

    @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
    private fun saveData(action: String) {
        try {
            val date = Date()
            val formatter = SimpleDateFormat("MMM dd yyyy HH:mm")
            val now: String = formatter.format(date).toString()

            val sharePref = getSharedPreferences("noteData", Context.MODE_PRIVATE)
            val editor = sharePref.edit()

            val gson = Gson()
            val emptyArray = gson.toJson(noteData)
            val jsonGet = sharePref.getString("json", emptyArray)
            val type = object: TypeToken<ArrayList<NoteData>>() {}.type
            noteData = ArrayList()
            noteData = gson.fromJson(jsonGet, type)

            if(action == "insert") {
                insertData(now)
            }

            if(action == "update") {
                updateData(now)
            }

            if(action == "delete") {
                deleteData()
            }

            noteContentItemAdapter.setData(noteData)
            rvNotes.adapter!!.notifyDataSetChanged()

            val jsonPost = gson.toJson(noteData)
            editor.apply {
                putString("json", jsonPost)
                apply()
            }
        }
        catch (e: Exception) {
            Log.e("ContentActivity", "saveData: " + e.stackTraceToString())
        }
    }

    private fun saveValidate() {
        try {
            if(etTitleContent.text.toString() != "") {
                if(noteId == null) {
                    saveData("insert")
                }
                else {
                    saveData("update")
                }
            }
            finish()
        }
        catch (e: Exception) {
            Log.e("ContentActivity", "saveValidate: " +e.stackTraceToString())
        }
    }
}