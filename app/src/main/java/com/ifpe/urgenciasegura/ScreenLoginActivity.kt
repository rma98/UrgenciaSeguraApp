package com.ifpe.urgenciasegura

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth

class ScreenLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val buttonCadastro = findViewById<Button>(R.id.buttonCadastro)
        buttonCadastro.setOnClickListener {
            val intent = Intent(this, ScreenRegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
        val buttonHome = findViewById<ImageButton>(R.id.buttonHome)
        buttonHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        val editEmail = findViewById<EditText>(R.id.editTextEmail)
        val editSenha = findViewById<EditText>(R.id.editTextSenha)
        val buttonEntrar = findViewById<Button>(R.id.buttonEntrar)

        buttonEntrar.setOnClickListener {
            val emailTexto = editEmail.text.toString()
            val senhaTexto = editSenha.text.toString()

            if (emailTexto.isEmpty() || senhaTexto.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()
            val referenciaUsuarios = database.getReference("usuarios")

            auth.signInWithEmailAndPassword(emailTexto, senhaTexto)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid

                        if (uid != null) {
                            referenciaUsuarios.child(uid).get()
                                .addOnSuccessListener { snapshot ->
                                    if (snapshot.exists()) {
                                        val nome = snapshot.child("nome").value.toString()
                                        val celular = snapshot.child("celular").value.toString()
                                        val email = snapshot.child("email").value.toString()
                                        val idade = snapshot.child("idade").value.toString()

                                        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                        with(sharedPref.edit()) {
                                            putString("nome", nome)
                                            putString("email", email)
                                            putString("idade", idade)
                                            putString("celular", celular)
                                            apply() // Salva de forma assíncrona
                                        }
                                        Toast.makeText(this, "Bem-vindo(a), $nome!", Toast.LENGTH_SHORT).show()

                                        // Ir para próxima tela (ex: tela principal)
                                        val intent = Intent(this, ScreenHomeActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Usuário não encontrado no banco", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Erro ao buscar dados: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Erro ao fazer login: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}