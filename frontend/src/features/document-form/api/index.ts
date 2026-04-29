export {
  createDocument,
  getDocumentDetail,
  updateDocument,
  uploadDocumentVersion,
  type UpdateDocumentMetadataPayload,
} from '../../document/api'
import { apiClient } from '../../../shared/api/client'

export async function getDocumentFilePreview(fileId: number): Promise<Blob> {
  const response = await apiClient.get<Blob>(`/files/${fileId}/preview`, {
    responseType: 'blob',
  })
  return response.data
}
