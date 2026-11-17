/**
 * Utilidades para decodificar y extraer información del JWT en Base64url sin librerías externas.
 */

/**
 * Decodifica una cadena Base64url a string UTF-8.
 * @param {string} base64Url
 * @returns {string}
 */
export function decodeBase64Url(base64Url) {
  if (!base64Url || typeof base64Url !== 'string') {
    return ''
  }

  let base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
  while (base64.length % 4 !== 0) {
    base64 += '='
  }

  try {
    const binary = atob(base64)
    const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0))
    return new TextDecoder().decode(bytes)
  } catch {
    return ''
  }
}

/**
 * Parsea el payload (segundo segmento) de un token JWT.
 * @param {string} token
 * @returns {Record<string, unknown> | null}
 */
export function parseJwtPayload(token) {
  if (!token || typeof token !== 'string') {
    return null
  }

  const parts = token.split('.')
  if (parts.length !== 3) {
    return null
  }

  try {
    const jsonString = decodeBase64Url(parts[1])
    if (!jsonString) {
      return null
    }
    return JSON.parse(jsonString)
  } catch {
    return null
  }
}

/**
 * Extrae el ID de usuario numérico del JWT o de un payload ya decodificado.
 * Busca id, userId o sub numérico.
 * @param {string | Record<string, unknown> | null} tokenOrPayload
 * @returns {number | null}
 */
export function extractUserId(tokenOrPayload) {
  if (!tokenOrPayload) {
    return null
  }

  const payload =
    typeof tokenOrPayload === 'string'
      ? parseJwtPayload(tokenOrPayload)
      : tokenOrPayload

  if (!payload || typeof payload !== 'object') {
    return null
  }

  const rawId = payload.userId ?? payload.id ?? payload.usuarioId ?? payload.sub

  if (rawId === null || rawId === undefined) {
    return null
  }

  const parsed = Number(rawId)
  return Number.isFinite(parsed) ? parsed : null
}
