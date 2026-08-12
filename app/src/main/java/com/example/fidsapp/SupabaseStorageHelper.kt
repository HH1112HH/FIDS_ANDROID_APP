package com.example.fidsapp

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object SupabaseStorageHelper {
    
    suspend fun uploadImage(context: Context, bucketName: String, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Đọc ByteArray từ Uri (Quan trọng để tránh lỗi metadata)
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@withContext null
                inputStream.close()

                // 2. Tạo tên file duy nhất
                val fileName = "${UUID.randomUUID()}.jpg"

                // 3. Upload lên Supabase Storage
                val bucket = SupabaseClient.client.storage.from(bucketName)
                bucket.upload(fileName, bytes) {
                    upsert = true
                }

                // 4. Lấy URL công khai
                bucket.publicUrl(fileName)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
