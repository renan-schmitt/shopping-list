# Plano de Implementação — Lista de Compras 60+

> Documento vivo. Baseado em [ideias.md](ideias.md). Última revisão: 2026-11-24.

## Princípios

- **Zero fricção:** sem login, sem cadastro, sem configuração. O app abre direto numa folha de lista vazia.
- **Linguagem do dia a dia:** "Jogar a lista fora", "Terminei", "Desfazer" — nunca "apagar", "finalizar". ("Ops, voltar" virou "Desfazer" em 2026-11-23.)
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
- [ ] Edição simples do produto: nome, botões grandes − / + para quantidade (fundo verde), campo de detalhe (sem teclado numérico nem lista de unidades) e **Tirar da lista** em destaque, com lixeira. Abre pelo modo **Mudar a lista**, não pelo toque na lista (2026-11-22, retorno do primeiro uso)
- [ ] Botão **Mudar a lista** no fim da folha: abre a lista inteira em modo de mudança; tocar num produto abre a edição. **Jogar a lista fora** fica no fim desse modo, com a confirmação de sempre (2026-11-22)
- [ ] Produto repetido não duplica: "Leite já está na lista. Quer mais um?"
- [ ] Marcar como comprado **deslizando ou tocando** — item fica riscado e cinza, com vibração. Tocar só risca: não abre a edição, para evitar toques acidentais (2026-11-22)
- [ ] **Desfazer** qualquer ação (marcar, tirar, jogar fora) — no lugar de "Ops, voltar" desde 2026-11-23
- [ ] Botão **Terminei**: tira da lista o que foi comprado e grava no histórico
- [ ] **Tela única que se adapta** (sem "modo No mercado"):
  - Antes de riscar o primeiro produto: botão grande **Adicionar**
  - Depois de riscar o primeiro produto: botão grande **Terminei** com contador "Faltam 5"; "Adicionar" continua disponível, menor; tela para de apagar
  - Depois do "Terminei": volta ao estado inicial; produtos não riscados ficam para a próxima vez
- [ ] Lista sempre agrupada por seção na ordem típica do mercado
- [ ] Produto riscado vai para a seção **Já peguei**, no fim da lista; as seções de cima ficam só com o que falta (2026-11-22, retorno do primeiro uso; antes ficava no lugar)
- [ ] Topo da lista vazia: título **Lista de Compras** centralizado e "Toque em **Adicionar** para começar sua lista." (2026-11-22)
- [ ] **Jogar a lista fora**: com confirmação e possibilidade de voltar atrás, dentro do modo **Mudar a lista**

### 1.2 Adicionar produtos
- [ ] Tela de adicionar (2026-11-22, retorno do primeiro uso):
  - Campo "Digite ou fale o que quer comprar."; botão de voltar com seta curva cheia que gira para trás ao abrir (opção A de `docs/prototipos/icone-voltar.html`), ao lado de "Voltar para a lista"
  - Resultados da busca sob o título "Adicionar na lista de compras", em fundo verde-folha, cada produto numa caixa creme com contorno; ficam por cima da lista de adicionados enquanto abertos
  - Lista **Adicionados**, sem seções, logo na tela: o que a pessoa acabou de pôr aparece ali, para ela ver que entrou. Ao voltar, a lista principal mostra tudo organizado, como sempre
  - Aviso depois de pôr: "Adicionado: Farinha." (forma que serve para masculino e feminino)
  - Produto repetido: "Sim, mais um" e "Não" com a mesma fonte
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
- [x] Tela **O que já comprei** — 2026-11-23, #66: compras da mais recente para a mais antiga, cada uma com a data por extenso na faixa ("Hoje, 2 produtos", "Ontem", "Há 12 dias", em dias do calendário no fuso do celular; sem "semana passada", ambíguo) e os produtos com ícone e quantidade, só para ver. Grátis mostra os últimos 30 dias (de hoje até há 30 dias); as compras antigas ficam no banco para a previsão e o premium. Entrada pelo botão **O que já comprei** no fim da folha, abaixo de "Mudar a lista", e na lista vazia, só quando há compra nesses 30 dias
- [ ] Previsões no MVP usam só a frequência; quantidade fica gravada para uso futuro
- [ ] Modelo simples: intervalo médio entre compras de cada produto
- [ ] Sugestão no topo da lista: "Faz tempo que você não compra café" — um toque adiciona

### 1.6 Compartilhar (sem backend)
- [x] Enviar a lista como texto pelo WhatsApp / outros apps — 2026-11-24, #67: botão **Mandar a lista** no fim da folha, abaixo de "Mudar a lista", só quando há o que comprar; abre a tela de compartilhar do Android. O texto leva só o que falta (sem "Já peguei"), por seção na ordem do mercado, com o negrito do WhatsApp (`*Lista de compras*`, `*Hortifrúti*`) e um produto por linha com a quantidade como na lista: "- Leite desnatado (2 litros)". O evento `lista_compartilhada` entra com o analytics
- [x] Colar uma lista recebida (texto) e transformar em itens — 2026-11-24, #68: na tela de adicionar, "Copiou uma lista no WhatsApp?" e **Colar a lista**, só quando há texto copiado (o app olha só se há texto, sem ler; o texto é lido no toque, e o Android 12+ mostra o aviso de que colou). O texto passa pelo mesmo leitor da voz (§3.3), depois de limpo: marcadores e numeração ("-", "•", "1.", "☐"), negrito do WhatsApp, cabeçalho de mensagem copiada ("[18:23, 24/11/2026] Maria:"), títulos ("Lista de compras", linha que termina em ":", nome de seção) e a quantidade depois do produto, que vem para o começo ("Leite (2 litros)", "Arroz 5kg", "Banana x6"). O nosso "Mandar a lista" volta igual. Antes de entrar, a conferência **Lista recebida**: um produto por linha com a lixeira, "Leite já está na lista." (não entra de novo, como na voz) e, presos no pé da tela, **Pôr 5 produtos na lista** e **Deixa pra lá** (também o voltar do celular). Sem produto: "Não achei produtos no texto copiado.". A conferência fica pronta para a voz (#61). Fica para outra issue: receber pelo "Compartilhar" do WhatsApp e corrigir um produto na conferência

### 1.7 Acessibilidade
- [x] Letras grandes por padrão (nome do produto ~28sp, linhas ≥ 72dp) e respeito ao tamanho de fonte do sistema — 2026-11-22, #71: fonte até 200% sem texto cortado; palavra que não cabe quebra com hífen, na separação de sílabas do português ("DESNA-TADO"), em vez de partir em qualquer letra
- [x] Áreas de toque ≥ 64dp
- [x] Alto contraste; estado nunca indicado só por cor
- [x] Compatível com TalkBack — 2026-11-22, #71: na linha da lista, além de riscar, as ações "Mudar" (abre a edição) e "Tirar da lista" (com aviso e "Ops, voltar"), sem passar pelo "Mudar a lista"; quem enxerga não vê diferença (o motivo da #121 era o toque acidental, que não existe num menu de ações). Campo vazio lê o exemplo depois do rótulo. Teste manual sem falhas em 2026-11-22 ([roteiro](teste-usuarios/talkback.md))
- [x] Tela não apaga enquanto a pessoa está comprando (após riscar o primeiro produto)

Checks automáticos (#71): `ScreensAccessibilityTest` confere, em todas as telas e estados, com fonte em 100% e 200%, que todo tocável tem nome e 64dp, que nenhum texto é cortado nem palavra partida sem hífen e que só campo de texto tem descrição própria. Substitui o Accessibility Test Framework, que não roda no Robolectric. Os testes rodam em pt-BR, como o celular.

### 1.8 Segurança dos dados
- [x] Android Auto Backup ativado (salva no Google Drive do celular, sem login no app) — 2026-11-22, #73: só o banco e as preferências entram no backup e na troca de celular; sem exigir bloqueio de tela (quem não tem ficaria sem backup)

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
| 12 | ~~Rodapé "Adicionar" + "Terminei"~~ | **Decidido (2026-11-21, #52):** sempre empilhados, em largura total, de 100% a 200% de fonte. Comprando: "Adicionar" menor (64dp) em cima e "Terminei" grande (88dp) embaixo, perto do polegar, com "Falta 1" / "Faltam N" / "Não falta nada". Antes de riscar: só o "Adicionar" grande |
| 13 | ~~Aviso com "Ops, voltar"~~ | **Decidido (2026-11-22, #59):** aviso no pé da tela depois de tirar um produto, do "Terminei" e de jogar a lista fora; fica **15 s** (50% a mais que o padrão longo do Android), ou mais se o TalkBack pedir; um aviso novo tira o anterior na hora. Riscar não mostra aviso (guia de linguagem): tocar de novo tira o risco, e o "Ops, voltar" também desfaz | **Revisto (2026-11-23, teste do app):** o aviso fica **no meio da tela**, sem escurecer a lista, com **Desfazer** (no lugar de "Ops, voltar") e **Certo** (só fecha), um embaixo do outro. Abrir outra tela fecha o aviso. A lista **Adicionados** ganhou uma lixeira em cada produto (única exceção ao "ícone sempre com o nome"), com o mesmo aviso. Na busca, quando só há batidas aproximadas ("nescau" → Nescafé), **Pôr “nescau” na lista** vem primeiro e é o que entra pelo "pronto" do teclado

---

## 3. Precisa de mais análise

Funcionalidades com risco técnico, de política das lojas ou de UX que exigem estudo antes de decidir.

### 3.1 Aviso ao chegar perto do mercado
- **Problema:** exige permissão de localização "o tempo todo". No Android 11+ essa opção fica escondida nas configurações — difícil para o público 60+. O Google Play exige justificativa e revisão para localização em segundo plano.
- **Problema 2:** saber onde ficam os mercados exige API paga (ex.: Google Places), o que conflita com "sem backend".
- **Hipótese a validar:** botão "Estou no mercado" que grava o local na primeira visita; depois um geofence local (limite de 100 por app) avisa nas próximas. Sem API externa.
- **Versão completa (2026-11-24, #79).** A ordem das seções **por mercado** (seção 3.8) nasce junto com este aviso, que sabe em qual mercado a pessoa está.
- **A analisar:** taxa de aceitação da permissão nesse público, consumo de bateria, política do Google Play, comportamento no iOS.

### 3.2 Notificações em geral
- Android 13+ exige permissão explícita para notificações — quando e como pedir sem assustar?
- Frequência: evitar que o app vire "chato". Definir limites (ex.: no máximo 1 por dia).
- Gatilhos: lista grande, produto atrasado em relação ao padrão, lembrete de compra.

### 3.3 Reconhecimento de voz

**Decidido (2026-11-21, #29).** A interpretação de quantidades já foi feita (#45, #46, #47).

**Revisto em 2026-11-22 (retorno do primeiro uso):** a voz não depende de internet. No Android 12+, o app usa o reconhecedor do próprio celular (`createOnDeviceSpeechRecognizer`); sem a voz pt-BR, pede para baixar uma vez (Android 13+). Só sem esse caminho (Android 8–11, ou celular sem reconhecedor no aparelho) usa o serviço padrão, que pode precisar de internet. O "Sem internet" aparece só em erro de rede de verdade; "idioma não disponível" leva ao download da voz. **Segurar para falar, soltar para parar** (como no WhatsApp): sem "Terminei" e sem reabrir o microfone sozinho; o TalkBack ganha a ação de tocar para abrir e tocar para fechar. Na tela (#123): "Segure para falar." acima do botão, que fica no mesmo lugar e vira **Terminei** com o microfone aberto; um toque rápido mostra "Segure o botão enquanto fala.". Enquanto o dedo segura, a escuta continua depois das pausas; o limite de ~8 s de silêncio fica só no modo do TalkBack. Depois de "Usar o microfone" ou da permissão do Android, a pessoa segura de novo.

~~**Reconhecedor:** `SpeechRecognizer` com o serviço de voz padrão do celular (quase sempre o do Google), pedindo offline com `EXTRA_PREFER_OFFLINE`. Sem o pacote pt-BR baixado, usa a internet.~~
- Pede até 5 alternativas e fica com a que mais bate com o catálogo.
- Mostra o texto aparecendo enquanto a pessoa fala (resultados parciais).
- A escuta não acaba numa pausa curta: o app reabre o microfone sozinho até a pessoa tocar em "Terminei" ou ficar uns 8 s em silêncio (valor provisório).
- Android 13+ sem o pacote pt-BR offline: oferecer o download uma vez, nas configurações ("Baixar a voz para usar sem internet"), sem insistir.
- Descartados: só o reconhecedor do aparelho (`createOnDeviceSpeechRecognizer`, Android 12+), que deixaria muita gente sem voz; motor próprio embarcado (Vosk, Whisper), que pesa dezenas de MB e é lento em aparelho de entrada. O motor próprio fica como plano para depois, se a privacidade ou a qualidade virarem problema.

**Vários itens numa frase:** fronteiras pelo catálogo, no `shared` (funciona offline e no iOS). O produto é sempre o nome mais longo que o catálogo conhece, então nomes com "e" ou "de" ficam inteiros ("biscoito água e sal", "pão de queijo").
- Separa em: pausa (cada resultado da voz é separado por conta própria), vírgula, ponto, ponto-e-vírgula, quebra de linha, a palavra "e" fora de um nome do catálogo, início de um produto conhecido, início de uma quantidade seguida de produto conhecido. Vírgula e ponto entre dígitos não separam ("1,5 kg") (2026-11-21, #48).
- Só nome ou sinônimo exato (sem acento, singular) gera fronteira. A busca aproximada fica de fora, para palavras soltas não virarem produto por engano.
- Palavra de ligação ("com", "sem", "de") antes de um produto não separa: "frango com batata" é um item, com detalhe "com batata".
- Produto desconhecido vai até a próxima fronteira. Palavras sem fronteira depois de um produto viram detalhe dele.
- Antes de entrar na lista, a pessoa vê os itens entendidos, um por linha, e pode tirar algum. A tela fica para a issue da tela de voz.
- Limite conhecido: dois produtos desconhecidos seguidos, sem pausa e sem "e", viram um item só. A pessoa corrige na conferência.
- Descartados: exigir pausa ou "e" entre os itens, que joga o trabalho na pessoa; IA para separar, que mandaria a lista para fora do celular.

**Sem internet e privacidade:**
- Sem internet e sem pacote offline: "Sem internet, não consigo ouvir agora. Você pode escrever.", com a opção de escrever logo ali.
- (2026-11-22) Com o reconhecedor do aparelho, o som não sai do celular e o aviso longo sai: fica só o pedido de microfone do Android. O aviso abaixo, com o botão **Escrever** (antes "Não, vou escrever"), só aparece quando o app cair no serviço padrão.
- Aviso uma vez, junto da explicação antes de pedir o microfone: "Para você falar os produtos, o app precisa usar o microfone. Para entender o que você fala, o celular pode enviar o som ao Google." Depois fica na tela de privacidade das configurações.
- O texto reconhecido nunca vai para logs, crashes ou analytics. Só o evento `item_adicionado { origem: voz }` (seção 4.5).

**Na tela (2026-11-22, #60):**
- Botão **Falar** (com microfone) na tela de adicionar, logo abaixo de "Procurar produto". Fora do rodapé da lista, para não competir com "Adicionar" e "Terminei". Some se o celular não tiver serviço de voz.
- A explicação vem antes do pedido de microfone do Android, com **Usar o microfone** e **Não, vou escrever**. Respondendo "Não", ela volta na próxima vez.
- Ouvindo: "Estou ouvindo. Pode falar.", cada trecho entendido numa linha e **Terminei**. O TalkBack lê só o "Estou ouvindo", para o microfone não ouvir o próprio celular.
- Recados: os do guia, mais "Sem o microfone, não consigo ouvir. Você pode escrever." (permissão negada) e "Não deu certo ouvir agora. Você pode escrever." (microfone ou serviço com problema). Os que sugerem escrever abrem o teclado.
- Até a conferência (#61), o que foi falado entra direto na lista, sem a pergunta de produto repetido, com o aviso "3 produtos entraram na lista."
  - **Revisto em 2026-11-22 (#127):** o que já está na lista não entra de novo nem muda a quantidade, como no "Não" da pergunta de produto repetido; o repetido na mesma fala também entra uma vez só. O aviso diz o que entrou e, em outra linha, o que já estava ("Leite já estava na lista." ou "2 produtos já estavam na lista."). Descartados: somar na quantidade (muitas vezes a pessoa só esqueceu que já estava) e perguntar no meio da fala (fica para a conferência da #61, que mostra "já está na lista").

**Prova de conceito:** não haverá uma separada. A primeira issue da tela de voz começa medindo num celular de entrada:
- se a escuta reabre sem bipe nem atraso incômodo;
- se ~8 s de silêncio é um bom limite;
- quanto o offline erra;
- se as alternativas vêm também no offline.

Se a escuta não reabrir bem, revemos a escolha do reconhecedor.

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
- A palavra principal é o começo do nome que o catálogo reconhece: "café da marca X" entra como Café, com o detalhe "da marca X", já em Mercearia. O que não começa com produto conhecido vai para **Outros**, sem procurar produto no meio do nome ("sabão de coco" não vai para Hortifrúti). No ícone, o produto desconhecido usa o da seção em que está; em Outros, o carrinho é o ícone genérico (2026-11-21, #49).
- Se um catálogo novo trouxer o produto, o produto próprio passa a apontar para o do catálogo, sem perder o histórico.

**Evolução do catálogo:**
- O catálogo atualizado vai **junto com a atualização do app**, sem servidor.
- Origem dos termos novos: no MVP, testes de usabilidade, avaliações na loja e comentários; na fase 2, termos desconhecidos agregados (seção 4.5, #101). Só os mais frequentes vão para revisão, pela mesma curadoria.
- **O id de um produto do catálogo nunca muda nem é reaproveitado.** Produto retirado fica marcado como antigo. Dois produtos juntados viram um só, e o id antigo aponta para o novo.

**Formato da planilha mestre (2026-11-11, #15):** CSV em `catalog/`, documentado em [catalog/README.md](../catalog/README.md). `products.csv` traz um produto por linha (seção, ordem, ícone, ativo/antigo, "substituído por"); `terms.csv` traz um sinônimo ou regionalismo por linha, com região, estado, evidência e marcação de ambíguo.

### 3.6 Compartilhamento em tempo real com a família
- Exige backend. Proposta: login **anônimo** (Firebase/Supabase) + código ou link de pareamento — sem email.
- A analisar: resolução de conflitos (duas pessoas editando), custo de infraestrutura, LGPD, o que acontece ao trocar de celular.

### 3.7 Anúncios
**Decidido (2026-11-24, #82):** a versão grátis tem anúncios; a versão completa não tem nenhum. A concorrência é grande, e "sem anúncios" ajuda a vender a versão completa. Implementação na #89.

- **Formato:** intersticial (tela cheia entre uma tela e outra), com imagem, texto e vídeo. O vídeo começa sem som (`MobileAds.setAppMuted(true)`). Rewarded e banner fora.
- **Quando aparece:**
  - ao voltar da tela de adicionar para a lista, **a cada 3 vezes**;
  - depois de editar um produto, **a cada 3 edições** (contador separado do anterior);
  - depois do **Terminei**, quando a pessoa toca **Certo** ou **Desfazer** no aviso;
  - depois de **Jogar a lista fora**, quando a pessoa toca **Certo** ou **Desfazer** no aviso.
- **Nunca:**
  - com algum produto riscado (a pessoa está comprando), em nenhum dos momentos acima;
  - quando o aviso fecha sozinho depois de 15 s (a pessoa pode já ter guardado o celular);
  - na **primeira semana** de uso.
- **Limite geral:** no máximo 1 anúncio a cada 5 minutos e 5 por dia, somando todos os momentos. Configurado no bloco de anúncio do AdMob, para ajustar sem versão nova.
- **Aviso do fim da primeira semana:** no 7º dia, na primeira vez que a pessoa abre o app sem produto riscado, uma vez só, no aviso do meio da tela: "A partir de amanhã, o app continua de graça, com alguns anúncios. Se preferir sem anúncios, tem a versão completa." · **Certo** (principal) · **Conhecer a versão completa** (abre a tela da #88). A tela da versão completa explica o mesmo.
- **Conteúdo:** classificação máxima **G** (público geral). Categorias bloqueadas, pela marca Mente Alerta (contra golpes): apostas e cassino (inclusive jogos sociais de cassino), enriquecimento rápido, empréstimos e crédito, criptomoedas, namoro, astrologia e simpatias, emagrecimento, remédios e suplementos, procedimentos estéticos, saúde sexual, política e religião. Revisar a central de revisão de anúncios do AdMob de vez em quando, mais no começo.
- **Consentimento:** UMP integrado, que só pede consentimento onde a lei exige (União Europeia, Reino Unido, alguns estados dos EUA); no Brasil não aparece. Anúncios personalizados no Brasil, com a base legal (legítimo interesse, a validar ao escrever a política) na política de privacidade, e a chave **Anúncios pelo meu interesse** nos Ajustes, ligada por padrão; desligada, o app pede só anúncios não personalizados. No iPhone (fase 5), entra o pedido de rastreamento da Apple (ATT).
- **Implementação (2026-11-25, #89):**
  - Se a pessoa não abrir o app no 7º dia, o aviso aparece na primeira abertura depois dele, sem produto riscado; os anúncios só começam no dia seguinte ao aviso, para o "a partir de amanhã" ser sempre verdade.
  - Conta como edição sair da tela de edição com algo mudado ou com o produto tirado da lista. Tirado da lista, o anúncio espera o aviso "Feijão saiu da lista." e só vem depois de **Certo** ou **Desfazer**, como no Terminei.
  - Os contadores voltam a zero na 3ª vez mesmo que o anúncio seja bloqueado (produto riscado, limite do AdMob): "a cada 3" ao pé da letra.
  - Abrir outra tela também fecha o aviso; nesse caso, como no aviso que some sozinho, não há anúncio.
  - O consentimento do UMP só é pedido quando os anúncios começam, nunca na primeira semana.
  - A chave **Anúncios pelo meu interesse** fica numa tela de **Ajustes** mínima, que por enquanto só aparece na versão grátis (a tela completa vem na #70). Texto de apoio: "Desligada, ainda aparecem anúncios, mas sem usar seus interesses." · "Sua lista nunca vai para os anúncios."
  - IDs do AdMob, limite de frequência, categorias e o que muda na política: [configurar-anuncios.md](configurar-anuncios.md).
- **Riscos aceitos:** toque acidental (o "X" do anúncio é do Google, o app não controla o tamanho) e impacto na nota da loja. Acompanhar as avaliações e os números do AdMob, e ajustar frequência e limites se preciso.

### 3.8 Ordem das seções e produtos riscados
- **Ordem personalizada das seções** ("no meu mercado a padaria fica no fundo"): **decidido (2026-11-24, #79): versão completa**, fase 2, com **uma** ordem só. A ordem padrão continua grátis, e mudar a seção de um produto também (#56). A ordem **por mercado** fica para a fase 4, junto do aviso perto do mercado (3.1). Como reordenar fica para a #85.
- ~~**Mover riscados para o final da seção:** facilita ver o que falta, mas quebra a metáfora do papel. Testar com usuários na fase 0.~~ **Decidido (2026-11-22):** riscados vão para a seção "Já peguei", no fim da lista (seção 1.1).

### 3.9 Validação com usuários reais
- Testes de usabilidade com pessoas 60+ antes do lançamento (gesto de deslizar, vocabulário, tamanho de fonte, entrada por voz).
- Confirmar a legibilidade dos nomes da lista em caixa alta e do estilo quadrinhos.
- **Roteiro decidido (2026-11-22, #30):** [docs/teste-usuarios/roteiro.md](teste-usuarios/roteiro.md), com [modelo de anotação](teste-usuarios/modelo-sessao.md) por sessão.
  - Roda no **app de verdade** (APK de teste), no celular de quem conduz, com a letra no tamanho que a pessoa usa. O protótipo HTML não tem voz nem segue a letra do sistema.
  - 3 a 5 familiares 60+ que nunca viram o app, misturando idade (70+ e se possível 80+), experiência com celular, quem usa papel e quem não faz lista. Sem termo de consentimento (são da família); anotações sem nome e idade em faixa.
  - 30 a 40 min, em lugar tranquilo; uma pessoa conduz; grava a tela com áudio só se a pessoa aceitar, e apaga depois de anotar.
  - 12 tarefas contadas como situações da vida, sem as palavras do app (adicionar digitando e falando, produto repetido, editar, tirar, Ops, voltar, riscar, Terminei, jogar fora). Riscados no lugar × no fim da seção (#32) por duas imagens.
  - Por tarefa: ✅ sozinha / 🟡 com ajuda / ❌; nota final de 1 a 5 com carinhas (sem SUS). Problema em 2 ou mais pessoas é corrigido antes do lançamento.
- **Resultado da primeira sessão (2026-11-22, #33, #34):** [sessao-1.md](teste-usuarios/sessao-1.md), retorno por escrito do primeiro uso num tablet, fora do roteiro.
  - Achados e decisões: lista (tocar risca, "Já peguei", **Mudar a lista**, #121), tela de adicionar ("Adicionados", busca em verde, #122), voz (segurar para falar, sem internet, #123) e edição (**Tirar da lista** em destaque, #124). Registrados nas seções 1.1 e 3.3 e no guia de linguagem; riscados no fim fecha a #32.
  - Janela flutuante no tablet Samsung (#126): era o modo de janela lembrado pelo aparelho, não o app. Sem mudança no app; o roteiro confere a tela cheia antes da sessão.
  - Não observados: caixa alta e estilo quadrinhos, passar o dedo, **Terminei**, a voz nova e o "Ops, voltar". Sem segunda rodada antes do lançamento: o teste fica fechado com esta sessão.

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
│   ├── data/               # Banco (Room KMP), repositórios
│   └── di/                 # Módulo Koin do shared
├── androidApp/             # Jetpack Compose, Material 3, integrações Android
└── iosApp/                 # Fase 5
```

- Padrão: camadas (UI → ViewModel → casos de uso → repositório), com estado exposto via `StateFlow`.
- Integrações de plataforma (voz, notificações, localização) atrás de interfaces no `shared`, implementadas em cada plataforma.
- **Injeção de dependências (2026-11-11, #7):** Koin. O `shared` expõe o `sharedModule` (pacote `di`: repositórios como `single`, casos de uso como `factory`); cada plataforma soma o seu módulo (no Android, `appModule` com os ViewModels) ao subir o app. Interfaces ficam no pacote de quem usa; implementações em `data` ou na plataforma.
- **Modelo de domínio (2026-11-12, #35):** o produto canônico e a seção são os tipos do catálogo (`CatalogProduct`, `CatalogSection`); o domínio guarda só o `id` deles, que é o que vai para o banco (um `id` antigo leva ao produto que o absorveu). O item da lista (`ListItem`) guarda o nome que a pessoa usou, o produto (nulo se desconhecido), a seção (no próprio item, porque a pessoa pode trocar), quantidade e detalhe opcionais e se está riscado. A compra (`Purchase`) guarda a data e uma cópia dos itens comprados. Ids de item e de compra são UUID (o "Ops, voltar" devolve o mesmo item; a sincronização da fase 3 não depende de id do banco). Quantidade em `Double` com uma das unidades do §1.2; "2 leites" é 2 unidades.
- **Idioma do código (2026-11-11, #119):** nomes em inglês (classes, funções, variáveis, parâmetros, arquivos, pastas, recursos e testes). Comentários, KDoc, commits e documentos em português; texto exibido segue o [guia de linguagem](guia-de-linguagem.md). O pacote e o applicationId continuam `br.com.mentealerta.listadecompras` (identidade do app na Play Store).
- **Traduções (2026-11-11, #120):** o MVP é só pt-BR, mas o código fica pronto para outros idiomas:
  - Todo texto exibido fica em `androidApp/src/main/res/values/strings.xml` (`values/` é o pt-BR, declarado em `res/resources.properties`); nunca fixo no código. A lista de idiomas do app é gerada pelo Android (`generateLocaleConfig`).
  - Plural com `<plurals>` ("Falta 1" / "Faltam 5"); frases com marcadores (`%1$s`), nunca montadas por concatenação.
  - O `shared` devolve dados (ids, números, datas), nunca frase pronta; quem monta o texto é a UI.
  - Caixa alta, datas e números seguem o idioma do aparelho, sem pt-BR fixo.
  - O que depende do idioma no `shared` (plural da normalização, voz) recebe o idioma por parâmetro ou interface.
  - Catálogo: os ids são neutros; `name` e os termos são pt-BR. Outro idioma ganha arquivos próprios com os mesmos ids.
  - O [guia de linguagem](guia-de-linguagem.md) vale para o pt-BR; cada idioma novo precisa do seu guia.
  - Textos ficam em recursos do Android, não em Compose Multiplatform Resources; rever na fase 5, se o iPhone usar Compose.

### 4.3 Componentes

| Necessidade | Escolha | Observação |
|---|---|---|
| Linguagem | Kotlin 2.x | |
| UI Android | Jetpack Compose + Material 3 | Tema próprio com fontes grandes e alto contraste |
| Banco local | Room 3 (KMP, `androidx.room3`) + `BundledSQLiteDriver` (2026-11-12, #37) | Mesma versão do SQLite no Android e no iOS. Room 3 em vez do 2.8: é a linha feita para KMP, só com coroutines, e evita migrar todos os imports depois. Schema de cada versão em `shared/schemas/` (no git); migration nunca apaga os dados da pessoa |
| Busca e normalização | Código Kotlin próprio no `shared` | Coluna normalizada (sem acento, singular) + sinônimos + Levenshtein. O catálogo (~1.500 itens) cabe em memória — não precisa de FTS |
| Catálogo | Código Kotlin gerado no build a partir da planilha mestre (2026-11-11, #21) | Facilita a curadoria; processo na seção 3.5. Sem leitura de arquivo nem parse ao abrir o app, igual no Android e no iOS. O build valida a planilha e falha se houver erro ([catalog/README.md](../catalog/README.md)). JSON e SQLite descartados: exigiriam leitura diferente por plataforma |
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
| Compras no app | Google Play Billing (Billing Library), direto (2026-11-24, #86) | Como no app Mente Alerta: sem serviço de terceiros (só o Google vê a compra), só o produto no Play Console, loja falsa no build debug. A lógica (guardar a compra, confirmar à loja, pendente, restaurar) fica no `shared` atrás da interface `BillingStore`; o iPhone (fase 5) implementa a mesma interface com StoreKit. Sem servidor, a compra não é conferida fora do celular. RevenueCat descartado por ora: reavaliar na fase 3, com a assinatura do Plano Família e o servidor. Passo a passo: [configurar-compras.md](configurar-compras.md) |
| Anúncios | Google Mobile Ads (AdMob) + UMP | Intersticiais nos momentos da seção 3.7, nunca durante a compra |
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

**Ícones próprios da Mercearia (2026-11-22, #25):** 13 ícones em `catalog/own-icons/`, no estilo Color do Fluent e sem marca. São eles: pacote de arroz, de feijão, de açúcar, de farinha de trigo, de farinha de mandioca, de fubá e de café; óleo, azeite, vinagre, molho de tomate, achocolatado e goma de tapioca. Cobrem 28 produtos, alguns como aproximado (ex.: açúcar mascavo usa o pacote de açúcar). Arroz, feijão e café trocam a tigela, os grãos e a xícara do Fluent pelo pacote. O resto da Mercearia espera a lista por prioridade da medição de cobertura (#23).

**Ícones próprios de Frios e laticínios (2026-11-22, #26):** 9 ícones em `catalog/own-icons/`, no mesmo estilo e sem marca: caixinha de leite, pote de margarina, copo de requeijão, pote de iogurte, presunto, mortadela, linguiça, pão de queijo e queijo coalho no espeto. Cobrem 23 produtos, alguns como aproximado (ex.: apresuntado e peito de peru usam o presunto; calabresa e salsichão usam a linguiça). Linguiça e pão de queijo ficam no Açougue e na Padaria, mas entraram nesta leva. O leite troca o copo do Fluent pela caixinha, e o queijo coalho troca o queijo amarelo pelo espeto. O copo de leite continua nas bebidas de soja e vegetal.

**Ícones próprios de Limpeza e higiene (2026-11-22, #27):** 10 ícones em `catalog/own-icons/`, no mesmo estilo e sem marca: caixa de sabão em pó, frasco de detergente, galão de água sanitária, frasco de desinfetante, frasco de amaciante, rolo de papel-toalha, tubo de creme dental, desodorante em spray, absorvente e fralda. Cobrem 18 produtos, alguns como aproximado (ex.: multiuso e desengordurante usam o detergente; sabão líquido usa o amaciante; pomada usa o tubo de creme dental). A fralda aparece aberta, não no pacote, para se reconhecer em 32 px; a geriátrica é fiel e a infantil, aproximada. O papel-toalha troca o rolo do papel higiênico pelo rolo alto. O resto das duas seções espera a lista por prioridade (#23).

**Ícones próprios de Hortifrúti (2026-11-22, #28):** 10 ícones em `catalog/own-icons/`, no mesmo estilo, os primeiros da fila de prioridade 1 (`catalog/own-icons-queue.csv`): mandioca, mamão, abóbora cabotiá, chuchu, beterraba, repolho, couve-flor, quiabo, goiaba e maracujá. Cobrem 13 produtos, três como aproximado: mandioca congelada usa a mandioca, repolho roxo usa o repolho verde (a variação roxa fica para depois) e goiabada usa a goiaba, até ter a lata própria. Mamão, goiaba e maracujá aparecem abertos, para se reconhecer pela polpa; o maracujá é o amarelo, o mais comum no mercado brasileiro.

**Ícones próprios de Açougue, Mercearia e laticínios (2026-11-22, #128):** 13 ícones em `catalog/own-icons/`, no mesmo estilo e sem marca, os seguintes da fila de prioridade 1: carne moída na bandeja, fígado, miúdos de frango (coração e moela na bandeja), manta de carne-seca, caixinha de creme de leite, lata de leite em pó, bisnaga de molho, pote de tempero, caldo em tablete, lata de fermento, adoçante conta-gotas, filtro de café e caixa de aveia. Cobrem 30 produtos, 16 como aproximado: fígado de frango usa o de boi; carne de sol usa a carne-seca; creme de leite fresco, a caixinha; farinha láctea, a lata de leite em pó; tempero pronto, o caldo; fermento biológico, a lata do fermento em pó. A bisnaga tem a cor da maionese, e o pote, um tempero laranja: ketchup, mostarda e os 8 temperos ficam como aproximado até ganharem as variações de cor.

**Ícones próprios de Limpeza, Higiene e farmácia e Outros (2026-11-22, #129):** 9 ícones em `catalog/own-icons/`, no mesmo estilo e sem marca, os últimos da fila de prioridade 1: frasco de álcool, palha de aço, pano de prato dobrado, caixa de papel-alumínio com o rolo, pilha de guardanapos, dentadura, frasco de fitas de glicemia, frasco âmbar de remédio e botijão de gás. Cobrem 21 produtos, 12 como aproximado: álcool em gel usa o frasco de álcool; pano de chão, pano multiuso e flanela, o pano de prato; filme plástico e papel-manteiga, o papel-alumínio; lenço de papel, o guardanapo; soro fisiológico, água oxigenada, antisséptico e soro de reidratação, o frasco de remédio (o xarope é o fiel). Fixador e limpador de dentadura usam a própria dentadura, como aproximado. Com esta leva, a prioridade 1 da fila acaba.

**Cobertura real do catálogo v1 (2026-11-22, #23)**, página [prototipos/icones.html](prototipos/icones.html), agora gerada por `./gradlew :shared:generateIconsPage` a partir de `catalog/`:
- **662 produtos ativos:** **184 com ícone fiel** (28%; 141 Fluent e 43 próprios), **239 aproximados** (36%; 213 Fluent e 26 próprios) e **239 sem ícone** (36%), que usam o ícone da seção.
- As lacunas se concentram na Mercearia (57: temperos, molhos, misturas e salgadinhos), no Hortifrúti (53, quase só frutas e legumes regionais), em Higiene e farmácia (29) e na Limpeza (26: panos, rodo e utensílios). Padaria, Açougue e Bebidas têm poucas.
- **Fila do complemento próprio:** [`catalog/own-icons-queue.csv`](../catalog/own-icons-queue.csv). Cada linha é um desenho, que pode cobrir vários produtos (ex.: um pote de tempero serve para pimenta-do-reino, colorau, cominho e outros cinco). Os 239 produtos sem ícone viram **141 desenhos**:
  - **Prioridade 1** (compra de toda semana ou todo mês, conferida com a cesta básica do DIEESE e a POF): **32 desenhos, 64 produtos**. Exemplos: mandioca, mamão, abóbora, chuchu, carne moída, creme de leite, leite em pó, pote de tempero, bisnaga de molho, adoçante, filtro de café, álcool, palha de aço, pano, papel-alumínio, dentadura, fita de glicemia, frasco de remédio, botijão de gás.
  - **Prioridade 2** (compra comum): 57 desenhos, 103 produtos.
  - **Prioridade 3** (ocasional ou regional): 52 desenhos, 72 produtos, a maioria frutas regionais.
- A prioridade é uma estimativa, sem dado de compra real; quando houver analytics, revisar.
- A fila passa da estimativa de 50–100 ícones. Fazer a prioridade 1 e depois decidir até onde ir na 2; o resto continua com o ícone da seção.
- Os 239 aproximados ficam fora da fila: a revisão deles é a #24.

**Descartados:** Noto Emoji (mesmas lacunas, estilo mais simples), OpenMoji (CC BY-SA obriga adaptações na mesma licença; estilo menos atraente), conjunto 100% próprio (custo e prazo).

### 4.5 Privacidade, métricas e coleta de dados

**Promessa ao usuário:** *"Sua lista e seu histórico nunca saem do seu celular."*
(Não dá para prometer "nenhum dado sai do celular": a versão grátis usa AdMob, que coleta dados do aparelho.)

| Camada | Quando | Como | Regras |
|---|---|---|---|
| Crashes | MVP | Firebase Crashlytics + Play Console Android Vitals | Sem dados pessoais nos logs |
| Uso do app | MVP | Firebase Analytics com lista fechada de eventos | Nunca nome de produto; coleta automática de ID de publicidade desligada; opção para desativar nas configurações |
| Termos desconhecidos | Fase 2 | Envio agregado, sem identificador | Só com consentimento explícito ("Ajudar a melhorar o app"); filtrar termos de saúde (remédios, etc.) |
| Anúncios (só grátis) | Fase 2 | Google Mobile Ads (AdMob) + UMP | ID de publicidade só para anúncios; personalização desligável nos Ajustes ("Anúncios pelo meu interesse"); nada da lista vai para o anúncio (seção 3.7). O que entra na política e no formulário: [configurar-anuncios.md](configurar-anuncios.md) (#89) |
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
- Informar que o reconhecimento de voz pode enviar áudio ao Google quando o modo offline não estiver disponível (desde 2026-11-22, só quando o app cai no serviço padrão; ver 3.3). É a única exceção à promessa (o app em si nunca envia a lista). O aviso aparece antes do primeiro uso, junto da explicação do microfone (seção 3.3, 2026-11-21, #29).
- Nomes de produtos podem revelar saúde (dado sensível pela LGPD) — por isso o conteúdo da lista nunca é enviado.

### 4.6 Qualidade e testes

| Tipo | Ferramenta | O que cobre |
|---|---|---|
| Unitários | `kotlin.test` + Turbine | Normalização, busca, previsão, regras da lista |
| Interface | Compose UI Test | Fluxos: adicionar, riscar, Terminei, jogar fora, voltar |
| Screenshots | Roborazzi | Cada tela com fonte do sistema em 100%, 150% e 200%, claro e escuro |
| Acessibilidade | Checks próprios no Compose UI Test (`AccessibilityChecks`; o Accessibility Test Framework não roda no Robolectric) e `ContrastTest` | Área de toque, contraste, rótulos para TalkBack, texto cortado (#71) |
| Estilo/estática | ktlint + detekt | Formatação (estilo `intellij_idea`, regras de Compose) e análise estática, sem baseline; `./gradlew lint`. detekt 2.0 (alpha), o único compatível com Kotlin 2.4 (2026-11-11) |
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
| Logo | "Lista de Compras" em Bangers, branco com contorno preto, com "Mente Alerta" pequeno acima. **Na lista vazia (2026-11-23):** a arte do ícone do app (cantos arredondados, contorno e sombra de quadrinhos) e "Mente Alerta" em verde-folha logo acima do título **Lista de Compras** |
| Ícone do app | Arte da família Mente Alerta: casal 60+ com carrinho e prancheta de lista ([identidade/icone-original.png](identidade/icone-original.png)). **Decidido (2026-11-22, #10):** a arte inteira vale também no celular, sem versão simplificada. Ícone adaptativo com fundo azul-petróleo `#02979F` e a arte na área visível (72 de 108dp): no círculo, rostos, prancheta e carrinho ficam inteiros. Os cantos brancos da arte foram pintados com a cor da borda; na Play Store vai [identidade/icone-play-store.png](identidade/icone-play-store.png) (512 px, quadrado cheio). Sem versão de uma cor (ícone temático do Android 13+): a ilustração não vira silhueta, e o celular mostra o ícone colorido. Se no teste com pessoas 60+ o ícone não for reconhecido a 48 px, volta a ideia da versão simplificada ([prototipos/icone-tamanhos.html](prototipos/icone-tamanhos.html)). O logo fica para outra issue |
| Modo escuro | Segue o sistema — paleta a definir. **Grátis (2026-11-24, #79):** modo escuro e a escolha manual de claro ou escuro nos Ajustes nunca são da versão completa |

---

## 5. Grátis × Premium

**Regra:** tudo que é essencial para fazer a lista e usar no mercado, e toda a acessibilidade, é grátis. O premium vende **conforto, família e inteligência extra**. Na interface, o premium se chama **Versão completa** ([guia de linguagem](guia-de-linguagem.md)).

Revisada em 2026-11-24 (#79), à luz do MVP. A tabela diz o que **vai ser** grátis ou pago; o que já está pronto se acompanha no board.

| Funcionalidade | Grátis | Versão completa |
|---|:---:|:---:|
| Lista única, por seção na ordem do mercado, com ícones | ✅ | ✅ |
| Adicionar digitando, falando e por sugestão | ✅ | ✅ |
| Nomes do jeito da pessoa: sinônimos, regionalismos, busca aproximada | ✅ | ✅ |
| Riscar, Terminei, Desfazer | ✅ | ✅ |
| Mudar a lista: quantidade, detalhe, Tirar da lista, Jogar a lista fora | ✅ | ✅ |
| Mudar a seção de um produto (o app lembra) | ✅ | ✅ |
| Letras grandes, alto contraste, TalkBack, tela que não apaga durante a compra | ✅ | ✅ |
| Modo escuro (segue o celular ou escolhido nos Ajustes) | ✅ | ✅ |
| Mandar a lista e Colar a lista (WhatsApp), com a conferência "Lista recebida" | ✅ | ✅ |
| Backup automático (Android Auto Backup) | ✅ | ✅ |
| O que já comprei (últimos 30 dias) | ✅ | ✅ |
| Sugestões dentro do app: "Está faltando?" e "Você costuma comprar" | ✅ | ✅ |
| **Sem anúncios** | — | ✅ |
| O que já comprei sem limite de tempo | — | ✅ |
| Lembretes fora do app (lista grande, produto atrasado) | — | ✅ |
| "Os de sempre" (pôr os produtos fixos na lista com um toque) | — | ✅ |
| Atalho na tela do celular (widget) | — | ✅ |
| Ordem das seções do meu mercado | — | ✅ |
| Temas decorativos: cores e estilos de papel | — | ✅ |
| "Quanto gastei" e estatísticas de gastos | — | ✅ |
| Aviso ao chegar perto do mercado (fase 4) | — | ✅ |
| Lista compartilhada em tempo real com a família (fase 3) | — | Plano Família |

**Fronteiras decididas (2026-11-24, #79):**
- **Sugestões:** o grátis sugere **dentro do app**, quando a pessoa abre, e ela põe **um a um**. A versão completa avisa **fora do app** (notificação) e tem a **lista fixa da pessoa**, que entra toda de uma vez. As sugestões do grátis usam o **histórico inteiro**, não só os 30 dias que a tela mostra: a inteligência básica não piora para quem não paga.
- **Histórico:** as compras antigas ficam no celular (e no backup); quem compra a versão completa vê tudo na hora.
- **Temas:** todos com contraste de pelo menos 4,5:1, os mesmos tamanhos e o estado nunca só por cor. Tema **mais fácil de ler** que o padrão vai para o grátis: a versão completa vende enfeite, nunca legibilidade.
- **Atalho na tela do celular:** sem versão grátis (o ícone do app já abre direto na lista). O app oferece um botão que põe o atalho sozinho, sem a pessoa procurar o widget (#97).
- **Ordem das seções:** uma ordem personalizada; a ordem por mercado fica para a fase 4 (seções 3.1 e 3.8).
- **Quanto gastei:** o app só pede o valor a quem tem a versão completa; o Terminei do grátis fica simples como é (#84).
- **Anúncios:** a versão grátis tem intersticiais em momentos de pausa, nunca durante a compra; a versão completa não tem nenhum (2026-11-24, #82, seção 3.7).

### Modelo de cobrança (híbrido)

| Produto | Tipo | Fase | Quem paga | O que libera |
|---|---|---|---|---|
| **Premium para sempre** (na interface: **Versão completa**) | Compra única | 2 | O próprio usuário | Tudo da coluna Versão completa, de hoje e do futuro, exceto a lista compartilhada |
| **Plano Família** | Assinatura (mensal/anual) | 3 | Filho/neto | Lista compartilhada em tempo real + versão completa liberada em todos os celulares pareados |

**Por quê:**
- O público 60+ desconfia de assinatura; sem backend não há custo recorrente que a justifique.
- A sincronização (fase 3) gera custo recorrente — a assinatura entra junto com ela, voltada a quem a aceita melhor (filhos/netos).
- O Google Play não permite presentear compras dentro do app; o "Dar de presente" só fica viável com celulares pareados (Plano Família).
- Quem comprou a versão completa mantém tudo o que comprou e ganha o que entrar nela depois, menos o que precisa de servidor (família) — "para sempre" tem que ser para sempre (2026-11-24, #79).
- "Premium para sempre" é o nome interno; no Play Console, o produto é `versao_completa` (compra única, não consumível; 2026-11-24, #86). O nome do Plano Família na interface, e como falar da cobrança mensal sem "assinar", fica para a fase 3.
- Meios de pagamento no Google Play Brasil a confirmar (Pix, operadora, vale-presente) — relevantes para quem não tem cartão.

**Ideias de comercialização:**
- "Dar de presente" via Plano Família: filhos/netos pagam e liberam a versão completa dos pais.
- Anúncios só na versão grátis, nos momentos de pausa e nunca durante a compra (seção 3.7, #82). Na primeira semana, sem anúncio; no 7º dia, um aviso diz que o app continua de graça, com anúncios.

---

## 6. Fases sugeridas

| Fase | Escopo |
|---|---|
| **0 — Fundação** | Decidir stack, montar catálogo inicial e ícones, protótipo navegável, teste com 3–5 pessoas 60+ |
| **1 — MVP Android (grátis)** | Seção 1 completa, publicação na Play Store |
| **2 — Monetização** | Anúncios + versão completa (sem anúncios, histórico completo, lembretes, atalho na tela do celular, "Os de sempre", ordem das seções) |
| **3 — Família** | Backend com login anônimo, lista compartilhada em tempo real |
| **4 — Localização** | Aviso perto do mercado (após análise da seção 3.1) |
| **5 — iPhone** | Publicação na App Store |
