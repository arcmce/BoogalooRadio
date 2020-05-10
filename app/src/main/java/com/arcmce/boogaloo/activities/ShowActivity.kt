package com.arcmce.boogaloo.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arcmce.boogaloo.R
import kotlinx.android.synthetic.main.activity_show.*

class ShowActivity : AppCompatActivity() {

    var slug: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_show)

        slug = intent.getSerializableExtra("SLUG") as String

        textView.text = slug

    }
}
