package com.example.sproj

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sproj.ui.theme.SPROJTheme
import com.example.sproj.util.FileHandler
import java.io.File
import java.io.FileInputStream

class OCRActivity : ComponentActivity() {

    private lateinit var files: MutableList<File>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SPROJTheme {

                FileListView(files) {
                    val result = FileHandler().deleteFile(it)
                    if (result) {
                        Toast.makeText(
                            applicationContext,
                            "File Deleted: ${it.name}",
                            Toast.LENGTH_LONG
                        ).show()
                        files.remove(it)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Failed to delete File: ${it.name}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        loadFiles()
    }

    private fun loadFiles() {
        files = FileHandler().getFilesInDirectory(filesDir).toMutableList()
    }

    companion object {
        private const val TAG = "OCRActivity"
    }
}

@Composable
fun FileListView(files: List<File>, onClick: (File) -> Unit) {
    LazyColumn {
        items(files.size) { index ->
            val fileContent = remember(files[index]) {
                String(FileInputStream(files[index]).readBytes())
            }
            FileItem(files[index].name, fileContent) {
                onClick(files[index])
            }
        }
    }
}

@Composable
fun FileItem(fileName: String, fileContent: String, onClick: () -> Unit) {
    Column {
        Divider(color = Color.Black, thickness = 1.dp)
        Row(Modifier.fillMaxWidth()) {
            Text("${fileName}\n")
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null, // Handle accessibility properly
                    tint = Color.Red // Tint color for the icon
                )
            }

        }
        Text(fileContent)
    }
}
