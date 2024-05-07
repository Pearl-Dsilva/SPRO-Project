package com.example.sproj

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.sproj.ui.theme.SPROJTheme
import com.example.sproj.util.FileHandler
import com.example.sproj.util.TextProcessor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : ComponentActivity() {

    private lateinit var recognizer: TextRecognizer
    private lateinit var questions: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val options = GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setGalleryImportAllowed(true)
            .setPageLimit(5)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .build()
        val scanner = GmsDocumentScanning.getClient(options)

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        questions = MutableList(0) { "" }

        setContent {
            SPROJTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val imageuris =
                        mutableListOf<Uri>()

                    val scannerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartIntentSenderForResult(),
                        onResult = {
                            CoroutineScope(Dispatchers.IO).launch {
                                processContent(it, imageuris)
                            }
                        }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        imageuris.forEach { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Button(onClick = {
                            scanner.getStartScanIntent(this@MainActivity)
                                .addOnSuccessListener {
                                    scannerLauncher.launch(
                                        IntentSenderRequest.Builder(it).build()
                                    )
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        applicationContext,
                                        it.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }) {
                            Text(text = "Scan PDF")
                        }

                        Button(onClick = {
                            startActivity(Intent(this@MainActivity, OCRActivity::class.java))
                        }) {
                            Text(text = "list Files")
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    private fun processContent(it: ActivityResult, imageuris: MutableList<Uri>) {
        if (it.resultCode == RESULT_OK) {
            val result =
                GmsDocumentScanningResult.fromActivityResultIntent(it.data)
            imageuris.clear()
            imageuris.addAll(result?.pages?.map { it.imageUri } ?: emptyList())

            if (imageuris.isNotEmpty()) {
                for (imageUri in imageuris) {
                    val image =
                        InputImage.fromFilePath(applicationContext, imageUri)

                    val res = recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            Toast.makeText(
                                applicationContext,
                                visionText.text,
                                Toast.LENGTH_LONG
                            ).show()

                            questions = TextProcessor().processText(visionText.text).toMutableList()

                            FileHandler().storeAsText(
                                questions,
                                "${Date().time}.txt",
                                filesDir
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "Text extracted is: NUll")
                            Toast.makeText(
                                applicationContext,
                                "Failed to load text",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            } else {
                Log.d(TAG, "No Text Extracted as no images found")
            }
        }
    }

}

