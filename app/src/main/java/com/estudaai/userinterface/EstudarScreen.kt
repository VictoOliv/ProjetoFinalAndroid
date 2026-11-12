package com.estudaai.userinterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.estudaai.viewmodel.Autor
import com.estudaai.viewmodel.EstudoViewModel
import com.estudaai.viewmodel.Mensagem
import com.estudaai.viewmodel.StatusIA

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EstudarScreen(viewModel: EstudoViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var pergunta by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    val mostrarBoasVindas = estado.mensagens.isEmpty() && estado.status !is StatusIA.Erro
    val temItemDeStatus = estado.status == StatusIA.Carregando || estado.status is StatusIA.Erro
    val totalItens = (if (mostrarBoasVindas) 1 else 0) + estado.mensagens.size + (if (temItemDeStatus) 1 else 0)

    // Mantém a última mensagem (ou o status) visível.
    LaunchedEffect(totalItens) {
        if (totalItens > 0) listState.animateScrollToItem(totalItens - 1)
    }

    fun enviar() {
        if (pergunta.isNotBlank() && estado.status != StatusIA.Carregando) {
            viewModel.enviar(pergunta)
            pergunta = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estudar com IA") },
                actions = {
                    if (estado.mensagens.isNotEmpty()) {
                        TextButton(onClick = viewModel::novaConversa) { Text("Nova conversa") }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
        ) {
            OutlinedTextField(
                value = estado.tema,
                onValueChange = viewModel::atualizarTema,
                label = { Text("Matéria ou contexto do estudo") },
                placeholder = { Text("Ex.: Biologia - fotossíntese") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (mostrarBoasVindas) {
                    item { BoasVindas() }
                }
                items(estado.mensagens, key = { it.id }) { mensagem ->
                    BalaoMensagem(mensagem, onSalvar = { viewModel.salvar(mensagem) })
                }
                when (val status = estado.status) {
                    StatusIA.Carregando -> item { IndicadorCarregando() }
                    is StatusIA.Erro -> item {
                        CartaoErro(status.mensagem, onTentarNovamente = viewModel::tentarNovamente)
                    }
                    else -> Unit
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = pergunta,
                    onValueChange = { pergunta = it },
                    placeholder = { Text("Digite sua dúvida...") },
                    maxLines = 4,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = ::enviar,
                    enabled = pergunta.isNotBlank() && estado.status != StatusIA.Carregando
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}

@Composable
private fun BoasVindas() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Olá! Sou seu tutor de estudos.", style = MaterialTheme.typography.titleMedium)
            Text("1. Escreva acima a matéria ou o assunto que está estudando.")
            Text("2. Mande sua dúvida no campo de baixo.")
            Text("3. Gostou da explicação? Toque em Salvar para guardar na aba Anotações.")
        }
    }
}

@Composable
private fun BalaoMensagem(mensagem: Mensagem, onSalvar: () -> Unit) {
    val doAluno = mensagem.autor == Autor.ALUNO
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (doAluno) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (doAluno) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                if (!doAluno) {
                    Text(
                        "Tutor IA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(mensagem.texto, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (!doAluno) {
            TextButton(onClick = onSalvar, enabled = !mensagem.salva) {
                Icon(
                    if (mensagem.salva) Icons.Filled.Check else Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(if (mensagem.salva) "Salvo" else "Salvar")
            }
        }
    }
}

@Composable
private fun IndicadorCarregando() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
        Spacer(Modifier.width(12.dp))
        Text("A IA está pensando...", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CartaoErro(mensagem: String, onTentarNovamente: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Não foi possível obter a resposta",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(mensagem, color = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.size(8.dp))
            Button(onClick = onTentarNovamente) { Text("Tentar novamente") }
        }
    }
}
