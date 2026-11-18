package com.personal.expensemanager.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val FILENAME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

fun saveCsvToDownloads(context: Context, content: String): Uri? {
    val timestamp = LocalDateTime.now().format(FILENAME_FORMATTER)
    val fileName = "expenses_$timestamp.csv"

    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null

    resolver.openOutputStream(uri)?.use { outputStream ->
        outputStream.write(content.toByteArray(Charsets.UTF_8))
    } ?: return null

    return uri
}

fun buildShareIntent(uri: Uri): Intent = Intent(Intent.ACTION_VIEW).apply {
    setDataAndType(uri, "text/csv")
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
