import type { DocumentFormValues } from './types'

export function buildCreateFormData(values: DocumentFormValues, file: File): FormData {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('title', values.title.trim())

  const category = values.category.trim()
  const equipmentType = values.equipmentType.trim()
  const description = values.description.trim()
  const tags = values.tags.trim()

  if (category) formData.append('category', category)
  if (equipmentType) formData.append('equipmentType', equipmentType)
  if (description) formData.append('description', description)
  if (tags) formData.append('tags', tags)

  return formData
}

export function buildVersionFormData(file: File, changeReason = 'manual update'): FormData {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('changeReason', changeReason)
  return formData
}
