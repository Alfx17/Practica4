import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.practica4.FileItemListener
import com.example.practica4.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileAdapter(
    private val context: Context,
    private var files: List<File>,
    private val listener: FileItemListener
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>(){

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val icon: ImageView = itemView.findViewById(R.id.fileIcon)
        val name: TextView = itemView.findViewById(R.id.fileName)
        val size: TextView = itemView.findViewById(R.id.fileSize)
        val date: TextView = itemView.findViewById(R.id.fileDate)
        val cardView: View = itemView.findViewById(R.id.fileCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.name.text = file.name
        holder.date.text = dateFormat.format(Date(file.lastModified()))

        if(file.isDirectory){
            holder.icon.setImageResource(R.drawable.ic_folder)
            holder.size.text = "${file.list()?.size ?: 0} items"
        }else{
            val iconRes = getFileIcon(file.name)
            holder.icon.setImageResource(iconRes)
            holder.size.text = formatFileSize(file.length())
        }
        holder.cardView.setOnClickListener {
            listener.onFileClicked(file)
        }

    }

    override fun getItemCount(): Int = files.size

    fun updateFiles(newFiles: List<File>) {
        files = newFiles
        notifyDataSetChanged()
    }

    private fun getFileIcon(fileName: String): Int{
        return when{
            fileName.endsWith(".txt", true) -> R.drawable.ic_text
            fileName.endsWith(".pdf", true) -> R.drawable.ic_pdf
            fileName.endsWith("jpg", true) || fileName.endsWith(".jpeg", true) ||
                    fileName.endsWith(".png", true) -> R.drawable.ic_image
            fileName.endsWith(".json", true) -> R.drawable.ic_json
            fileName.endsWith(".xml", true) -> R.drawable.ic_xml
            else -> R.drawable.ic_file

        }
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size /1024} KB"
            else -> "${size / (1024*1024)} MB"
        }
    }

}
