package com.ifpe.urgenciasegura

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.*
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth

class RequestUrgencyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_request_urgency)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupOpcao)
        val layoutOutraPessoa = findViewById<LinearLayout>(R.id.layoutOutraPessoa)
        val layoutDadosUsuario = findViewById<LinearLayout>(R.id.layoutDadosUsuario)

        // Referência aos campos do usuário
        val editNome = findViewById<EditText>(R.id.editNome)
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editCelular = findViewById<EditText>(R.id.editCelular)
        val editIdade = findViewById<EditText>(R.id.editIdade)

        // Inicialmente esconde os dois layouts
        layoutDadosUsuario.visibility = View.GONE
        layoutOutraPessoa.visibility = View.GONE

        // Dados do SharedPreferences
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val nome = prefs.getString("nome", "")
        val email = prefs.getString("email", "")
        val celular = prefs.getString("celular", "")
        val idade = prefs.getString("idade", "")

        // Força a seleção inicial do botão "Para mim"
        radioGroup.check(R.id.radioEu)

        // Aplica manualmente a lógica do "Para mim"
        layoutOutraPessoa.visibility = View.GONE
        layoutDadosUsuario.visibility = View.VISIBLE
        editNome.setText(nome)
        editEmail.setText(email)
        editCelular.setText(celular)
        editIdade.setText(idade)

        // Listener
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioOutro) {
                layoutOutraPessoa.visibility = View.VISIBLE
                layoutDadosUsuario.visibility = View.GONE
            } else {
                layoutOutraPessoa.visibility = View.GONE
                layoutDadosUsuario.visibility = View.VISIBLE
                editNome.setText(nome)
                editEmail.setText(email)
                editCelular.setText(celular)
                editIdade.setText(idade)
            }
        }

        // Exemplo de campo adicional para "Outro" no spinner
        val spinnerGravidade = findViewById<Spinner>(R.id.spinnerGravidade)
        val editOutroTipo = findViewById<EditText>(R.id.inputOutroTipo)

        spinnerGravidade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val itemSelecionado = parent.getItemAtPosition(position).toString()
                if (itemSelecionado == "Outro") {
                    editOutroTipo.visibility = View.VISIBLE
                } else {
                    editOutroTipo.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
