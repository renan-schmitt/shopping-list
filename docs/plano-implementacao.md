# Plano de Implementação — Lista de Compras 60+

> Documento vivo. Baseado em [ideias.md](ideias.md). Última revisão: 2026-11-11.

## Princípios

- **Zero fricção:** sem login, sem cadastro, sem configuração. O app abre direto numa folha de lista vazia.
- **Linguagem do dia a dia:** "Jogar a lista fora", "Terminei", "Ops, voltar" — nunca "apagar", "finalizar", "desfazer".
- **Acessibilidade não é premium:** tudo que torna o app usável fica grátis.
- **Dados no celular:** sem backend na primeira fase. Privacidade como argumento de venda.
- **Android primeiro**, com tecnologia que permita levar para o iPhone sem reescrever.

---

## 1. Básico (MVP)

O que precisa existir na primeira versão publicada.

### 1.1 Lista
- [ ] Lista única, com aparência de folha de papel
- [ ] Linha do produto: marca de riscar → nome → ícone no fim da linha
- [ ] Quantidade opcional ("2 leites", "1 kg de carne moída"): só aparece se a pessoa informou, em letra menor
- [ ] Detalhe livre junto ao nome ("leite **desnatado**", "café da marca X")
- [ ] Tocar no produto abre edição simples: nome, botões grandes − / + para quantidade, campo de detalhe (sem teclado numérico nem lista de unidades)
- [ ] Produto repetido não duplica: "Leite já está na lista. Quer mais um?"
- [ ] Marcar como comprado **deslizando ou tocando** — item fica riscado e cinza, com vibração
- [ ] "Ops, voltar" para desfazer qualquer ação (marcar, remover, jogar fora)
- [ ] Botão **Terminei**: tira da lista o que foi comprado e grava no histórico
- [ ] **Tela única que se adapta** (sem "modo No mercado"):
  - Antes de riscar o primeiro produto: botão grande **Adicionar**
  - Depois de riscar o primeiro produto: botão grande **Terminei** com contador "Faltam 5"; "Adicionar" continua disponível, menor; tela para de apagar
  - Depois do "Terminei": volta ao estado inicial; produtos não riscados ficam para a próxima vez
- [ ] Lista sempre agrupada por seção na ordem típica do mercado
- [ ] Produto riscado fica no lugar (como no papel)
- [ ] Botão **Jogar a lista fora**: com confirmação e possibilidade de voltar atrás

### 1.2 Adicionar produtos
- [ ] Digitando, com busca aproximada ("maca" encontra "maçã")
- [ ] Falando — inclusive vários itens de uma vez ("arroz, feijão e dois litros de leite")
- [ ] Escolhendo de sugestões (mais comprados / sugestões do histórico)
- [ ] Produto desconhecido é aceito normalmente, com ícone da categoria ou ícone genérico
- [ ] Separação automática do texto/fala em quantidade + unidade + produto + detalhe ("dois litros de leite", "meio quilo de carne moída", "uma dúzia de ovos")
- [ ] Unidades reconhecidas: unidade, kg, grama, litro, dúzia, meia dúzia, pacote, caixa, lata, garrafa, maço, bandeja, pote
- [ ] Números por extenso (um a vinte, meio/meia, dúzia)

### 1.3 Catálogo e normalização
- [ ] Catálogo local (~1.500 produtos) com: nome, sinônimos, categoria, ícone, ordem de corredor
- [ ] Normalização: minúsculas, sem acento, plural → singular, sinônimos (maça = maçã = maçãs)
- [ ] Regionalismos do Brasil inteiro (aipim/mandioca/macaxeira, mexerica/bergamota/tangerina, pão francês/cacetinho/pão de sal, bolacha/biscoito...)
- [ ] Produtos regionais (flocão, goma de tapioca, farinha d'água, erva-mate, queijo coalho...)
- [ ] Exibir sempre o nome que a pessoa usou; internamente mapeado ao produto canônico (mesmo ícone, categoria e histórico)
- [ ] Aprender localmente o termo preferido da pessoa para as sugestões

### 1.4 Categorias
- [ ] Agrupamento automático pela categoria do catálogo
- [ ] Mudar a categoria de um item manualmente (e o app lembra da escolha)
- [ ] Categorias ordenadas como um mercado costuma ser (hortifrúti, padaria, açougue, frios, mercearia, limpeza, higiene...)

**Seções decididas (2026-11-11, #14)** — lista fechada em [catalog/sections.csv](../catalog/sections.csv), nesta ordem: Hortifrúti, Padaria, Açougue e peixaria, Frios e laticínios, **Congelados**, Mercearia, Bebidas, Limpeza, Higiene e farmácia, Pet e **Outros**.
- Na interface a palavra é **"Seção"**, nunca "Categoria".
- **Congelados** é seção própria (sorvete, pizza, lasanha não cabem nas outras) e fica junto de Frios, na área refrigerada.
- **Outros** recebe os produtos que o app não reconhece e fica sempre por último.
- Cada seção tem um ícone Fluent, usado como reserva do produto sem ícone próprio (seção 4.4).
- O id da seção (em inglês) nunca muda; a ordem pode mudar. A planilha mestre do catálogo usa esses ids.

### 1.5 Histórico e sugestões
- [ ] Histórico de compras (data + produtos comprados + quantidade) gravado a cada "Terminei"
- [ ] Previsões no MVP usam só a frequência; quantidade fica gravada para uso futuro
- [ ] Modelo simples: intervalo médio entre compras de cada produto
- [ ] Sugestão no topo da lista: "Faz tempo que você não compra café" — um toque adiciona

### 1.6 Compartilhar (sem backend)
- [ ] Enviar a lista como texto pelo WhatsApp / outros apps
- [ ] Colar uma lista recebida (texto) e transformar em itens

### 1.7 Acessibilidade
- [ ] Letras grandes por padrão (nome do produto ~28sp, linhas ≥ 72dp) e respeito ao tamanho de fonte do sistema
- [ ] Áreas de toque ≥ 64dp
- [ ] Alto contraste; estado nunca indicado só por cor
- [ ] Compatível com TalkBack
- [ ] Tela não apaga enquanto a pessoa está comprando (após riscar o primeiro produto)

### 1.8 Segurança dos dados
- [ ] Android Auto Backup ativado (salva no Google Drive do celular, sem login no app)

---

## 2. Pontos em aberto

Decisões que ainda precisam ser tomadas, mas não exigem pesquisa aprofundada.

| # | Ponto | Opções / observação |
|---|---|---|
| 1 | ~~Stack principal~~ | **Decidido:** Kotlin Multiplatform + Jetpack Compose (ver seção 4) |
| 2 | ~~Modelo de cobrança do premium~~ | **Decidido:** híbrido — compra única "Premium para sempre" (fase 2) + assinatura "Plano Família" (fase 3). Ver seção 5 |
| 3 | Preço do premium | **Em discussão** — depende da evolução do app. Referência inicial: Premium para sempre ~R$ 19,90–29,90; Plano Família ~R$ 6,90/mês ou R$ 49,90/ano. Concorrentes: Bring! US$ 8,99/ano, AnyList US$ 9,99–14,99/ano, OurGroceries ~US$ 1/mês. Validar com experimentos de preço do Play Console |
| 4 | ~~Abrangência regional do catálogo~~ | **Decidido:** Brasil inteiro desde o MVP, só pt-BR. Nome exibido é sempre o que a pessoa usou; preferência aprendida localmente, sem perguntar região. Curadoria na fase 0 com validação em 4 regiões (processo na seção 3.5). Portugal/outros países fora do escopo |
| 5 | ~~Nome e identidade visual~~ | **Decidido (2026-11-10, #1):** o app faz parte da família **Mente Alerta** (marca do app para 60+ sobre golpes e educação; publicado pela mesma conta da Play Store). Título na loja **"Lista de Compras: Mente Alerta"** (30 caracteres, o limite); nome embaixo do ícone **"Lista de Compras"** (diferente do app Mente Alerta; pode aparecer cortado em celulares de uma linha — conferir num Motorola nos testes da fase 0). Evitar "vó", "sênior", "melhor idade". Identidade visual: estilo quadrinhos com destaque verde-folha — ver seção 4.8. **Verificações:** "Anota Aí" descartado — conflita com a Anota AI (robô de pedidos ligado ao iFood, app com 1,2 mi+ instalações) e `anotaai.com.br`/`.com` estão ocupados; reservas Caderninho e Listinha descartadas (já existem); `mentealerta.com.br` pertence a terceiros (não bloqueia: política de privacidade pode ficar no GitHub Pages). **Pendente:** busca da marca "Mente Alerta" no INPI (classes 9 e 42) |
| 6 | ~~Tom das mensagens~~ | **Decidido:** ver [guia-de-linguagem.md](guia-de-linguagem.md). Validar vocabulário nos testes da fase 0 |
| 7 | ~~Quantidade e unidade~~ | **Decidido:** entrada livre com separação automática (quantidade, unidade, produto, detalhe); quantidade opcional; edição com botões − / +. Ver seções 1.1 e 1.2 |
| 8 | ~~Itens "Sempre compro"~~ | **Decidido:** bloco "Os de sempre" (fase 2, premium). Produtos **nunca** voltam sozinhos para a lista — a pessoa põe todos com um toque ou um a um. Bloco aparece com a lista vazia ou após o "Terminei". Produto vira "de sempre" pelo botão "Compro sempre" na edição ou por sugestão do app baseada no histórico |
| 9 | ~~Modo "No mercado"~~ | **Decidido:** sem modo separado — uma tela que se adapta sozinha. Riscar o primeiro produto marca "estou comprando". Ver seção 1.1 |
| 10 | ~~Banco de ícones~~ | **Decidido:** Fluent Emoji Color + complemento próprio (ver seção 4.4) |
| 11 | ~~Coleta de métricas de uso~~ | **Decidido:** Crashlytics + eventos anônimos; conteúdo da lista nunca sai do celular (ver seção 4.5) |
| 12 | Rodapé "Adicionar" + "Terminei" | **Em aberto (2026-11-10, #12):** no protótipo os dois ficam lado a lado (1 : 1,5), mas com Lexend Deca 22sp "Adicionar" não cabe num celular de 411dp sem partir a palavra ("ADICION/AR"), nem com a fonte em 100%. Opções: empilhar sempre; lado a lado só com fonte pequena e empilhar acima de ~130%; dividir a largura meio a meio; diminuir a letra do "Adicionar". Decidir na tarefa da tela da lista, conferindo nos screenshots em 100/150/200% |

---

## 3. Precisa de mais análise

Funcionalidades com risco técnico, de política das lojas ou de UX que exigem estudo antes de decidir.

### 3.1 Aviso ao chegar perto do mercado
- **Problema:** exige permissão de localização "o tempo todo". No Android 11+ essa opção fica escondida nas configurações — difícil para o público 60+. O Google Play exige justificativa e revisão para localização em segundo plano.
- **Problema 2:** saber onde ficam os mercados exige API paga (ex.: Google Places), o que conflita com "sem backend".
- **Hipótese a validar:** botão "Estou no mercado" que grava o local na primeira visita; depois um geofence local (limite de 100 por app) avisa nas próximas. Sem API externa.
- **A analisar:** taxa de aceitação da permissão nesse público, consumo de bateria, política do Google Play, comportamento no iOS.

### 3.2 Notificações em geral
- Android 13+ exige permissão explícita para notificações — quando e como pedir sem assustar?
- Frequência: evitar que o app vire "chato". Definir limites (ex.: no máximo 1 por dia).
- Gatilhos: lista grande, produto atrasado em relação ao padrão, lembrete de compra.

### 3.3 Reconhecimento de voz
- Qualidade do reconhecimento offline em pt-BR em aparelhos de entrada.
- Separar vários itens numa frase ("arroz feijão e leite" sem pausas).
- Interpretar quantidades ("dois litros de leite", "uma dúzia de ovos").

### 3.4 Modelos de padrão de compra
- Quantas compras são necessárias até a sugestão ser confiável?
- Produtos comprados juntos (coocorrência) — vale no MVP?
- Como explicar a sugestão de forma simples ("você costuma comprar a cada 10 dias").

### 3.5 Curadoria do catálogo e dos ícones

**Decidido (2026-11-11, #13).** A cobertura de ícones foi decidida na seção 4.4.

**Fonte da lista inicial:** rascunho gerado com IA, uma seção por vez, direto no formato da planilha mestre. Depois, cada linha passa por revisão humana.
- **Conferência de cobertura:** fontes públicas servem só de checklist, sem copiar: lista de produtos da POF/IBGE (básicos e regionais), tabela TACO (alimentos) e cesta básica do DIEESE (o mínimo).
- **Open Food Facts descartado:** cadastra produtos de marca com código de barras, não genéricos, e a licença ODbL obrigaria a publicar a base derivada com a mesma licença.

**Validação dos regionalismos:** 4 grupos de regiões (Norte, Nordeste, Centro-Oeste + Sudeste, Sul).
- Regionalismo vindo da IA entra como **"a confirmar"**. Vira **confirmado** com pelo menos uma evidência registrada na planilha: site de mercado da região ou dicionário que marque o termo como regional.
- **Foco nos termos ambíguos** (ex.: "bergamota", "goma", "bolacha"): é aí que mora o risco de levar ao produto errado, com seção e ícone errados. Um sinônimo que ninguém usa não faz mal, porque o app mostra o nome que a pessoa usou.
- Termo ambíguo só entra no catálogo depois de revisão humana: leva ao produto mais comum ou fica fora dos sinônimos (e vira produto desconhecido). Nunca é decidido só pela IA.
- Regionalismo "a confirmar" e não ambíguo pode ir para o app.
- Revisores conhecidos de cada região, quando houver, revisam só uma lista curta: os termos ambíguos e os "a confirmar" da sua região.

**Produtos novos no celular (MVP):**
- Produto desconhecido vira um **produto próprio da pessoa**: nome normalizado, seção e ícone da categoria. Entra no histórico e nas sugestões como qualquer outro. Fica só no celular.
- A seção é adivinhada pela palavra principal ("queijo canastra" → Frios). Se não reconhecer nenhuma palavra, vai para a seção **Outros** (seção 1.4). A pessoa pode mudar, e o app lembra (seção 1.4).
- Se um catálogo novo trouxer o produto, o produto próprio passa a apontar para o do catálogo, sem perder o histórico.

**Evolução do catálogo:**
- O catálogo atualizado vai **junto com a atualização do app**, sem servidor.
- Origem dos termos novos: no MVP, testes de usabilidade, avaliações na loja e comentários; na fase 2, termos desconhecidos agregados (seção 4.5, #101). Só os mais frequentes vão para revisão, pela mesma curadoria.
- **O id de um produto do catálogo nunca muda nem é reaproveitado.** Produto retirado fica marcado como antigo. Dois produtos juntados viram um só, e o id antigo aponta para o novo.

### 3.6 Compartilhamento em tempo real com a família
- Exige backend. Proposta: login **anônimo** (Firebase/Supabase) + código ou link de pareamento — sem email.
- A analisar: resolução de conflitos (duas pessoas editando), custo de infraestrutura, LGPD, o que acontece ao trocar de celular.

### 3.7 Anúncios
- Risco de toques acidentais nesse público e de punição pelo Google Play.
- Onde exibir sem atrapalhar (nunca na tela da lista).
- Receita esperada × impacto na avaliação do app na loja.

### 3.8 Ordem das seções e produtos riscados
- **Ordem personalizada das seções** ("no meu mercado a padaria fica no fundo"): candidata a premium na fase 2. Pode se integrar ao aviso perto do mercado (3.1), guardando a ordem por mercado.
- **Mover riscados para o final da seção:** facilita ver o que falta, mas quebra a metáfora do papel. Testar com usuários na fase 0.

### 3.9 Validação com usuários reais
- Testes de usabilidade com pessoas 60+ antes do lançamento (gesto de deslizar, vocabulário, tamanho de fonte, entrada por voz).
- Confirmar a legibilidade dos nomes da lista em caixa alta e do estilo quadrinhos.

---

## 4. Tecnologia (decidido)

### 4.1 Decisão

**Kotlin Multiplatform (KMP) + Jetpack Compose no Android.**

Critério principal: **qualidade**. O código será escrito pelo Claude, então a familiaridade do time com a linguagem não pesa.

**Por quê:**
- **Melhor qualidade no Android:** acesso direto e sem ponte a tudo que o app precisa (widget com Glance, reconhecimento de voz, TTS, WorkManager, geofence, Auto Backup, Play Billing).
- **Acessibilidade de primeira:** o Compose tem suporte nativo a TalkBack, escala de fonte e semântica, e testes automáticos de acessibilidade.
- **iPhone sem reescrever a lógica:** catálogo, normalização, banco, histórico e modelos de previsão ficam no módulo compartilhado (`shared`), em Kotlin, e rodam também no iOS.
- **Código gerado por IA mais seguro:** tipagem forte e compilador rigoroso pegam erros cedo; testes unitários rodam rápido no módulo compartilhado.

**Descartados:**
- *React Native + Expo:* mais rápido para o MVP, mas widget, voz e geofence dependem de módulos de terceiros ou de código nativo próprio — pior para qualidade.
- *Flutter:* bom, mas plugins de voz/geofence com qualidade variável e a UI não usa componentes nativos do sistema.

### 4.2 Arquitetura

```
shopping-list/
├── shared/                 # Kotlin Multiplatform (Android + iOS)
│   ├── domain/             # Item, Lista, Compra, Categoria, regras de negócio
│   ├── catalog/            # Catálogo embarcado, sinônimos, ícones
│   ├── normalization/      # Acentos, plural pt-BR, sinônimos, busca aproximada
│   ├── prediction/         # Intervalo médio entre compras, sugestões
│   └── data/               # Banco (Room KMP), repositórios
├── androidApp/             # Jetpack Compose, Material 3, integrações Android
└── iosApp/                 # Fase 5
```

- Padrão: camadas (UI → ViewModel → casos de uso → repositório), com estado exposto via `StateFlow`.
- Integrações de plataforma (voz, notificações, localização) atrás de interfaces no `shared`, implementadas em cada plataforma.
- **Idioma do código (2026-11-11, #119):** nomes em inglês (classes, funções, variáveis, parâmetros, arquivos, pastas, recursos e testes). Comentários, KDoc, commits e documentos em português; texto exibido segue o [guia de linguagem](guia-de-linguagem.md). O pacote e o applicationId continuam `br.com.mentealerta.listadecompras` (identidade do app na Play Store).

### 4.3 Componentes

| Necessidade | Escolha | Observação |
|---|---|---|
| Linguagem | Kotlin 2.x | |
| UI Android | Jetpack Compose + Material 3 | Tema próprio com fontes grandes e alto contraste |
| Banco local | Room (KMP) + `BundledSQLiteDriver` | Mesma versão do SQLite no Android e no iOS |
| Busca e normalização | Código Kotlin próprio no `shared` | Coluna normalizada (sem acento, singular) + sinônimos + Levenshtein. O catálogo (~1.500 itens) cabe em memória — não precisa de FTS |
| Catálogo | Arquivo JSON/SQLite embarcado, gerado por script a partir de uma planilha mestre | Facilita a curadoria; processo na seção 3.5 |
| Preferências | DataStore (KMP) | |
| Assincronia | Coroutines + Flow | |
| Injeção de dependências | Koin | Suporta KMP |
| Navegação | Navigation Compose | Poucas telas |
| Ícones | Microsoft Fluent Emoji Color (MIT) + complemento próprio | Detalhes na seção 4.4 |
| Voz → texto | `SpeechRecognizer` do Android (pt-BR, offline quando disponível) | |
| Texto → voz | `TextToSpeech` do Android | |
| Notificações | WorkManager + notificações locais | Sem servidor |
| Widget | Jetpack Glance | |
| Localização (fase 4) | Google Play Services Geofencing | Depende da análise da seção 3.1 |
| Backup | Android Auto Backup | |
| Compras no app | RevenueCat (SDK KMP) sobre o Google Play Billing | Mesmo código no iOS depois |
| Anúncios | Google Mobile Ads (AdMob) | Só em telas secundárias |
| Sincronização (fase 3) | Firebase ou Supabase com autenticação anônima | A decidir na fase 3 |
| Crashes | Firebase Crashlytics + Play Console Android Vitals | Detalhes na seção 4.5 |
| Uso (analytics) | Firebase Analytics, eventos anônimos | Detalhes na seção 4.5 |

### 4.4 Ícones

**Problema:** emojis padrão cobrem frutas, verduras e comida pronta, mas não os básicos brasileiros em embalagem (arroz em pacote, feijão, óleo, açúcar, farinha, café em pacote, leite de caixinha, sabão em pó, desinfetante). Para o público 60+, o ícone deve parecer o produto na prateleira — e **sempre acompanha o nome, nunca o substitui**.

**Decisão:**
1. **Base:** Microsoft Fluent Emoji, variação **Color** (SVG → VectorDrawable). Licença MIT. Cobre ~150 produtos e todas as categorias.
2. **Complemento próprio:** ~50–100 ícones no mesmo estilo para os básicos brasileiros que faltam (a variação Color é a mais fácil de imitar).
3. **Fallback:** ícone da categoria quando o produto não tem ícone próprio.
4. **Alto contraste (opcional):** usar a variação High Contrast do Fluent quando o celular estiver em modo alto contraste.
5. **Fase 0:** mapear o catálogo contra os ícones e medir a cobertura real antes de produzir o complemento.

**Primeiro levantamento (2026-11-10)** — página [prototipos/icones.html](prototipos/icones.html), com os ícones reais do [repositório oficial](https://github.com/microsoft/fluentui-emoji):
- **99 produtos** com ícone Fluent: **78 fiéis** ao produto e **21 aproximados** (ex.: arroz em tigela, café como xícara, leite como copo).
- **43 lacunas** sem ícone, que usam o ícone da categoria até ganhar ícone próprio — concentradas nos **básicos em embalagem** (óleo, açúcar, farinha, café em pacote, leite de caixinha, margarina, requeijão, sabão em pó, detergente, água sanitária) e em regionais (mandioca/aipim, chuchu, quiabo, goma de tapioca).
- Cobertura boa em hortifrúti, padaria e açougue.
- **Primeira lista do complemento próprio:** as 43 lacunas + revisar os 21 aproximados (~64 ícones, dentro da estimativa de 50–100).

**Descartados:** Noto Emoji (mesmas lacunas, estilo mais simples), OpenMoji (CC BY-SA obriga adaptações na mesma licença; estilo menos atraente), conjunto 100% próprio (custo e prazo).

### 4.5 Privacidade, métricas e coleta de dados

**Promessa ao usuário:** *"Sua lista e seu histórico nunca saem do seu celular."*
(Não dá para prometer "nenhum dado sai do celular": a versão grátis usa AdMob, que coleta dados do aparelho.)

| Camada | Quando | Como | Regras |
|---|---|---|---|
| Crashes | MVP | Firebase Crashlytics + Play Console Android Vitals | Sem dados pessoais nos logs |
| Uso do app | MVP | Firebase Analytics com lista fechada de eventos | Nunca nome de produto; coleta automática de ID de publicidade desligada; opção para desativar nas configurações |
| Termos desconhecidos | Fase 2 | Envio agregado, sem identificador | Só com consentimento explícito ("Ajudar a melhorar o app"); filtrar termos de saúde (remédios, etc.) |
| Conteúdo da lista / histórico | Nunca | — | — |

**Eventos iniciais do analytics (lista fechada):**
- `item_adicionado { origem: voz | digitado | sugestao | colado }`
- `item_riscado`, `ops_voltar`
- `terminei { qtd_itens }`, `lista_jogada_fora`
- `sugestao_exibida`, `sugestao_aceita`
- `lista_compartilhada`

**Obrigações:**
- Política de privacidade publicada (exigência da Play Store).
- Formulário de Segurança dos Dados da Play Store preenchido conforme as camadas acima.
- Informar que o reconhecimento de voz pode enviar áudio ao Google quando o modo offline não estiver disponível.
- Nomes de produtos podem revelar saúde (dado sensível pela LGPD) — por isso o conteúdo da lista nunca é enviado.

### 4.6 Qualidade e testes

| Tipo | Ferramenta | O que cobre |
|---|---|---|
| Unitários | `kotlin.test` + Turbine | Normalização, busca, previsão, regras da lista |
| Interface | Compose UI Test | Fluxos: adicionar, riscar, Terminei, jogar fora, voltar |
| Screenshots | Roborazzi | Cada tela com fonte do sistema em 100%, 150% e 200%, claro e escuro |
| Acessibilidade | Accessibility Test Framework (checks no Compose UI Test) | Área de toque, contraste, rótulos para TalkBack |
| Estilo/estática | ktlint + detekt | |
| CI | GitHub Actions | Build, testes e lint a cada push |

### 4.7 Plataforma

- **minSdk 26 (Android 8.0):** cobre a grande maioria dos aparelhos em uso, inclusive celulares mais antigos comuns nesse público.
- **targetSdk:** sempre a versão mais recente exigida pela Play Store.
- **iOS (fase 5):** a lógica vem do `shared`. A interface será decidida na época entre **SwiftUI nativo** (melhor qualidade e acessibilidade no iPhone) e **Compose Multiplatform** (reaproveita a UI do Android). Recomendação atual: SwiftUI.

### 4.8 Identidade visual

Decidida em 2026-11-10 a partir do protótipo [prototipos/cores-destaque.html](prototipos/cores-destaque.html).

| Elemento | Decisão |
|---|---|
| Estilo | **Quadrinhos:** contornos pretos grossos, sombras sólidas deslocadas, fundo com retícula (pontinhos) discreta. Os contornos também deixam botões e áreas de toque bem visíveis |
| Fundo | Papel creme (sem branco puro) |
| Texto | Tinta quase preta (`#141414`); riscado em cinza (`#66625A`) |
| Cor de destaque | **Verde-folha `#2E6B3A`** — única cor de destaque (topo, botões principais, marca de riscado). Descartadas: laranja-tijolo `#A8431F`, azul-caneta `#1F4E9C` |
| Fontes | **Bangers** só no logo, títulos de seção e botões principais; **Lexend Deca** (negrito) na lista e textos, com espaçamento entre letras um pouco maior (+0,04em) — feita para leitura fácil. Escolhida em 2026-11-10 no protótipo [prototipos/espacamento-letras.html](prototipos/espacamento-letras.html); descartadas Comic Neue, Atkinson Hyperlegible e Lexend Exa ([prototipos/fonte-lista.html](prototipos/fonte-lista.html)) |
| Caixa alta | Liberada em **textos curtos** — logo, títulos, botões, rótulos, avisos de poucas palavras e **nomes da lista** (como no protótipo) — para manter a cara de quadrinhos; o app não tem textos longos. Legibilidade a confirmar na fase 0 (ver 3.9). Texto escrito em caixa normal e maiúsculas aplicadas pelo estilo, para o TalkBack ler direito. Decidido em 2026-11-10 (#2) |
| Tamanho | Nome do produto ~28sp, linhas ≥ 72dp, botões principais ~88dp |
| Linha do produto | Marca de riscar → nome → ícone no fim da linha; quantidade só se informada, em letra menor. Ícones grandes, sempre ao lado do texto e nunca por cima dele. Nome com muitas palavras **quebra a linha** (sem cortar com reticências); a linha cresce em altura |
| Item riscado | Traço grosso por cima + texto cinza + marca preenchida com ✓ (estado nunca só por cor) |
| Logo | "Lista de Compras" em Bangers, branco com contorno preto, com "Mente Alerta" pequeno acima |
| Ícone do app | Arte da família Mente Alerta: casal 60+ com carrinho e prancheta de lista ([prototipos/img/icone-lista-compras.png](prototipos/img/icone-lista-compras.png)) — usada inteira na Play Store. No celular, **versão simplificada**: prancheta com ✓ grande no centro (talvez com o carrinho), fundo azul-petróleo e amarelo, em duas camadas (ícone adaptativo) e sem detalhes perto da borda. Ver [prototipos/icone-tamanhos.html](prototipos/icone-tamanhos.html) |
| Modo escuro | Segue o sistema — paleta a definir |

---

## 5. Grátis × Premium

**Regra:** tudo que é essencial para fazer a lista e usar no mercado, e toda a acessibilidade, é grátis. O premium vende **conforto, família e inteligência extra**.

| Funcionalidade | Grátis | Premium |
|---|:---:|:---:|
| Lista única com ícones e categorias | ✅ | ✅ |
| Adicionar digitando, falando e por sugestão | ✅ | ✅ |
| Normalização de nomes | ✅ | ✅ |
| Riscar, Terminei, Jogar a lista fora, Ops voltar | ✅ | ✅ |
| Letras grandes, alto contraste, TalkBack | ✅ | ✅ |
| Enviar / colar lista pelo WhatsApp | ✅ | ✅ |
| Backup automático (Android Auto Backup) | ✅ | ✅ |
| Histórico de compras (últimos 30 dias) | ✅ | ✅ |
| Sugestões básicas ("faz tempo que não compra") | ✅ | ✅ |
| **Sem anúncios** | — | ✅ |
| Histórico completo (sem limite de tempo) | — | ✅ |
| Notificações inteligentes (lista grande, produto atrasado) | — | ✅ |
| Aviso ao chegar perto do mercado | — | ✅ |
| Lista compartilhada em tempo real com a família | — | ✅ |
| "Quanto gastei" e estatísticas de gastos | — | ✅ |
| Widget na tela inicial | — | ✅ |
| "Os de sempre" (pôr os produtos fixos na lista com um toque) | — | ✅ |
| Temas de cor / estilos de papel | — | ✅ |

### Modelo de cobrança (híbrido)

| Produto | Tipo | Fase | Quem paga | O que libera |
|---|---|---|---|---|
| **Premium para sempre** | Compra única | 2 | O próprio usuário | Tudo da coluna Premium, exceto a lista compartilhada |
| **Plano Família** | Assinatura (mensal/anual) | 3 | Filho/neto | Lista compartilhada em tempo real + premium liberado em todos os celulares pareados |

**Por quê:**
- O público 60+ desconfia de assinatura; sem backend não há custo recorrente que a justifique.
- A sincronização (fase 3) gera custo recorrente — a assinatura entra junto com ela, voltada a quem a aceita melhor (filhos/netos).
- O Google Play não permite presentear compras dentro do app; o "Dar de presente" só fica viável com celulares pareados (Plano Família).
- Quem comprou o Premium para sempre mantém tudo o que comprou.
- Meios de pagamento no Google Play Brasil a confirmar (Pix, operadora, vale-presente) — relevantes para quem não tem cartão.

**Ideias de comercialização:**
- "Dar de presente" via Plano Família: filhos/netos pagam e liberam o premium dos pais.
- Anúncios nunca na tela da lista — só em telas secundárias (histórico, sugestões).

---

## 6. Fases sugeridas

| Fase | Escopo |
|---|---|
| **0 — Fundação** | Decidir stack, montar catálogo inicial e ícones, protótipo navegável, teste com 3–5 pessoas 60+ |
| **1 — MVP Android (grátis)** | Seção 1 completa, publicação na Play Store |
| **2 — Monetização** | Anúncios + premium (sem anúncios, histórico completo, notificações, widget, "Os de sempre") |
| **3 — Família** | Backend com login anônimo, lista compartilhada em tempo real |
| **4 — Localização** | Aviso perto do mercado (após análise da seção 3.1) |
| **5 — iPhone** | Publicação na App Store |
