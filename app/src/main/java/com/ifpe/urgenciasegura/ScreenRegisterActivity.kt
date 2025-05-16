package com.ifpe.urgenciasegura

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth


class ScreenRegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val buttonEntrar = findViewById<Button>(R.id.buttonEntrar)
        buttonEntrar.setOnClickListener {
            val intent = Intent(this, ScreenLoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        val buttonHome = findViewById<ImageButton>(R.id.buttonHome)
        buttonHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Conexão dos campos
        val nome = findViewById<EditText>(R.id.editTextNome)
        val idade = findViewById<EditText>(R.id.editTextIdade)
        val email = findViewById<EditText>(R.id.editTextEmail)
        val senha = findViewById<EditText>(R.id.editTextSenha)
        val celular = findViewById<EditText>(R.id.editTextCelular)
        val cpf = findViewById<EditText>(R.id.editTextCpf)
        val botaoCadastrar = findViewById<Button>(R.id.buttonCadastrar)

        // Aplicar a máscara de CPF no campo
        addCpfMask(cpf)

        botaoCadastrar.setOnClickListener {
            val nomeStr = nome.text.toString().trim()
            val idadeStr = idade.text.toString().trim()
            val emailStr = email.text.toString().trim()
            val senhaStr = senha.text.toString().trim()
            val celularStr = celular.text.toString().trim()
            val cpfStr = cpf.text.toString().trim()
            var isValid = true
            if (nomeStr.isEmpty()) {
                nome.error = "Digite seu nome"
                isValid = false
            }
            if (idadeStr.isEmpty()) {
                idade.error = "Informe sua idade"
                isValid = false
            }
            if (emailStr.isEmpty()) {
                email.error = "E-mail é obrigatório"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
                email.error = "E-mail inválido"
                isValid = false
            }
            if (senhaStr.isEmpty()) {
                senha.error = "Senha é obrigatória"
                isValid = false
            } else if (senhaStr.length < 6) {
                senha.error = "Senha deve ter pelo menos 6 caracteres"
                isValid = false
            }
            fun formatarCelular(celular: String): String {
                // Remove qualquer caractere não numérico
                val cleanCelular = celular.filter { it.isDigit() }

                // Aplica a máscara de celular no formato (XX) XXXXX-XXXX
                return when {
                    cleanCelular.length <= 2 -> "(${cleanCelular.take(2)})"
                    cleanCelular.length <= 7 -> "(${cleanCelular.take(2)}) ${cleanCelular.drop(2).take(5)}"
                    cleanCelular.length <= 11 -> "(${cleanCelular.take(2)}) ${cleanCelular.drop(2).take(5)}-${cleanCelular.drop(7).take(4)}"
                    else -> cleanCelular.take(11).let {
                        "(${it.take(2)}) ${it.drop(2).take(5)}-${it.drop(7).take(4)}"
                    }
                }
            }
            fun isCelularValid(celular: String): Boolean {
                val cleanCelular = celular.filter { it.isDigit() }

                // Verifica se o celular tem exatamente 11 dígitos
                if (cleanCelular.length != 11) return false

                // Validação simples: o celular deve começar com 9, pois é a regra para números móveis no Brasil
                if (cleanCelular[0] != '9') return false

                return true
            }
            if (celularStr.isEmpty()) {
                celular.error = "Celular é obrigatório"
                isValid = false
            }
            // Função para validar o CPF
            fun isCpfValid(cpf: String): Boolean {
                // Remove qualquer caractere não numérico antes de fazer a validação
                val cleanCpf = cpf.filter { it.isDigit() }
                // Verifica se o CPF tem exatamente 11 dígitos
                if (cleanCpf.length != 11) return false
                if (cleanCpf.all { it == cleanCpf[0] }) return false // Evita CPFs como "00000000000"
                try {
                    val digits = cleanCpf.map { it.toString().toInt() }
                    // Primeiro dígito verificador
                    val sum1 = (0..8).sumOf { (10 - it) * digits[it] }
                    val digit1 = if ((sum1 * 10) % 11 == 10) 0 else (sum1 * 10) % 11
                    if (digit1 != digits[9]) return false
                    // Segundo dígito verificador
                    val sum2 = (0..9).sumOf { (11 - it) * digits[it] }
                    val digit2 = if ((sum2 * 10) % 11 == 10) 0 else (sum2 * 10) % 11
                    if (digit2 != digits[10]) return false
                    return true
                } catch (e: Exception) {
                    return false
                }
            }
            if (cpfStr.isNotEmpty()) {
                if (!isCpfValid(cpfStr)) {
                    cpf.error = "CPF inválido"
                    isValid = false
                }
            }
            if (!isValid) return@setOnClickListener
            // Se chegou aqui, está tudo certo
            Toast.makeText(this, "Cadastro validado com sucesso!", Toast.LENGTH_SHORT).show()
            // 1. Instâncias do Firebase Auth e Realtime Database
            val auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()
            val referenciaUsuarios = database.getReference("usuarios")
            // 2. Capturar os valores digitados
            val nomeTexto = nome.text.toString()
            val idadeTexto = idade.text.toString()
            val emailTexto = email.text.toString()
            val senhaTexto = senha.text.toString()
            val celularTexto = celular.text.toString()
            val cpfTexto = cpf.text.toString()
            // 3. Validação básica
            if (emailTexto.isEmpty() || senhaTexto.isEmpty()) {
                Toast.makeText(this, "Email e senha são obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 4. Criar usuário no Firebase Authentication
            auth.createUserWithEmailAndPassword(emailTexto, senhaTexto)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 5. Pegando o UID do usuário autenticado
                        val uid = auth.currentUser?.uid
                        if (uid != null) {

                            // 🔐 Primeiro, criptografa o CPF e valida
                            val cpfCriptografado = CryptoUtil.encrypt(cpfTexto)

                            if (cpfCriptografado.isEmpty()) {
                                Toast.makeText(this, "Erro ao criptografar CPF", Toast.LENGTH_SHORT).show()
                                return@addOnCompleteListener
                            }

                            // 6. Criar o objeto usuário (sem a senha!)
                            val usuario = mapOf(
                                "nome" to nomeTexto,
                                "idade" to idadeTexto,
                                "email" to emailTexto,
                                "celular" to celularTexto,
                                "cpf" to cpfCriptografado
                            )

                            // 7. Salvar no Realtime Database usando UID
                            referenciaUsuarios.child(uid).setValue(usuario)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Cadastro completo!", Toast.LENGTH_SHORT).show()

                                    // Limpar os campos
                                    nome.text.clear()
                                    idade.text.clear()
                                    email.text.clear()
                                    senha.text.clear()
                                    celular.text.clear()
                                    cpf.text.clear()

                                    // Redirecionar para a tela de login
                                    val intent = Intent(this, ScreenLoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Erro ao salvar dados: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Erro ao cadastrar: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun addCpfMask(editText: EditText) {
        var isUpdating = false
        val mask = "###.###.###-##"

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                val str = s.toString().filter { it.isDigit() }
                var masked = ""
                var i = 0

                for (m in mask) {
                    if (m != '#' && i < str.length) {
                        masked += m
                    } else if (i < str.length) {
                        masked += str[i]
                        i++
                    } else {
                        break
                    }
                }

                isUpdating = true
                editText.setText(masked)
                editText.setSelection(masked.length)
                isUpdating = false
            }
        })
    }
}