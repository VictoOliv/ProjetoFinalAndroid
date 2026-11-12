# EstudaAI
App de estudos em que um tutor com inteligência artificial tira dúvidas com base na matéria informada e na conversa anterior. O aluno salva as explicações, escreve as próprias anotações e registra as horas estudadas, tudo na sua conta.

## API utilizada
**NVIDIA NIM (API de chat da NVIDIA)**: `https://integrate.api.nvidia.com/v1/`

Usamos o endpoint `POST chat/completions` (formato compatível com OpenAI), enviando:
- uma instrução de sistema com a matéria/contexto que o aluno digitou;
- as últimas mensagens da conversa (para a IA entender o contexto);
- a pergunta atual.

O modelo principal é `poolside/laguna-xs-2.1`. Se ele estiver sobrecarregado ou fora do ar, o app tenta automaticamente o modelo reserva `nvidia/nemotron-3.5-lightning-30b-a3b`. A resposta (`choices[0].message.content`) aparece na tela com estados de **carregando**, **sucesso** e **erro** (sem internet, chave inválida, tempo esgotado etc.).

> Esta API exige uma chave gratuita (obtida em https://build.nvidia.com). Por isso o uso dela precisa ser aprovado pelo professor, conforme o enunciado.

## Prints
<!-- Coloque as imagens na pasta prints/ com estes nomes -->
| Login | Cadastro | Estudar (IA) |
|-------|----------|--------------|
| ![Login](prints/login.png) | ![Cadastro](prints/cadastro.png) | ![Estudar](prints/estudar.png) |

| Salvos (lista) | Detalhe | Estado de erro |
|----------------|---------|----------------|
| ![Salvos](prints/salvos.png) | ![Detalhe](prints/detalhe.png) | ![Erro](prints/erro.png) |

## Divisão de tarefas
| Integrante | O que fez |
|------------|-----------|
| Nome 1 | `network` e `data`: Retrofit, Room (Entity, Dao, Database) e Repository |
| Nome 2 | `viewmodel` e `userinterface`: telas Compose e navegação |
| Nome 3 | Login, cadastro e sessão (UsuarioRepository, SessaoManager, telas de acesso) |

## O que aprendemos
- **Nome 1:** ...
- **Nome 2:** ...
- **Nome 3:** ...

---

## Como rodar o projeto

1. Abra a pasta do projeto no **Android Studio** (Ladybug ou mais novo) e espere o Gradle sincronizar.
   O `local.properties` com o caminho do SDK é criado automaticamente pelo Android Studio.
2. Crie uma chave gratuita em https://build.nvidia.com (botão *Get API Key*).
3. Adicione a chave no arquivo `local.properties` (na raiz do projeto, ele **não** vai para o Git):
   ```properties
   NVIDIA_API_KEY=nvapi-sua-chave-aqui
   # opcional: trocar os modelos
   # NVIDIA_MODEL=poolside/laguna-xs-2.1
   # NVIDIA_MODEL_RESERVA=nvidia/nemotron-3.5-lightning-30b-a3b
   ```
   Também funciona com uma variável de ambiente `NVIDIA_API_KEY`.
4. Clique em **Sync Project with Gradle Files** e rode o app no emulador.

Sem a chave, o app abre normalmente e a aba Estudar mostra o estado de erro avisando que a chave não está configurada.

> **Atenção:** não coloque o projeto em um drive de rede (ex.: `H:` mapeado). O Gradle não consegue gerar os arquivos de build nesse tipo de pasta. Use uma pasta local, como `C:\Users\<você>\AndroidStudioProjects`.

### Requisitos técnicos atendidos
| Requisito | Onde está |
|-----------|-----------|
| API REST com Retrofit + coroutines | `network/NvidiaApi.kt`, `network/RetrofitInstance.kt`, `data/EstudoRepository.kt` (`withContext(Dispatchers.IO)`) |
| Carregando / sucesso / erro | `viewmodel/EstudoViewModel.kt` (`StatusIA`), `userinterface/EstudarScreen.kt` |
| Cadastro e login com validação | `data/UsuarioRepository.kt`: e-mail válido, senha ≥ 6, e-mail único, mensagem genérica no login |
| Senha com hash | `data/Hash.kt` (SHA-256), coluna `senhaHash` na tabela `usuarios` |
| Sessão que sobrevive a fechar o app | `data/SessaoManager.kt` (SharedPreferences) |
| Room por usuário | `data/Anotacao.kt` (`usuarioId` com chave estrangeira), `data/AnotacaoDao.kt` |
| Barra inferior com 4 abas | `userinterface/EstudaAiNavegacao.kt`: Estudar, Anotações, Horas, Perfil |
| Anotações próprias | `userinterface/SalvosScreen.kt` (botão Nova anotação), `EstudoRepository.criarAnotacao` |
| Registro de horas estudadas | `data/RegistroEstudo.kt`, `data/HorasEstudoRepository.kt`, `userinterface/HorasScreen.kt`; total exibido no Perfil |
| Detalhe com rota type-safe | `userinterface/Rotas.kt` (`Detalhe(anotacaoId)`), `toRoute<Detalhe>()` |
| Botão Sair | `userinterface/PerfilScreen.kt` |

### Estrutura de pacotes
```
com.estudaai
├─ network        NvidiaApi (Retrofit) e RetrofitInstance
├─ data           data classes, Room (Usuario, Anotacao, Daos, AppDatabase), SessaoManager e Repositories
├─ viewmodel      AuthViewModel, EstudoViewModel, AnotacoesViewModel e seus estados
└─ userinterface  telas Compose, Rotas, navegação, tema e MainActivity.
```
