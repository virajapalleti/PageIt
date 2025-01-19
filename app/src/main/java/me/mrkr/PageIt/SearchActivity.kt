package me.mrkr.PageIt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SearchActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // SMS Receiver
        val br = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                for (sms in Telephony.Sms.Intents.getMessagesFromIntent(
                    p1
                )) {
                    val smsSender = sms.originatingAddress
                    val smsMessageBody = sms.displayMessageBody
                    if (smsMessageBody.contains("PageIt: ")) {
                        // TODO: LLM stuff and reply
                        Toast.makeText(
                            applicationContext,
                            "Received he SMS from $smsSender with content: $smsMessageBody",
                            Toast.LENGTH_LONG
                        ).show()
                        sendMessage(smsSender!!, "PageIt: $smsMessageBody - Manan")
                    }
                    Toast.makeText(
                        applicationContext,
                        "Received SMS from $smsSender with content: $smsMessageBody",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        registerReceiver(
            br,
            IntentFilter("android.provider.Telephony.SMS_RECEIVED"),
            RECEIVER_EXPORTED
        )

    }
    // when something is entered in enterTopic and enter is pressed, switch to HomeActivity and pass the topic entered


    override fun onStart() {
        super.onStart()

//        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
//        }
//        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
//        }
        if (checkSelfPermission(android.Manifest.permission.READ_SMS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_SMS), 1)
        }
        if (checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.RECEIVE_SMS), 2)
        }
        if (checkSelfPermission(android.Manifest.permission.SEND_SMS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.SEND_SMS), 3)
        }
        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), 4)
        }

        // check if all the permissions are granted
        if (checkSelfPermission(android.Manifest.permission.READ_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(android.Manifest.permission.SEND_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {

            // receive sms and pass the content to llm
//            val smsReceiver = SmsReceiver()

        }
    }

    fun sendMessage(phNo: String, content: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phNo, null, content, null, null)

    }

    fun enterTopic() {
        var topic = findViewById<EditText>(R.id.enterTopic).text.toString()
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("topic", topic)
        startActivity(intent)
    }

    // when user is done typing and presses enter on the keyboard, call enterTopic
    override fun onUserInteraction() {
        super.onUserInteraction()
        findViewById<EditText>(R.id.enterTopic).setOnEditorActionListener { _, _, _ ->
            enterTopic()
            true
        }
    }


}
