# Configurar os anúncios da versão grátis

Intersticiais do AdMob nos momentos de pausa, nunca durante a compra; a versão completa não tem nenhum ([plano §3.7](plano-implementacao.md#37-anúncios), #89).

## Como o código funciona

- `shared/.../ads/Ads.kt`: os momentos contados, o que o app lembra (primeiro uso, aviso do fim da primeira semana, contadores, chave dos Ajustes) e a interface `InterstitialAds`.
- `shared/.../domain/AdsUseCases.kt`: as regras. Primeira semana sem anúncio; aviso no 7º dia (ou na primeira abertura depois dele, sem produto riscado), uma vez só; anúncios a partir do dia seguinte ao aviso; a cada 3 voltas da tela de adicionar e a cada 3 edições; nunca com produto riscado nem com a versão completa.
- `androidApp/.../ui/list/ListViewModel.kt`: chama as regras nos momentos. Depois do **Terminei** e do **Jogar a lista fora**, só quando a pessoa toca **Certo** ou **Desfazer** (nunca quando o aviso some sozinho ou outra tela fecha o aviso).
- `androidApp/.../ads/AdMobInterstitialAds.kt`: o SDK. Pergunta ao UMP se precisa de consentimento (só onde a lei exige; no Brasil não aparece) e só então sobe o AdMob, com classificação máxima G e vídeo sem som. Deixa um anúncio pronto; sem anúncio pronto, não mostra nada. Chave desligada nos Ajustes: pede anúncio não personalizado (`npa=1`).
- **Build debug:** sempre os IDs de teste do Google, e cada **minuto** conta como um dia da primeira semana (`src/debug/.../ads/AdDaysFactory.kt`). Instalado do zero, o aviso aparece depois de 6 minutos (ao reabrir o app) e os anúncios de teste 1 minuto depois do aviso.
- **Build release:** dias do calendário e os IDs de `gradle.properties` (abaixo). Sem eles, usa os IDs de teste: **não publicar assim**.

O limite de frequência e as categorias bloqueadas ficam no AdMob, não no código: dá para ajustar sem versão nova.

## No AdMob (uma vez) — pendente

1. Criar a conta do AdMob (se ainda não houver) e o app Android `br.com.mentealerta.listadecompras`.
2. **Bloco de anúncios → Intersticial**, com imagem, texto e vídeo. Em **Configurações avançadas → Limite de frequência**: **1 a cada 5 minutos** e **5 por dia**.
3. Copiar os IDs para `gradle.properties` (não são segredo):
   ```properties
   admob.appId=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
   admob.interstitialId=ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ
   ```
4. **Controles da marca → Classificação de conteúdo:** máximo **G**.
5. **Controles da marca → Categorias confidenciais e gerais:** bloquear apostas e cassino (inclusive jogos sociais de cassino), enriquecimento rápido, empréstimos e crédito, criptomoedas, namoro, astrologia e simpatias, emagrecimento, remédios e suplementos, procedimentos estéticos, saúde sexual, política e religião.
6. **Privacidade e mensagens:** criar as mensagens de consentimento da União Europeia/Reino Unido (GDPR) e dos estados dos EUA, em português, com o link da política de privacidade (#75).
7. **app-ads.txt:** publicar no site do desenvolvedor informado na Play Store a linha que o AdMob mostra.
8. Revisar a **central de revisão de anúncios** de vez em quando, mais nas primeiras semanas.

## Política de privacidade e Segurança dos Dados

Com os anúncios, a política (#75) e o formulário da Play Store (#76) precisam dizer:

- A versão grátis usa o Google AdMob, que coleta o **ID de publicidade** e dados do aparelho (modelo, sistema, IP aproximado) para mostrar e medir anúncios e evitar fraude. Nada da lista nem do histórico vai para os anúncios.
- Anúncios personalizados no Brasil com base no **legítimo interesse** (a validar ao escrever a política); a chave **Anúncios pelo meu interesse** nos Ajustes desliga a personalização. Onde a lei exige, o app pede consentimento antes (UMP).
- A versão completa não tem anúncios.
- Formulário: marcar coleta de **Identificadores do dispositivo** (ID de publicidade) e **Informações e desempenho do app**, compartilhados com o Google para **Publicidade ou marketing** e **Prevenção de fraudes**; e declarar que o app **contém anúncios**. A permissão `AD_ID` vem do SDK do AdMob.

## Testar

| Teste | Como (build debug) | Esperado |
|---|---|---|
| Primeira semana | Instalar do zero e usar por menos de 6 min | Nenhum anúncio, nenhum aviso |
| Aviso | Depois de 6 min, mandar o app para segundo plano e voltar, sem produto riscado | "A partir de amanhã…", uma vez só; **Conhecer a versão completa** abre a tela da versão completa |
| Começo dos anúncios | 1 min depois do aviso, voltar da tela de adicionar 3 vezes | Anúncio de teste na 3ª |
| Comprando | Riscar um produto e voltar da tela de adicionar 3 vezes | Nenhum anúncio |
| Terminei | **Terminei** → **Certo** | Anúncio de teste |
| Aviso some sozinho | **Terminei** e esperar 15 s | Nenhum anúncio |
| Chave | Ajustes → desligar **Anúncios pelo meu interesse** | Próximos anúncios não personalizados |
| Versão completa | Comprar (loja falsa) | Nenhum anúncio; somem o botão **Ajustes** e o aviso |
| Consentimento | Rodar com um aparelho de teste com geografia da UE (`ConsentDebugSettings`) | Aparece o formulário do UMP antes do primeiro anúncio |
