package com.esmanureral.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.esmanureral.login.databinding.ActivitySignUpBinding

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var databaseTable: DatabaseTable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-Edge özelliği için gerekli kod
        enableEdgeToEdge()

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        databaseTable = DatabaseTable(this)

        // Kullanıcı kayıt işlemi
        binding.signupButton.setOnClickListener {
            val signupUsername = binding.signupUsername.text.toString()
            val signupPassword = binding.signupPassword.text.toString()
            val signupName = binding.signupName.text.toString()
            val signupEmail = binding.signupEmail.text.toString()

            signUpDatabase(signupUsername, signupPassword, signupName, signupEmail)
        }

        // Sayfa değiştirme işlemi
        binding.loginRedirect.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Veritabanına kullanıcı kaydetme
    private fun signUpDatabase(username: String, password: String, name: String, email: String) {
        val insertedRowId = databaseTable.insertUser(username, password, name, email)
        if (insertedRowId != -1L) {
            Toast.makeText(this, "Kayıt Oluşturuldu", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Kayıt Başarısız", Toast.LENGTH_SHORT).show()
        }
    }
}

