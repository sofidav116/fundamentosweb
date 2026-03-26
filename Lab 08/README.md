--- * -- PARTE 01 -- * --- 

COMPARACIÓN DE PROPIEDADES

Descripción: Se implementó una función de comparación de propiedades en un portal
inmobiliario desarrollado con React 19, TypeScript, Tailwind CSS y Shadcn UI.
Los datos se persisten en localStorage sin necesidad de un backend.


Características:
- Seleccionar hasta 3 propiedades para comparar
- Botón "Comparar" en cada tarjeta de propiedad
- Badge en el navbar que muestra cuántas propiedades están seleccionadas
- Tabla comparativa con métricas clave lado a lado
- Mejor valor de cada categoría destacado en verde
- Opción para quitar propiedades individualmente o limpiar toda la comparación
- Estado vacío cuando no hay propiedades seleccionadas


Archivos modificados/nuevos:
- src/components/CompareButton.tsx  (NUEVO)
    Botón para agregar o quitar una propiedad de la comparación.

- src/pages/ComparePage.tsx  (NUEVO)
    Página con la tabla comparativa y destacado de mejores valores.

- src/components/PropertyCard.tsx  (MODIFICADO)
    Se agregó el componente <CompareButton> en el footer de cada tarjeta.

- src/App.tsx  (MODIFICADO)
    Se agregó la ruta /compare y el enlace en el navbar con badge reactivo.

--- * -- PARTE 02 -- * ---

GALERÍA DE IMÁGENES

Descripción:
Se implementó una galería de imágenes interactiva en la página de detalles de cada propiedad dentro del portal inmobiliario desarrollado con React 19, TypeScript, Tailwind CSS y Shadcn UI.
Esta funcionalidad permite visualizar múltiples imágenes de una propiedad mediante miniaturas organizadas en una cuadrícula, con la posibilidad de abrir una vista en pantalla completa utilizando una ventana modal. La galería mejora la experiencia del usuario al facilitar la navegación visual y el acceso rápido a todas las imágenes disponibles.

Características:
- Visualización de una cuadrícula de miniaturas de imágenes de la propiedad
- Apertura de una ventana modal a pantalla completa al hacer clic en una imagen
- Navegación entre imágenes mediante botones izquierdo y derecho
- Compatibilidad con teclado (flechas izquierda/derecha y tecla Escape)
- Contador de imágenes que muestra la posición actual (por ejemplo, "3 de 10")
- Botón de cierre (X) en la ventana modal
- Cierre automático del modal al hacer clic fuera de la imagen
- Experiencia visual fluida e intuitiva para el usuario

Archivos modificados/nuevos:
- src/components/ImageGallery.tsx  (NUEVO)
  Componente encargado de mostrar la cuadrícula de miniaturas de las imágenes y manejar el evento de apertura del modal.

- src/components/ImageModal.tsx (NUEVO)
  Ventana modal que permite visualizar las imágenes en pantalla completa, incluyendo la navegación entre imágenes, el contador de posición y el control mediante teclado.

- src/pages/PropertyDetailPage.tsx  (MODIFICADO)
  Se integró el componente <ImageGallery> dentro de la página de detalles de la propiedad para mostrar todas las imágenes disponibles y permitir su visualización interactiva.

