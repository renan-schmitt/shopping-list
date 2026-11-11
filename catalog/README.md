# Catálogo

Fonte do catálogo de produtos embarcado no app. Os arquivos ficam em CSV (UTF-8, separado por vírgula) para serem curados à mão e versionados no git. Processo de curadoria: [plano §3.5](../docs/plano-implementacao.md#35-curadoria-do-catálogo-e-dos-ícones).

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
