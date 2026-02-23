--- * -- PARTE 01 -- * ---

DESCRIPCIÓN: En esta parte se mejora el manejo de errores durante la captura de fotografías.
Actualmente, la aplicación muestra errores genéricos cuando falla la cámara, lo
que dificulta que el usuario entienda qué ocurrió realmente.

El objetivo es implementar un manejo de errores granular y tipado para la captura
de fotos usando CameraX, propagando esta información desde la capa de utilidades
hasta la UI mediante el Repository.

CARACTERÍSTICAS
- Uso de una sealed class `CaptureError` para representar errores específicos.
- Diferenciación clara entre:
  * Cámara cerrada inesperadamente.
  * Fallo de captura por hardware.
  * Error de almacenamiento (I/O).
- Mapeo de los códigos de `ImageCaptureException` a errores tipados.
- Extensión de `CreateSpotResult` con el caso `PhotoCaptureFailed`.
- Mensajes de error claros y específicos para el usuario.
- Manejo seguro de errores sin crashes en la aplicación.

FLUJO:

Usuario
  ↓
CameraScreen (UI)
  ↓
CameraUtils.capturePhoto()
  ↓
[¿Éxito?]
  ├─ Sí → Uri de la imagen
  │        ↓
  │   SpotRepository.createSpot()
  │        ↓
  │   CreateSpotResult.Success
  │        ↓
  │   ViewModel
  │        ↓
  │   Mensaje de éxito en UI
  │
  └─ No → CaptureError
           ↓
     SpotRepository
           ↓
     CreateSpotResult.PhotoCaptureFailed
           ↓
     ViewModel
           ↓
     Mensaje de error específico en UI

--- * -- PARTE 02 -- * ---

DESCRIPCIÓN: En esta parte se agrega la funcionalidad para eliminar spots creados por el usuario.
La eliminación debe ser segura, confirmada por el usuario y limpiar tanto la base
de datos como el archivo de imagen asociado.

CARACTERÍSTICAS
- Eliminación de spots mediante long-press en un marcador del mapa.
- Diálogo de confirmación antes de eliminar.
- Nuevo método `deleteSpot(id: Long)` en `SpotDao`.
- Coordinación desde el `SpotRepository` para:
  * Eliminar el registro de Room.
  * Eliminar el archivo de imagen del almacenamiento interno.
- Actualización automática de la UI gracias a Flow y Room.
- Manejo de casos borde (archivo inexistente, errores silenciosos).

FLUJO:

Usuario
  ↓ (long-press)
Marcador en Google Map
  ↓
Mostrar diálogo de confirmación
  ↓
[¿Confirmar eliminación?]
  ├─ No → Cancelar acción
  │
  └─ Sí
       ↓
   MapViewModel.deleteSpot(id)
       ↓
   SpotRepository.deleteSpot()
       ↓
   Eliminar registro en Room
       ↓
   Eliminar archivo de imagen (filesDir)
       ↓
   Flow emite nueva lista de spots
       ↓
   MapScreen se recompone
       ↓
   Marcador desaparece del mapa
