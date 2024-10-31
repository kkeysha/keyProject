package com.example.ourbook

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ourbook.databinding.ActivityUpdateBinding
import com.squareup.picasso.Picasso
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import java.io.ByteArrayOutputStream
import java.util.Calendar

class ActivityUpdate : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateBinding
    private lateinit var db: DatabaseHelper
    private var bookId: Int = -1
    private var photoUri: Uri? = null
    private val CAMERA_REQUEST = 100
    private val STORAGE_PERMISSION = 101
    private val storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            photoUri = result.uriContent
            Picasso.get().load(photoUri).into(binding.updatefoto)
        } else {
            result.error?.printStackTrace()
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        db = DatabaseHelper(this)
        setContentView(binding.root)

        bookId = intent.getIntExtra("book_id", -1)
        if (bookId == -1) {
            finish()
            return
        }

        val book = db.getBookByID(bookId)
        binding.updateNama.setText(book.name)
        binding.updateNP.setText(book.nickname)
        binding.updateEmail.setText(book.email)
        binding.updateAlamat.setText(book.address)
        binding.updateTglLahir.setText(book.birthdate)
        binding.updateHP.setText(book.phone)

        book.photo?.let { uriString ->
            photoUri = Uri.parse(uriString)
            Picasso.get().load(photoUri).into(binding.updatefoto)
        }

        binding.updatefoto.setOnClickListener {
            if (!checkStoragePermission()) {
                requestStoragePermission()
            } else {
                pickFromGallery()
            }
        }

        binding.updateTglLahir.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    binding.updateTglLahir.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        binding.doneup.setOnClickListener {
            val newName = binding.updateNama.text.toString()
            val newSurename = binding.updateNP.text.toString()
            val newEmail = binding.updateEmail.text.toString()
            val newAddress = binding.updateAlamat.text.toString()
            val newDate = binding.updateTglLahir.text.toString()
            val newHP = binding.updateHP.text.toString()
            val emailPattern = "^[\\w.-]+@[\\w.-]+\\.com$".toRegex()

            if (newName.isEmpty() || newSurename.isEmpty() || newEmail.isEmpty() || newAddress.isEmpty() ||
                newDate.isEmpty() || newHP.isEmpty()) {
                Toast.makeText(this, "Book cannot be empty!", Toast.LENGTH_SHORT).show()
            } else if (!emailPattern.matches(newEmail)) {
                Toast.makeText(this, "Please enter a valid email with '@' and '.com'", Toast.LENGTH_SHORT).show()
            } else {
                val updateBook = Profile(bookId, newName, newSurename, newEmail, newAddress, newDate, newHP, photoUri?.toString())
                db.updateData(updateBook)

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                Toast.makeText(this, "Changes Saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestStoragePermission() {
        requestPermissions(storagePermissions, STORAGE_PERMISSION)
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun pickFromGallery() {
        cropImageLauncher.launch(CropImageContractOptions(null, CropImageOptions()))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery()
                } else {
                    Toast.makeText(this, "Enable Camera and Storage Permissions", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery()
                } else {
                    Toast.makeText(this, "Enable Storage Permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
