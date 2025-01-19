/*
package me.mrkr.PageIt

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions

class LLMTask(context: Context, topic: String) {
    val options = LlmInferenceOptions.builder()
        .setModelPath("/data/local/tmp/llm/model.bin")
        .setMaxTokens(3000)
        .setTopK(40)
        .setTemperature(0.8F)
        .setRandomSeed(42)
        .build()

    val llmInference = LlmInference.createFromOptions(context, options)

    fun getResponse(prompt: String): String {
        return llmInference.generateResponse(prompt)
    }

}
*/


//package me.mrkr.PageIt
//
//import android.content.Context
//import com.google.mediapipe.tasks.genai.llminference.LlmInference
//import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//class LLMTask private constructor(
//    private val llmInference: LlmInference,
//    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
//) {
//    companion object {
//        fun initialize(
//            context: Context,
//            topic: String,
//            onInitialized: (LLMTask) -> Unit,
//            onError: (Exception) -> Unit
//        ) {
//            // Use a separate thread for initialization
//            Thread {
//                try {
//                    val options = LlmInferenceOptions.builder()
//                        .setModelPath("/data/local/tmp/llm/model.bin")
//                        .setMaxTokens(64)
//                        .setTopK(40)
//                        .setTemperature(0.8F)
//                        .setRandomSeed(42)
//                        .build()
//
//                    val llmInference = LlmInference.createFromOptions(context, options)
//                    val llmTask = LLMTask(llmInference)
//                    onInitialized(llmTask)
//                } catch (e: Exception) {
//                    onError(e)
//                }
//            }.start()
//        }
//    }
//
//    fun getResponseAsync(
//        prompt: String,
//        onSuccess: (String) -> Unit,
//        onError: (Exception) -> Unit
//    ) {
//        executor.execute {
//            try {
//                val response = llmInference.generateResponse(prompt)
//                onSuccess(response)
//            } catch (e: Exception) {
//                onError(e)
//            }
//        }
//    }
//
//    fun cleanup() {
//        executor.shutdown()
//        llmInference.close()
//    }
//}


package me.mrkr.PageIt

import android.content.Context
import android.util.LruCache
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LLMTask private constructor(
    private val llmInference: LlmInference,
    private val executor: ExecutorService = Executors.newFixedThreadPool(2)  // Increased thread pool
) {
    // Cache for storing responses
    private val responseCache = LruCache<String, String>(50)  // Cache last 50 responses

    companion object {
        @Volatile private var instance: LLMTask? = null
        private val INSTANCE_LOCK = Any()

        fun initialize(
            context: Context,
            topic: String,
            onInitialized: (LLMTask) -> Unit,
            onError: (Exception) -> Unit
        ) {
            // Check if instance already exists
            instance?.let {
                onInitialized(it)
                return
            }

            Thread {
                try {
                    synchronized(INSTANCE_LOCK) {
                        // Double-check instance
                        instance?.let {
                            onInitialized(it)
                            return@synchronized
                        }

                        val options = LlmInferenceOptions.builder()
                            .setModelPath("/data/local/tmp/llm/model.bin")
                            .setMaxTokens(256)  // Reduced max tokens
                            .setTopK(20)         // Reduced top K
                            .setTemperature(0.7F) // Slightly reduced temperature
                            .setRandomSeed(42)
                            .build()

                        val llmInference = LlmInference.createFromOptions(context, options)
                        val llmTask = LLMTask(llmInference)
                        instance = llmTask
                        onInitialized(llmTask)
                    }
                } catch (e: Exception) {
                    onError(e)
                }
            }.apply {
                priority = Thread.MAX_PRIORITY  // Give initialization high priority
            }.start()
        }
    }

    fun getResponseAsync(
        prompt: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Check cache first
        responseCache.get(prompt)?.let {
            onSuccess(it)
            return
        }

        executor.execute {
            try {
                val response = llmInference.generateResponse(prompt)
                // Cache the response
                responseCache.put(prompt, response)
                onSuccess(response)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun cleanup() {
        executor.shutdown()
        llmInference.close()
        responseCache.evictAll()
        instance = null
    }
}