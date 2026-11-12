package com.estudaai.userinterface

import kotlinx.serialization.Serializable

// Rotas type-safe do Navigation Compose.

@Serializable data object Login
@Serializable data object Cadastro

// Abas da barra inferior
@Serializable data object Estudar
@Serializable data object Salvos
@Serializable data object Horas
@Serializable data object Perfil

// Tela de detalhe: recebe o id da anotação escolhida na lista.
@Serializable data class Detalhe(val anotacaoId: Long)
