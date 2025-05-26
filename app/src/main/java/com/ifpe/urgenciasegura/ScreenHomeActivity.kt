package com.ifpe.urgenciasegura

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ScreenHomeActivity : AppCompatActivity() {

    private lateinit var buttonSolicitarUrgencia: Button
    private lateinit var buttonLogout: Button
    private lateinit var buttonToggle: ImageButton
    private var isMenuVisible = false  // Controle de visibilidade do menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_home)

        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val nomeUsuario = prefs.getString("nome", "Usuário")

        // Atualiza a mensagem de boas-vindas
        val welcomeMessage = findViewById<TextView>(R.id.welcomeMessage)
        welcomeMessage.text = "Olá, $nomeUsuario! Tudo pronto para te ajudar."

        // Referências aos botões
        buttonSolicitarUrgencia = findViewById(R.id.buttonSolicitarUrgencia)
        buttonLogout = findViewById(R.id.buttonLogout)
        buttonToggle = findViewById(R.id.buttonToggle)

        // Botão de toggle (hamburger) para abrir/fechar o menu
        buttonToggle.setOnClickListener {
            isMenuVisible = !isMenuVisible
            if (isMenuVisible) {
                buttonSolicitarUrgencia.visibility = View.VISIBLE
                buttonLogout.visibility = View.VISIBLE
            } else {
                buttonSolicitarUrgencia.visibility = View.GONE
                buttonLogout.visibility = View.GONE
            }
        }

        // Ação do botão Solicitar Urgência
        buttonSolicitarUrgencia.setOnClickListener {
            val intent = Intent(this, RequestUrgencyActivity::class.java)
            startActivity(intent)
        }

        // Ação do botão Logout
        buttonLogout.setOnClickListener {
            prefs.edit().clear().apply()
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, ScreenLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
