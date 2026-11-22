# Roteiro: teste manual com TalkBack e fonte grande

Auditoria de acessibilidade antes do lançamento (#71, [plano §1.7](../plano-implementacao.md#17-acessibilidade)). Os checks automáticos (`ScreensAccessibilityTest`) conferem área de toque, rótulos, texto cortado e palavra partida em todas as telas. Este roteiro cobre o que só se confere no celular: o que o TalkBack fala, a ordem e os gestos.

## Preparação

- Celular Android com o app instalado (`./gradlew :androidApp:installDebug`), em português.
- Lista com uns 6 produtos de seções diferentes, um deles com nome longo e um com detalhe e quantidade (ex.: "2 litros de leite desnatado").
- TalkBack: Configurações → Acessibilidade → TalkBack. Gestos: deslizar para a direita/esquerda passa de item; tocar duas vezes ativa; o menu de ações abre deslizando para cima e depois para a direita, ou tocando com três dedos (depende do aparelho).

## 1. Lista

| # | Faça | Deve ouvir / acontecer | OK? |
|---|---|---|---|
| 1.1 | Abra o app e passe pelos itens | Título de seção como "título" ("Hortifrúti, título"); cada produto em caixa normal, com detalhe e quantidade ("Leite desnatado, 2 litros"); o ícone não é lido sozinho | |
| 1.2 | Toque duas vezes num produto | Vibra; o produto vai para "Já peguei" e é lido com "Comprado"; nenhuma mensagem | |
| 1.3 | Toque duas vezes num produto riscado | Volta para a seção dele, sem "Comprado" | |
| 1.4 | Abra o menu de ações de um produto | "Mudar" e "Tirar da lista" | |
| 1.5 | Ação "Mudar" | Abre a edição do produto | |
| 1.6 | Ação "Tirar da lista" | O produto sai e o TalkBack lê "Feijão saiu da lista." com "Ops, voltar" | |
| 1.7 | Toque duas vezes em "Ops, voltar" | O produto volta | |
| 1.8 | Passe até o rodapé | "Adicionar" e, com algo riscado, "Terminei" com "Faltam N" | |
| 1.9 | Toque duas vezes em "Terminei" | Lê "Pronto! N produtos comprados…" com "Ops, voltar" | |
| 1.10 | Riscado algum produto, deixe o celular parado | A tela não apaga | |

## 2. Mudar a lista e jogar fora

| # | Faça | Deve ouvir / acontecer | OK? |
|---|---|---|---|
| 2.1 | "Mudar a lista" | "Toque no produto que quer mudar."; cada produto como botão, com a ação "Mudar" | |
| 2.2 | "Jogar a lista fora" | A pergunta é anunciada: "Jogar a lista fora?", quantos produtos saem, "Sim, jogar fora" e "Não" | |
| 2.3 | "Sim, jogar fora" | Volta para a lista e lê "A lista foi jogada fora." com "Ops, voltar" | |

## 3. Edição

| # | Faça | Deve ouvir / acontecer | OK? |
|---|---|---|---|
| 3.1 | Passe pelos itens | "Voltar para a lista"; campo "Nome" com o nome; "Quantidade, título"; a quantidade como se fala ("meio quilo"); "Menos" e "Mais" | |
| 3.2 | Detalhe vazio | Lido como "Detalhe, Ex.: desnatado, da marca X" | |
| 3.3 | Toque em "Mais" | A quantidade nova é lida sozinha | |
| 3.4 | Quantidade em 1 | "Menos" lido como desativado | |
| 3.5 | "Tirar da lista" | Volta para a lista com o aviso e "Ops, voltar" | |

## 4. Adicionar

| # | Faça | Deve ouvir / acontecer | OK? |
|---|---|---|---|
| 4.1 | "Adicionar" | O campo de busca é lido com o rótulo; "Falar" com "Segure para falar." | |
| 4.2 | Digite "2 litros de lei" | Cada opção como botão, em caixa normal, com a quantidade; "Pôr “lei” na lista" no fim | |
| 4.3 | Toque duas vezes numa opção | "Adicionado: Leite." é lido sozinho | |
| 4.4 | Digite um produto que já está na lista | A pergunta "… já está na lista. Quer mais um?" é lida sozinha | |
| 4.5 | "Falar" com o TalkBack | Toque abre o microfone e lê só "Estou ouvindo"; "Terminei" fecha | |

## 5. Fonte grande (sem TalkBack)

Configurações → Tela → Tamanho da fonte no máximo (e "Tamanho da tela" no máximo, se houver).

| # | Confira | OK? |
|---|---|---|
| 5.1 | Nenhum texto cortado ou com reticências em nenhuma tela | |
| 5.2 | Palavra longa quebra com hífen ("DESNA-TADO"), nunca no meio sem hífen | |
| 5.3 | Todos os botões alcançáveis rolando (inclusive "Tirar da lista" na edição) | |
| 5.4 | Modo escuro: textos legíveis e o riscado aparece pelo traço e pela marca, não só pela cor | |

## Registro

Ao terminar, registrar na #71: aparelho e versão do Android, versão do TalkBack, data, o que falhou (número da linha) e o que foi corrigido.
