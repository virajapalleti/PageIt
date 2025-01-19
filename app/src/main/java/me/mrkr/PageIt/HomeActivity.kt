/*
package me.mrkr.PageIt

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.font.FontLoadingStrategy.Companion.Async
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Async

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val topic = intent.getStringExtra("topic")

        findViewById<ImageButton>(R.id.backBtn).setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.topicName).text = topic.toString()
        findViewById<TextView>(R.id.content).text = "Loading..."

        // send to llm


    }

    override fun onStart() {
        super.onStart()
        val topic = intent.getStringExtra("topic")
        val llm = LLMTask(this, topic.toString())
        GlobalScope.launch(Dispatchers.IO) { findViewById<TextView>(R.id.content).text = llm.getResponse("PageIt: $topic") }

    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

}

*/



//package me.mrkr.PageIt
//
//import android.content.Intent
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.widget.ImageButton
//import android.widget.TextView
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//
//class HomeActivity : AppCompatActivity() {
//    private var llmTask: LLMTask? = null
//    private lateinit var contentTextView: TextView
//    private val mainHandler = Handler(Looper.getMainLooper())
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_home)
//
//        setupWindowInsets()
//        setupViews()
//        initializeLLM()
//    }
//
//    private fun setupWindowInsets() {
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
//
//    private fun setupViews() {
//        val topic = intent.getStringExtra("topic") ?: ""
//
//        findViewById<ImageButton>(R.id.backBtn).setOnClickListener {
//            startActivity(Intent(this, SearchActivity::class.java))
//        }
//
//        findViewById<TextView>(R.id.topicName).text = topic
//        contentTextView = findViewById(R.id.content)
//        contentTextView.text = "Initializing..."
//    }
//
//    private fun initializeLLM() {
//        val topic = intent.getStringExtra("topic") ?: ""
//
//        LLMTask.initialize(applicationContext, topic,
//            onInitialized = { initializedLLM ->
//                llmTask = initializedLLM
//                generateResponse(topic)
//            },
//            onError = { error ->
//                mainHandler.post {
//                    contentTextView.text = "Initialization Error: ${error.message}"
//                }
//            }
//        )
//    }
//
//    private fun generateResponse(topic: String) {
//        contentTextView.text = "Generating response..."
//        llmTask?.getResponseAsync("PageIt: $topic",
//            onSuccess = { response ->
//                mainHandler.post {
//                    contentTextView.text = response
//                }
//            },
//            onError = { error ->
//                mainHandler.post {
//                    contentTextView.text = "Error: ${error.message}"
//                }
//            }
//        )
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        llmTask?.cleanup()
//        finish()
//    }
//}

package me.mrkr.PageIt

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {
    private var llmTask: LLMTask? = null
    private lateinit var contentTextView: TextView
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        setupWindowInsets()
        setupViews()
        initializeLLM()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupViews() {
        val topic = intent.getStringExtra("topic") ?: ""

        findViewById<ImageButton>(R.id.backBtn).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        findViewById<TextView>(R.id.topicName).text = topic
        contentTextView = findViewById(R.id.content)
        contentTextView.text = "Preparing..."
    }

    private fun initializeLLM() {
        val topic = intent.getStringExtra("topic") ?: ""

        // Show immediate feedback
        contentTextView.text = "Loading model..."

        LLMTask.initialize(applicationContext, topic,
            onInitialized = { initializedLLM ->
                llmTask = initializedLLM
                generateResponse(topic)
            },
            onError = { error ->
                mainHandler.post {
                    contentTextView.text = "Error: ${error.message}"
                }
            }
        )
    }

    private fun generateResponse(topic: String) {
        contentTextView.text = "Generating..."
        llmTask?.getResponseAsync("PageIt: $topic",
            onSuccess = { response ->
                mainHandler.post {
                    contentTextView.text = response
                }
            },
            onError = { error ->
                mainHandler.post {
                    contentTextView.text = "Error: ${error.message}"
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        llmTask?.cleanup()
        finish()
    }
}