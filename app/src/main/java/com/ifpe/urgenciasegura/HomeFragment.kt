package com.ifpe.urgenciasegura

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var isMenuOpen = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonLogin = view.findViewById<Button>(R.id.buttonLogin)
        val buttonCadastro = view.findViewById<Button>(R.id.buttonCadastro)
        val buttonToggle = view.findViewById<ImageButton>(R.id.buttonToggle)

        buttonLogin.setOnClickListener {
            val intent = Intent(requireContext(), ScreenLoginActivity::class.java)
            startActivity(intent)
        }

        buttonCadastro.setOnClickListener {
            val intent = Intent(requireContext(), ScreenRegisterActivity::class.java)
            startActivity(intent)
        }

        buttonToggle.setOnClickListener {
            if (isMenuOpen) {
                // Fechar menu
                buttonLogin.visibility = View.GONE
                buttonCadastro.visibility = View.GONE
            } else {
                // Abrir menu
                buttonLogin.visibility = View.VISIBLE
                buttonCadastro.visibility = View.VISIBLE
            }
            isMenuOpen = !isMenuOpen
        }
    }
}
