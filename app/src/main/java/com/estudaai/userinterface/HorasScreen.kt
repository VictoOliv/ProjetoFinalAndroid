package com.estudaai.userinterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.estudaai.data.RegistroEstudo
import com.estudaai.viewmodel.HorasViewModel
import com.estudaai.viewmodel.ResumoHoras

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HorasScreen(viewModel: HorasViewModel = viewModel()) {
    val registros by viewModel.registros.collectAsStateWithLifecycle()
    val resumo by viewModel.resumo.collectAsStateWithLifecycle()
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    var materia by rememberSaveable { mutableStateOf("") }
    var horas by rememberSaveable { mutableStateOf("") }
    var minutos by rememberSaveable { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Horas de estudo") }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { CartaoResumo(resumo) }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Registrar estudo", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = materia,
                            onValueChange = { materia = it },
                            label = { Text("Matéria") },
                            placeholder = { Text("Ex.: Matemática") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = horas,
                                onValueChange = { novo -> horas = novo.filter(Char::isDigit).take(2) },
                                label = { Text("Horas") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = minutos,
                                onValueChange = { novo -> minutos = novo.filter(Char::isDigit).take(3) },
                                label = { Text("Minutos") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        estado.erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        Button(
                            onClick = {
                                viewModel.registrar(materia, horas, minutos) {
                                    materia = ""
                                    horas = ""
                                    minutos = ""
                                }
                            },
                            enabled = !estado.salvando,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (estado.salvando) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Registrar")
                            }
                        }
                    }
                }
            }

            item { Text("Histórico", style = MaterialTheme.typography.titleMedium) }

            val lista = registros
            when {
                lista == null -> item { CircularProgressIndicator() }
                lista.isEmpty() -> item {
                    Text(
                        "Nenhum estudo registrado ainda.",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                else -> items(lista, key = { it.id }) { registro ->
                    LinhaRegistro(registro, onExcluir = { viewModel.excluir(registro.id) })
                }
            }
        }
    }
}

@Composable
fun CartaoResumo(resumo: ResumoHoras) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp)) {
            ValorResumo("Total estudado", formatarDuracao(resumo.totalMinutos), Modifier.weight(1f))
            ValorResumo("Últimos 7 dias", formatarDuracao(resumo.ultimos7DiasMinutos), Modifier.weight(1f))
            ValorResumo("Sessões", "${resumo.sessoes}", Modifier.weight(1f))
        }
    }
}

@Composable
private fun ValorResumo(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun LinhaRegistro(registro: RegistroEstudo, onExcluir: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(registro.materia, style = MaterialTheme.typography.titleSmall)
                Text(
                    formatarData(registro.data),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                formatarDuracao(registro.minutos),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onExcluir) {
                Icon(Icons.Filled.Delete, contentDescription = "Excluir registro")
            }
        }
    }
}
