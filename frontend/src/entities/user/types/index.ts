export type UserProfile = {
  name: string
  email: string
  role: string
  joinedAt: string
  lastLoginAt: string
  accountStatus: string
}

export type Affiliation = {
  organization: string
  team: string
  role: string
  permissions: string[]
}
