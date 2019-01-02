package org.synknote.sync

import okhttp3.OkHttpClient

class HttpClient {
    companion object {
        public val Client = OkHttpClient()
    }
}