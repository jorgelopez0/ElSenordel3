package com.example.elsenordel3.data

enum class ModoJuego {
    CLASICO,
    VIDAS,
    RELAMPAGO
}

enum class EtapaPartida {
    CONFIGURACION,       // Pantalla inicial para setear reglas/jugadores
    BUSCANDO_SENOR,      // Primera ronda: 1 dado para ver quién es el Señor del 3
    RONDA_PARTIDA        // Partida normal: 2 dados con repeticiones
}

data class Jugador(
    val nombre: String,
    val esSenorDel3: Boolean = false,
    val puntos: Int = 0 // Útil para futuros modos
)

data class JuegoEstado(
    val modo: ModoJuego = ModoJuego.CLASICO,
    val etapa: EtapaPartida = EtapaPartida.CONFIGURACION,
    val jugadores: List<Jugador> = emptyList(),
    val jugadorActualIndex: Int = 0,
    val dado1: Int = 1,
    val dado2: Int = 1,
    val historialAcciones: List<String> = emptyList(),

    // Configuraciones de partida
    val permitirMultiplesSenores: Boolean = true,
    val senorElegidoEnEstaRonda: Boolean = false // Flag auxiliar para el modo de un solo Señor
)