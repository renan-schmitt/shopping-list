package br.com.mentealerta.listadecompras.domain

import kotlinx.coroutines.flow.Flow

/**
 * A lista agrupada por seção, na ordem típica do mercado e sem seções vazias; emite de novo a cada
 * mudança ([ShoppingListRepository.observeSections]).
 */
class ObserveListUseCase(private val repository: ShoppingListRepository) {
    operator fun invoke(): Flow<List<ListSection>> = repository.observeSections()
}
