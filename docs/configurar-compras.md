# Configurar a compra da versão completa

Compra única **"Premium para sempre"** (na interface: **Versão completa**), direto pelo Google Play Billing ([plano §4.3](plano-implementacao.md#43-componentes) e [§5](plano-implementacao.md#5-grátis--premium)). Não há servidor nem serviço de terceiros: só o produto no Play Console.

## Como o código funciona

- `shared/.../premium/BillingStore.kt`: a interface da loja; `PremiumRepository.kt` diz se a pessoa tem a versão completa.
- `shared/.../data/StorePremiumRepository.kt`: guarda a compra nas preferências (funciona sem internet), confirma cada compra à loja (sem isso o Google devolve o dinheiro em 3 dias), libera o pagamento pendente (Pix, boleto) só quando a loja confirma e restaura em silêncio quando o app abre.
- `androidApp/.../billing/PlayBillingStore.kt`: a loja de verdade, usada no build **release**.
- `androidApp/src/debug/.../billing/DebugBillingStore.kt`: loja falsa do build **debug**. Comprar libera na hora; limpar os dados do app volta para a versão grátis.

## No Play Console (uma vez)

1. Enviar um build **release** assinado para a faixa de **teste interno** (o Play Console só deixa criar produtos depois de um app com a permissão de cobrança, que vem da Billing Library).
2. **Monetizar com o Play → Produtos → Produtos únicos → Criar produto:**
   - ID do produto: `versao_completa` (não muda depois de criado; é o `FULL_VERSION_PRODUCT_ID` do código)
   - Nome: "Versão completa"; descrição curta, seguindo o [guia de linguagem](guia-de-linguagem.md)
   - Opção de compra: **comprar**, não consumível; preço do item 3 da [seção 2 do plano](plano-implementacao.md#2-pontos-em-aberto) (enquanto não decidido, um preço provisório; pode mudar depois)
   - Ativar o produto
3. **Configuração → Teste de licença:** pôr as contas Google de quem vai testar e deixar a resposta como `RESPOND_NORMALLY`.
4. Na faixa de teste interno, pôr as mesmas contas como testadores e abrir o link de participação no celular.

## Testar em sandbox

Com a conta de teste no celular e o app instalado pela Play Store (faixa de teste interno):

| Teste | Como | Esperado |
|---|---|---|
| Comprar | "Comprar a versão completa" → cartão de teste "sempre aprovado" | Versão completa liberada na hora |
| Cancelar | Fechar a janela do Google Play | Continua a versão grátis, sem mensagem de erro |
| Pagamento pendente | Cartão de teste "aprovado com atraso" | Não libera; libera sozinha quando o pagamento cai (alguns minutos) |
| Pendente recusado | Cartão de teste "recusado com atraso" | Continua a versão grátis |
| Restaurar | Desinstalar, instalar de novo e tocar "Já comprei" (ou só abrir o app) | Versão completa de volta |
| Sem internet | Modo avião depois de comprar e abrir o app | Continua liberada |
| Reembolso | Play Console → Pedidos → reembolsar (com "remover direito") e abrir o app | Volta para a versão grátis |
| Confirmação | Play Console → Pedidos, depois de 5 min | Pedido sem reembolso automático (compra confirmada) |

As compras de teste não são cobradas. Na sandbox, o reembolso automático por falta de confirmação acontece em minutos, não em 3 dias: é o que mostra se a confirmação funciona.
