package com.example.practica4

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.view.View
class FileViewerActivity : AppCompatActivity() {

    private lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_viewer)

        val filePath = intent.getStringExtra("file_path")
        if (filePath == null) {
            finish()
            return
        }

        file = File(filePath)
        setupViews()
        displayFileContent()
    }

    private fun setupViews() {
        findViewById<TextView>(R.id.fileNameText).text = file.name
        findViewById<TextView>(R.id.fileInfoText).text = getFileInfo()

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }

        findViewById<ImageButton>(R.id.shareButton).setOnClickListener {
            shareFile()
        }

        findViewById<ImageButton>(R.id.openWithButton).setOnClickListener {
            openWithOtherApp()
        }
    }

    private fun displayFileContent() {
        val contentView = findViewById<ScrollView>(R.id.contentScrollView)
        val textView = findViewById<TextView>(R.id.contentTextView)
        val imageView = findViewById<ImageView>(R.id.contentImageView)

        textView.movementMethod = ScrollingMovementMethod()

        when {
            file.name.endsWith(".txt", true) || file.name.endsWith(".md", true) ||
                 file.name.endsWith(".json", true) || file.name.endsWith(".xml", true) -> {
                displayTextFile(textView)
                contentView.visibility = View.VISIBLE
                imageView.visibility = View.GONE
            }
            file.name.endsWith(".jpg", true) || file.name.endsWith(".jpeg", true) ||
                    file.name.endsWith(".png", true) -> {
                displayImageFile(imageView)
                contentView.visibility = View.GONE
                imageView.visibility = View.VISIBLE
            }
            else -> {
                showOpenWithDialog()
            }
        }
    }

    private fun displayTextFile(textView: TextView) {
        try {
            val content = file.readText()
            textView.text = content
        } catch (e: Exception) {
            textView.text = "Error al leer el archivo: ${e.message}"
        }
    }

    private fun displayImageFile(imageView: ImageView) {
        try {
            val uri = getFileUri(file)
            imageView.setImageURI(uri)
            //Configurar zoom básico
            imageView.setOnTouchListener { view, event ->
                when(event.action and android.view.MotionEvent.ACTION_MASK){
                    android.view.MotionEvent.ACTION_POINTER_DOWN -> {
                        //Multi-touch - permitir zoom
                        view.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    android.view.MotionEvent.ACTION_UP -> {
                        view.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                false

            }

        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileUri(file: File): Uri{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            //Usando FileProvider para Android 7+
            FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
        }else{
            //Para versiones anteriores
            Uri.fromFile(file)
        }
    }

    private fun showOpenWithDialog() {
        AlertDialog.Builder(this)
            .setTitle("Abrir archivo")
            .setMessage("Este tipo de archivo no puede ser visualizado directamente. ¿Desea abrirlo con otra aplicación?")
            .setPositiveButton("Abrir con") { _, _ -> openWithOtherApp() }
            .setNegativeButton("Cancelar") { _, _ -> finish() }
            .show()
    }

    private fun openWithOtherApp() {
        try {
            val uri = getFileUri(file)
            val openIntent = Intent().apply {
                action = Intent.ACTION_VIEW
                setDataAndType(uri, getMimeType(file.name))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            //Crear el chooser con título personalizado
            val chooserIntent = Intent.createChooser(openIntent, "Abrir archivo con:")
            //Verificar que hay aplicaciones disponibles
            if (openIntent.resolveActivity(packageManager) != null){
                startActivity(chooserIntent)
            }else{
                Toast.makeText(this, "No hay aplicaciones disponibles para abrir este archivo", Toast.LENGTH_SHORT).show()
            }
        }catch (e: Exception){
            Toast.makeText(this, "Error al abrir el archivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile() {
        try {
            val uri = getFileUri(file)

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = getMimeType(file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            //Creando el choosen con título
            val chooserIntent = Intent.createChooser(shareIntent, "Compartir archivo con: ")
            //Verificar que hay aplicaciones disponibles
            if(shareIntent.resolveActivity(packageManager) != null){
                startActivity(chooserIntent)
            }else{
                Toast.makeText(this, "No hay aplicaciones disponibles para compartir", Toast.LENGTH_SHORT).show()
            }
        }catch (e: Exception){
            Toast.makeText(this, "Error al compartir el archivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }

    private fun getFileInfo(): String {
        return "Tamaño: ${formatFileSize(file.length())}\n" +
                "Modificado: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(file.lastModified()))}"
    }

    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".txt", true) -> "text/plain"
            fileName.endsWith(".pdf", true) -> "application/pdf"
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
            fileName.endsWith(".png", true) -> "image/png"
            else -> "*/*"
        }
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }
}