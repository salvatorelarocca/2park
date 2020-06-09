package com.example.a2park

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient
import kotlinx.android.synthetic.main.qrcode.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class QrCodeUserActivity() : AppCompatActivity() {
    var data : String = ""
    var diff_h : Int = 0
    var tariffa_ora : Float = 0.0F
    var prezzoBase : Float = 0.0F
    var cod : String = ""
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qrcode)
        generaQr()
        calcolaPrezzo()
        timer()
    }

    fun generaQr(){
        var intent = intent
        cod = intent.getStringExtra("codice")
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.encodeBitmap(cod, BarcodeFormat.QR_CODE, 800, 800)
        qr_image.setImageBitmap(bitmap)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    fun calcolaPrezzo(){
        val strTariffaCodice = cod.take(1)


        var mongoClient = MongoClient("192.168.1.20", 27017)
        var db = mongoClient.getDB("Parcheggio")
        var tbl = db.getCollection("prezzi")
        var oggetto : DBObject = BasicDBObject("key", "prezzo_$strTariffaCodice")


        tariffa_ora = (tbl.findOne(oggetto)["value"].toString().toFloat())


        tbl = db.getCollection("users")
        var oggetto1 : DBObject = BasicDBObject("postoPrenotato",  cod)
        var emailUser = tbl.findOne(oggetto1)["email"].toString()
        tbl = db.getCollection("postiOccupati")
        oggetto = BasicDBObject("email", emailUser)
        data = tbl.findOne(oggetto)["data"].toString() //EEE MMM dd HH:mm:ss zzz yyyy
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(data, formatter)
        //differenza moltiplico e la faccio vedere sulla label prezzoTextView
        diff_h = LocalDateTime.now().hour - dateTime.hour
        prezzoBase = ( diff_h * tariffa_ora ) + tariffa_ora
        prezzoTextView.text = "$prezzoBase $"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun timer(){
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(data, formatter)
        val start = (60 - LocalDateTime.now().minute + dateTime.minute) * 60 * 1000

        object : CountDownTimer(start.toLong(), 1000){

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                val prz_aggiornato = prezzoBase + tariffa_ora
                prezzoTextView.text = "$prz_aggiornato $"
            }

            @SuppressLint("SetTextI18n")
            override fun onTick(p: Long) {
                var diff = p
                val secondsInMilli: Long = 1000
                val minutesInMilli = secondsInMilli * 60
                val hoursInMilli = minutesInMilli * 60

                val elapsedHours = diff / hoursInMilli
                diff %= hoursInMilli
                val elapsedMinutes = diff / minutesInMilli
                diff %= minutesInMilli
                val elapsedSeconds = diff / secondsInMilli
                textTimer.text = "$elapsedMinutes : $elapsedSeconds"
            }
        }.start()
    }

}