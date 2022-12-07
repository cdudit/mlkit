package fr.cdudit.mlkit.features.main

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import fr.cdudit.mlkit.R
import fr.cdudit.mlkit.databinding.ActivityMainBinding
import fr.cdudit.mlkit.managers.PermissionManager
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {
    enum class GetPictureType {
        PICTURE, GALLERY
    }

    // Launchers
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { getTextFromImage(it) }
    }
    private val pictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            latestUri?.let { getTextFromImage(it) }
        }
    }
    private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isSuccess ->
        if (!isSuccess) {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
        }
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()
    private var latestUri: Uri? = null
    private val stringsList: ArrayList<String> = arrayListOf()
    private lateinit var adapter: ListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(this.binding.root)
        setupListeners()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        this.adapter = ListAdapter(stringsList)
        this.binding.recyclerView.adapter = this.adapter
        this.viewModel.getItemTouchHelper(stringsList, adapter) { deleted: String, position: Int ->
            Snackbar.make(binding.root, deleted, Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    stringsList.add(position, deleted)
                    adapter.notifyItemInserted(position)
                }
                .show()
        }.attachToRecyclerView(this.binding.recyclerView)
    }

    private fun setupListeners() {
        this.binding.buttonTakePicture.setOnClickListener { getPicture(GetPictureType.PICTURE) }

        this.binding.buttonOpenGallery.setOnClickListener { getPicture(GetPictureType.GALLERY) }

        this.binding.floatingActionButton.setOnClickListener { clearList() }
    }

    private fun getPicture(type: GetPictureType) {
        if (viewModel.isCameraPermissionGranted(this)) {
            lifecycleScope.launch {
                when (type) {
                    GetPictureType.PICTURE -> {
                        viewModel.getTmpFileUri(this@MainActivity).let {
                            latestUri = it
                            pictureLauncher.launch(it)
                        }
                    }
                    GetPictureType.GALLERY -> galleryLauncher.launch("image/*")
                }
            }
        } else {
            this.permissionsLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun getTextFromImage(uri: Uri) {
        val image = InputImage.fromFilePath(this, uri)
        viewModel.recognizeText(
            image,
            onSuccess = {
                clearList()
                stringsList.addAll(it)
                this.adapter.notifyItemRangeInserted(0, this.stringsList.size)
            },
            onError = {
                Toast.makeText(this, it.stackTraceToString(), Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun clearList() {
        val olderSize = stringsList.size
        stringsList.clear()
        this.adapter.notifyItemRangeRemoved(0, olderSize)
    }
}