package com.esmanureral.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.esmanureral.login.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var databaseTable: DatabaseTable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        databaseTable = DatabaseTable(this)

        //Login Butonu
        binding.loginButton.setOnClickListener {
            val loginUsername = binding.loginUsername.text.toString()
            val loginPassword = binding.loginPassword.text.toString()
            //bilgilerin kontrolü için:loginDatabase
            loginDatabase(loginUsername, loginPassword)
        }
        //Signup Yönlendirmesi
        binding.signupRedirect.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            finish()
        }
    }
    // loginDatabase fonksiyonunda
    private fun loginDatabase(username: String, password: String) {
        val userId = databaseTable.readUser(username, password) // Kullanıcı ID'sini kontrol et
        if (userId != null) {
            Toast.makeText(this, "Giriş Başarılı", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ProfileActivity::class.java) // Doğru aktiviteye yönlendir
            intent.putExtra("USER_ID", userId)  // userId'yi Intent ile gönder
            startActivity(intent)
            finish() // Mevcut aktiviteyi kapat
        } else {
            Toast.makeText(this, "Giriş Başarısız", Toast.LENGTH_SHORT).show() // Hata mesajı
        }
    }
}