interface JwtPayload {
  userId?: unknown
  sub?: unknown
  id?: unknown
}

const parseUserId = (value: unknown): number | null => {
  if (typeof value !== 'number' && typeof value !== 'string') {
    return null
  }

  const userId = Number(value)
  return Number.isSafeInteger(userId) && userId > 0 ? userId : null
}

export const parseJwtPayload = (token: string): JwtPayload | null => {
  const payload = token.split('.')[1]

  if (!payload) {
    return null
  }

  try {
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const paddedBase64 = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')
    const bytes = Uint8Array.from(atob(paddedBase64), (character) => character.charCodeAt(0))
    const decodedPayload = new TextDecoder().decode(bytes)
    const value: unknown = JSON.parse(decodedPayload)

    return value && typeof value === 'object' ? (value as JwtPayload) : null
  } catch {
    return null
  }
}

export const getUserIdFromToken = (token: string | null): number | null => {
  if (!token) {
    return null
  }

  const payload = parseJwtPayload(token)
  return parseUserId(payload?.userId) ?? parseUserId(payload?.id) ?? parseUserId(payload?.sub)
}
