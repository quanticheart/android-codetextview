package com.quanticheart.codeedittext

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        defaultCode.setOnCompleteListener { }

        defaultCode.setOnCompleteListener { code ->
            toast(code)
        }
        defaultCode.setText("123456")

        code1.setOnCompleteListener {
            toast(it)
        }
        code2.setOnCompleteListener {
            toast(it)
        }
        code3.setOnCompleteListener {
            toast(it)
        }
        code4.setOnCompleteListenerValidation { code, validation ->
            if (validation) toast("your code is $code, finish is $validation")
        }

        btnShow.setOnClickListener {
            code4.showCode()
        }
    }
}