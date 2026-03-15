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
