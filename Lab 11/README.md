---- * ---- PARTE 1: MIS EVENTOS ---- * ----

DESCRIPCIÓN: Implementación de la página "Mis Eventos", una ruta protegida
que permite a los usuarios autenticados ver y gestionar
únicamente los eventos que ellos han creado.
 
CARACTERÍSTICAS:
- Ruta protegida: redirige a /auth si no hay sesión activa
- Filtro por usuario: solo muestra eventos donde organizerId === user.uid
- Botón de edición: navega al formulario con datos precargados
- Botón Eliminar: el servidor valida propiedad antes de eliminar
- Estado vacío: mensaje "Aún no hay eventos" con CTA para crear
- Estado de carga: esqueleto animado con Suspense mientras carga los datos
- Autenticación con Firebase Admin SDK en el servidor
 
ARCHIVOS CREADOS / MODIFICADOS:
src/app/my-events/page.tsx    
  - Página principal de Mis Eventos con protección de ruta,
    skeleton de carga, estado vacío y lista de eventos filtrados.
 
src/data/events.ts             
  - Se agregó la función getEventsByOrganizer(userId) para
    filtrar eventos por organizador en la capa de datos.
 
src/lib/firebase/firestore.ts   
  - Se corrigió la función createEvent para eliminar campos
    undefined antes de guardar en Firestore (fix para imageUrl).
 
next.config.ts                  
  - Se agregó hostname '**' en remotePatterns para permitir
    imágenes de cualquier dominio externo en next/image.

---- * ---- Parte 2: Generación de IA Mejorada ---- * ----

DESCRIPCIÓN: Mejora de la función de IA en el formulario de creación de eventos de EventPass Pro.
Se extendió la integración con Gemini AI para generar múltiples variantes de descripción
con soporte de tonos, permitiendo al usuario elegir la que mejor se adapte a su evento.

CARACTERÍSTICAS:
- 3 variantes de descripción: Gemini genera 3 opciones distintas en lugar de una sola.
- Selector de tono: El usuario elige entre Formal, Informal o Emocionante antes de generar.
- Tarjetas de selección: Las variantes se muestran como tarjetas interactivas con checkmark al seleccionar.
- Botón Aplicar: La variante seleccionada rellena automáticamente el campo de descripción y etiquetas.
- Indicador de progreso: Spinner con mensaje mientras Gemini genera las variantes.
- Botón Regenerar: Permite obtener nuevas variantes con el mismo tono si ninguna convence.
- Manejo de errores: Mensaje visible si la generación falla.

ARCHIVOS MODIFICADOS:
actions/aiActions.ts
  - Nuevos tipos: DescriptionTone, DescriptionVariant, GeneratedVariantsResult
  - Nueva Server Action: generateEventDescriptionVariantsAction(title, tone)
    Solicita a Gemini 3 variantes de descripción según el tono elegido
    y retorna variantes + categoría + tags.

components/EventForm.tsx
  - Nuevo componente DescriptionVariantsPanel que reemplaza el botón simple de generación.
  - Incluye selector de tono, spinner de progreso, tarjetas seleccionables,
    botón Aplicar y botón Regenerar.
  - Solo visible en modo creación (no en edición).


