-------------------- * ------------------------
Lab 09 — Parte 1: Paginación y Metadatos
  RealEstate Hub API | Module 3

DESCRIPCIÓN:
La API devolvía todas las propiedades de una sola vez, lo cual
resulta ineficiente para conjuntos de datos grandes. Se implementó
paginación real usando los parámetros page y limit, junto con
metadatos de navegación en cada respuesta.


CARACTERÍSTICAS IMPLEMENTADAS:
- Acepta parámetros de consulta page y limit con valores por
  defecto (page=1, limit=10).
- Devuelve metadatos: total de registros, página actual, límite
  y total de páginas.
- Páginas fuera de rango devuelven array vacío, no un error.
- Valida que page y limit sean enteros positivos, rechazando
  valores negativos o no numéricos.
- Queries en paralelo con $transaction para mayor eficiencia
  (findMany + count).


ARCHIVOS MODIFICADOS:
1. src/controllers/propertyController.ts
   - Se leen los parámetros page y limit desde la URL.
   - Se validan como enteros positivos.
   - La respuesta ahora incluye el objeto meta:
     { total, page, limit, pages }
   - Páginas fuera de rango devuelven data: [].

2. src/routes/propertyRoutes.ts
   - Se actualizó la documentación del endpoint GET /api/properties.
   - Se agregaron ejemplos de uso con paginación.
   - Se documenta el formato de respuesta con el objeto meta.

3. src/repositories/propertyRepository.ts
   - Se corrigió el import de PrismaClient para Prisma 7.
   - Se eliminó el adapter PrismaBetterSqlite3 (ya no necesario).
   - findAll() acepta un segundo parámetro { page, limit }.
   - Usa skip y take en la query para traer solo la página pedida.
   - Retorna { data: Property[], total: number }.


-------------------- * ------------------------
  Lab 09 — Parte 2: Estadísticas de Propiedades
  RealEstate Hub API | Module 3


DESCRIPCIÓN:
Se implementó un nuevo endpoint GET /api/properties/stats que
devuelve estadísticas agregadas de todas las propiedades, como
conteo por tipo, precio medio por tipo, rango de precios global
y total de propiedades. Si la base de datos está vacía, devuelve
ceros en lugar de errores.


CARACTERÍSTICAS IMPLEMENTADAS:
- Endpoint GET /api/properties/stats devuelve datos estadísticos.
- Conteo por tipo de propiedad: { house: 10, apartment: 15, ... }
- Precio medio por tipo de propiedad.
- Rango de precios global: { min: 50000, max: 2000000 }
- Recuento total de propiedades.
- Usa groupBy y aggregate de Prisma para las consultas.
- Base de datos vacía devuelve ceros, no errores.


ARCHIVOS MODIFICADOS:
1. src/controllers/propertyController.ts
   - Se agregó la función getPropertyStats().
   - Llama al repositorio para obtener conteos, medias y rangos.
   - Maneja el caso de base de datos vacía devolviendo ceros.

2. src/routes/propertyRoutes.ts
   - Se agregó la ruta GET /stats antes de GET /:id para evitar
     conflictos de rutas en Express.
   - Se documenta el formato de respuesta del endpoint.

3. src/repositories/propertyRepository.ts
   - Se agregó el método getStats() al repositorio.
   - Usa prisma.property.groupBy() para contar y promediar
     por tipo de propiedad.
   - Usa prisma.property.aggregate() para obtener min, max
     y count global.

