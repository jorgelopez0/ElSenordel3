package com.example.elsenordel3.data

enum class ModoJuego {
    NORMAL,
    HARDCORE
}

enum class EtapaPartida {
    TUTORIAL,
    CONFIGURACION,
    BUSCANDO_SENOR,
    RONDA_PARTIDA
}

data class Jugador(
    val nombre: String,
    val esSenorDel3: Boolean = false,
    val numerosAsignados: List<Int> = emptyList(), // Para el modo Hardcore
    val vecesBebidas: Int = 0
)

data class JuegoEstado(
    val modo: ModoJuego = ModoJuego.NORMAL,
    val etapa: EtapaPartida = EtapaPartida.TUTORIAL,
    val jugadores: List<Jugador> = emptyList(),
    val jugadorActualIndex: Int = 0,
    val dado1: Int = 1,
    val dado2: Int = 1,
    val historialAcciones: List<String> = emptyList(),

    // Contadores en partida
    val tiradasJugadorActual: Int = 1,
    val vecesHaBebidoSenorDel3: Int = 0,

    // Control interno para Fase 1 Normal
    val senorElegidoEnEstaRonda: Boolean = false,

    // Control interno para Fase 1 Hardcore
    val hardcoreFase1Completada: Boolean = false,

    // Contador de tiradas en fase 2 Hardcore (máx 3 por turno)
    val tiradasEnTurnoActual: Int = 0,

    // Señal para reproducir el audio ZZZ (solo al recibir el móvil en fase de 2 dados)
    val reproducirAudioZZZ: Boolean = false
)