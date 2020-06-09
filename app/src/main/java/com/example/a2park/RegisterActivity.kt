package com.example.a2park

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient
import kotlinx.android.synthetic.main.register.*
import kotlinx.android.synthetic.main.register.text_email
import kotlinx.android.synthetic.main.register.text_pass


class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)
        Click_Signin()
        //** le 2 righe sotto permettono di eseguire il thred network separatamente dal thread main
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        //**
    }

    fun Click_Signin(){
        var mongoClient = MongoClient("192.168.1.20", 27017)
        var db = mongoClient.getDB("Parcheggio")
        var tbl = db.getCollection("users")
        val email = text_email.text
        val pass = text_pass.text
        val passconfirm = text_pass_confirm.text
        signin_button.setOnClickListener {
            System.out.println("Ecco i dati " + email + pass + passconfirm);
            if (pass.toString() != passconfirm.toString()) {
                Toast.makeText(this, "Le password sono diverse", Toast.LENGTH_SHORT).show()
                text_email.text.clear()
                text_pass.text.clear()
                text_pass_confirm.text.clear()
            }
            else {
                // Toast.makeText(this, "Le password combaciano", Toast.LENGTH_SHORT).show()
                var oggetto : DBObject = BasicDBObject("email", email.toString())
                oggetto.put("password",pass.toString())
                oggetto.put("saldo",50)
                tbl.insert(oggetto)
                val intent = Intent(this, MainActivity::class.java)
                this.startActivity(intent)
            }
        }
    }
}