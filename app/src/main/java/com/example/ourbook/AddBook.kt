package com.example.ourbook

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.ourbook.databinding.ActivityAddBookBinding // View binding import
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.util.Calendar

class AddBook : AppCompatActivity() {
    private lateinit var binding: ActivityAddBookBinding
    private lateinit var myDb: DatabaseHelper
    private var photoUri: Uri? = null
    private val GALLERY_REQUEST_CODE = 1
    private val CAMERA_REQUEST_CODE = 0

    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                Picasso.get().load(uri).into(binding.addfoto)
            }
        } else {
            result.error?.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myDb = DatabaseHelper(this)

        // Save button listener
        binding.done.setOnClickListener {
            saveData()
        }

        // Button to select photo
        binding.addfoto.setOnClickListener {
            selectPhoto()
        }

        // Date picker dialog for selecting birthdate
        binding.addTglLahir.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    binding.addTglLahir.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                },
                year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun saveData() {
        val name = binding.addNama.text.toString()
        val nickname = binding.addNP.text.toString()
        val email = binding.addEmail.text.toString()
        val address = binding.addAlamat.text.toString()
        val birthdate = binding.addTglLahir.text.toString()
        val phone = binding.addHP.text.toString()
        val photoUri = photoUri?.toString() // Convert URI to String for storage

        // Insert data into the database
        val isInserted = myDb.insertData(name, nickname, email, address, birthdate, phone, photoUri)

        if (isInserted) {
            Toast.makeText(this, "Data added successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            Toast.makeText(this, "Error adding data", Toast.LENGTH_SHORT).show()
        }
    }


    // Function to convert Bitmap to byte array
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun selectPhoto() {
        val options = arrayOf("Camera", "Gallery")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select Photo")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera() // Camera option
                1 -> openGallery() // Gallery option
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePicture, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val selectedPhoto = data?.extras?.get("data") as Bitmap
                    val uri = getImageUriFromBitmap(selectedPhoto)
                    binding.addfoto.setImageURI(uri)
                    photoUri = uri // Save the URI
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri: Uri? = data?.data
                    binding.addfoto.setImageURI(selectedImageUri)
                    photoUri = selectedImageUri // Save the URI
                }
            }
        }
    }

    // Helper function to convert Bitmap to URI
    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }
}
