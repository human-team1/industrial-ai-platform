export type DocumentFormMode = 'create' | 'edit'

export type DocumentFormValues = {
  title: string
  category: string
  equipmentType: string
  tags: string
  description: string
}

export type DocumentFormFieldErrors = Partial<Record<keyof DocumentFormValues | 'file', string>>

export type DocumentFormPageText = {
  title: string
  description: string
  submitLabel: string
}

export const EMPTY_DOCUMENT_FORM_VALUES: DocumentFormValues = {
  title: '',
  category: '',
  equipmentType: '',
  tags: '',
  description: '',
}
