package com.phuoc.ho.publicapi

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.io.path.Path

class CryptoHelper {
  companion object {
    fun publickey(): String {
      return read("public_key.pem")
    }

    private fun read(file: String): String {
      val path = Path("public-api", file).let { if (it.toFile().exists()) it else Path("..", file) }
      return Files.readAllLines(path, StandardCharsets.UTF_8).joinToString("\n")
    }

    fun privatekey(): String {
      return read("private_key.pem")
    }
  }
}