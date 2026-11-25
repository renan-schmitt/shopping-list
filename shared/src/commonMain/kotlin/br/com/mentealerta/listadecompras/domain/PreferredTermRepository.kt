package br.com.mentealerta.listadecompras.domain

/**
 * Termo que a pessoa usa para cada produto do catálogo (plano §1.3), aprendido no celular: quem
 * escreve "cacetinho" passa a ver "cacetinho", e não "Pão francês", nas sugestões. A implementação
 * fica em `data`; quem usa recebe esta interface pelo Koin.
 */
interface PreferredTermRepository {
    /** Guarda [term] como o termo da pessoa para o produto [productId], no lugar do anterior. */
    suspend fun learn(productId: String, term: String)

    /** `id` do produto → termo aprendido, só para os produtos de [productIds] que têm termo. */
    suspend fun preferredTerms(productIds: Collection<String>): Map<String, String>
}
