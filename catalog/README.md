# Catálogo

Fonte do catálogo de produtos embarcado no app. Os arquivos ficam em CSV (UTF-8, separado por vírgula) para serem curados à mão e versionados no git. Nomes e termos são em pt-BR; os `id` valem para todos os idiomas, e uma tradução futura ganha arquivos próprios com os mesmos `id` ([plano §4.2](../docs/plano-implementacao.md#42-arquitetura), traduções). Processo de curadoria: [plano §3.5](../docs/plano-implementacao.md#35-curadoria-do-catálogo-e-dos-ícones).

## `sections.csv` — seções do mercado

Lista fechada das seções que agrupam a lista de compras. Na interface a palavra é sempre **"Seção"**, nunca "Categoria" ([guia de linguagem](../docs/guia-de-linguagem.md)).

| Coluna | O que é |
|---|---|
| `id` | Identificador fixo, em inglês. **Nunca muda nem é reaproveitado.** |
| `name` | Nome exibido, em caixa normal (a caixa alta vem do estilo) |
| `order` | Posição na lista, na ordem típica do mercado (1 = primeira) |
| `fluent_icon` | Nome da pasta do ícone em [`microsoft/fluentui-emoji`](https://github.com/microsoft/fluentui-emoji/tree/main/assets), variação Color |

Regras:

- O ícone da seção é o reserva do produto que não tem ícone próprio.
- `other` ("Outros") é a seção dos produtos que o app não reconhece e fica sempre por último.
- A ordem pode mudar livremente; o `id` não.
- A coluna de seção da planilha mestre de produtos (#15) usa os `id` deste arquivo.

## `products.csv` — produtos

Uma linha por produto do catálogo.

| Coluna | O que é |
|---|---|
| `id` | Identificador fixo, em inglês, minúsculas e `_` (ex.: `french_bread`). **Nunca muda nem é reaproveitado.** |
| `name` | Nome canônico, em caixa normal |
| `section` | `id` de uma seção de `sections.csv` |
| `order` | Posição dentro da seção, de 10 em 10, para encaixar produtos no meio sem renumerar |
| `icon` | `fluent:<pasta>` (pasta em [`microsoft/fluentui-emoji`](https://github.com/microsoft/fluentui-emoji/tree/main/assets), variação Color) ou `own:<id>` (complemento próprio). Vazio = ícone da seção |
| `icon_fit` | `exact` (o ícone parece o produto) ou `approx` (aproximado, ex.: baguete no lugar do pãozinho). Vazio quando não há ícone |
| `status` | `active` (no app) ou `retired` (antigo, fora do app) |
| `replaced_by` | `id` do produto que absorveu este, quando dois produtos foram juntados |
| `notes` | Observações livres, só para a curadoria |

Regras:

- Produto não se apaga: quando sai do catálogo, vira `retired`, e o `id` fica reservado para sempre.
- Ao juntar dois produtos, o que sai vira `retired` com `replaced_by` apontando para o que fica. O histórico de quem usava o antigo passa para o novo.
- `replaced_by` só aparece em produto `retired` e aponta para um produto `active`.
- Os ícones `approx` e os vazios formam a lista do complemento próprio ([plano §4.4](../docs/plano-implementacao.md#44-ícones)).

Exemplo de junção (ilustrativo, fora do arquivo):

```csv
id,name,section,order,icon,icon_fit,status,replaced_by,notes
bread_roll,Pãozinho,bakery,20,,,retired,french_bread,Juntado com pão francês
```

## `terms.csv` — sinônimos e regionalismos

Um termo por linha. Cada termo leva a um produto, e o app mostra sempre o nome que a pessoa usou. Plural, acento e maiúsculas a normalização já resolve: não precisa cadastrar "maçãs" para "maçã".

| Coluna | O que é |
|---|---|
| `product_id` | `id` do produto em `products.csv` |
| `term` | O termo, em minúsculas e com acento |
| `regions` | Vazio = Brasil todo. Senão, grupos separados por `;`: `north`, `northeast`, `central_southeast`, `south` |
| `status` | `to_confirm` (a confirmar), `confirmed` (confirmado) ou `rejected` (recusado) |
| `ambiguous` | `yes` quando o termo pode levar a outro produto (ex.: "bergamota", "goma"); senão `no` |
| `evidence` | Link ou fonte: site de mercado da região ou dicionário que marque o termo como regional |
| `notes` | Observações livres, só para a curadoria |

Quando um termo entra no app:

| `status` | `ambiguous = no` | `ambiguous = yes` |
|---|---|---|
| `to_confirm` | entra | **não entra** (aguarda revisão humana) |
| `confirmed` | entra | entra (revisado por uma pessoa) |
| `rejected` | não entra | não entra |

Regras:

- Termo vindo da IA começa como `to_confirm`.
- Regionalismo (`regions` preenchido) só vira `confirmed` com `evidence`. Sinônimo do Brasil todo pode ser `confirmed` sem evidência.
- Termo `ambiguous = yes` só vira `confirmed` por decisão humana, nunca só pela IA.
- `rejected` fica no arquivo, para ninguém cadastrar o termo de novo.
- Um termo pode aparecer em mais de uma linha (um termo ambíguo com produtos candidatos), mas só uma delas pode entrar no app.
- Um termo nunca repete o `name` de um produto.

## Geração do catálogo embarcado

O build transforma estes CSV em código Kotlin (`EmbeddedCatalog`, no pacote `catalog` do `shared`) e valida todas as regras acima. Só entram no app as seções, os produtos `active` e os termos que entram pela tabela acima; os `retired` ficam só como `id` reservado e `replaced_by`.

- Gerar e validar: `./gradlew :shared:generateCatalog` (também roda em toda compilação do `shared` e no `check`)
- Código gerado em `shared/build/generated/catalog/`, fora do git: a mesma planilha gera sempre o mesmo arquivo
- Se houver erro, o build falha e lista cada um com arquivo, linha e motivo
- O script fica em `build-logic/` (plugin `listadecompras.catalog`), com testes em `build-logic/src/test/`

### Ícones

- `fluent-svg/`: SVG da variação Color dos ícones do [Fluent Emoji](https://github.com/microsoft/fluentui-emoji) (licença MIT, em [`docs/licencas/MIT-FluentEmoji.txt`](../docs/licencas/MIT-FluentEmoji.txt)) usados pelas seções e pelos produtos `active`. Ficam no git: o build não depende da internet, e os mesmos SVG servem para o iPhone.
- `fluent-icons.txt`: todas as pastas do Fluent com SVG Color, para conferir nomes sem internet.
- `own-icons/`: SVG do complemento próprio; `own:<id>` exige o arquivo `own-icons/<id>.svg` (ver [`own-icons/README.md`](own-icons/README.md)).
- Usou um ícone Fluent novo na planilha? Rode `./gradlew :shared:downloadFluentIcons`. A tarefa baixa os SVG que faltam, apaga os que ninguém usa e regrava `fluent-icons.txt`, sempre do mesmo commit do Fluent (`FLUENT_COMMIT`, em `build-logic/.../catalog/FluentIcons.kt`). Ícone com tom de pele usa o tom padrão. Sem isso, o build falha dizendo qual SVG falta.
- No build do app, cada SVG vira um VectorDrawable (`catalog_fluent_red_apple`, `catalog_own_<id>`), gerado em `androidApp/build/generated/`, fora do git. Os filtros do Fluent (desfoque e sombra) não existem no VectorDrawable e são descartados; a diferença no ícone é mínima.
- No app, `CatalogProduct.iconRes(section)` devolve o ícone do produto ou, sem ícone próprio, o da seção.
- Para conferir todos os ícones convertidos: screenshot `androidApp/src/test/screenshots/catalog_icons.png`.
