package com.example.a2park

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import kotlinx.android.synthetic.main.tariffe.*

class TariffeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tariffe)
        text_diversi.isEnabled = false;
        text_outdoor.isEnabled = false;
        text_indoor.isEnabled = false;
        //Leggere su mongo le tariffe e inserirle nelle text
        var mongoClient = MongoClient("192.168.1.20", 27017)
        var db = mongoClient.getDB("Parcheggio")
        var tbl = db.getCollection("prezzi")
        //carico i prezzi per le tariffe
        var query =  BasicDBObject("key","prezzo_0")
        text_diversi.setText(tbl.findOne(query)["value"].toString().take(4))
        query =  BasicDBObject("key","prezzo_1")
        text_indoor.setText(tbl.findOne(query)["value"].toString().take(4))
        query =  BasicDBObject("key","prezzo_2")
        text_outdoor.setText(tbl.findOne(query)["value"].toString().take(4))



        click_modifica()
        click_salva()
    }

    fun click_modifica(){
        modifica_button.setOnClickListener() {
            Toast.makeText(this, "Modifica Abilitata", Toast.LENGTH_SHORT).show()
            //Attivi i pulsanti
            text_diversi.isEnabled = true;
            text_outdoor.isEnabled = true;
            text_indoor.isEnabled = true;
        }
    }

    fun click_salva() {
        //
        salva_button.setOnClickListener() {
            text_diversi.isEnabled = false;
            text_outdoor.isEnabled = false;
            text_indoor.isEnabled = false;
            Toast.makeText(this, "Dati Salvati", Toast.LENGTH_SHORT).show()
            //Estrarre delle text le nuove tariffe e scriverle su mongo
            var mongoClient = MongoClient("192.168.1.20", 27017)
            var db = mongoClient.getDB("Parcheggio")
            var tbl = db.getCollection("prezzi")
            var oggetto = BasicDBObject("key", "prezzo_0")
            var update = BasicDBObject("key","prezzo_0")
            update.append("value", text_diversi.text.toString())
            tbl.findAndModify(oggetto,update) // aggiorno prezzo div abili


            oggetto = BasicDBObject("key", "prezzo_1")
            update = BasicDBObject("key","prezzo_1")
            update.append("value", text_indoor.text.toString())
            tbl.findAndModify(oggetto,update) // aggiorno prezzo al chiuso


            oggetto = BasicDBObject("key", "prezzo_2")
            update = BasicDBObject("key","prezzo_2")
            update.append("value", text_outdoor.text.toString())
            tbl.findAndModify(oggetto,update) // aggiorno prezzo all aperto
        }
    }


}