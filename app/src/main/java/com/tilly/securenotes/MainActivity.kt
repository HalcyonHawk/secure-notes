package com.tilly.securenotes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tilly.securenotes.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // Lateinit is a variable that cant be null but is initialized later
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}