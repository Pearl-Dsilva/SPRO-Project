package com.example.sproj.util

import java.io.File
import java.io.FileInputStream


class FileHandler {
    fun readFile(filesDir: File, filename: String): String {
        return String(FileInputStream(File(filesDir, filename)).readBytes())
    }

    fun writeFile() {}

    fun getFilesInDirectory(directory: File): List<File> {
        val fileList = mutableListOf<File>()

        // Check if the provided path is a directory
        if (directory.isDirectory) {
            // List all files in the directory
            val files = directory.listFiles()

            // Add the names of all files to the list
            files?.forEach { file ->
                if (isText(file.name))
                    fileList.add(file)
            }
        } else {
            println("Provided path is not a directory.")
        }

        return fileList
    }

    private fun isText(fileName: String): Boolean {
        // Split the file name based on the dot
        val parts = fileName.split(".")

        // Get the last part (suffix) of the file name
        val suffix = parts.lastOrNull()


        // Check if the suffix is "pdf" (case-insensitive)
        return suffix.equals("txt", ignoreCase = true)
    }


    fun storeAsText(questions: List<String>, filename: String, filesDir: File) {
        val file = File(filesDir, filename)
        file.bufferedWriter().use { out ->
            questions.forEach { question ->
                out.write(question)
                out.newLine() // Add a newline after each question
            }
        }
    }

    fun deleteFile(file: File): Boolean {
        return file.delete()
    }


}