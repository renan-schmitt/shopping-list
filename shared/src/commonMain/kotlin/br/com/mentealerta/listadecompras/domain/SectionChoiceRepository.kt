package br.com.mentealerta.listadecompras.domain

/**
 * Seção que a pessoa escolheu para cada produto (plano §1.4, #56), aprendida no celular: quem põe
 * "sabão de coco" em Limpeza uma vez encontra o produto lá nas próximas. A chave é montada pelo
 * [RememberSectionUseCase]. A implementação fica em `data`; quem usa recebe esta interface pelo Koin.
 */
interface SectionChoiceRepository {
    /** Guarda [sectionId] como a seção do produto [key], no lugar da anterior. */
    suspend fun learn(key: String, sectionId: String)

    /** Chave do produto → seção escolhida, de todas as escolhas (são poucas: só as que a pessoa mudou). */
    suspend fun choices(): Map<String, String>
}
