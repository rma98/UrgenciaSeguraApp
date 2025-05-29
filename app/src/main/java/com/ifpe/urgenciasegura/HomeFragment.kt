package com.ifpe.urgenciasegura

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonLogin = view.findViewById<Button>(R.id.buttonLogin)
        val buttonCadastro = view.findViewById<Button>(R.id.buttonCadastro)

        buttonLogin.setOnClickListener {
            val intent = Intent(requireContext(), ScreenLoginActivity::class.java)
            startActivity(intent)
        }

        buttonCadastro.setOnClickListener {
            val intent = Intent(requireContext(), ScreenRegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
