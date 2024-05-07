package com.izontechnology.dcapp.utils

import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

open class UnZipUtil {
    fun unzip(zipFilePath: String?, unzipAtLocation: String?,progress: ((response: Int) -> Unit)? = null) {
        val archive = File(zipFilePath)
//        try {
            val total_len: Long = archive.length()
            var total_installed_len: Long = 0

            val zipfile = ZipFile(archive)
            val e: Enumeration<*> = zipfile.entries()
            while (e.hasMoreElements()) {
                val entry = e.nextElement() as ZipEntry
                unzipEntry(zipfile, entry, unzipAtLocation)
                total_installed_len += entry.getCompressedSize()
                val percent = (total_installed_len * 100 / total_len).toInt()
                progress?.invoke(percent)
            }
//        } catch (e: Exception) {
//            Log.e("Unzip zip", "Unzip exception", e)
//        }
    }

    @Throws(IOException::class)
    private fun unzipEntry(zipfile: ZipFile, entry: ZipEntry, outputDir: String?) {
        if (entry.isDirectory) {
            createDir(File(outputDir, entry.name))
            return
        }
        val outputFile = File(outputDir, entry.name)
        if (!outputFile.parentFile.exists()) {
            createDir(outputFile.parentFile)
        }
        Log.v("ZIP E", "Extracting: $entry")
        val zin = zipfile.getInputStream(entry)
        val inputStream = BufferedInputStream(zin)
        val outputStream = BufferedOutputStream(FileOutputStream(outputFile))
        try {

            //IOUtils.copy(inputStream, outputStream);
            try {
                var c = inputStream.read()
                while (c != -1) {
                    outputStream.write(c)
                    c = inputStream.read()
                }
            } finally {
                outputStream.close()
            }
        } finally {
            outputStream.close()
            inputStream.close()
        }
    }

    private fun createDir(dir: File) {
        if (dir.exists()) {
            return
        }
        Log.v("ZIP E", "Creating dir " + dir.name)
        if (!dir.mkdirs()) {
            throw RuntimeException("Can not create dir $dir")
        }
    }
}