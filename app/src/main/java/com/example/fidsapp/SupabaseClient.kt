package com.example.fidsapp

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SupabaseClient {
    private var _client: SupabaseClient? = null

    val client: SupabaseClient
        get() {
            if (_client == null) {
                _client = createSupabaseClient(
                    supabaseUrl = BuildConfig.SUPABASE_URL,
                    supabaseKey = BuildConfig.SUPABASE_KEY
                ) {
                    install(Postgrest)
                }
            }
            return _client!!
        }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            _client?.close()
            _client = null
        }
    }
}
