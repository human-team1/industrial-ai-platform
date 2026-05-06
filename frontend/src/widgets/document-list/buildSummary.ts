import type { Document } from '../../entities/document/model/types'
import type { DocumentSummaryView } from './types'

export function buildDocumentSummaryView(items: Document[]): DocumentSummaryView {
  const summary: DocumentSummaryView = {
    totalCount: items.length,
    pdfCount: 0,
    docxCount: 0,
    completedCount: 0,
    processingCount: 0,
    failedCount: 0,
  }

  for (const item of items) {
    const docType = String(item.documentType ?? '').toUpperCase()
    if (docType === 'PDF') summary.pdfCount += 1
    if (docType === 'DOCX') summary.docxCount += 1

    if (item.indexingStatus === 'COMPLETED') summary.completedCount += 1
    if (item.indexingStatus === 'PROCESSING') summary.processingCount += 1
    if (item.indexingStatus === 'FAILED') summary.failedCount += 1
  }

  return summary
}
