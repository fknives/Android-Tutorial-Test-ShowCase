package org.fnives.test.showcase.core.content

class FetchContentUseCase internal constructor(private val contentRepository: ContentRepository) {

    fun invoke() = contentRepository.fetch()
}
