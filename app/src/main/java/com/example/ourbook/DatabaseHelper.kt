package com.example.ourbook

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import android.widget.ImageView
import java.io.ByteArrayOutputStream

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "OurBook.db"
        private const val DATABASE_VERSION = 1

        // Table name
        private const val TABLE_NAME = "book_data"

        // Table columns
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_NICKNAME = "nickname"
        private const val COL_EMAIL = "email"
        private const val COL_ADDRESS = "address"
        private const val COL_BIRTHDATE = "birthdate"
        private const val COL_PHONE = "phone"
        private const val COL_PHOTO = "photo"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME ("
                + "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_NAME TEXT NOT NULL, "
                + "$COL_NICKNAME TEXT NOT NULL, "
                + "$COL_EMAIL TEXT NOT NULL, "
                + "$COL_ADDRESS TEXT, "
                + "$COL_BIRTHDATE TEXT NOT NULL, "
                + "$COL_PHONE TEXT NOT NULL, "
                + "$COL_PHOTO TEXT)") // Store photo as base64 encoded string
        db.execSQL(createTable)
    }

    private fun ImageViewToBase64(img: ImageView): String {
        val bitmap: Bitmap = if (img.drawable is BitmapDrawable) {
            (img.drawable as BitmapDrawable).bitmap
        } else {
            val vectorDrawable = img.drawable
            val width = vectorDrawable.intrinsicWidth
            val height = vectorDrawable.intrinsicHeight
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                val canvas = Canvas(this)
                vectorDrawable.setBounds(0, 0, width, height)
                vectorDrawable.draw(canvas)
            }
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }

    fun getBookByID(bookId: Int): Profile {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COL_ID = $bookId"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()

        val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME))
        val nickname = cursor.getString(cursor.getColumnIndexOrThrow(COL_NICKNAME))
        val email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL))
        val address = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDRESS))
        val date = cursor.getString(cursor.getColumnIndexOrThrow(COL_BIRTHDATE))
        val hp = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE))
        val image = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHOTO))

        cursor.close()
        db.close()
        return Profile(id, name, nickname, email, address, date, hp, image)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Insert new record (photo can be null)
    fun insertData(name: String, nickname: String, email: String, address: String?, birthdate: String, phone: String, photoUri: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_NICKNAME, nickname)
            put(COL_EMAIL, email)
            put(COL_ADDRESS, address)
            put(COL_BIRTHDATE, birthdate)
            put(COL_PHONE, phone)
            put(COL_PHOTO, photoUri) // Save URI string
        }

        val result = db.insert(TABLE_NAME, null, contentValues)
        return result != -1L // Return true if insert was successful
    }

    fun getAllBooks(): List<Profile> {
        val books = mutableListOf<Profile>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME))
                val nickname = cursor.getString(cursor.getColumnIndexOrThrow(COL_NICKNAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL))
                val address = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDRESS))
                val birthdate = cursor.getString(cursor.getColumnIndexOrThrow(COL_BIRTHDATE))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE))
                val photo = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHOTO))

                books.add(Profile(id, name, nickname, email, address, birthdate, phone, photo))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return books
    }

    fun updateData(profile: Profile): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COL_NAME, profile.name)
            put(COL_NICKNAME, profile.nickname)
            put(COL_EMAIL, profile.email)
            put(COL_ADDRESS, profile.address)
            put(COL_BIRTHDATE, profile.birthdate)
            put(COL_PHONE, profile.phone)
            profile.photo?.let { put(COL_PHOTO, it) }
        }
        val result = db.update(TABLE_NAME, contentValues, "$COL_ID = ?", arrayOf(profile.id.toString()))
        return result > 0
    }

    fun deleteData(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$COL_ID = ?", arrayOf(id.toString()))
        return result > 0 // Return true if delete was successful
    }
}
