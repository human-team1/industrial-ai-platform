import { useCallback, useEffect, useState } from 'react'
import axios from 'axios'
import { updateMyProfile } from '../api'
import type { UserMeResponse } from '../../user-profile-view/types'

type Args = {
  initial: { name: string; phone: string }
  onSaved: (res: UserMeResponse) => void
}

type Result = {
  isEditing: boolean
  isSaving: boolean
  name: string
  phone: string
  errorMessage: string | null
  onNameChange: (value: string) => void
  onPhoneChange: (value: string) => void
  onStartEdit: () => void
  onCancel: () => void
  onSave: () => Promise<void>
}

export function useEditMyProfile({ initial, onSaved }: Args): Result {
  const [isEditing, setIsEditing] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [name, setName] = useState(initial.name)
  const [phone, setPhone] = useState(initial.phone)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  useEffect(() => {
    if (!isEditing) {
      setName(initial.name)
      setPhone(initial.phone)
    }
  }, [initial.name, initial.phone, isEditing])

  const onStartEdit = useCallback(() => {
    setName(initial.name)
    setPhone(initial.phone)
    setErrorMessage(null)
    setIsEditing(true)
  }, [initial.name, initial.phone])

  const onCancel = useCallback(() => {
    setIsEditing(false)
    setErrorMessage(null)
  }, [])

  const onSave = useCallback(async () => {
    setIsSaving(true)
    setErrorMessage(null)
    try {
      const trimmedName = name.trim()
      const trimmedPhone = phone.trim()
      const payload: { name?: string; phone?: string } = {}
      if (trimmedName !== initial.name) payload.name = trimmedName
      if (trimmedPhone !== initial.phone) payload.phone = trimmedPhone

      if (Object.keys(payload).length === 0) {
        setIsEditing(false)
        return
      }
      const res = await updateMyProfile(payload)
      onSaved(res)
      setIsEditing(false)
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const data = err.response?.data as { detail?: string; message?: string } | undefined
        setErrorMessage(data?.detail ?? data?.message ?? '저장에 실패했습니다.')
      } else {
        setErrorMessage('저장에 실패했습니다.')
      }
    } finally {
      setIsSaving(false)
    }
  }, [initial.name, initial.phone, name, phone, onSaved])

  return {
    isEditing,
    isSaving,
    name,
    phone,
    errorMessage,
    onNameChange: setName,
    onPhoneChange: setPhone,
    onStartEdit,
    onCancel,
    onSave,
  }
}
