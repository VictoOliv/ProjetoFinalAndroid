package com.estudaai.userinterface

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.estudaai.viewmodel.AuthViewModel

private data class Aba(val rota: Any, val titulo: String, val icone: ImageVector)

private val abas = listOf(
    Aba(Estudar, "Estudar", Icons.Filled.Home),
    Aba(Salvos, "Anotações", Icons.Filled.Edit),
    Aba(Horas, "Horas", Icons.Filled.DateRange),
    Aba(Perfil, "Perfil", Icons.Filled.Person)
)

private fun NavDestination?.estaEm(rota: Any): Boolean =
    this?.hierarchy?.any { it.hasRoute(rota::class) } == true

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EstudaAiNavegacao(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val usuarioId by authViewModel.usuarioId.collectAsStateWithLifecycle()

    // Sessão salva: quem já entrou abre direto no app.
    val inicio: Any = remember { if (authViewModel.usuarioId.value != null) Estudar else Login }

    val entradaAtual by navController.currentBackStackEntryAsState()
    val destinoAtual = entradaAtual?.destination
    val mostrarBarra = abas.any { destinoAtual.estaEm(it.rota) }

    // Só quem entrou vê o app: entrar/cadastrar leva às abas, sair volta ao login.
    LaunchedEffect(usuarioId) {
        val destino = navController.currentDestination ?: return@LaunchedEffect
        val naTelaDeAcesso = destino.estaEm(Login) || destino.estaEm(Cadastro)
        if (usuarioId != null && naTelaDeAcesso) {
            navController.navigate(Estudar) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        } else if (usuarioId == null && !naTelaDeAcesso) {
            navController.navigate(Login) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar {
                    abas.forEach { aba ->
                        NavigationBarItem(
                            selected = destinoAtual.estaEm(aba.rota),
                            onClick = {
                                navController.navigate(aba.rota) {
                                    popUpTo(Estudar) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(aba.icone, contentDescription = aba.titulo) },
                            label = { Text(aba.titulo) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = inicio,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            composable<Login> {
                LoginScreen(
                    viewModel = authViewModel,
                    onIrParaCadastro = { navController.navigate(Cadastro) }
                )
            }
            composable<Cadastro> {
                CadastroScreen(
                    viewModel = authViewModel,
                    onIrParaLogin = { navController.popBackStack() }
                )
            }
            composable<Estudar> {
                EstudarScreen()
            }
            composable<Salvos> {
                SalvosScreen(onAbrirAnotacao = { id -> navController.navigate(Detalhe(id)) })
            }
            composable<Horas> {
                HorasScreen()
            }
            composable<Perfil> {
                PerfilScreen(authViewModel = authViewModel)
            }
            composable<Detalhe> { entrada ->
                val detalhe: Detalhe = entrada.toRoute()
                DetalheScreen(
                    anotacaoId = detalhe.anotacaoId,
                    onVoltar = { navController.popBackStack() }
                )
            }
        }
    }
}
