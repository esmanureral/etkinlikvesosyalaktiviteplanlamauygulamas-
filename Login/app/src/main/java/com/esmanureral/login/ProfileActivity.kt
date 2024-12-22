package com.esmanureral.login

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {


    private lateinit var database: DatabaseTable
    private var userId: Int = -1
    private lateinit var userNameTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var recyclerView: RecyclerView // RecyclerView eklendi
    private lateinit var eventAdapter: EventAdapter // Adapter eklendi
    private var profileImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                profileImageUri = uri
                profileImageView.setImageURI(uri)
                saveProfileImage(uri) // Profil fotoğrafını veritabanına kaydet
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Intent'ten userId'yi al
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            finish()
            return
        }
        findViewById<Button>(R.id.createEventButton).setOnClickListener {
            val intent = Intent(this, EventActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    val intent = Intent(this, EventActivity::class.java)
                    intent.putExtra("USER_ID", userId) // userId'yi EventActivity'ye geçiriyoruz
                    startActivity(intent) // Intent başlatıldı
                    true
                }
                R.id.profileFragment -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent) // Intent başlatıldı
                    true
                }
                R.id.katilFragment -> {
                    val intent = Intent(this, JoinedEventsActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }


        database = DatabaseTable(this)
        userNameTextView = findViewById(R.id.userNameTextView)
        profileImageView = findViewById(R.id.profileImageView)
        val changePhotoButton: Button = findViewById(R.id.changePhotoButton)
        recyclerView = findViewById(R.id.eventListRecyclerView2) // RecyclerView tanımlandı

        // RecyclerView ayarları
        recyclerView.layoutManager = LinearLayoutManager(this)
        eventAdapter = EventAdapter(mutableListOf(), database, userId)
        recyclerView.adapter = eventAdapter

        // Kullanıcı adı ve fotoğrafı yükle
        loadUserInfo()

        // Kullanıcının etkinliklerini yükle
        loadUserEvents()

        // Profil fotoğrafını değiştirme butonu
        changePhotoButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun loadUserInfo() {
        val (userName, profileImageUri) = database.getUserInfo(userId)
        userNameTextView.text = userName

        if (profileImageUri != null) {
            Glide.with(this).load(profileImageUri).into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.ic_launcher_background)
        }
    }

    private fun loadUserEvents() {
        val events = database.getEventsByUser(userId) // Kullanıcıya özel etkinlikleri getir
        if (events.isNotEmpty()) {
            eventAdapter.updateEvents(events) // Adapter'e etkinlikleri ekle
        } else {
            Toast.makeText(this, "Henüz etkinlik yok", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveProfileImage(uri: Uri) {
        val db = database.writableDatabase
        val values = ContentValues().apply {
            put("profile_image", uri.toString())
        }
        val rowsUpdated = db.update("data", values, "id=?", arrayOf(userId.toString()))
        if (rowsUpdated > 0) {
            loadUserInfo() // Kullanıcı bilgilerini yeniden yükleyin
            Toast.makeText(this, "Profil fotoğrafı güncellendi", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Profil fotoğrafı güncellenemedi", Toast.LENGTH_SHORT).show()
        }
    }

}
