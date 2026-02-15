--- * -- PARTE 01 -- * ---

Descripción:
Aplicación Android que implementa manejo elegante de errores de red usando arquitectura MVVM con Jetpack Compose y enfoque offline-first.
Prioriza los datos almacenados en Room Database, mostrando contenido incluso sin conexión. 
Cuando la API falla, presenta un Snackbar no bloqueante con opción de reintentar, sin interrumpir la experiencia del usuario.

Características Implementadas:
- UiState: Uso de sealed interfaces para manejar estados de UI (Loading, Success, Error) de forma segura.
- Room Database: Persistencia local para funcionamiento offline.
- Paginación: Infinite scroll con tamaños configurables (20, 50, 100).
- Errores Tipados: Clasificación (NETWORK, PARSE, DATABASE, UNKNOWN) para mostrar mensajes adecuados.
- Pull-to-Refresh: Actualización mediante gesto, manteniendo el contenido visible.
- Snackbar No Bloqueante: Permite reintentar sin ocultar los datos disponibles.

Arquitectura:
El proyecto sigue el patrón MVVM, con un Repository que gestiona los datos entre API y base local, un ViewModel que expone estados
mediante StateFlow, y una UI en Jetpack Compose que se actualiza de forma reactiva.

Flujo de Datos:
Inicio de la app → ViewModel llama refreshAmiibos() → Se verifica caché en Room → Si hay datos: emite Success (isRefreshing=true), si no: Loading →
Repository intenta descargar desde la API → Si falla: captura el error y emite Error con datos en caché → La UI muestra Snackbar no bloqueante con opción Reintentar y 
mantiene el grid visible → Usuario pulsa Reintentar → Se ejecuta refreshAmiibos() sin perder la vista → Si no hay caché, la UI muestra pantalla de error completa.

--- * -- PARTE 02 -- * ---

Descripción:
Implementación de búsqueda local en tiempo real que filtra amiibos por nombre sin realizar solicitudes de red. 
Utiliza consultas SQL LIKE en Room y conmutación reactiva con flatMapLatest, permitiendo resultados instantáneos mientras el usuario escribe.

Características Implementadas:
- Campo de Búsqueda en UI: OutlinedTextField con placeholder "Buscar amiibo..." y botón Clear (X) dinámico.
- Consulta SQL LIKE: Búsqueda parcial directamente en Room mediante Flow reactivo.
- Conmutación Reactiva: Uso de flatMapLatest para alternar automáticamente entre lista completa y resultados filtrados.
- Filtrado en Base de Datos: Mayor eficiencia al evitar filtrado en memoria y optimizar el rendimiento.
- Actualización en Tiempo Real: Cada cambio en el texto recompone la UI sin bloqueos ni debounce manual.

Arquitectura:
Flujo MVVM unidireccional donde la UI actualiza un StateFlow en el ViewModel.
Mediante flatMapLatest, se decide dinámicamente entre mostrar la lista completa o la filtrada. 
Room ejecuta las consultas SQL y Compose recompone automáticamente la interfaz al recibir nuevos datos.

Flujo de Datos:
Usuario escribe → ViewModel actualiza StateFlow → flatMapLatest conmuta el flujo → Repository consulta DAO → Room retorna resultados → Compose recompone la lista.
