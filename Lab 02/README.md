Android Lab – Jetpack Compose  
RPG Character Sheet & Traffic Light Simulator
-- * --
PARTE 1: RPG Character Sheet – Dice Roller
Descripción  
Aplicación Android que simula una hoja de creación de personaje estilo RPG, permitiendo generar estadísticas mediante lanzamientos de dados animados.

Objetivo de la PARTE 1
Practicar manejo de estado, composables reutilizables y corrutinas en Jetpack Compose.

Funcionalidades Principales  
- Tres estadísticas: Vitality, Dexterity y Wisdom  
- Botón individual para lanzar cada dado  
- Animación de lanzamiento usando corrutinas  
- Cálculo automático del puntaje total  
- Feedback según el puntaje obtenido  

Conceptos Aplicados  
- Jetpack Compose  
- State Hoisting  
- Kotlin Coroutines  
- Material 3  

-- * --
PARTE 2: Traffic Light Simulator – Semáforo
Descripción  
Simulador de semáforo desarrollado con Jetpack Compose que funciona de manera automática y continua, sin interacción del usuario.

Objetivo de la PARTE 2
Practicar manejo de estado y el uso de LaunchedEffect para ejecutar lógica automática.

Lógica Principal  
- Uso de un enum class para los estados del semáforo  
- Estado observable con mutableStateOf  
- Corrutina con LaunchedEffect(Unit) y ciclo infinito  

Temporización  
- Rojo: 2 segundos  
- Verde: 2 segundos  
- Amarillo: 1 segundo  

Interfaz de Usuario  
- Tres círculos alineados verticalmente  
- Luz activa en color brillante y luces inactivas en gris  
- Interfaz centrada con diseño limpio  

-- * --
APOYO
- Kotlin  
- Jetpack Compose  
- Coroutines  

BY 
Sofía Dávila  
Android Lab – Jetpack Compose
