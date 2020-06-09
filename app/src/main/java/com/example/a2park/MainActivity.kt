package com.example.a2park

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.text_email
import kotlinx.android.synthetic.main.activity_main.text_pass
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //** le 2 righe sotto permettono di eseguire il thred network separatamente dal thread main
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        //**
        clicklogin()
        Click_Signin()
    }

    fun clicklogin() {
        var mongoClient = MongoClient("192.168.1.20", 27017)
        var db = mongoClient.getDB("Parcheggio")
        var tbl = db.getCollection("users")
        login_button.setOnClickListener {
            val email = text_email.text
            val pass = text_pass.text
            /*Controllare se esiste e se è admin o account normale*/
            if (text_email.text.toString() == "admin")
            {
                val intent = Intent(this, AdminActivity::class.java)
                this.startActivity(intent)
            }
            else
            {
                tbl = db.getCollection("users")
                var oggetto: DBObject = BasicDBObject("email", email.toString())
                var querypassword = tbl.findOne(oggetto)
                if (querypassword == null) //se l email non esiste
                {
                    /*PopUp errore*/
                    Toast.makeText(this, "Email/Password errato/i", Toast.LENGTH_SHORT).show()
                    text_email.text.clear()
                    text_pass.text.clear()
                }

                else if (querypassword["password"].toString() != pass.toString()) //se la password è errata
                {
                    /*PopUp errore*/
                    Toast.makeText(this, "Email/Password errato/i", Toast.LENGTH_SHORT).show()
                    text_email.text.clear()
                    text_pass.text.clear()
                }
                else { //altrimenti controllo se ho gia un posto prenotato
                    var saldo = tbl.findOne(oggetto)["saldo"].toString()
                    tbl = db.getCollection("postiOccupati")
                    var oggetto: DBObject = BasicDBObject("email", email.toString())
                    if (tbl.findOne(oggetto) != null ) //se ho un posto prenotato mi ricavo il codice e vado alla schermata finale
                    {
                        var cod = tbl.findOne(oggetto)["tipo"].toString()+"-"+ tbl.findOne(oggetto)["numero"].toString()
                        val intent = Intent(this, QrCodeUserActivity::class.java)
                        intent.putExtra("codice", cod)
                        this.startActivity(intent)
                    }
                    else { // altrimenti apro la useractivity normalmente caricando il nome e il saldo
                        val intent = Intent(this, UserActivity::class.java)
                        intent.putExtra("nomeUtente", email.toString())
                        intent.putExtra("saldo", saldo)
                        this.startActivity(intent)
                    }
                }
            }
        }
    }

    fun Click_Signin(){
        label_signin.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            this.startActivity(intent)
        }
    }
}

