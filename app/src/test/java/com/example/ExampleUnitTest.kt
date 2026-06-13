package com.example

import org.junit.Assert.*
import org.junit.Test
import java.net.URL
import java.net.HttpURLConnection
import java.io.File

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun fetchKeysList() {
    try {
      val url = URL("https://generate-key.gazacode.workers.dev/api/keys/list")
      val connection = url.openConnection() as HttpURLConnection
      connection.requestMethod = "GET"
      val responseText = connection.inputStream.bufferedReader().use { it.readText() }
      File("keys_list_output.txt").writeText(responseText)
    } catch (e: Exception) {
      File("keys_list_output.txt").writeText("Error: " + e.message + "\n" + e.stackTraceToString())
    }
  }
}
