package com.ifpe.urgenciasegura

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import java.util.Date
import java.util.Locale

class ConfirmUrgencyActivity : AppCompatActivity() {
    private lateinit var radioGroupServico: RadioGroup
    private lateinit var spinnerTipoUrgencia: Spinner
    private lateinit var editOutroTipoUrgencia: EditText
    private val tiposSamu = listOf("Selecione a gravidade", "Acidente", "Mal-estar", "Desmaio", "Queimadura", "Sangramento", "Mal súbito", "Outro")
    private val tiposDefesaCivil = listOf("Selecione a gravidade", "Deslizamento", "Alagamento", "Incêndio Florestal", "Desabamento", "Vazamento de Gás", "Risco Estrutural", "Outro")
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var ultimaLocalizacao: String? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private var fotoUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirm_urgency)

        val nome = intent.getStringExtra("nome") ?: ""
        val idade = intent.getStringExtra("idade") ?: ""
        val celular = intent.getStringExtra("celular") ?: ""
        val observacao = intent.getStringExtra("observacao") ?: ""

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val botaoVoltar = findViewById<Button>(R.id.buttonVoltar)
        botaoVoltar.setOnClickListener {
            val intent = Intent(this, RequestUrgencyActivity::class.java)
            startActivity(intent)
            finish()
        }
        radioGroupServico = findViewById(R.id.radioGroupServico)
        spinnerTipoUrgencia = findViewById(R.id.spinnerTipoUrgencia)
        editOutroTipoUrgencia = findViewById(R.id.editOutroTipoUrgencia)
        // Deixa o SAMU selecionado por padrão
        radioGroupServico.check(R.id.radioSamu)
        // Inicialmente coloca as opções de SAMU
        atualizarSpinner(tiposSamu)
        // Quando mudar o RadioButton
        radioGroupServico.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioSamu -> atualizarSpinner(tiposSamu)
                R.id.radioDefesaCivil -> atualizarSpinner(tiposDefesaCivil)
            }
        }
        // Quando selecionar no Spinner
        spinnerTipoUrgencia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selecionado = parent?.getItemAtPosition(position).toString()
                if (selecionado == "Outro") {
                    editOutroTipoUrgencia.visibility = View.VISIBLE
                } else {
                    editOutroTipoUrgencia.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                editOutroTipoUrgencia.visibility = View.GONE
            }
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
        val botaoTirarFoto = findViewById<Button>(R.id.buttonTirarFoto)
        botaoTirarFoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
            } else {
                abrirCamera()
            }
        }
        val buttonEnviar = findViewById<Button>(R.id.buttonEnviar)
        buttonEnviar.setOnClickListener {
            enviarSolicitacaoParaFirebase(nome, idade, celular, observacao)
        }
    }
    private fun atualizarSpinner(opcoes: List<String>) {
        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            R.id.textSpinnerItem,
            opcoes
        )
        spinnerTipoUrgencia.adapter = adapter
    }
    @SuppressLint("MissingPermission")
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
                    ultimaLocalizacao = "$latitude, $longitude"

                    Toast.makeText(this, "📍 Latitude: $latitude\nLongitude: $longitude", Toast.LENGTH_LONG).show()
                } else {
                    ultimaLocalizacao = "Indefinida"
                    Toast.makeText(this, "Localização indisponível no momento", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                ultimaLocalizacao = "Erro ao obter localização"
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
    private fun abrirCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "Não foi possível abrir a câmera", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imagemBitmap = data?.extras?.get("data") as? Bitmap
            if (imagemBitmap != null) {
                // Exemplo: mostrar num ImageView
                val imageView = findViewById<ImageView>(R.id.imageViewFoto)
                imageView.setImageBitmap(imagemBitmap)
                // Pode também salvar ou enviar conforme a necessidade
            }
        }
    }
    private fun enviarSolicitacaoParaFirebase(
        nome: String,
        idade: String,
        celular: String,
        observacao: String
    ) {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("urgencias")

        val tipoUrgencia = obterTipoUrgencia()
        val dataHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val localizacaoAtual = ultimaLocalizacao ?: "Localização não disponível"

        val dadosUrgencia = mutableMapOf(
            "nome" to nome,
            "idade" to idade,
            "celular" to celular,
            "observacao" to observacao,
            "tipoUrgencia" to tipoUrgencia,
            "dataHora" to dataHora,
            "localizacao" to localizacaoAtual
        )

        ref.child("usuario").push().setValue(dadosUrgencia)
            .addOnSuccessListener {
                Toast.makeText(this, "Solicitação enviada com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao enviar solicitação: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
    private fun obterTipoUrgencia(): String {
        val tipoSelecionado = spinnerTipoUrgencia.selectedItem.toString()
        return if (tipoSelecionado == "Outro") {
            editOutroTipoUrgencia.text.toString()
        } else {
            tipoSelecionado
        }
    }
}
