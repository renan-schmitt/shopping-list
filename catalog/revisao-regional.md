# Revisão regional do catálogo

Lista curta para os revisores de cada região ([plano §3.5](../docs/plano-implementacao.md#35-curadoria-do-catálogo-e-dos-ícones)). Cada pessoa olha só a seção da sua região e responde, para cada termo: **"aqui se fala assim"**, **"aqui não se fala"** ou **"aqui isso é outra coisa"** (e qual).

- **Ambíguos**: o termo pode levar ao produto errado, com seção e ícone errados. Ficam fora do app até uma pessoa decidir: vão para o produto mais comum ou ficam fora dos sinônimos.
- **A confirmar**: regionalismos sugeridos pela IA. Já entram no app; a revisão confirma ou recusa.

Respostas voltam para `terms.csv`: `status` vira `confirmed` (com a fonte em `evidence`) ou `rejected`. Gerado a partir de `terms.csv` em 2026-11-12, na #20; refazer quando a planilha mudar muito.

## Conflitos que a planilha não resolve sozinha

Um termo não pode repetir o nome de outro produto, então estes casos precisam de decisão antes de virar termo:

| Termo | Onde | Significado regional | No catálogo hoje |
|---|---|---|---|
| inhame | Nordeste | o cará do Sudeste | produto Inhame (taro) |
| canjica | Nordeste | o curau (creme de milho verde) | produto Canjica (milho branco, o mungunzá do Nordeste) |
| bolacha | Sul, Sudeste | qualquer biscoito | termo ambíguo de Biscoito |

## Norte

### Ambíguos

| Termo | Leva a | Observação |
|---|---|---|
| alfavaca | Manjericão | No Norte a alfavaca é outra planta, parecida |
| farinha | Farinha de mandioca | Em outras regiões, farinha é a de trigo |

### A confirmar

| Termo | Leva a | Observação |
|---|---|---|
| ata | Fruta-do-conde |  |
| bacalhau da amazônia | Pirarucu |  |
| bolo de macaxeira | Bolo de mandioca |  |
| chup-chup | Geladinho |  |
| coentro-bravo | Chicória-do-pará |  |
| coentrão | Chicória-do-pará |  |
| farinha de água | Farinha d'água |  |
| feijão macassar | Feijão-de-corda |  |
| jerimum | Abóbora |  |
| macaxeira | Mandioca |  |
| pacova | Banana-da-terra |  |
| puba | Massa puba |  |
| pão careca | Pão francês |  |
| taperebá | Cajá |  |
| urucum | Colorau |  |

## Nordeste

### Ambíguos

| Termo | Leva a | Observação |
|---|---|---|
| alfavaca | Manjericão | No Norte a alfavaca é outra planta, parecida |
| chã | Coxão mole | Pode ser chã de dentro ou chã de fora |
| cuscuz | Flocão | Também é o prato pronto (cuscuz paulista, cuscuz nordestino) |
| farinha | Farinha de mandioca | Em outras regiões, farinha é a de trigo |
| gasosa | Refrigerante | Também pode ser água com gás |
| goma | Goma de tapioca | Também pode ser goma de passar roupa |
| jabá | Carne-seca | Também é usado para o charque |

### A confirmar

| Termo | Leva a | Observação |
|---|---|---|
| ata | Fruta-do-conde |  |
| biscoito de goma | Biscoito de polvilho |  |
| bolo de macaxeira | Bolo de mandioca |  |
| cabrito | Carne de bode |  |
| carimã | Massa puba |  |
| carne do sertão | Carne de sol |  |
| chup-chup | Geladinho |  |
| chã de dentro | Coxão mole |  |
| chã de fora | Coxão duro |  |
| coentro-bravo | Chicória-do-pará |  |
| coentrão | Chicória-do-pará |  |
| dindim | Geladinho |  |
| dindin | Geladinho |  |
| farinha de copioba | Farinha de mandioca | Farinha fina da Bahia |
| feijão macassar | Feijão-de-corda |  |
| feijão-de-macassar | Feijão-fradinho |  |
| flau | Geladinho |  |
| flocão de milho | Flocão |  |
| galinha de capoeira | Frango caipira |  |
| imbu | Umbu |  |
| jerimum | Abóbora |  |
| macaxeira | Mandioca |  |
| manteiga da terra | Manteiga de garrafa |  |
| massa de cuscuz | Flocão |  |
| mel de engenho | Melado |  |
| mungunzá | Canjica |  |
| pinha | Fruta-do-conde |  |
| puba | Massa puba |  |
| pão carioca | Pão francês | Confiança baixa; conferir com revisores |
| pão jacó | Pão francês | Confiança baixa; conferir com revisores |
| queijo de manteiga | Queijo manteiga |  |
| queimado | Bala | Confiança baixa; conferir com revisores |
| quiboa | Água sanitária | Nome de marca usado como nome do produto |
| saco de chão | Pano de chão |  |
| sururu | Mexilhão |  |

## Centro-Oeste e Sudeste

### Ambíguos

| Termo | Leva a | Observação |
|---|---|---|
| bolacha | Biscoito | Em cada região, bolacha e biscoito podem ser tipos diferentes |

### A confirmar

| Termo | Leva a | Observação |
|---|---|---|
| aipim | Mandioca |  |
| batata-baroa | Mandioquinha |  |
| benjamim | Adaptador de tomada |  |
| bexiga | Balão de festa |  |
| bolacha cream cracker | Biscoito cream cracker |  |
| bolacha de arroz | Biscoito de arroz |  |
| bolacha integral | Biscoito integral |  |
| bolacha maisena | Biscoito maisena |  |
| bolacha recheada | Biscoito recheado |  |
| bolacha wafer | Biscoito wafer |  |
| bolacha água e sal | Biscoito água e sal |  |
| bolo de aipim | Bolo de mandioca |  |
| cândida | Água sanitária | Nome de marca usado como nome do produto |
| erva de tereré | Erva-mate |  |
| laranja-cravo | Mexerica |  |
| mixirica | Mexerica |  |
| peta | Biscoito de polvilho |  |
| pão de sal | Pão francês |  |
| qboa | Água sanitária | Nome de marca usado como nome do produto |
| sacolé | Geladinho |  |
| tubaína | Refrigerante |  |

## Sul

### Ambíguos

| Termo | Leva a | Observação |
|---|---|---|
| bergamota | Mexerica | Também é o nome de outra fruta cítrica |
| bolacha | Biscoito | Em cada região, bolacha e biscoito podem ser tipos diferentes |
| erva | Erva-mate | Pode ser qualquer erva ou tempero |
| gasosa | Refrigerante | Também pode ser água com gás |
| guisado | Carne moída | No resto do país é o prato de carne ensopada |
| marisco | Mexilhão | Também é o nome genérico de fruto do mar |
| nata | Creme de leite fresco | Também é a camada de gordura do leite fervido |

### A confirmar

| Termo | Leva a | Observação |
|---|---|---|
| aipim | Mandioca |  |
| banana-caturra | Banana |  |
| batata-salsa | Mandioquinha |  |
| bife de chorizo | Contrafilé |  |
| bolacha cream cracker | Biscoito cream cracker |  |
| bolacha de arroz | Biscoito de arroz |  |
| bolacha integral | Biscoito integral |  |
| bolacha maisena | Biscoito maisena |  |
| bolacha recheada | Biscoito recheado |  |
| bolacha wafer | Biscoito wafer |  |
| bolacha água e sal | Biscoito água e sal |  |
| bolo de aipim | Bolo de mandioca |  |
| cacetinho | Pão francês |  |
| chimarrão | Erva-mate |  |
| chimia | Geleia |  |
| costelão | Costela |  |
| cuca alemã | Cuca |  |
| cândida | Água sanitária | Nome de marca usado como nome do produto |
| erva de chimarrão | Erva-mate |  |
| erva para chimarrão | Erva-mate |  |
| fiambre | Presunto |  |
| grampo de roupa | Prendedor de roupa |  |
| mimosa | Mexerica |  |
| schmier | Geleia |  |
| tatu | Lagarto |  |
| tempero verde | Cheiro-verde |  |
| vazio | Fraldinha |  |
| vina | Salsicha |  |

## Brasil todo

Ambíguos sem região: qualquer revisor pode opinar.

| Termo | Leva a | Observação |
|---|---|---|
| alvejante | Água sanitária | Também pode ser o alvejante sem cloro |
| bateria | Pilha | Pode ser de celular, carro ou aparelho auditivo |
| bifinho | Petisco de cachorro | Também pode ser bife pequeno |
| cana | Cachaça | Também é a cana-de-açúcar |
| capim-cidreira | Capim-santo | Pode ser confundido com erva-cidreira |
| carne picada | Carne moída | Também é carne em cubos |
| castanha | Castanha-do-pará | Pode ser qualquer castanha |
| ervilha | Ervilha-torta | Também é a ervilha em lata (Mercearia) |
| escarola | Chicória | Em alguns lugares escarola e chicória são folhas diferentes |
| escova | Escova de dente | Também pode ser escova de cabelo ou de limpeza |
| farinha | Farinha de trigo | No Norte e no Nordeste, farinha é a de mandioca |
| fermento | Fermento em pó | Pode ser o em pó ou o biológico |
| filão | Pão italiano | Também é o pão francês comprido |
| filé | Filé mignon | Também é filé de frango ou de peixe |
| fita | Fita adesiva | Pode ser fita de glicemia, de cetim etc. |
| fralda | Fralda geriátrica | Pode ser a geriátrica ou a infantil |
| fralda | Fralda infantil | Pode ser a geriátrica ou a infantil |
| goma | Passa-roupa | Também é a goma de tapioca |
| gás | Gás de cozinha | Também pode ser água com gás |
| lenço | Lenço de papel | Também pode ser o lenço umedecido |
| lima | Lima-da-pérsia | Também pode ser a laranja-lima |
| linha | Linha de costura | Também pode ser fio dental |
| massa | Macarrão | Também pode ser massa de pastel, de lasanha ou de bolo |
| mate | Chá-mate | No Sul, mate pode ser a erva-mate do chimarrão |
| molho | Molho de tomate | Pode ser qualquer molho |
| máscara | Máscara descartável | Também é máscara de cabelo |
| pano de limpeza | Pano de chão | Também pode ser o pano multiuso |
| papel | Papel higiênico | Pode ser papel-toalha, de presente etc. |
| pasta | Creme dental | Também pode ser pasta de amendoim e outras |
| pastilha | Pastilha para garganta | Também é a pastilha para dentadura ou bala |
| petisco | Petisco de cachorro | Pode ser de cachorro ou de gato |
| pipoca | Milho de pipoca | Também pode ser a de micro-ondas |
| polvilho | Polvilho doce | Pode ser o doce ou o azedo |
| pá | Paleta | Também é a pá de lixo |
| pá | Pá de lixo | Também é corte de carne |
| pão de leite | Bisnaguinha | Também é um pão doce maior |
| queijo branco | Queijo minas | Também pode ser ricota ou queijo coalho |
| ração | Ração de cachorro | Pode ser de cachorro, gato, pássaro ou peixe |
| sabão | Sabão em barra | Pode ser em pó, líquido ou em barra |
| sachê | Sachê de gato | Pode ser de gato ou de cachorro |
| salgadinho | Salgadinho de pacote | Também é o salgado de festa (coxinha, esfiha) |
| soda | Soda cáustica | Também é refrigerante |
| soja | Proteína de soja | Também pode ser a bebida ou o grão |
| soro | Soro fisiológico | Também pode ser o soro de reidratação |
| tapioca | Goma de tapioca | Também é a tapioca pronta |
| tempero | Tempero pronto | Pode ser qualquer tempero ou o tempero verde |
