Parte 01 – Implementación de Favoritos con MVVM y UDF
En esta primera parte se implementa un sistema básico de canciones favoritas aplicando los principios de Model–View–ViewModel (MVVM) y Unidirectional Data Flow (UDF). 
Se agrega un ícono de corazón a cada SongCard, permitiendo alternar el estado de favorito al tocarlo.El evento se propaga correctamente
desde la UI → ViewModel → Repositorio, demostrando el uso de state hoisting, donde los composables hijos no poseen estado propio, sino que reciben datos y emiten eventos. 
Se actualiza el modelo Song añadiendo el campo isFavorite, y la interfaz refleja automáticamente los cambios mediante recomposición reactiva. Esto garantiza una arquitectura limpia, desacoplada
y fácil de mantener.

Parte 02 – Sistema Completo de Favoritos y Navegación
En esta segunda parte se amplía la funcionalidad implementando un sistema completo de canciones favoritas utilizando Jetpack Compose, manteniendo la arquitectura MVVM y el flujo UDF.
Los usuarios pueden marcar canciones como favoritas desde HomeScreen, donde se muestran categorías con tarjetas, y visualizar las canciones filtradas en HighlightsScreen, manteniendo sincronización en tiempo real 
gracias a un ViewModel compartido que gestiona el estado con StateFlow. La aplicación incorpora navegación type-safe con serialización de Kotlin, Bottom Navigation con cuatro pestañas 
principales (Home, Highlights, Search, Library), y una estructura basada en componentes reutilizables. Se respeta el state hoisting y la inmutabilidad, utilizando .copy() para actualizar estados de forma segura.
El stack tecnológico incluye Jetpack Compose, Navigation Compose, Koin, Material 3 y Kotlin Coroutines.
