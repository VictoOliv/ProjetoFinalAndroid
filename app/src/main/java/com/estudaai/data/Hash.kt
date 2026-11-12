package com.estudaai.data

// A senha nunca é salva em texto puro: no banco fica só o hash SHA-256.
fun String.sha256(): String =
    java.security.MessageDigest.getInstance("SHA-256")
        .digest(toByteArray()).joinToString("") { "%02x".format(it) }
