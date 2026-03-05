// =============================================================================
// UTILIDAD: ALMACENAMIENTO LOCAL - Country Explorer
// =============================================================================
// Este módulo encapsula el acceso a localStorage para manejar favoritos.
//
// ## ¿Por qué encapsular localStorage?
// En lugar de llamar localStorage directamente desde todos lados,
// centralizamos la lógica aquí. Esto hace el código más mantenible
// y fácil de cambiar en el futuro (ej: migrar a IndexedDB).
// =============================================================================

/** Clave usada en localStorage para guardar los favoritos */
const FAVORITES_KEY = 'country-explorer-favorites';

/**
 * Obtiene el listado de códigos de países favoritos desde localStorage.
 * Si no hay nada guardado, devuelve un array vacío.
 *
 * @returns Array de códigos de países favoritos (ej: ["COL", "MEX"])
 */
export function getFavorites(): string[] {
  try {
    const stored = localStorage.getItem(FAVORITES_KEY);
    return stored ? (JSON.parse(stored) as string[]) : [];
  } catch {
    return [];
  }
}

/**
 * Guarda el listado completo de favoritos en localStorage.
 *
 * @param favorites - Array de códigos de países a guardar
 */
function saveFavorites(favorites: string[]): void {
  localStorage.setItem(FAVORITES_KEY, JSON.stringify(favorites));
}

/**
 * Verifica si un país está en favoritos.
 *
 * @param cca3 - Código del país (ej: "COL")
 * @returns true si el país es favorito
 */
export function isFavorite(cca3: string): boolean {
  return getFavorites().includes(cca3);
}

/**
 * Agrega o elimina un país de favoritos (toggle).
 * Si ya es favorito lo elimina, si no lo agrega.
 *
 * @param cca3 - Código del país (ej: "COL")
 * @returns true si quedó como favorito, false si fue eliminado
 */
export function toggleFavorite(cca3: string): boolean {
  const favorites = getFavorites();
  const index = favorites.indexOf(cca3);

  if (index === -1) {
    // No era favorito: lo agregamos
    favorites.push(cca3);
    saveFavorites(favorites);
    return true;
  } else {
    // Ya era favorito: lo eliminamos
    favorites.splice(index, 1);
    saveFavorites(favorites);
    return false;
  }
}

/**
 * Elimina todos los favoritos guardados.
 */
export function clearFavorites(): void {
  localStorage.removeItem(FAVORITES_KEY);
}