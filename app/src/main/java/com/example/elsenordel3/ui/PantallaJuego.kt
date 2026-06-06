package com.example.elsenordel3.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elsenordel3.data.EtapaPartida
import com.example.elsenordel3.viewmodel.JuegoViewModel

@Composable
fun PantallaJuego(viewModel: JuegoViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()
    val jugadorActual = estado.jugadores.getOrNull(estado.jugadorActualIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Cabecera e información de jugadores
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Modo: ${estado.modo.name}", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Lista rápida de jugadores en pantalla
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                estado.jugadores.forEachIndexed { index, jug ->
                    val esSuTurno = index == estado.jugadorActualIndex
                    Text(
                        text = "${jug.nombre}${if (jug.esSenorDel3) " 👑" else ""}",
                        color = if (esSuTurno) Color.Red else Color.Black,
                        fontSize = if (esSuTurno) 18.sp else 14.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }

        // 2. Zona central: Turno actual y Dados
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Turno de: ${jugadorActual?.nombre ?: "Nadie"}",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = if (estado.etapa == EtapaPartida.BUSCANDO_SENOR) "Fase 1: Buscando al Señor del 3" else "Fase 2: Partida Regular",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ComponenteDado(valor = estado.dado1)

                // Solo se muestra el segundo dado si ya estamos en la etapa de ronda de partida
                if (estado.etapa == EtapaPartida.RONDA_PARTIDA) {
                    Spacer(modifier = Modifier.width(16.dp))
                    ComponenteDado(valor = estado.dado2)
                }
            }
        }

        // 3. Historial de acciones (Scrollable)
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp)
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            LazyColumn(reverseLayout = true) { // reverseLayout para ver lo último siempre arriba
                items(estado.historialAcciones.reversed()) { accion ->
                    Text(text = accion, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }

        // 4. Botón de acción principal
        Button(
            onClick = { viewModel.lanzarDados() },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Text(
                text = if (estado.etapa == EtapaPartida.BUSCANDO_SENOR) "Tirar 1 Dado" else "Tirar 2 Dados",
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun ComponenteDado(valor: Int) {
    Box(
        modifier = Modifier
            .size(75.dp)
            .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = valor.toString(), fontSize = 30.sp, style = MaterialTheme.typography.headlineLarge)
    }
}