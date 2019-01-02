package org.synknote.sync

import android.content.Context
import okhttp3.FormBody
import okhttp3.Request
import org.synknote.misc.getDefaultPref
import java.io.IOException
import android.os.StrictMode
import android.widget.Toast
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import org.synknote.misc.SyncReturnType
import org.synknote.models.NoteReturn
import org.synknote.models.NoteSkeleton
import org.synknote.preferences.PrefGroup
import org.synknote.preferences.PrefManager
import java.lang.reflect.Type


class SyncManager(context: Context) {
    private var _lastResult = ""
    private var _isClientError = false
    private val _context = context

    init {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        //if (!getDefaultPref(context).contains("sync_server_address")) {
            getDefaultPref(context).edit().putString("sync_server_address", "192.168.1.224").apply()
        //}
    }

    private fun makeRequest(request: Request, type: SyncReturnType = SyncReturnType.MAP): Any {
        return try {
            val response = HttpClient.Client.newCall(request).execute()
            val responseBody = response.body()!!.string()
            _lastResult = responseBody
            _isClientError = false

            if (resultIsError() || responseBody.isNullOrEmpty()) {
                if (responseBody.isNullOrEmpty()) _isClientError = true
                val errorCode = getErrorCode()

                Toast.makeText(_context, errorCode.name, Toast.LENGTH_SHORT).show()
                mutableMapOf(Pair("error", errorCode))
            } else {
                getResult(type, responseBody)
            }
        } catch (e: IOException) {
            _isClientError = true
            Toast.makeText(_context, e.toString(), Toast.LENGTH_SHORT).show()
            mutableMapOf(Pair("error", e.toString()))
        }
    }

    private fun getResult(type: SyncReturnType, responseBody: String): Any {
        return when (type) {
            SyncReturnType.NOTERETURN -> {
                val result = GsonBuilder().create().fromJson(responseBody, NoteReturn::class.java)
                PrefManager(_context, PrefGroup.Sync).setString("token",
                        result.token)

                result
            }
            else -> {
                val result = GsonBuilder().create().fromJson(responseBody, Map::class.java)
                if (result.containsKey("token")) {
                    if (result["token"] != null)
                        PrefManager(_context, PrefGroup.Sync).setString("token",
                                result["token"].toString())
                }

                result
            }
        }

    }

    private fun getUrl(extra: String): HttpUrl {
        val host = getDefaultPref(_context).getString("sync_server_address", null)
        return HttpUrl.parse("http://$host:5000/$extra")!!
    }

    fun register(email: String, password: String): Map<*, *> {
        val formBody = FormBody.Builder()
                .add("email",    email)
                .add("password", password)
                .build()
        val request = Request.Builder()
                .url(getUrl("api/user"))
                .post(formBody)
                .build()

        return makeRequest(request) as Map<*, *>
    }

    fun login(email: String, password: String): Map<*, *> {
        val formBody = FormBody.Builder()
                .add("email",    email)
                .add("password", password)
                .build()
        val request = Request.Builder()
                .url(getUrl("api/login"))
                .post(formBody)
                .build()

        return makeRequest(request) as Map<*, *>
    }

    fun createNotebook(userId: String, token: String, name: String): Map<*, *> {
        val formBody = FormBody.Builder()
                .add("userId", userId)
                .add("token",  token)
                .add("name",   name)
                .build()
        val request = Request.Builder()
                .url(getUrl("api/notebook"))
                .post(formBody)
                .build()

        return makeRequest(request) as Map<*, *>
    }

    fun getAllNotebooks(userId: String, token: String): Map<*, *> {
        val httpUrl = HttpUrl.parse(getUrl("api/notebook").toString())!!.newBuilder()
                .addQueryParameter("userId", userId)
                .addQueryParameter("token", token)
                .build()
        val request = Request.Builder()
                .url(httpUrl)
                .build()

        return makeRequest(request) as Map<*, *>
    }

    fun createNote(userId: String, token: String, location: String, notebookId: String): Map<*, *> {
        val formBody = FormBody.Builder()
                .add("userId",     userId)
                .add("token",      token)
                .add("location",   location)
                .add("notebookId", notebookId)
                .build()
        val request = Request.Builder()
                .url(getUrl("api/note"))
                .post(formBody)
                .build()

        return makeRequest(request) as Map<*, *>
    }

    fun getNote(userId: String, token: String, noteId: String): Map<*, *> {
        val httpUrl = HttpUrl.parse(getUrl("api/note/$noteId").toString())!!.newBuilder()
                .addQueryParameter("userId", userId)
                .addQueryParameter("token", token)
                .build()
        val request = Request.Builder()
                .url(httpUrl)
                .build()

        return makeRequest(request) as Map<*, *>
    }

    fun getAllNotebookNotes(userId: String, token: String, notebookId: String): NoteReturn {
        val httpUrl = HttpUrl.parse(getUrl("api/notebook/$notebookId").toString())!!.newBuilder()
                .addQueryParameter("userId", userId)
                .addQueryParameter("token", token)
                .build()
        val request = Request.Builder()
                .url(httpUrl)
                .build()

        return makeRequest(request, SyncReturnType.NOTERETURN) as NoteReturn
    }

    fun editNote(userId: String, token: String, noteId: String, content: String): Map<*, *> {
        val formBody = FormBody.Builder()
                .add("userId",     userId)
                .add("token",       token)
                .add("content",   content)
                .build()
        val request = Request.Builder()
                .url(getUrl("api/note/$noteId/edit"))
                .post(formBody)
                .build()

        return makeRequest(request) as Map<*, *>
    }

    fun getErrorCode(): ReturnCode {
        val errorCode: Int
        if (_isClientError) return ReturnCode.ClientError
        else errorCode = _lastResult.substring(8).toInt()

        return ReturnCode.values()[errorCode]
    }

    fun resultIsError(): Boolean {
        return _lastResult.startsWith("[Error] ") || _isClientError
    }
}