package com.example.elsenordel3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.elsenordel3.data.EtapaPartida
import com.example.elsenordel3.data.JuegoEstado
import com.example.elsenordel3.data.Jugador
import com.example.elsenordel3.data.ModoJuego
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class JuegoViewModel : ViewModel() {

    private val _estado = MutableStateFlow(JuegoEstado())
    val estado: StateFlow<JuegoEstado> = _estado.asStateFlow()

    // --- NAVEGACIÓN DE PANTALLAS DE INSTRUCCIONES ---
    /** Instrucciones generales -> pantalla de configuración */
    fun saltarTutorial() {
        _estado.update { it.copy(etapa = EtapaPartida.CONFIGURACION) }
    }

    // --- MÉTODOS DE CONFIGURACIÓN ---
    fun agregarJugador(nombre: String) {
        if (nombre.isNotBlank()) {
            _estado.update { it.copy(jugadores = it.jugadores + Jugador(nombre.trim())) }
        }
    }

    fun quitarJugador(jugador: Jugador) {
        _estado.update { it.copy(jugadores = it.jugadores - jugador) }
    }

    fun setModoJuego(modo: ModoJuego) {
        _estado.update { it.copy(modo = modo) }
    }

    /** Configuración -> instrucciones del modo elegido */
    fun iniciarPartida() {
        val estadoActual = _estado.value
        if (estadoActual.jugadores.size < 2) return
        _estado.update { it.copy(etapa = EtapaPartida.INSTRUCCIONES_MODO) }
    }

    /** Instrucciones del modo -> empieza la partida de verdad */
    fun empezarPartida() {
        val estadoActual = _estado.value
        if (estadoActual.jugadores.size < 2) return

        val mensajeInicial = if (estadoActual.modo == ModoJuego.NORMAL) {
            "Comienza el Modo Normal. Tirad 1 dado para buscar al Señor del 3."
        } else {
            "Comienza el Modo Hardcore. Cada jugador tira 1 dado. Ese será su número."
        }

        _estado.update {
            it.copy(
                etapa = EtapaPartida.BUSCANDO_SENOR,
                historialAcciones = listOf(mensajeInicial),
                jugadorActualIndex = 0,
                tiradasJugadorActual = 1,
                tiradasEnTurnoActual = 0,
                vecesHaBebidoSenorDel3 = 0,
                senorElegidoEnEstaRonda = false,
                reproducirAudioZZZ = false
            )
        }
    }

    /** Llamado desde la UI tras consumir el evento de audio ZZZ */
    fun audioZZZConsumido() {
        _estado.update { it.copy(reproducirAudioZZZ = false) }
    }

    // --- MOTOR PRINCIPAL ---
    fun lanzarDados() {
        val estadoActual = _estado.value
        if (estadoActual.jugadores.isEmpty()) return

        when (estadoActual.etapa) {
            EtapaPartida.TUTORIAL -> { }
            EtapaPartida.CONFIGURACION -> { }
            EtapaPartida.INSTRUCCIONES_MODO -> { }
            EtapaPartida.BUSCANDO_SENOR -> if (estadoActual.modo == ModoJuego.NORMAL)
                jugarFaseBusquedaNormal(estadoActual)
            else
                jugarFaseBusquedaHardcore(estadoActual)

            EtapaPartida.RONDA_PARTIDA -> if (estadoActual.modo == ModoJuego.NORMAL)
                jugarFasePartidaNormal(estadoActual)
            else
                jugarFasePartidaHardcore(estadoActual)
        }
    }

    // --- FASE 1: BUSCANDO AL SEÑOR (NORMAL) ---
    private fun jugarFaseBusquedaNormal(estadoActual: JuegoEstado) {
        val dado = Random.nextInt(1, 7)
        val jugadorActual = estadoActual.jugadores[estadoActual.jugadorActualIndex]
        val nuevosJugadores = estadoActual.jugadores.toMutableList()
        val nuevoHistorial = estadoActual.historialAcciones.toMutableList()
        var yaHaySenor = estadoActual.senorElegidoEnEstaRonda

        if (dado == 3) {
            yaHaySenor = true
            nuevosJugadores[estadoActual.jugadorActualIndex] = jugadorActual.copy(esSenorDel3 = true)
            nuevoHistorial.add("${jugadorActual.nombre} sacó un 3 y es el SEÑOR DEL 3.")
        } else {
            nuevoHistorial.add("${jugadorActual.nombre} sacó un $dado.")
        }

        val esUltimoJugador = estadoActual.jugadorActualIndex == estadoActual.jugadores.lastIndex

        if (esUltimoJugador) {
            if (yaHaySenor) {
                nuevoHistorial.add("Comienza la partida con 2 dados.")
                _estado.update {
                    it.copy(
                        dado1 = dado, jugadores = nuevosJugadores, jugadorActualIndex = 0,
                        etapa = EtapaPartida.RONDA_PARTIDA, historialAcciones = nuevoHistorial,
                        tiradasJugadorActual = 1, tiradasEnTurnoActual = 0,
                        reproducirAudioZZZ = false // El inicio no dispara el ZZZ
                    )
                }
            } else {
                nuevoHistorial.add("Nadie fue Señor del 3. Otra ronda de calentamiento.")
                _estado.update {
                    it.copy(
                        dado1 = dado, jugadorActualIndex = 0, historialAcciones = nuevoHistorial,
                        senorElegidoEnEstaRonda = false, reproducirAudioZZZ = false
                    )
                }
            }
        } else {
            _estado.update {
                it.copy(
                    dado1 = dado, senorElegidoEnEstaRonda = yaHaySenor,
                    jugadores = nuevosJugadores, jugadorActualIndex = estadoActual.jugadorActualIndex + 1,
                    historialAcciones = nuevoHistorial,
                    reproducirAudioZZZ = false
                )
            }
        }
    }

    // --- FASE 1: BUSCANDO NÚMEROS (HARDCORE) ---
    private fun jugarFaseBusquedaHardcore(estadoActual: JuegoEstado) {
        val dado = Random.nextInt(1, 7)
        val index = estadoActual.jugadorActualIndex
        val jugadorActual = estadoActual.jugadores[index]
        val nuevosJugadores = estadoActual.jugadores.toMutableList()
        val nuevoHistorial = estadoActual.historialAcciones.toMutableList()

        nuevosJugadores[index] = jugadorActual.copy(numerosAsignados = listOf(dado))
        nuevoHistorial.add("${jugadorActual.nombre} obtiene el número $dado. Ese es su número.")

        val esUltimoJugador = index == estadoActual.jugadores.lastIndex

        if (esUltimoJugador) {
            nuevoHistorial.add("Todos tienen su número. Comienza la partida.")
            _estado.update {
                it.copy(
                    dado1 = dado, jugadores = nuevosJugadores, jugadorActualIndex = 0,
                    etapa = EtapaPartida.RONDA_PARTIDA, historialAcciones = nuevoHistorial,
                    tiradasJugadorActual = 1, tiradasEnTurnoActual = 0,
                    hardcoreFase1Completada = true,
                    reproducirAudioZZZ = false // El inicio no dispara el ZZZ
                )
            }
        } else {
            _estado.update {
                it.copy(
                    dado1 = dado, jugadores = nuevosJugadores,
                    jugadorActualIndex = index + 1,
                    historialAcciones = nuevoHistorial,
                    reproducirAudioZZZ = false
                )
            }
        }
    }

    // --- FASE 2: PARTIDA NORMAL (2 DADOS) ---
    private fun jugarFasePartidaNormal(estadoActual: JuegoEstado) {
        val d1 = Random.nextInt(1, 7)
        val d2 = Random.nextInt(1, 7)
        val suma = d1 + d2
        val indexActual = estadoActual.jugadorActualIndex
        val jugadorActual = estadoActual.jugadores[indexActual]
        val nuevoHistorial = estadoActual.historialAcciones.toMutableList()
        var vecesSenorBebio = estadoActual.vecesHaBebidoSenorDel3

        nuevoHistorial.add("${jugadorActual.nombre} tiró [$d1, $d2] (Suma: $suma).")

        var pasoAlgo = false

        if (d1 == 3 || d2 == 3) {
            nuevoHistorial.add("Salió un 3. El Señor del 3 bebe.")
            vecesSenorBebio++
            pasoAlgo = true
        }
        if (suma == 7) {
            val indexAnterior = (indexActual - 1 + estadoActual.jugadores.size) % estadoActual.jugadores.size
            nuevoHistorial.add("Suma 7. Bebe ${estadoActual.jugadores[indexAnterior].nombre}.")
            pasoAlgo = true
        }
        if (suma == 8) {
            nuevoHistorial.add("Suma 8. Todos dicen 'Mierda', el último bebe.")
            pasoAlgo = true
        }
        if (suma == 9) {
            val indexSiguiente = (indexActual + 1) % estadoActual.jugadores.size
            nuevoHistorial.add("Suma 9. Bebe ${estadoActual.jugadores[indexSiguiente].nombre}.")
            pasoAlgo = true
        }
        if (d1 == d2) {
            nuevoHistorial.add("Dobles ($d1). ${jugadorActual.nombre} reparte tragos.")
            pasoAlgo = true
        }

        if (pasoAlgo) {
            nuevoHistorial.add("${jugadorActual.nombre} conserva el turno.")
            _estado.update {
                it.copy(
                    dado1 = d1, dado2 = d2, historialAcciones = nuevoHistorial,
                    vecesHaBebidoSenorDel3 = vecesSenorBebio,
                    tiradasJugadorActual = it.tiradasJugadorActual + 1,
                    tiradasEnTurnoActual = it.tiradasEnTurnoActual + 1,
                    reproducirAudioZZZ = false
                )
            }
        } else {
            nuevoHistorial.add("Nada especial. Pasa el móvil.")
            // El jugador hizo estadoActual.tiradasJugadorActual tiradas en este turno.
            // El ZZZ solo suena si tiró menos de 2 veces (pasó casi al recibir el móvil).
            val sonarZZZ = estadoActual.tiradasJugadorActual < 2
            _estado.update {
                it.copy(
                    dado1 = d1, dado2 = d2, historialAcciones = nuevoHistorial,
                    vecesHaBebidoSenorDel3 = vecesSenorBebio,
                    jugadorActualIndex = (indexActual + 1) % it.jugadores.size,
                    tiradasJugadorActual = 1,
                    tiradasEnTurnoActual = 0,
                    reproducirAudioZZZ = sonarZZZ
                )
            }
        }
    }

    // --- FASE 2: PARTIDA HARDCORE (2 DADOS, MÁXIMO 3 TIRADAS POR TURNO) ---
    private fun jugarFasePartidaHardcore(estadoActual: JuegoEstado) {
        val d1 = Random.nextInt(1, 7)
        val d2 = Random.nextInt(1, 7)
        val indexActual = estadoActual.jugadorActualIndex
        val jugadorActual = estadoActual.jugadores[indexActual]
        val nuevoHistorial = estadoActual.historialAcciones.toMutableList()
        val nuevasTiradasEnTurno = estadoActual.tiradasEnTurnoActual + 1

        nuevoHistorial.add("${jugadorActual.nombre} tiró [$d1, $d2]. (Tirada $nuevasTiradasEnTurno/3)")
        var pasoAlgo = false

        estadoActual.jugadores.forEach { jug ->
            if (jug.numerosAsignados.any { it == d1 || it == d2 }) {
                nuevoHistorial.add("Salió el número de ${jug.nombre}. Bebe.")
                pasoAlgo = true
            }
        }

        val forzarCambioTurno = nuevasTiradasEnTurno >= 3

        if (pasoAlgo && !forzarCambioTurno) {
            nuevoHistorial.add("${jugadorActual.nombre} conserva el turno.")
            _estado.update {
                it.copy(
                    dado1 = d1, dado2 = d2, historialAcciones = nuevoHistorial,
                    tiradasJugadorActual = it.tiradasJugadorActual + 1,
                    tiradasEnTurnoActual = nuevasTiradasEnTurno,
                    reproducirAudioZZZ = false
                )
            }
        } else {
            if (forzarCambioTurno) {
                nuevoHistorial.add("3 tiradas completadas. Pasa el móvil.")
            } else {
                nuevoHistorial.add("Ningún número. Pasa el móvil.")
            }
            // El jugador hizo nuevasTiradasEnTurno tiradas. ZZZ solo si fueron menos de 2.
            val sonarZZZ = nuevasTiradasEnTurno < 2
            _estado.update {
                it.copy(
                    dado1 = d1, dado2 = d2, historialAcciones = nuevoHistorial,
                    jugadorActualIndex = (indexActual + 1) % it.jugadores.size,
                    tiradasJugadorActual = 1,
                    tiradasEnTurnoActual = 0,
                    reproducirAudioZZZ = sonarZZZ
                )
            }
        }
    }
}