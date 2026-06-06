# 👑 El Señor del 3 - Android App

¡Bienvenido! Este es el repositorio de **El Señor del 3**, una aplicación móvil nativa para Android basada en el popular juego social de dados. El proyecto está diseñado bajo una arquitectura limpia y moderna, sirviendo como mi primera experiencia práctica en el desarrollo móvil nativo.

---

## 🛠️ Stack Tecnológico & Arquitectura

Para este desarrollo decidí aplicar los estándares de la industria móvil actual, dejando de lado las tecnologías heredadas (como Java o vistas tradicionales en XML):

* **Lenguaje de Programación:** [Kotlin](https://kotlinlang.org/) (100% Nativo).
* **Framework de UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (UI Declarativa y Reactiva).
* **Entorno de Desarrollo:** Android Studio.
* **Patrón de Arquitectura:** **MVVM (Model-View-ViewModel)**.
    * Separación estricta de responsabilidades.
    * Manejo del estado del juego de forma reactiva mediante flujos asíncronos (`MutableStateFlow`).
    * Desacoplamiento total entre la lógica del negocio (reglas de juego) y la renderización en pantalla.

---

## 🎲 Mecánica del Juego (Modo Clásico)

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

## 📂 Estructura del Proyecto

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
│   └── PantallaJuego.kt# Composición visual de la mesa de juego, dados e historial
│
└── MainActivity.kt     # Punto de entrada de la aplicación