package com.esmanureral.login

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

/*
Kullanıcılar etkinlik oluşturabilir
Etkinliklere göz atabilir
Arama yapabilir
Etkinliklere ait resimler seçebilir
 */


class EventActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private var userId: Int = -1

    private lateinit var database: DatabaseTable
    private lateinit var eventNameInput: EditText
    private lateinit var eventDateInput: EditText
    private lateinit var createEventButton: Button
    private lateinit var eventListRecyclerView: RecyclerView
    private lateinit var eventImageView: ImageView
    private lateinit var searchView: SearchView


    private val openGalleryLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri = uri
                eventImageView.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_event)

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Kullanıcı ID'si geçersiz!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database = DatabaseTable(this)
        eventNameInput = findViewById(R.id.eventNameInput)
        eventDateInput = findViewById(R.id.eventDateInput)
        createEventButton = findViewById(R.id.createEventButton)
        eventListRecyclerView = findViewById(R.id.eventListRecyclerView)
        eventImageView = findViewById(R.id.eventImageView)
        searchView = findViewById(R.id.searchView)

        eventListRecyclerView.layoutManager = LinearLayoutManager(this)

        //Arama
        /*
         arama çubuğuna yazı yazdıkça onQueryTextChange() metodu tetiklenir.
         filterEvents(newText) fonksiyonu, kullanıcının yazdığı metne göre etkinlikleri filtreler.
         */
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Arama butonuna tıklandığında çalışır
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Kullanıcı yazı yazdıkça çalışır
                if (newText != null) {
                    filterEvents(newText)
                }
                return true
            }
        })

        eventDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog =
                DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                    eventDateInput.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                }, year, month, day)

            datePickerDialog.show()
        }

        eventImageView.setOnClickListener {
            openGallery()
        }

        createEventButton.setOnClickListener {
            val eventName = eventNameInput.text.toString()
            val eventDate = eventDateInput.text.toString()

            if (eventName.isNotEmpty() && eventDate.isNotEmpty() && imageUri != null) {
                database.insertEvent(eventName, eventDate, imageUri.toString(), userId)
                Toast.makeText(this, "Etkinlik oluşturuldu!", Toast.LENGTH_SHORT).show()
                loadEvents()
            } else {
                Toast.makeText(
                    this,
                    "Tüm alanları doldurun ve bir resim seçin.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    startActivity(Intent(this, EventActivity::class.java))
                    finish()
                    true
                }

                R.id.profileFragment -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
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

        loadEvents()
    }

    private fun loadEvents() {
        val events = database.getAllEvents() // Veritabanından etkinlikleri al
        val adapter = EventAdapter(events, database, userId)
        eventListRecyclerView.layoutManager = LinearLayoutManager(this)
        eventListRecyclerView.adapter = adapter
    }

    private fun openGallery() {
        openGalleryLauncher.launch("image/*")
    }

//kullanıcı tarafından girilen arama
    private fun filterEvents(query: String) {
        val allEvents = database.getAllEvents() // Tüm etkinlikleri al
        val filteredEvents = allEvents.filter { event ->
            event.first.contains(query, ignoreCase = true) // Etkinlik adı
        }
        val adapter = EventAdapter(filteredEvents, database, userId)
        eventListRecyclerView.adapter = adapter
    }
}
