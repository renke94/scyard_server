package server

import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset

object IO {
    fun readJSON(path: String): JSONObject {
        val text = File(path).readText(Charset.defaultCharset())
        return JSONObject(text)
    }
}