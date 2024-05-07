package com.izontechnology.dcapp.utils

import java.io.File
import java.io.FileWriter
import java.io.IOException


fun loadJSONFromFile(filePath: String): String? {
    try {
        return File(filePath).bufferedReader().use { reader ->
            reader.readText()
        }
    } catch (e: Exception) {
        return null
    }

}

@Throws(IOException::class)
fun saveJSONFile(jsonString: String?, filePath: String) {
    try {
        val jsonFile = File(filePath)
        if (jsonFile.exists()) {
            jsonFile.deleteOnExit()
        }
        val writer = FileWriter(jsonFile)
        writer.write(jsonString)
        writer.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}