package com.example.practica4

import FileAdapter
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MainActivity : AppCompatActivity(), FileItemListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var currentPathText: TextView
    private lateinit var emptyStateText: TextView
    private lateinit var fileAdapter: FileAdapter
    private var currentPath: String = Environment.getExternalStorageDirectory().path
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            loadFiles(currentPath)
        } else {
            requestStoragePermission()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupRecyclerView()
        checkPermissions()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerView)
        currentPathText = findViewById(R.id.currentPathText)
        emptyStateText = findViewById(R.id.emptyStateText)

        findViewById<Button>(R.id.btnThemeIPN).setOnClickListener {
            ThemeManager.setTheme(this, ThemeManager.THEME_IPN)
            recreate()
        }

        findViewById<Button>(R.id.btnThemeESCOM).setOnClickListener {
            ThemeManager.setTheme(this, ThemeManager.THEME_ESCOM)
            recreate()
        }

        findViewById<Button>(R.id.btnToggleTheme).setOnClickListener {
            toggleNightMode()
        }
    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(this, emptyList(), this)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = fileAdapter
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                loadFiles(currentPath)
            } else {
                requestStoragePermission()
            }
        } else {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
                loadFiles(currentPath)
            } else {
                permissionLauncher.launch(permissions)
            }
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, 2296)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 2296)
            }
        }
    }

    private fun loadFiles(path: String) {
        currentPath = path
        currentPathText.text = path

        val directory = File(path)
        val files = directory.listFiles()?.toList() ?: emptyList()

        if (files.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
            fileAdapter.updateFiles(files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })))
        }
    }

    private fun toggleNightMode() {
        val currentMode = ThemeManager.getCurrentNightMode(this)
        val newMode = when (currentMode) {
            ThemeManager.MODE_LIGHT -> ThemeManager.MODE_DARK
            ThemeManager.MODE_DARK -> ThemeManager.MODE_SYSTEM
            else -> ThemeManager.MODE_LIGHT
        }
        ThemeManager.setNightMode(this, newMode)
        recreate()
    }

    override fun onFileClicked(file: File) {
        if (file.isDirectory) {
            loadFiles(file.absolutePath)
        } else {
            openFile(file)
        }
    }

    override fun onBackPressed() {
        val parent = File(currentPath).parentFile
        if (parent != null && currentPath != Environment.getExternalStorageDirectory().path) {
            loadFiles(parent.absolutePath)
        } else {
            super.onBackPressed()
        }
    }

    private fun openFile(file: File) {
        val intent = Intent(this, FileViewerActivity::class.java).apply {
            putExtra("file_path", file.absolutePath)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_favorites -> true
            R.id.action_recent -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}