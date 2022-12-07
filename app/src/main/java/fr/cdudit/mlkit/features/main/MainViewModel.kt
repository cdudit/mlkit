package fr.cdudit.mlkit.features.main

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import fr.cdudit.mlkit.managers.PermissionManager
import java.io.File
import java.lang.Exception

class MainViewModel : ViewModel() {
    companion object {
        private const val AUTHORITY = "fr.cdudit.mlkit.provider"
        private const val PREFIX = "tmp_image_file"
        private const val SUFFIX = ".png"
    }

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun isCameraPermissionGranted(context: Context) : Boolean {
        return PermissionManager.isPermissionGranted(context, Manifest.permission.CAMERA)
    }

    fun recognizeText(image: InputImage, onSuccess: (ArrayList<String>) -> Unit, onError: (Exception) -> Unit) {
        recognizer.process(image)
            .addOnSuccessListener { text ->
                val list = arrayListOf<String>()
                text.textBlocks.forEach { block ->
                    list.addAll(block.lines.map { it.text })
                }
                onSuccess(list)
            }
            .addOnFailureListener(onError)
    }

    fun getItemTouchHelper(stringsList: ArrayList<String>, adapter: ListAdapter, onRemove: (String, Int) -> Unit): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deleted = stringsList[viewHolder.adapterPosition]
                val position = viewHolder.adapterPosition
                stringsList.removeAt(viewHolder.adapterPosition)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                onRemove(deleted, position)
            }
        })
    }

    fun getTmpFileUri(context: Context): Uri {
        val tmpFile = File.createTempFile(PREFIX, SUFFIX, context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(context, AUTHORITY, tmpFile)
    }
}