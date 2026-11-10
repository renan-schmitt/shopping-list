# Lista de Compras: Mente Alerta

Lista de compras para pessoas **60+**, da família de apps **Mente Alerta**. Android primeiro (iPhone na fase 5), sem login, sem cadastro e **sem servidor no MVP**. O app abre direto numa folha de lista. Fases de 0 a 5: [plano §6](docs/plano-implementacao.md#6-fases-sugeridas).

## Documentos

- [docs/plano-implementacao.md](docs/plano-implementacao.md) — **fonte de verdade**: escopo, decisões, tecnologia, grátis × premium
- [docs/guia-de-linguagem.md](docs/guia-de-linguagem.md) — como escrever todo texto do app
- [docs/ideias.md](docs/ideias.md) — ideias originais
- [docs/prototipos/](docs/prototipos/) — protótipos HTML (cores, fontes, ícones)
- Identidade visual (cores, fontes, tamanhos, linha do produto): [plano §4.8](docs/plano-implementacao.md#48-identidade-visual)

Leia o plano e o guia antes de implementar qualquer tela ou texto.

## Regras que não se negociam

- **Todo texto exibido segue o guia de linguagem** ("Tirar da lista", "Terminei", "Ops, voltar"; nada de "OK", "excluir", "desfazer").
- **Acessibilidade nunca é premium.** Áreas de toque grandes, TalkBack, escala de fonte até 200%, estado nunca só por cor.
- **O conteúdo da lista e o histórico nunca saem do celular** — nem em logs, crashes ou analytics (só a lista fechada de eventos do [plano §4.5](docs/plano-implementacao.md#45-privacidade-métricas-e-coleta-de-dados)).
- **Ícone sempre acompanha o nome**, ao lado do texto — nunca por cima, nunca no lugar dele.
- **Caixa alta só em textos curtos** e aplicada pelo estilo: o texto fica em caixa normal no código, para o TalkBack ler direito.
- **Nomes longos quebram a linha**, sem cortar com reticências.
- Nada de texto longo na interface.

## Stack e arquitetura

Resumo do [plano §4](docs/plano-implementacao.md#4-tecnologia-decidido).

- **Kotlin Multiplatform** + **Jetpack Compose** (Material 3, tema próprio) no Android; minSdk 26, targetSdk 36, compileSdk 37
- applicationId e pacote: `br.com.mentealerta.listadecompras`; versões em `gradle/libs.versions.toml`
- `shared/` — domínio, catálogo, normalização, previsão e dados (Room KMP + `BundledSQLiteDriver`, DataStore), roda também no iOS
- `androidApp/` — UI Compose e integrações Android (voz, TTS, WorkManager, Glance, Auto Backup)
- Camadas: UI → ViewModel → casos de uso → repositório; estado via `StateFlow`; coroutines + Flow; Koin para DI
- Integrações de plataforma atrás de interfaces no `shared`
- Testes: `kotlin.test` + Turbine, Compose UI Test, Roborazzi (fonte 100/150/200%, claro e escuro), checks de acessibilidade; ktlint + detekt; CI no GitHub Actions

## Comandos

Requer JDK 21 e o Android SDK (`local.properties` com `sdk.dir`, fora do git). Lint e CI entram nas tarefas #5 e #6, que atualizam esta seção.

- Build completo (Android, iOS e testes): `./gradlew build`
- Instalar no emulador ou celular: `./gradlew :androidApp:installDebug`
- Testes do `shared` (Android e simulador iOS): `./gradlew :shared:allTests`
- Lint: _a definir (#5)_

## Como trabalhar

- **Português** em conversas, commits, código voltado ao usuário e documentos.
- Tarefas vêm do GitHub Project #5 ("Shopping List"), executadas com `/pick-task`. Cada issue traz contexto, critérios de aceite e dependências.
- Decisões: discutir um ponto por vez, com opções e uma recomendação; só depois registrar no plano (com data).
- Protótipos e HTML sempre como arquivo local em `docs/prototipos/` — nunca publicar no Claude (artifacts/docs).
- Rodar build, testes e lint antes de pedir validação.
- Commits referenciam a issue, ex.: `Adiciona X (#N)`.
