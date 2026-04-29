import { publicAxios } from '../../../shared/api/client'
import type { PublicOrganization } from '../types'

export async function getPublicOrganizations(): Promise<PublicOrganization[]> {
  const response = await publicAxios.get<{ success: boolean; data: PublicOrganization[] }>(
    '/signup-requests/organizations/public',
  )
  return response.data.data
}
