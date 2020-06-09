package com.example.a2park


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient
import kotlinx.android.synthetic.main.user.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


var indexTariffa : Int = -1
var indexPosto : Int = -1

class UserActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user)
        click_tariffa()
        loadScrool(20)
        var intent = intent
        val nome = intent.getStringExtra("nomeUtente")
        val saldo = intent.getStringExtra("saldo")
        Nome.setText(nome)
        Saldo.setText(saldo+" €")
        var mongoClient = MongoClient("192.168.1.20", 27017)
        var db = mongoClient.getDB("Parcheggio")
        var tbl = db.getCollection("prezzi")
        //carico i prezzi per le tariffe
        var query =  BasicDBObject("key","prezzo_0")
        prezzo_0.setText(tbl.findOne(query)["value"].toString()+" €/h")
        query =  BasicDBObject("key","prezzo_1")
        prezzo_1.setText(tbl.findOne(query)["value"].toString()+" €/h")
        query =  BasicDBObject("key","prezzo_2")
        prezzo_2.setText(tbl.findOne(query)["value"].toString()+" €/h")
        acquistaTicket()
    }
      @RequiresApi(Build.VERSION_CODES.O)
      fun acquistaTicket(){
        qr_button.setOnClickListener {
            if (indexTariffa == -1 || indexPosto == -1) {
                Toast.makeText(this, "Seleziona posto e tariffa", Toast.LENGTH_SHORT).show()
            }
            else {
                var cod = indexTariffa.toString() + "-" + indexPosto.toString() //creo il codice qr
                var mongoClient = MongoClient("192.168.1.20", 27017)
                var db = mongoClient.getDB("Parcheggio")

                var tbl_a = db.getCollection("postiOccupati")
                var oggetto_a: DBObject = BasicDBObject("email", intent.getStringExtra("nomeUtente"))
                var posto_gia_occupato = tbl_a.findOne(oggetto_a)
                if (posto_gia_occupato == null) //se non ha gia un posto prosegui
                {
                    var tbl = db.getCollection("users")
                    val updatedDocument = BasicDBObject()
                    updatedDocument.append(
                        "\$set",
                        BasicDBObject().append("postoPrenotato", cod)
                    )
                    val queryUtente = BasicDBObject("email", intent.getStringExtra("nomeUtente"))
                    tbl.update(queryUtente, updatedDocument)

                    tbl = db.getCollection("postiOccupati")
                    val oggetto = BasicDBObject("email", intent.getStringExtra("nomeUtente"))
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                    //val datenow = LocalDateTime.parse(LocalDateTime.now().toString(), formatter)
                    oggetto.append("data", LocalDateTime.now().format(formatter))
                    oggetto.append("tipo", indexTariffa)
                    oggetto.append("numero", indexPosto)
                    tbl.insert(oggetto)
                    //tutto sto casino fino a mo è per assegnare all utente nel db il codice qr corrispondete al posto
                    val intent = Intent(this, QrCodeUserActivity::class.java)
                    //passo il codice all'attivita dove verrà generato il qr
                    intent.putExtra("codice", cod)
                    this.startActivity(intent)
                }else{
                    Toast.makeText(this, "Posto già prenotato", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun loadScrool(dim : Int){

        //Array contentente le label cliccabili dello scroll orizzontale
        val listScroll: ArrayList<TextView> = ArrayList()
        //Indica l'ultimo elemento cliccato


        val l = layout
        val horizontalScrollView = HorizontalScrollView(this)
        //setting height and width
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        horizontalScrollView.layoutParams = layoutParams

        val linearLayout = LinearLayout(this)
        val linearParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        linearLayout.layoutParams = linearParams

        horizontalScrollView.addView(linearLayout)
        for(i in 0..dim)
        {
            val t = TextView(this)
            t.text = "      " + i + "      "
            t.textSize = 30F
            t.setBackgroundColor(Color.TRANSPARENT)
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            t.layoutParams = params
            listScroll.add(t)
            t.setOnClickListener {
                indexPosto = i
                selezionaPosto(listScroll, i)

            }
            linearLayout.addView(t)
        }
        l?.addView(horizontalScrollView)

        val listOccupati: ArrayList<Int> = ArrayList()//devo assgnare i posti occupati per quel tipo di parcheggio
        var mongoClient = MongoClient("192.168.1.20", 27017)
        var db = mongoClient.getDB("Parcheggio")
        var tbl = db.getCollection("postiOccupati")

        var oggetto: DBObject = BasicDBObject("tipo", indexTariffa)
        for (cur in tbl.find(oggetto)) {
            listOccupati.add(cur["numero"] as Int)
        }

        postiOccupati(listOccupati, listScroll)
    }

    fun selezionaPosto(list : ArrayList<TextView>, index : Int){
        for(l in list) {
            val cd = l.getBackground() as ColorDrawable
            val colorCode = cd.color
            if ( colorCode != Color.RED) {
                l.setBackgroundColor(Color.TRANSPARENT)
            }
        }
        list[index].setBackgroundColor(R.drawable.backgroud_sfumatura_tariffe)
        //Toast.makeText(this, indexPosto.toString(), Toast.LENGTH_SHORT).show()
    }

    fun click_tariffa(){
        //0 diversamente abili 1 coperto 2 aperto
        //array botton per scegliere la tariffa
        val arrayTariffa = ArrayList<ImageView>()
        arrayTariffa.add(div_button)
        arrayTariffa.add(in_button)
        arrayTariffa.add(out_button)

        in_button.setOnClickListener {
            indexTariffa = 1
            selezionaTariffa(arrayTariffa, 1)
        }

        out_button.setOnClickListener {
            indexTariffa = 2
            selezionaTariffa(arrayTariffa, 2)
        }

        div_button.setOnClickListener {
            indexTariffa = 0
            selezionaTariffa(arrayTariffa, 0)
        }
    }

    fun selezionaTariffa(list : ArrayList<ImageView>, index : Int){
        //metto lo sfondo trasparente a tutti, tranne a quello cliccato che lo riporto con lo sfondo colorato
        val cd = in_button.getBackground() as ColorDrawable
        val colorCode = cd.color
        for(l in list)
        {
            l.setBackgroundColor(Color.TRANSPARENT)
        }
        list[index].setBackgroundColor(R.drawable.backgroud_sfumatura_tariffe)
        layout.removeAllViews() //rimuove il layer precedente altrimenti si accavallano
        loadScrool(20) //ricarico la lista per ogni tipo di parcheggio
    }

    fun postiOccupati(list: ArrayList<Int>, listPosti: ArrayList<TextView>){
        for(i in list){
            listPosti[i].setBackgroundColor(Color.RED)
            listPosti[i].isClickable = false
        }
    }
}

