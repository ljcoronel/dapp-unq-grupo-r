export function validateNombre(nombre) {
  if (!nombre || !nombre.trim()) {
    return 'El nombre de usuario es obligatorio.'
  }

  if (nombre.trim().length < 4 || nombre.trim().length > 16) {
    return 'El nombre debe tener entre 4 y 16 caracteres.'
  }

  if (/\s/.test(nombre)) {
    return 'El nombre no puede contener espacios.'
  }

  return ''
}

export function validatePassword(password) {
  if (!password || !password.trim()) {
    return 'La contraseña es obligatoria.'
  }

  if (password.length < 4 || password.length > 16) {
    return 'La contraseña debe tener entre 4 y 16 caracteres.'
  }

  return ''
}

export function validateCredentials({ nombre, password }) {
  return {
    nombre: validateNombre(nombre),
    password: validatePassword(password),
  }
}
