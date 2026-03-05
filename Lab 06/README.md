--- * -- PARTE 01 -- * ---

DESCRIPCIÓN: Funcionalidad que permite a los usuarios marcar recetas como favoritas y filtrar
su lista para ver solo las que mas les gustan. El estado se sincroniza con Firestore
y persiste entre sesiones, por lo que los favoritos se mantienen aunque el usuario
cierre y vuelva a abrir la app.

CARACTERÍSTICAS

- Icono de corazon en cada tarjeta de receta (relleno = favorito, vacio = no favorito)
- Toggle de favorito con un tap, actualizando el documento en Firestore
- UI optimista: el corazon cambia visualmente al instante sin esperar respuesta del servidor
- Si ocurre un error en Firestore, el cambio se revierte automaticamente
- Filtro para alternar entre ver todas las recetas o solo las favoritas
- Actualizaciones en tiempo real gracias al listener de Firestore
- Campo isFavorite (Boolean) agregado al modelo Recipe

FLUJO:

Usuario toca el corazon
        |
        v
Corazon cambia visualmente al instante (UI optimista)
        |
        v
toggleFavorite() envia actualizacion a Firestore
        |
        v
Exito? --> Se mantiene el cambio en pantalla
Error? --> Se revierte el icono al estado anterior

--- * -- PARTE 02 -- * ---

DESCRIPCIÓN: Funcionalidad que permite a los usuarios compartir sus recetas generadas por IA
como una imagen. Se captura la pantalla de detalle completa, incluyendo la foto
del plato y la informacion de la receta, y se comparte usando el share sheet
nativo de Android.

CARACTERÍSTICAS

- Captura la pantalla de detalle de la receta como imagen Bitmap
- La imagen incluye la foto del plato generada por IA, el titulo y los ingredientes
- Usa el share sheet nativo de Android para elegir la app destino
- Compatible con cualquier app que acepte imagenes: WhatsApp, Telegram, Email, etc.
- Muestra un estado de carga mientras se prepara la imagen para compartir
- Usa FileProvider en el AndroidManifest para compartir archivos de forma segura
- Los archivos temporales generados se eliminan automaticamente despues de compartir

FLUJO:

Usuario toca el boton Compartir
        |
        v
Se captura RecipeDetailScreen completo como Bitmap
        |
        v
Imagen se guarda temporalmente en almacenamiento interno (FileProvider)
        |
        v
Se abre el Share Sheet nativo de Android
        |
        v
Usuario elige la app destino y se comparte la imagen
        |
        v
Archivo temporal se elimina del dispositivo
