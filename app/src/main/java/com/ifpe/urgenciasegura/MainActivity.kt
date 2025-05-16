package com.ifpe.urgenciasegura

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Encontre os botões pelo ID
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val buttonCadastro = findViewById<Button>(R.id.buttonCadastro)

        // Clique no botão de Login → abrir tela de login
        buttonLogin.setOnClickListener {
            val intent = Intent(this, ScreenLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Clique no botão de Cadastro → abrir tela de cadastro
        buttonCadastro.setOnClickListener {
            val intent = Intent(this, ScreenRegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
