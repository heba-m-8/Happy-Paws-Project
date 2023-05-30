package com.example.happypaws

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val map: ImageButton = findViewById(R.id.map_btn)
        map.setOnClickListener {
            val mapIntent: Intent = Uri.parse(
                "geo:0,0?q=Princess Sumaya University for Technology, Amman, Jordan"
            ).let { location ->
                Intent(Intent.ACTION_VIEW, location)
            }
            startActivity(mapIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.appointment_option ->{ val intent = Intent(this, BookAptActivity::class.java)
                startActivity(intent)
                startService()}
        }
        return true
    }

    private fun startService() {
        val serviceIntent = Intent(this, NewService::class.java)
        serviceIntent.putExtra("inputExtra", "We can't wait to see you and your furry friend!")
        ContextCompat.startForegroundService(this, serviceIntent)
    }


}
