package com.lacorp.simple_note.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lacorp.simple_note.R
import com.lacorp.simple_note.adapter.NoteContentItemAdapter
import com.lacorp.simple_note.data.NoteData
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.activity_menu.contentToolbar
import kotlin.math.log


@SuppressLint("NotifyDataSetChanged")
class MenuActivity : AppCompatActivity() {
    companion object {
        lateinit var rvNotes: RecyclerView
        lateinit var noteContentItemAdapter: NoteContentItemAdapter
        lateinit var noteData: ArrayList<NoteData>
    }

    private lateinit var displayData: ArrayList<NoteData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        setSupportActionBar(contentToolbar)

        initialize()
        onClick()
        loadData()
        loadSetting()
    }

    private fun initialize() {
        noteData = ArrayList()
        displayData = ArrayList()
        rvNotes = rvNoteList
        rvNotes.layoutManager = LinearLayoutManager(applicationContext)
    }

    @SuppressLint("CommitPrefEdits")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        if (menu != null) {
            /*-- Search --*/
            searchOptionsMenu(menu)

            contentToolbar.setOnMenuItemClickListener { item ->
                val settingData = getSharedPreferences("settingData", Context.MODE_PRIVATE)
                when(item.itemId) {
                    /*-- Sort --*/
                    R.id.appBarItemSort -> {
                        sortOptionsMenu(settingData)
                        true
                    }
                    /*-- View --*/
                    R.id.appBarItemView -> {
                        viewOptionsMenu(settingData)
                        true
                    }
                    else -> false
                }
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun searchOptionsMenu(menu: Menu) {
        val searchItem = menu.findItem(R.id.appBarItemSearch)
        searchItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                try {
                    for (i in 0 until menu.size()) {
                        val item = menu.getItem(i)
                        if (item != searchItem) item.isVisible = false
                    }
                }
                catch (e: Exception) {
                    Log.e("MenuActivity", "searchOptionsMenu: " + e.stackTraceToString())
                }
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                invalidateOptionsMenu()
                return true
            }
        })
        val searchView = searchItem.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = "Search here..."
        searchView.setOnQueryTextListener(object:
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(text: String?): Boolean {
                try {
                    if (text!!.isNotEmpty()) {
                        noteData.forEach {
                            if (it.noteTitle.lowercase().contains(text.lowercase())) {
                                displayData.clear()
                                displayData.add(it)
                            }
                        }
                    }
                    else {
                        displayData.clear()
                        displayData.addAll(noteData)
                        loadSetting()
                    }
                    rvNotes.adapter!!.notifyDataSetChanged()
                }
                catch (e: Exception) {
                    Log.e("MenuActivity", "searchOptionsMenu: " + e.stackTraceToString())
                }
                return true
            }
        })
    }

    private fun sortOptionsMenu(settingData: SharedPreferences) {
        try {
            val items = arrayOf("A-Z", "Z-A", "Time")
            val builder = AlertDialog.Builder(this)
            var position = -1
            builder.setTitle("Sort by")
            builder.setSingleChoiceItems(items, settingData.getInt("sort", 0)) { dialog, i ->
                displayData.clear()
                if (items[i] == "A-Z") {
                    displayData.addAll(noteData.sortedBy {it.noteTitle})
                    position = i
                }
                else if (items[i] == "Z-A") {
                    displayData.addAll(noteData.sortedByDescending {it.noteTitle})
                    position = i
                }
                else if (items[i] == "Time") {
                    displayData.addAll(noteData.sortedBy {it.noteLastUpdated})
                    position = i
                }
                saveSetting("sort", position)
                rvNotes.adapter!!.notifyDataSetChanged()
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel") { _, _ ->}
            builder.show()
        }
        catch (e: Exception) {
            Log.e("MainActivity", "sortOptionsMenu: " + e.stackTraceToString())
        }
    }

    private fun viewOptionsMenu(settingData: SharedPreferences) {
        try {
            val items = arrayOf("List", "Grid")
            val builder = AlertDialog.Builder(this)
            var position = -1
            builder.setTitle("Choose an view")
            builder.setSingleChoiceItems(items, settingData.getInt("view", 0)) { dialog, i ->
                if (items[i] == "List") {
                    rvNotes.layoutManager = LinearLayoutManager(applicationContext)
                    position = i
                }
                else if (items[i] == "Grid") {
                    rvNotes.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
                    position = i
                }
                saveSetting("view", position)
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel") { _, _ ->}
            builder.show()
        }
        catch (e: Exception) {
            Log.e("MenuActivity", "viewOptionsMenu: " + e.stackTraceToString())
        }
    }

    private fun onClick() {
        btnWriteNote.setOnClickListener {
            val intent = Intent(this, ContentActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadData() {
        try {
            val sharePref = getSharedPreferences("noteData", Context.MODE_PRIVATE)
            val gson = Gson()
            val emptyArray = gson.toJson(noteData)
            val json = sharePref.getString("json", emptyArray)
            val type = object: TypeToken<ArrayList<NoteData>>() {}.type

            noteData = gson.fromJson(json, type)
            displayData.addAll(noteData)
            noteContentItemAdapter = NoteContentItemAdapter(displayData)
            noteContentItemAdapter.setItemClick(object: NoteContentItemAdapter.AdapterListener{
                override fun onClick(position: Int) {
                    val intent = Intent(this@MenuActivity, ContentActivity::class.java)
                    intent.putExtra("POSITION", position)
                    startActivity(intent)
                }
            })
            rvNotes.adapter = noteContentItemAdapter
        }
        catch (e: Exception) {
            Log.e("MainActivity", "loadData: " + e.stackTraceToString() )
        }
    }

    private fun saveSetting(type: String, position: Int) {
        try {
            val settingData = getSharedPreferences("settingData", Context.MODE_PRIVATE)
            val editor = settingData.edit()
            if(position != -1) {
                /*-- Sort --*/
                if (type == "sort") {
                    editor.apply {
                        clear()
                        putInt("sort", position)
                        apply()
                    }
                }
                /*-- View --*/
                else if (type == "view") {
                    editor.apply {
                        clear()
                        putInt("view", position)
                        apply()
                    }
                }
            }
        }
        catch (e: Exception) {
            Log.e("MainActivity", "saveSetting: " + e.stackTraceToString())
        }
    }

    private fun loadSetting() {
        try {
            val settingData = getSharedPreferences("settingData", Context.MODE_PRIVATE)
            displayData.clear()
            /*-- Sort --*/
            if (settingData.getInt("sort", 0) == 0) {
                displayData.addAll(noteData.sortedBy {it.noteTitle})
            }
            else if (settingData.getInt("sort", 0) == 1) {
                displayData.addAll(noteData.sortedByDescending {it.noteTitle})
            }
            else if (settingData.getInt("sort", 0) == 2) {
                displayData.addAll(noteData.sortedBy {it.noteLastUpdated})
            }

            /*-- View --*/
            if (settingData.getInt("view", 0) == 0) {
                rvNotes.layoutManager = LinearLayoutManager(applicationContext)
            }
            else if (settingData.getInt("view", 0) == 1) {
                rvNotes.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
            }
            rvNotes.adapter!!.notifyDataSetChanged()
        }
        catch (e: Exception) {
            Log.e("MainActivity", "loadSetting: " + e.stackTraceToString())
        }
    }
}