import { createDocument, deleteDocument, updateDocument, uploadDocumentVersion } from '../api'
import type { UpdateDocumentMetadataPayload } from '../api/types'

export async function submitNewDocument(formData: FormData) {
  return createDocument(formData)
}

export async function submitDocumentMetadata(documentId: number, body: UpdateDocumentMetadataPayload) {
  return updateDocument(documentId, body)
}

export async function submitDocumentVersion(documentId: number, formData: FormData) {
  return uploadDocumentVersion(documentId, formData)
}

export async function removeDocument(documentId: number) {
  return deleteDocument(documentId)
}
