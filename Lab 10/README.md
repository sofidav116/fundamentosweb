------ * ------- Parte 1 ------ * -------
Filtros de Eventos con Estado de URL

Los filtros de categoría, estado y precio se guardan en la URL (?category=music&status=upcoming)
para que puedan compartirse y persistir al refrescar la página.

Características principales:
- La URL se actualiza sin recargar usando router.push()
- El filtrado ocurre en el servidor, no en el cliente
- Los filtros activos se muestran visualmente en los selectores
- El botón "Borrar todo" limpia todos los filtros de la URL
- Muestra "No se encontraron eventos" cuando no hay resultados

Archivos modificados:
- src/app/events/EventFiltersForm.tsx
  Se reemplazó el formulario tradicional (method="GET") por useRouter +
  useSearchParams para actualizar la URL sin recarga completa.

------ * ------- Parte 2 ------ * -------
Inscripción Optimista al Evento

El botón de registro responde de forma instantánea usando useOptimistic de
React 19, sin esperar la respuesta del servidor.

Características principales:
- El contador de plazas baja inmediatamente al hacer clic (optimista)
- El botón se deshabilita durante el proceso con spinner de carga
- Si el servidor falla, el contador se revierte automáticamente
- Muestra mensaje verde de éxito o mensaje rojo de error según el resultado
- Si no hay plazas disponibles, muestra "Evento Agotado"

Archivos modificados:
- src/components/RegisterButton.tsx
  Se agregó manejo de feedback (éxito/error) con useState y mensajes
  visuales tras completar la acción del servidor.

