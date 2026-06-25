# El Señor del 3 - Android App

Bienvenido.
Este es el repositorio de **El Señor del 3**, una aplicación móvil nativa para Android basada en
un juego revolucionario para las previas con tus amigos. Alguna vez has pensado: esta previa está muerta
no hay nadie borracho ni pinta que lo vaya a haber, nos vamos a discoteca y estamos todos tiesos,
te traigo una solución, una luz al final del túnel de la previa más aburrida de tu vida, que todo el
mundo se recargue el cubata porque estamos a punto de darle la vuelta a la tortilla.

El proyecto está diseñado bajo una
arquitectura limpia y moderna, sirviendo como mi primera experiencia práctica en el desarrollo
móvil nativo. No me juzgues mucho.

---

## Stack Tecnológico & Arquitectura

Para este desarrollo decidí aplicar los estándares de la industria móvil actual, dejando de lado las tecnologías heredadas (como Java o vistas tradicionales en XML):

* **Lenguaje de Programación:** [Kotlin](https://kotlinlang.org/) (100% Nativo).
* **Framework de UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (UI Declarativa y Reactiva).
* **Entorno de Desarrollo:** Android Studio.
* **Patrón de Arquitectura:** **MVVM (Model-View-ViewModel)**.
    * Separación estricta de responsabilidades.
    * Manejo del estado del juego de forma reactiva mediante flujos asíncronos (`MutableStateFlow`).
    * Desacoplamiento total entre la lógica del negocio (reglas de juego) y la renderización en pantalla.

---

## Historial de la evolución del proyecto

Esta sección documenta cómo ha ido cambiando el proyecto. No se elimina lo antiguo: se conserva como referencia y se anotan los cambios de rumbo.

### Version inicial (commit anterior)
* Solo el Modo Normal estaba implementado correctamente.
* El Señor del 3 se marcaba en la app con una corona.
* El Modo Hardcore estaba presente pero mal planteado (cada jugador tiraba 3 veces en la fase 1).
* Para tirar los dados habia un boton dedicado.
* Los dados eran dos cuadrados con un numero dentro, sin animacion real.
* El tema visual era verde oscuro tipo mesa de casino.
* La interfaz mostraba un historial de acciones fijo bajo los dados.

### Cambios de rumbo aplicados despues
* **Modo Hardcore corregido.** Ahora la fase 1 es una sola ronda: cada jugador tira un dado una unica vez y ese numero pasa a ser el suyo. La fase 2 tira dos dados con un maximo de 3 tiradas por turno.
* **La corona desaparece.** El Señor del 3 ya no se marca con una corona, sino con un 3 junto a su nombre. Se eliminaron todos los emojis de la interfaz y de los mensajes del historial.
* **Tema visual nuevo.** Se sustituye el verde oscuro por una paleta de azul claro y rosa.
* **Dados protagonistas.** Los dos cuadrados planos pasan a ser dados grandes (uno arriba y otro abajo) que ocupan la mayor parte de la pantalla, con sus puntos dibujados como un dado real. Los contadores y los nombres pasan a una zona secundaria.
* **Animaciones 3D escaladas.** Los dados se animan en 3D. Cuanto mas alto es el contador de veces que ha bebido el Señor del 3, mas estilos raros se desbloquean (saltos, giros planos en 2D, gigantes, temblores, giros lentos e incluso animaciones casi inexistentes mezcladas con las mas caoticas). Cada dado elige su estilo de forma independiente.
* **Tirar tocando la pantalla.** Se elimina el boton de lanzamiento; ahora un toque en la zona de juego lanza los dados.
* **Pantalla de instrucciones.** Al abrir la app se muestra un tutorial resumido con un boton para saltarlo.
* **Audio "ZZZ".** Se reproduce un sonido cuando un jugador recibe el movil en la fase de dos dados.

### Cambios respecto a funciones que ya no existen
* La version inicial mencionaba poder **configurar al inicio si se admite un unico Señor del 3 o multiples**. En la implementacion actual esa opcion de configuracion no existe: cualquiera que saque un 3 en la fase de busqueda se convierte en Señor del 3, por lo que puede haber varios. Se deja anotado por si se quisiera recuperar como ajuste.

### Pendiente / proximos pasos
* Persistir el tutorial para que aparezca solo la primera vez tras instalar (SharedPreferences).
* Modo Ultra Hardcore con reconocimiento de voz: detectar cuando un jugador nombra a otro para que ese beba.
* Decidir como se combinan las dificultades y modos entre si.

---

## Mecánica del Juego (Modo Clásico) — diseño original

> Esta es la descripción tal y como estaba en el commit anterior. Se conserva como referencia historica. El comportamiento actual aparece mas abajo en "Mecanica actual".

El juego se divide en dos macro-etapas secuenciales gestionadas mediante una máquina de estados en el ViewModel:

### Fase 1: La Búsqueda del Señor del 3
1. Cada jugador tira **un solo dado** por turnos.
2. Si un jugador saca el número `3`, se convierte automáticamente en el **Señor del 3** (marcado en la app con una corona `👑`).
3. El juego permite configurar al inicio si se admite un único Señor o múltiples.
4. Al finalizar la primera ronda, si ya existe al menos un Señor del 3, el juego avanza automáticamente a la siguiente fase.

### Fase 2: Partida Regular
1. Los jugadores tiran **dos dados** en su turno.
2. **Regla de repetición:** El jugador actual mantiene el turno y continúa lanzando si se cumple alguna de estas condiciones:
    * La suma de ambos dados es igual a **7, 8 o 9**.
    * Al menos uno de los dados muestra el número **3**.
3. Si no ocurre ninguna de las anteriores, el turno pasa limpiamente al siguiente jugador.

---

## Mecánica actual

### Modo Normal

**Fase 1: La Busqueda del Señor del 3**
1. Cada jugador tira un solo dado por turnos.
2. Quien saca un 3 se convierte en el Señor del 3 (marcado con un 3 junto a su nombre).
3. Pueden ser varios si varios sacan un 3 en la ronda.
4. Al terminar la ronda, si ya hay al menos un Señor del 3 se pasa a la fase de dos dados. Si no, se repite la ronda.

**Fase 2: Partida con dos dados**
1. Los jugadores tiran dos dados en su turno.
2. El jugador conserva el turno y sigue lanzando si:
    * La suma es 7, 8 o 9, o
    * Al menos un dado muestra un 3 (en ese caso bebe el Señor del 3 y el contador sube).
3. Reglas adicionales por suma: con 7 bebe el jugador anterior, con 8 todos dicen "Mierda" y el ultimo bebe, con 9 bebe el siguiente. Los dobles hacen que el jugador reparta tragos.
4. Si no se cumple ninguna condicion de repeticion, el turno pasa al siguiente jugador y suena el "ZZZ".

### Modo Hardcore

**Fase 1: Asignacion de numeros**
1. Cada jugador tira un solo dado una unica vez. El numero que saque es su numero personal para el resto de la partida.

**Fase 2: Partida**
1. Se tiran dos dados, con un maximo de 3 tiradas por turno.
2. Si aparece el numero de algun jugador, ese jugador bebe y el jugador actual conserva el turno.
3. Al llegar a 3 tiradas, o si no sale ningun numero, el turno pasa al siguiente jugador.

---

## Diseño e interacción (estado actual)

* **Tema visual:** degradado de azul claro a rosa, con acentos para destacar el turno activo.
* **Dados:** dos dados grandes (uno arriba y otro abajo) que ocupan la mayor parte de la pantalla, con sus puntos dibujados. En la fase de busqueda se muestra un solo dado centrado.
* **Tirar:** se lanza tocando la zona de juego; no hay boton.
* **Animaciones:** en 3D, con estilos que se vuelven mas raros segun el contador del Señor del 3.
* **Tutorial:** pantalla inicial con los pasos resumidos y opcion de saltar.
* **Audio "ZZZ":** suena cuando un jugador recibe el movil en la fase de dos dados.

### Como anadir el audio "ZZZ"
1. Coloca tu archivo de sonido en `app/src/main/res/raw/` con el nombre `zzz` (por ejemplo `zzz.mp3`, `zzz.ogg` o `zzz.wav`).
2. La app lo localiza automaticamente por su nombre. Si el archivo no existe, el juego funciona igual sin reproducir nada.

---

## Estructura del Proyecto

El código fuente sigue una distribución modularizada por capas para asegurar la escalabilidad hacia futuros modos de juego:

```text
com.example.elsenordel3/
│
├── data/               # CAPA MODELO: Estructura de datos y definición de estados
│   └── JuegoEstado.kt  # Clases de datos (Jugador, EtapaPartida, ModoJuego)
│
├── viewmodel/          # CAPA CEREBRO: Lógica de negocio y máquina de estados
│   └── JuegoViewModel.kt
│
├── ui/                 # CAPA VISTA: Componentes e interfaces gráficas reactivas
│   ├── theme/          # Sistema de diseño (Colores, Tipografías)
│   └── PantallaJuego.kt  # Tutorial, configuración, mesa de juego, dados y animaciones
│
└── MainActivity.kt     # Punto de entrada de la aplicación
```

> Nota historica: en el commit anterior, PantallaJuego.kt se describia como "Composicion visual de la mesa de juego, dados e historial". Ahora ademas incluye la pantalla de tutorial, la de configuracion y todo el sistema de animaciones de los dados.