package com.esmanureral.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esmanureral.login.DatabaseTable
import com.esmanureral.login.R
import com.google.android.material.bottomnavigation.BottomNavigationView

// Katıldığı etkinlikleri başka bir activity'de gösterme
class JoinedEventsActivity : AppCompatActivity() {
    private lateinit var database: DatabaseTable
    private lateinit var joinedEventsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joined_events)

        val userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Kullanıcı bilgisi bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
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
                    intent.putExtra("USER_ID", userId)
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
        joinedEventsRecyclerView = findViewById(R.id.joinedEventsRecyclerView)
        joinedEventsRecyclerView.layoutManager = LinearLayoutManager(this)

        loadJoinedEvents(userId)
    }

    private fun loadJoinedEvents(userId: Int) {
        val events = database.getEventsByParticipation(userId)
        val adapter = EventAdapter(events, database, userId)
        joinedEventsRecyclerView.adapter = adapter
    }

}
