package com.example.fidsapp

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object FileUtil {
    fun getFileSize(context: Context, uri: Uri): Long {
        var size: Long = 0
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1 && cursor.moveToFirst()) {
                size = cursor.getLong(sizeIndex)
            }
        }
        return size
    }

    fun isFileSizeValid(context: Context, uri: Uri, limitInMb: Int): Boolean {
        val sizeInBytes = getFileSize(context, uri)
        val limitInBytes = limitInMb * 1024 * 1024
        return sizeInBytes <= limitInBytes
    }
}
