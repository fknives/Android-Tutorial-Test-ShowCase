package org.fnives.test.showcase.hilt.core.content

import javax.inject.Inject

class FetchContentUseCase @Inject internal constructor(private val contentRepository: ContentRepository) {

    fun invoke() = contentRepository.fetch()
}
