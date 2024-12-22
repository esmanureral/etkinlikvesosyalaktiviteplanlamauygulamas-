package com.esmanureral.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/*
Etkinlikleri RecyclerView ile listelemek,
Favorilere eklemek/çıkarmak,
Etkinliklere katılım sağlamak,
Yorumları yönetmek
 */

class EventAdapter(
    private var events: List<Triple<String, String, String>>,
    private val database: DatabaseTable, // Veritabanı nesnesi
    private val userId: Int              // Kullanıcı ID'si

) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    // Etkinlik listesini güncellemek için kullanılan fonksiyon
    fun updateEvents(newEvents: List<Triple<String, String, String>>) {
        events = newEvents
        notifyDataSetChanged() // RecyclerView'a veri setinin değiştiğini bildirir
    }

    // ViewHolder nesnesini oluşturuyoruz
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    // Her öğe için veri bağlama işlemi
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position] // Etkinliği alıyoruz
        holder.eventName.text = event.first
        holder.eventDate.text = event.second


        // Glide ile görsel yükleme
        Glide.with(holder.itemView.context)
            .load(event.third) // Görsel URI
            .placeholder(R.drawable.placeholder) // Görsel yüklenene kadar placeholder göster
            .into(holder.eventImage) // Görseli ImageView'e yerleştir

        val eventId = position + 1 // Varsayım: Etkinlik ID'leri sıralı

        // Favori simgesine tıklama işlemi
        holder.favoriteIcon.setOnClickListener {
            val eventId = position + 1
            // fav.eklenmiş mi diye kontrol
            if (database.isEventFavorited(userId, eventId)) {
                // fav.daysa kaldır
                database.removeEventFromFavorites(userId, eventId)
                holder.favoriteIcon.setImageResource(R.drawable.ic_favorite_border)
                Toast.makeText(holder.itemView.context, "Favorilerden kaldırıldı", Toast.LENGTH_SHORT).show()
            } else {
                // fav.eklenmemişse fav ekle
                database.addEventToFavorites(userId, eventId)

                holder.favoriteIcon.setImageResource(R.drawable.kirmizikalp)
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.blue))
                Toast.makeText(holder.itemView.context, "Favorilere eklendi", Toast.LENGTH_SHORT).show()
            }
            notifyItemChanged(position)
        }

// İlk favori durumu kontrolü
        if (database.isEventFavorited(userId, eventId)) {
            holder.favoriteIcon.setImageResource(R.drawable.kirmizikalp) // Dolu favori simgesi
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.blue)) // Use a favorite background color

        } else {
            holder.favoriteIcon.setImageResource(R.drawable.ic_favorite_border) // Boş favori simgesi
        }

        // Kullanıcı adı TextView'ini güncelle
        val username = database.getUsernameById(userId) ?: "Kullanıcı Adı"
        holder.kullaniciAdiTextView.text = username



        // Kullanıcının katılım durumunu kontrol et
        if (database.isUserParticipating(userId, eventId)) {
            holder.katilbuton.text = "Katıldınız" // Katılım durumu varsa metni güncelle
            holder.katilbuton.isEnabled = false  // Butonu devre dışı bırak
        } else {
            holder.katilbuton.text = "Katıl"
            holder.katilbuton.isEnabled = true // Butonu etkinleştir
        }

// "Katıl" butonuna tıklama işlemi
        holder.katilbuton.setOnClickListener {
            if (!database.isUserParticipating(userId, eventId)) {
                database.addUserToEvent(userId, eventId) // Katılım kaydı
                holder.katilbuton.text = "Katıldınız" // Buton metnini güncelle
                holder.katilbuton.isEnabled = false  // Butonu devre dışı bırak
                Toast.makeText(holder.itemView.context, "Etkinliğe katıldınız", Toast.LENGTH_SHORT).show()

                notifyItemChanged(position)
            } else {
                Toast.makeText(holder.itemView.context, "Zaten katıldınız", Toast.LENGTH_SHORT).show()
            }
        }

// "Katılma" butonuna tıklama işlemi
        holder.katilmabuton.setOnClickListener {
            val result = database.removeUserFromEvent(userId, eventId)
            if (result > 0) {
                holder.katilbuton.text = "Katıl" // Buton metnini geri değiştir
                holder.katilbuton.isEnabled = true // Butonu etkinleştir
                Toast.makeText(holder.itemView.context, "Etkinlikten çıkarıldınız", Toast.LENGTH_SHORT).show()
                notifyItemChanged(position)
            } else {
                Toast.makeText(holder.itemView.context, "Etkinlikten çıkarılamadı", Toast.LENGTH_SHORT).show()
            }
        }


        holder.etkinlik.setOnClickListener {
            val eventId = position + 1 // Etkinlik ID'si, bu örnekte sıralı kabul ediliyor
            val participantCount =
                database.getParticipantCountForEvent(eventId) // Katılımcı sayısını al

            // Katılımcı sayısını bir Toast ile gösteriyoruz
            Toast.makeText(
                holder.itemView.context,
                "Etkinliğe katılan kişi sayısı: $participantCount",
                Toast.LENGTH_LONG
            ).show()
        }

        // Yorum gönderme işlemi
        holder.yorumGonder.setOnClickListener {
            val yorum = holder.yorumAlani.text.toString()
            if (yorum.isNotBlank()) {
                database.addComment(
                    eventId = eventId,
                    comment = yorum,
                    userId = userId
                )
                Toast.makeText(holder.itemView.context, "Yorum gönderildi!", Toast.LENGTH_SHORT)
                    .show()
                holder.yorumAlani.text.clear() // Yorum alanını temizle
            } else {
                Toast.makeText(holder.itemView.context, "Lütfen bir yorum yazın", Toast.LENGTH_SHORT).show()
            }

        }



// Yorumları alalım
        val comments = database.getCommentsForEvent(eventId)
// Yalnızca yorumları alacak şekilde bir liste oluşturuyoruz
        val commentAdapter = CommentAdapter(comments)
        holder.yorumlar_listesi.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.yorumlar_listesi.adapter = commentAdapter

    }

    // RecyclerView'daki öğe sayısını belirliyoruz
    override fun getItemCount(): Int = events.size

    // ViewHolder sınıfı, öğe görünümündeki bileşenleri tutar
    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventName: TextView = view.findViewById(R.id.eventNameText)
        val eventDate: TextView = view.findViewById(R.id.eventDateText)
        val eventImage: ImageView = view.findViewById(R.id.eventImageView)
        val katilbuton: Button = view.findViewById(R.id.katilbuton)
        val katilmabuton: Button = view.findViewById(R.id.katilmabuton)
        val etkinlik:View=view.findViewById(R.id.etkinlik)
        val yorumAlani: EditText = view.findViewById(R.id.yorum_alani)
        val yorumGonder: Button = view.findViewById(R.id.yorum_gonder)
        val yorumlar_listesi: RecyclerView = view.findViewById(R.id.yorumlar_listesi)
        val kullaniciAdiTextView: TextView = view.findViewById(R.id.kullanici_adi_value)
        val favoriteIcon: ImageView = view.findViewById(R.id.favoriteIcon)

    }
}