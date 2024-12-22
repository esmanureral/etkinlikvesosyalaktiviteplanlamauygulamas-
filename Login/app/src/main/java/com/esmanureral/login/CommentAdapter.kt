package com.esmanureral.login

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View

// yorumları listeleyebilmek amacıyla RecyclerView  için bir adaptör
/*
Bir CommentAdapter sınıfı tanımladım.
Yorum listesini görüntülemek için bir RecyclerView adaptörü oluşturdum.
Görünümleri item_comment.xml dosyasından aldım ve RecyclerView öğelerine bağladım.
onCreateViewHolder, onBindViewHolder, ve getItemCount metotlarını kullanarak RecyclerView’in temel işleyişini kurdum.
 */
class CommentAdapter(private val comments: List<Comment>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    // ViewHolder'ı oluşturuyoruz
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        //
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    // ViewHolder'a veriyi bağlıyoruz
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.usernameText.text = comment.username // Kullanıcı adı
        holder.commentText.text = comment.commentText // Yorum metni
    }

    // Listede kaç öğe olduğunu döndürüyoruz
    override fun getItemCount(): Int = comments.size


    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usernameText: TextView = view.findViewById(R.id.usernameText) // Kullanıcı adı
        val commentText: TextView = view.findViewById(R.id.commentText) // Yorum metni
    }
}
