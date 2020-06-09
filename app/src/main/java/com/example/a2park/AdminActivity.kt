package com.example.a2park


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient
import kotlinx.android.synthetic.main.admin.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class AdminActivity() : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin)
        leggiqr()
        click_tariffe()
        ricarica()
    }

    fun leggiqr(){
        qr_button.setOnClickListener {
            val qrScanner = IntentIntegrator(this)
            qrScanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            qrScanner.setBeepEnabled(false)
            qrScanner.setOrientationLocked(false)
            qrScanner.initiateScan(null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                var mongoClient = MongoClient("192.168.1.20", 27017)
                var db = mongoClient.getDB("Parcheggio")
                var tbl = db.getCollection("users")

                var oggetto1 : DBObject = BasicDBObject("postoPrenotato",  result.contents.toString())
                var emailUser1 = tbl.findOne(oggetto1)["email"].toString()
                val saldo_utente = tbl.findOne(oggetto1)["saldo"].toString()
                tbl = db.getCollection("postiOccupati")
                var oggetto2 = BasicDBObject("email", emailUser1)
                var data = tbl.findOne(oggetto2)["data"].toString()
                if(data != null) {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val dateTime = LocalDateTime.parse(data, formatter)

                    var oggetto3: DBObject =
                        BasicDBObject("key", "prezzo_" + tbl.findOne(oggetto2)["tipo"].toString())
                    tbl = db.getCollection("prezzi")
                    val tariffa_ora = (tbl.findOne(oggetto3)["value"].toString().toFloat())
                    //La prima ora si paga comunque
                    val diff_h = (LocalDateTime.now().hour - dateTime.hour) + 1
                    val prezzo = (diff_h * tariffa_ora)

                    tbl = db.getCollection("users")
                    val updatedDocument = BasicDBObject()
                    updatedDocument.append(
                        "\$set",
                        BasicDBObject().append("saldo", (saldo_utente.toFloat() - prezzo))
                    )
                    val queryUtente = BasicDBObject("email", emailUser1)
                    tbl.update(queryUtente, updatedDocument)

                    //Elimina posto dalla collezione dei posti occupati
                    var oggetto: DBObject =
                        BasicDBObject("postoPrenotato", result.contents.toString())
                    var emailUser = tbl.findOne(oggetto)["email"].toString()
                    tbl = db.getCollection("postiOccupati")
                    oggetto = BasicDBObject("email", emailUser)
                    tbl.remove(oggetto)


                }else{
                    Toast.makeText(this, "Posto libero", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun click_tariffe(){
        tariffe_button.setOnClickListener {
            val intent = Intent(this, TariffeActivity::class.java)
            this.startActivity(intent)
        }
    }

    fun ricarica(){
        ricarica_button.setOnClickListener {
            var mongoClient = MongoClient("192.168.1.20", 27017)
            var db = mongoClient.getDB("Parcheggio")
            var tbl = db.getCollection("users")
            var oggetto: DBObject = BasicDBObject("email", text_email.text.toString())
            var queryemail = tbl.findOne(oggetto)
            if (queryemail == null) //se l email non esiste
            {
                /*PopUp errore*/
                Toast.makeText(this, "Email non esiste!", Toast.LENGTH_SHORT).show()
                text_email.text.clear()
                text_pass.text.clear()
            }
            else{
                var puntiUser = queryemail["saldo"].toString().toInt()
                puntiUser = puntiUser + (text_pass.text.toString().toInt())
                val updatedDocument = BasicDBObject()
                updatedDocument.append(
                    "\$set",
                    BasicDBObject().append("saldo", puntiUser.toString())
                )
                tbl.update(queryemail, updatedDocument)
                Toast.makeText(this, "Ricarica effettuata con successo!", Toast.LENGTH_SHORT).show()


            }
        }
    }

}

