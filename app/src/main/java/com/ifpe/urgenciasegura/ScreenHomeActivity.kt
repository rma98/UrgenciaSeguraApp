package com.ifpe.urgenciasegura

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ScreenHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_home)

        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val nomeUsuario = prefs.getString("nome", "Usuário")

        val textBoasVindas = findViewById<TextView>(R.id.textBoasVindas)
        textBoasVindas.text = "Olá, $nomeUsuario! Tudo pronto para te ajudar."

        val buttonSolicitarUrgencia = findViewById<Button>(R.id.buttonSolicitarUrgencia)
        buttonSolicitarUrgencia.setOnClickListener {
            val intent = Intent(this, RequestUrgencyActivity::class.java)
            startActivity(intent)
        }

        val buttonLogout = findViewById<Button>(R.id.buttonLogout)
        buttonLogout.setOnClickListener {
            // Limpa SharedPreferences
            prefs.edit().clear().apply()
            // Desloga do Firebase
            FirebaseAuth.getInstance().signOut()
            // Volta para a tela de login
            val intent = Intent(this, ScreenLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
