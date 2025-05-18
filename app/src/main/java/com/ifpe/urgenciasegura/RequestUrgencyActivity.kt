package com.ifpe.urgenciasegura

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth

class RequestUrgencyActivity : AppCompatActivity() {
    private lateinit var imagemSelecionadaUri: Uri
    private lateinit var selecionarImagemLauncher: ActivityResultLauncher<Intent>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_request_urgency)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val botaoVoltarHome = findViewById<Button>(R.id.buttonVoltarHome)
        botaoVoltarHome.setOnClickListener {
            val intent = Intent(this, ScreenHomeActivity::class.java)
            startActivity(intent)
            finish()
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
        val spinnerGravidade = findViewById<Spinner>(R.id.spinnerGravidade)
        val editOutroTipo = findViewById<EditText>(R.id.inputOutroTipo)
        val opcoes = listOf("Selecione a gravidade", "Acidente", "Mal-estar", "Desmaio", "Queimadura", "Sangramento", "Outro")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opcoes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGravidade.adapter = adapter
        spinnerGravidade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val itemSelecionado = parent.getItemAtPosition(position).toString()

                if (itemSelecionado == "Outro") {
                    editOutroTipo.visibility = View.VISIBLE
                } else {
                    editOutroTipo.visibility = View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Nada aqui por enquanto
            }
        }
        val botaoMostrarAviso = findViewById<Button>(R.id.buttonMostrarAviso)
        val avisoCamera = findViewById<TextView>(R.id.avisoCamera)
        botaoMostrarAviso.setOnClickListener {
            if (avisoCamera.visibility == View.GONE) {
                avisoCamera.visibility = View.VISIBLE
                botaoMostrarAviso.text = "❌ Ocultar aviso"
            } else {
                avisoCamera.visibility = View.GONE
                botaoMostrarAviso.text = "ℹ️ Aviso sobre envio de imagem"
            }
        }
        // Inicializa o seletor de imagens
        selecionarImagemLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                imagemSelecionadaUri = result.data!!.data!!
                Toast.makeText(this, "Imagem selecionada com sucesso!", Toast.LENGTH_SHORT).show()
                // Aqui você pode exibir a imagem ou fazer o upload
            }
        }
        val botaoFoto = findViewById<Button>(R.id.buttonEnviarImagem)
        botaoFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            selecionarImagemLauncher.launch(intent)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val botaoLocalizacao = findViewById<Button>(R.id.buttonLocalizacao)
        botaoLocalizacao.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Atenção antes de usar a localização")
                .setMessage(
                    """
            Para garantir que sua localização seja detectada corretamente:
            
            1. Abra o app Google Maps.
            2. Toque no ícone de localização (📍) para que o GPS encontre sua posição atual.
            3. Depois, volte para este app e clique novamente no botão de localização.
            """.trimIndent()
                )
                .setPositiveButton("Abrir Google Maps") { dialog, _ ->
                    dialog.dismiss()
                    try {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("geo:0,0?q=Minha+Localização")
                        )
                        intent.setPackage("com.google.android.apps.maps")
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(this, "Google Maps não está instalado", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNeutralButton("Continuar") { dialog, _ ->
                    dialog.dismiss()
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            LOCATION_PERMISSION_REQUEST_CODE
                        )
                    } else {
                        obterLocalizacaoAtual()
                    }
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
    private fun obterLocalizacaoAtual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permissão de localização não concedida", Toast.LENGTH_SHORT).show()
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Toast.makeText(this, "📍 Latitude: $latitude\nLongitude: $longitude", Toast.LENGTH_LONG).show()
                    // Aqui você pode armazenar essa localização
                } else {
                    Toast.makeText(this, "Localização indisponível no momento", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao obter localização", Toast.LENGTH_SHORT).show()
            }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            obterLocalizacaoAtual()
        } else {
            Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
        }
    }
}
