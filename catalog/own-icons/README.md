# Ícones próprios

Complemento próprio aos ícones do Fluent Emoji, para os produtos que não têm um ícone fiel ([plano §4.4](../../docs/plano-implementacao.md#44-ícones)).

- Um SVG por ícone: `<id>.svg`, com o `id` em minúsculas, números e `_` (ex.: `rice_package.svg`). Na planilha, o produto usa `own:<id>`.
- Mesmo estilo da variação **Color** do Fluent: área 32×32, degradês suaves, sem contorno.
- Evitar filtros e máscaras: o VectorDrawable não tem esses recursos. O build converte o SVG mesmo assim, mas descarta os filtros e avisa sobre as máscaras.
- O build converte cada SVG daqui em VectorDrawable (`catalog_own_<id>`) junto com os do Fluent.
