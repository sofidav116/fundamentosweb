COUNTRY EXPLORER — MÓDULO 1
Aplicación web para explorar información turística de países del mundo,
construida con HTML5 semántico, Tailwind CSS 4, TypeScript vanilla y
la REST Countries API.

--- * -- PARTE 01 -- * ---
 FILTRO DE REGIÓN

DESCRIPCIÓN
Se implementó un sistema de filtrado por región geográfica que permite
al usuario reducir los resultados de búsqueda a un continente específico,
sin realizar llamadas adicionales a la API.

CARACTERÍSTICAS
- Menú flotante con 5 regiones: África, América, Asia, Europa y Oceanía
- Botón triangular integrado dentro del input de búsqueda
- Opción "Todas las regiones" para restablecer el filtro
- Badge visual debajo del buscador que indica la región activa
- El menú se cierra automáticamente al hacer clic fuera de él
- Compatible con el buscador por nombre simultáneamente

ARCHIVOS MODIFICADOS
- index.html → Se agregó el botón triángulo y el menú flotante de
  regiones dentro del buscador
- src/main.ts → Se agregaron la variable de estado currentRegion,
  la función filterByRegion, el manejador handleRegionSelect y
  el badge de región activa

--- * -- PARTE 02 -- * ---
SISTEMA DE FAVORITOS

DESCRIPCIÓN
Se implementó un sistema de favoritos persistente usando localStorage,
que permite al usuario marcar países como favoritos, filtrar solo los
favoritos y borrarlos, todo sin necesidad de recargar la página.

CARACTERÍSTICAS
- Ícono de corazón en cada tarjeta: vacío si no es favorito, rojo si lo es
- Al hacer clic en el corazón se agrega o elimina el favorito sin abrir el modal
- Los favoritos persisten al recargar la página gracias a localStorage
- Interruptor "Solo favoritos" para mostrar únicamente países marcados
- Botón "Borrar favoritos" para eliminar todos los favoritos de una vez
- Compatible con el filtro de región y el buscador simultáneamente

ARCHIVOS MODIFICADOS
- src/utils/storage.ts → Archivo nuevo que encapsula todas las operaciones
  con localStorage: getFavorites, isFavorite, toggleFavorite y clearFavorites
- src/components/CountryCard.ts → Se agregó el botón de corazón en cada
  tarjeta con su lógica de toggle visual
- src/main.ts → Se agregaron la variable showOnlyFavorites, la función
  filterByFavorites, los manejadores handleFavoritesToggle y handleClearFavorites
- index.html → Se agregaron el interruptor visual y el botón de borrar favoritos
