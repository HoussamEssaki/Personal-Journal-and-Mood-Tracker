package com.personaljournal.domain.usecase.analytics

import com.personaljournal.domain.model.ExportRequest
import com.personaljournal.domain.repository.JournalRepository
import java.io.File
import javax.inject.Inject

class ExportReportUseCase @Inject constructor(
    private val journalRepository: JournalRepository
) {
    suspend operator fun invoke(request: ExportRequest): Result<File> =
        journalRepository.export(request)
}
