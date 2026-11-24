package br.com.mentealerta.listadecompras.premium

/**
 * O que a versão completa libera (coluna "Versão completa" do plano §5). Só entra aqui o que é pago:
 * acessibilidade e tudo o que é essencial para fazer a lista e usar no mercado são grátis e nunca
 * passam por este controle. O Plano Família (fase 3) é outro produto e fica de fora.
 */
enum class Feature {
    /** Sem anúncios (plano §3.7). */
    NoAds,

    /** "O que já comprei" sem limite de tempo; o grátis mostra os últimos 30 dias. */
    FullHistory,

    /** Lembretes fora do app: lista grande, produto atrasado. */
    Reminders,

    /** "Os de sempre": pôr os produtos fixos na lista com um toque. */
    UsualItems,

    /** Atalho na tela do celular (widget). */
    HomeScreenShortcut,

    /** Ordem das seções do meu mercado. */
    SectionOrder,

    /** Temas decorativos: cores e estilos de papel. Nunca um tema mais fácil de ler. */
    DecorativeThemes,

    /** "Quanto gastei" e estatísticas de gastos. */
    Spending,

    /** Aviso ao chegar perto do mercado (fase 4). */
    NearStoreAlert,
}
