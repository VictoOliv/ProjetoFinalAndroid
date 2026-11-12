package com.estudaai.userinterface

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatarData(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR")).format(Date(millis))

/** 90 -> "1h 30min", 45 -> "45min", 120 -> "2h". */
fun formatarDuracao(minutos: Int): String {
    val horas = minutos / 60
    val resto = minutos % 60
    return when {
        horas == 0 -> "${resto}min"
        resto == 0 -> "${horas}h"
        else -> "${horas}h ${resto}min"
    }
}
