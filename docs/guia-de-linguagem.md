# Guia de Linguagem

> Referência obrigatória para todo texto do app. Versão inicial — validar nos testes com pessoas 60+ da fase 0 (principalmente "Seção" e "Desfazer").

## Princípios

1. **Tratar por "você"**, com frases curtas (até ~10 palavras) e na ordem direta.
2. **Respeitar sem infantilizar.** Nada de "vovó", excesso de diminutivos, exclamações demais ou emoji em mensagens importantes.
3. **Botões dizem a ação concreta.** Nunca "OK" ou "Confirmar": usar "Sim, jogar fora" e "Não, deixar como está". Para só fechar um aviso, **Certo** (2026-11-23).
4. **Nunca culpar a pessoa.** "Não entendi, pode falar de novo?", nunca "Erro no reconhecimento de voz".
5. **Ícone sempre acompanhado de texto.** Nenhum botão só com ícone. Única exceção: a lixeira de cada produto em **Adicionados** e na conferência da lista colada, que se repete em cada linha; o TalkBack lê "Tirar leite da lista" (2026-11-23; conferência em 2026-11-24, #68).
6. **Voltar atrás em vez de pedir confirmação.** Confirmação só para "Jogar a lista fora"; nas outras ações, aviso no meio da tela com **Desfazer** e **Certo** (2026-11-23, no lugar de "Ops, voltar", pedido no teste do app).
7. **Datas relativas e por extenso:** "ontem", "há 12 dias", "semana passada".
8. **Sem siglas e sem palavras em inglês.**
9. **Caixa alta liberada em textos curtos** (logo, títulos, botões, rótulos, avisos de poucas palavras e nomes da lista) — faz parte do estilo quadrinhos. Frase com mais de uma linha, sempre em caixa normal. O texto é escrito em caixa normal e as maiúsculas vêm do estilo, para o TalkBack não soletrar palavras como se fossem siglas.
10. **Nada de texto longo.** Se precisa de um parágrafo para explicar, a tela está complicada demais.

## Termos

| ❌ Não usar | ✅ Usar |
|---|---|
| Apagar / excluir item | **Tirar da lista** |
| Apagar / limpar lista | **Jogar a lista fora** |
| Finalizar compra | **Terminei** |
| Ops, voltar *(até 2026-11-23)* | **Desfazer** |
| OK (fechar aviso) | **Certo** |
| Salvar | *(não existe; tudo é salvo sozinho)* |
| Item | **Produto** |
| Categoria | **Seção** (como no mercado: "seção de limpeza") |
| Buscar / pesquisar | **Procurar** |
| Reconhecimento de voz / microfone | **Falar** |
| Compartilhar | **Mandar a lista** / **Mandar pelo WhatsApp** |
| Sincronizar | **Lista da família** |
| Notificação | **Aviso** / **Lembrete** |
| Configurações | **Ajustes** |
| Histórico | **O que já comprei** |
| Sugestões | **Está faltando?** / **Você costuma comprar** |
| Premium / upgrade / assinar | **Versão completa** / **Comprar a versão completa** |
| Clicar / tap | **Tocar** |
| Swipe / deslizar | **Passar o dedo** |
| Widget | **Atalho na tela do celular** |
| Erro / falha | **Não deu certo** |
| Cancelar | **Não** / **Deixa pra lá** |
| Permissão | *(explicar o motivo antes)* "Para você falar os produtos, o app precisa usar o microfone." |

## Exemplos de mensagens

| Situação | Mensagem |
|---|---|
| Lista vazia | Título **Lista de Compras** · "Toque em **Adicionar** para começar sua lista." (2026-11-22) |
| Campo da tela de adicionar | "Digite ou fale o que quer comprar." · resultados sob **Adicionar na lista de compras** · lista **Adicionados** (2026-11-22) |
| Produto posto na lista | "Adicionado: Farinha." (serve para masculino e feminino; 2026-11-22) |
| Falado que já estava na lista (não entra de novo) | "Leite já estava na lista." · com vários, "2 produtos já estavam na lista." — em outra linha, depois do que entrou (2026-11-22, #127) |
| Produtos riscados | Seção **Já peguei**, no fim da lista (2026-11-22) |
| Mudar produtos da lista | Botão **Mudar a lista** (não "Corrigir": a pessoa não errou); **Jogar a lista fora** fica lá dentro (2026-11-22) |
| Produto riscado | *(só o risco e a vibração, sem mensagem)* |
| Produto tirado da lista (edição ou lixeira de **Adicionados**) | "Feijão saiu da lista." · **Desfazer** · **Certo** (no meio da tela; 2026-11-23) |
| Jogar a lista fora | "Jogar a lista fora? Os 12 produtos serão tirados." · **Sim, jogar fora** · **Não** |
| Lista jogada fora | "A lista foi jogada fora." · **Desfazer** · **Certo** |
| Terminei | "Pronto! 8 produtos comprados. Ficaram 3 na lista para a próxima vez." · **Desfazer** · **Certo** |
| Botão de voz (segurar para falar) | "Segure para falar." acima de **Falar**; com o microfone aberto, o botão vira **Terminei** (2026-11-22, #123) |
| Toque rápido no **Falar**, sem segurar | "Segure o botão enquanto fala." |
| Fim da primeira semana (uma vez, no 7º dia) | "A partir de amanhã, o app continua de graça, com alguns anúncios. Se preferir sem anúncios, tem a versão completa." · **Certo** · **Conhecer a versão completa** (2026-11-24, #82) |
| Versão completa | Botão **Versão completa** no fim da folha e na lista vazia, só na versão grátis. Na tela: "Pague uma vez e use para sempre." · "Por R$ 19,90, uma vez só." · **O que vem** (um item por linha, com ✓, terminando em "E o que entrar depois") · "Fazer a lista e usar no mercado continua de graça." · **Comprar a versão completa** · **Já comprei**. Comprada: "Você tem a versão completa. Obrigado!" Recados: "Quando o pagamento for confirmado, a versão completa é liberada sozinha." · "Não consegui falar com a loja. Confira a internet e tente de novo." · "Não deu certo. Tente de novo mais tarde." · "Não achei a compra nesta conta do Google." Desistir na janela da loja não mostra recado (2026-11-24, #88) |
| Chave de anúncios nos Ajustes | **Anúncios pelo meu interesse**, com o texto de apoio "Desligada, ainda aparecem anúncios, mas sem usar seus interesses." e, em outra linha, "Sua lista nunca vai para os anúncios." Botão **Ajustes** no fim da folha e na lista vazia, por enquanto só na versão grátis (2026-11-24, #82; texto em 2026-11-25, #89) |
| Voz não entendida | "Não entendi. Pode falar de novo?" |
| Voz pt-BR ainda baixando | "Estou baixando a voz para ouvir sem internet. Você pode escrever enquanto isso." (2026-11-22, #123) |
| Voz sem internet (só em erro de rede de verdade) | "Sem internet, não consigo ouvir agora. Você pode escrever." |
| Antes de pedir o microfone (uma vez) | "Para você falar os produtos, o app precisa usar o microfone. Para entender o que você fala, o celular pode enviar o som ao Google." · **Usar o microfone** · **Escrever** — só quando o app cai no serviço de voz padrão (2026-11-22) |
| Voz para usar sem internet (configurações, Android 13+) | **Baixar a voz para usar sem internet** |
| Histórico | Botão e título **O que já comprei**; cada compra "Hoje, 2 produtos" · "Ontem, 1 produto" · "Há 12 dias, 8 produtos"; sem compras, "Aqui aparecem as compras de quando você toca em **Terminei**." (2026-11-23, #66) |
| Mandar a lista | Botão **Mandar a lista** no fim da folha; o texto começa com "Lista de compras" e traz só o que falta, por seção: "- Leite desnatado (2 litros)" (2026-11-24, #67) |
| Colar uma lista recebida | "Copiou uma lista no WhatsApp?" acima de **Colar a lista**; conferência com o título **Lista recebida**, "Toque na lixeira para tirar o que não quer.", "Leite já está na lista." ou "2 produtos já estão na lista.", **Pôr 5 produtos na lista** · **Deixa pra lá**; sem produto, "Não achei produtos no texto copiado." (2026-11-24, #68) |
| Sugestão | "Faz 12 dias que você comprou café. Está faltando?" · **Sim, pôr na lista** · **Não** |
| Lembrete | "Sua lista já tem 20 produtos. Que tal ir ao mercado?" |
