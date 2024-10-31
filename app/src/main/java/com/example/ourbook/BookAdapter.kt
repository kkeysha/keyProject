package com.example.ourbook

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class BookAdapter(private var books: List<Profile>, context: Context) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private val db: DatabaseHelper = DatabaseHelper(context)

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: ImageView = itemView.findViewById(R.id.profile_image) // Tambahkan ini
        val nameTextView: TextView = itemView.findViewById(R.id.nama)
        val nicknameTextView: TextView = itemView.findViewById(R.id.np)
        val emailTextView: TextView = itemView.findViewById(R.id.email)
        val addressTextView: TextView = itemView.findViewById(R.id.alamat)
        val birthdateTextView: TextView = itemView.findViewById(R.id.tglLahir)
        val phoneTextView: TextView = itemView.findViewById(R.id.HP)
        val editButton: ImageView = itemView.findViewById(R.id.edit)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        return BookViewHolder(view)
    }

    override fun getItemCount(): Int = books.size

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.nameTextView.text = book.name
        holder.nicknameTextView.text = book.nickname
        holder.emailTextView.text = book.email
        holder.addressTextView.text = book.address
        holder.birthdateTextView.text = book.birthdate
        holder.phoneTextView.text = book.phone

        // Set profile image
        book.photo?.let { uriString ->
            val uri: Uri = Uri.parse(uriString)
            val bitmap = BitmapFactory.decodeStream(holder.itemView.context.contentResolver.openInputStream(uri))
            holder.profileImageView.setImageBitmap(bitmap)
        }

        // Set onClickListener for edit button
         holder.editButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, ActivityUpdate::class.java).apply {
                putExtra("book_id", book.id)
            }
            holder.itemView.context.startActivity(intent)
        }

        // Set onClickListener for delete button
        holder.deleteButton.setOnClickListener {
            db.deleteData(book.id)
            refreshData(db.getAllBooks())
            Toast.makeText(holder.itemView.context, "Book Deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun refreshData(newBooks: List<Profile>) {
        books = newBooks
        notifyDataSetChanged()
    }
}