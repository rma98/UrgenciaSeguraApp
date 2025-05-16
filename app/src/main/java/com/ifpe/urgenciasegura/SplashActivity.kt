package com.ifpe.urgenciasegura

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            val intent = Intent(
                this@SplashActivity,
                MainActivity::class.java
            )
            startActivity(intent)
            finish() // Fecha a splash
        }, SPLASH_TIME.toLong())
    }

    companion object {
        private const val SPLASH_TIME = 3000 // 3 segundos
    }
}