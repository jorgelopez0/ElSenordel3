package com.example.elsenordel3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.elsenordel3.data.EtapaPartida
import com.example.elsenordel3.data.JuegoEstado
import com.example.elsenordel3.data.Jugador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class JuegoViewModel : ViewModel() {

    private val _estado = MutableStateFlow(JuegoEstado())
    val estado: StateFlow<JuegoEstado> = _estado.asStateFlow()

    // Inicializador temporal para pruebas (reemplaza la pantalla de configuración por ahora)
    init {
        iniciarPartidaDePrueba()
    }

    private fun iniciarPartidaDePrueba() {
        val listaPrueba = listOf(Jugador("Jugador1"), Jugador("Mondo"), Jugador("Jugador3"))
        _estado.update { it.copy(
            jugadores = listaPrueba,
            etapa = EtapaPartida.BUSCANDO_SENOR,
            historialAcciones = listOf("Fase de Elección: Tiren 1 dado para buscar al Señor del 3.")
        )}
    }

    fun lanzarDados() {
        val estadoActual = _estado.value
        if (estadoActual.jugadores.isEmpty()) return

        when (estadoActual.etapa) {
            EtapaPartida.CONFIGURACION -> { /* Se maneja en la UI antes de empezar */ }
            EtapaPartida.BUSCANDO_SENOR -> jugarFaseBusqueda(estadoActual)
            EtapaPartida.RONDA_PARTIDA -> jugarFasePartida(estadoActual)
        }
    }

    // --- FASE 1: BUSCANDO AL SEÑOR DEL 3 (1 DADO) ---
    private fun jugarFaseBusqueda(estadoActual: JuegoEstado) {
        val resultadoDado = Random.nextInt(1, 7)
        val jugadorActual = estadoActual.jugadores[estadoActual.jugadorActualIndex]
        var nuevoHistorial = estadoActual.historialAcciones.toMutableList()
        var nuevosJugadores = estadoActual.jugadores.toMutableList()
        var yaHaySenor = estadoActual.senorElegidoEnEstaRonda

        var esSenor = false
        if (resultadoDado == 3) {
            if (estadoActual.permitirMultiplesSenores || !yaHaySenor) {
                esSenor = true
                yaHaySenor = true
                nuevosJugadores[estadoActual.jugadorActualIndex] = jugadorActual.copy(esSenorDel3 = true)
                nuevoHistorial.add("¡${jugadorActual.nombre} sacó un 3 y es SEÑOR DEL 3! 👑")
            } else {
                nuevoHistorial.add("${jugadorActual.nombre} sacó un 3, pero ya hay un Señor del 3.")
            }
        } else {
            nuevoHistorial.add("${jugadorActual.nombre} sacó un $resultadoDado.")
        }

        // Calcular siguiente turno o pasar a la siguiente etapa
        val esUltimoJugador = estadoActual.jugadorActualIndex == estadoActual.jugadores.lastIndex

        if (esUltimoJugador) {
            // Verificamos si alguien se convirtió en Señor. Si no, se repite la ronda de búsqueda.
            val algunSenor = nuevosJugadores.any { it.esSenorDel3 }
            if (algunSenor) {
                nuevoHistorial.add("--- ¡Comienza la partida con 2 dados! ---")
                _estado.update { it.copy(
                    dado1 = resultadoDado,
                    jugadores = nuevosJugadores,
                    jugadorActualIndex = 0, // Reinicia el orden para la partida
                    etapa = EtapaPartida.RONDA_PARTIDA,
                    historialAcciones = nuevoHistorial
                )}
            } else {
                nuevoHistorial.add("Nadie sacó 3. ¡Otra vuelta de calentamiento!")
                _estado.update { it.copy(
                    dado1 = resultadoDado,
                    jugadorActualIndex = 0,
                    historialAcciones = nuevoHistorial
                )}
            }
        } else {
            // Pasa al siguiente jugador en la ronda de búsqueda
            _estado.update { it.copy(
                dado1 = resultadoDado,
                senorElegidoEnEstaRonda = yaHaySenor,
                jugadores = nuevosJugadores,
                jugadorActualIndex = estadoActual.jugadorActualIndex + 1,
                historialAcciones = nuevoHistorial
            )}
        }
    }

    // --- FASE 2: RONDA DE PARTIDA REPETITIVA (2 DADOS) ---
    private fun jugarFasePartida(estadoActual: JuegoEstado) {
        val d1 = Random.nextInt(1, 7)
        val d2 = Random.nextInt(1, 7)
        val suma = d1 + d2
        val jugadorActual = estadoActual.jugadores[estadoActual.jugadorActualIndex]
        val nuevoHistorial = estadoActual.historialAcciones.toMutableList()

        // Regla: Se repite turno si suma 7, 8, 9 o si algún dado es 3
        val cumpleReglaRepeticion = (suma in 7..9) || (d1 == 3 || d2 == 3)

        var textoAccion = "${jugadorActual.nombre} tiró [$d1, $d2] (Suma: $suma)."

        // Aquí es donde en el futuro añadiremos el "castigo" de beber o perder vidas según el modo
        if (cumpleReglaRepeticion) {
            textoAccion += " ¡Se activó el 3! Vuelve a tirar."
        }

        nuevoHistorial.add(textoAccion)

        if (cumpleReglaRepeticion) {
            // El jugador mantiene el turno, solo actualizamos los dados y el historial
            _estado.update { it.copy(
                dado1 = d1,
                dado2 = d2,
                historialAcciones = nuevoHistorial
            )}
        } else {
            // No pasó nada especial, el turno cambia al siguiente jugador
            val siguienteJugadorIndex = (estadoActual.jugadorActualIndex + 1) % estadoActual.jugadores.size
            _estado.update { it.copy(
                dado1 = d1,
                dado2 = d2,
                jugadorActualIndex = siguienteJugadorIndex,
                historialAcciones = nuevoHistorial
            )}
        }
    }
}