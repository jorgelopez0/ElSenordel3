package com.example.elsenordel3.ui

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elsenordel3.R
import com.example.elsenordel3.data.EtapaPartida
import com.example.elsenordel3.data.ModoJuego
import com.example.elsenordel3.viewmodel.JuegoViewModel
import kotlinx.coroutines.delay

// ─── Paleta de colores temática ──────────────────────────────────────────────
private val ColorMesa = Color(0xFF1B4332)          // Verde mesa de juego oscuro
private val ColorMesaClaro = Color(0xFF2D6A4F)     // Verde mesa más claro
private val ColorDado = Color(0xFFF5F0E8)           // Crema cálido para los dados
private val ColorDadoSombra = Color(0xFFD4C9B0)    // Sombra del dado
private val ColorPunto = Color(0xFF1A1A2E)          // Puntos casi negros con toque azul
private val ColorPunto3 = Color(0xFFE63946)         // Punto central del 3 en rojo (tradición)
private val ColorAccent = Color(0xFFFFD700)         // Oro para la corona y acentos
private val ColorTextoClaro = Color(0xFFF8F9FA)
private val ColorTarjeta = Color(0xFF0D2818)

@Composable
fun PantallaJuego(viewModel: JuegoViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    // ── Reproducir audio ZZZ cuando cambia el turno ──────────────────────────
    LaunchedEffect(estado.reproducirAudioZZZ) {
        if (estado.reproducirAudioZZZ) {
            reproducirAudioZZZ(context)
            viewModel.audioZZZConsumido()
        }
    }

    if (estado.etapa == EtapaPartida.CONFIGURACION) {
        PantallaConfiguracion(viewModel)
    } else {
        PantallaMesa(viewModel)
    }
}

/** Intenta reproducir R.raw.zzz si existe, sin crashear si no hay archivo */
private fun reproducirAudioZZZ(context: Context) {
    try {
        val resId = context.resources.getIdentifier("zzz", "raw", context.packageName)
        if (resId != 0) {
            val mp = MediaPlayer.create(context, resId)
            mp?.setOnCompletionListener { it.release() }
            mp?.start()
        }
    } catch (_: Exception) { /* Silencioso si no hay audio */ }
}

// ─── Pantalla de Configuración ───────────────────────────────────────────────
@Composable
fun PantallaConfiguracion(viewModel: JuegoViewModel) {
    val estado by viewModel.estado.collectAsState()
    var nombreInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0D2818), ColorMesa))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título
            Text(
                text = "👑",
                fontSize = 56.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "El Señor del 3",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ColorAccent,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Juego de dados",
                fontSize = 14.sp,
                color = ColorTextoClaro.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Selector de modo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorTarjeta),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MODO DE JUEGO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorAccent,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ModoBoton(
                            texto = "Normal",
                            emoji = "🎲",
                            seleccionado = estado.modo == ModoJuego.NORMAL,
                            modifier = Modifier.weight(1f)
                        ) { viewModel.setModoJuego(ModoJuego.NORMAL) }
                        ModoBoton(
                            texto = "Hardcore",
                            emoji = "💀",
                            seleccionado = estado.modo == ModoJuego.HARDCORE,
                            modifier = Modifier.weight(1f)
                        ) { viewModel.setModoJuego(ModoJuego.HARDCORE) }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val descripcion = if (estado.modo == ModoJuego.NORMAL)
                        "Busca al Señor del 3. Sumas 7/8/9 o un 3 mantienen el turno."
                    else
                        "Cada jugador tiene su número. 3 tiradas por turno."
                    Text(
                        text = descripcion,
                        fontSize = 12.sp,
                        color = ColorTextoClaro.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Añadir jugadores
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorTarjeta),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "JUGADORES (${estado.jugadores.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorAccent,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nombreInput,
                            onValueChange = { nombreInput = it },
                            label = { Text("Nombre", color = ColorTextoClaro.copy(alpha = 0.6f)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ColorTextoClaro,
                                unfocusedTextColor = ColorTextoClaro,
                                focusedBorderColor = ColorAccent,
                                unfocusedBorderColor = ColorTextoClaro.copy(alpha = 0.3f),
                                cursorColor = ColorAccent
                            )
                        )
                        Button(
                            onClick = { viewModel.agregarJugador(nombreInput); nombreInput = "" },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    if (estado.jugadores.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        estado.jugadores.forEachIndexed { i, jug ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${i + 1}. ${jug.nombre}",
                                    color = ColorTextoClaro,
                                    fontSize = 15.sp
                                )
                                TextButton(onClick = { viewModel.quitarJugador(jug) }) {
                                    Text("✕", color = Color(0xFFE63946))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.iniciarPartida() },
                enabled = estado.jugadores.size >= 2,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorAccent,
                    disabledContainerColor = ColorAccent.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "¡EMPEZAR!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
            if (estado.jugadores.size < 2) {
                Text(
                    "Necesitas al menos 2 jugadores",
                    fontSize = 12.sp,
                    color = ColorTextoClaro.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ModoBoton(texto: String, emoji: String, seleccionado: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (seleccionado) ColorAccent else ColorMesaClaro
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            "$emoji $texto",
            color = if (seleccionado) Color.Black else ColorTextoClaro,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

// ─── Pantalla Mesa de juego ───────────────────────────────────────────────────
@Composable
fun PantallaMesa(viewModel: JuegoViewModel) {
    val estado by viewModel.estado.collectAsState()
    val jugadorActual = estado.jugadores.getOrNull(estado.jugadorActualIndex)

    // Estado de animación de dados
    var lanzando by remember { mutableStateOf(false) }
    var dado1Visible by remember { mutableIntStateOf(estado.dado1) }
    var dado2Visible by remember { mutableIntStateOf(estado.dado2) }

    // Sincronizar valores de dados con el estado
    LaunchedEffect(estado.dado1, estado.dado2) {
        dado1Visible = estado.dado1
        dado2Visible = estado.dado2
    }

    // Historial expandible
    var historialExpandido by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D2818), ColorMesa)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Cabecera: modo y jugadores ─────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (estado.modo == ModoJuego.NORMAL) "🎲 Normal" else "💀 Hardcore",
                    fontSize = 12.sp,
                    color = ColorTextoClaro.copy(alpha = 0.6f)
                )
                if (estado.modo == ModoJuego.NORMAL) {
                    Text(
                        text = "👑 bebió: ${estado.vecesHaBebidoSenorDel3}×",
                        fontSize = 12.sp,
                        color = ColorAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Fichas de jugadores ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
            ) {
                estado.jugadores.forEach { jug ->
                    val esTurno = jug == jugadorActual
                    val indicativo = when {
                        estado.modo == ModoJuego.NORMAL && jug.esSenorDel3 -> " 👑"
                        estado.modo == ModoJuego.HARDCORE && jug.numerosAsignados.isNotEmpty() ->
                            " [${jug.numerosAsignados.joinToString(",")}]"
                        else -> ""
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (esTurno) ColorAccent else ColorTarjeta)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${jug.nombre}$indicativo",
                            fontSize = 13.sp,
                            fontWeight = if (esTurno) FontWeight.Bold else FontWeight.Normal,
                            color = if (esTurno) Color.Black else ColorTextoClaro.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Turno actual ───────────────────────────────────────────────
            Text(
                text = "TURNO DE",
                fontSize = 11.sp,
                letterSpacing = 3.sp,
                color = ColorTextoClaro.copy(alpha = 0.5f)
            )
            Text(
                text = jugadorActual?.nombre ?: "—",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ColorTextoClaro
            )
            if (estado.etapa == EtapaPartida.RONDA_PARTIDA) {
                val tiradasEnTurno = estado.tiradasEnTurnoActual
                val textoTiradas = if (estado.modo == ModoJuego.HARDCORE) {
                    "Tirada ${tiradasEnTurno + 1}/3 de este turno"
                } else {
                    "Tirada #${estado.tiradasJugadorActual} de este turno"
                }
                Text(
                    text = textoTiradas,
                    fontSize = 13.sp,
                    color = ColorTextoClaro.copy(alpha = 0.5f)
                )
            } else {
                val textoFase = if (estado.modo == ModoJuego.HARDCORE)
                    "Cada jugador tira 1 dado → ese es su número"
                else
                    "Buscando al Señor del 3 — Tira 1 dado"
                Text(
                    text = textoFase,
                    fontSize = 13.sp,
                    color = ColorTextoClaro.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Zona de dados ──────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DadoAnimado(valor = dado1Visible, lanzando = lanzando, tamano = 110.dp)
                if (estado.etapa == EtapaPartida.RONDA_PARTIDA) {
                    DadoAnimado(valor = dado2Visible, lanzando = lanzando, tamano = 110.dp, delayMs = 80)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Último evento del historial ────────────────────────────────
            if (estado.historialAcciones.isNotEmpty()) {
                val ultimoEvento = estado.historialAcciones.last()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ColorTarjeta),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = ultimoEvento,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        color = ColorTextoClaro.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Historial colapsable ───────────────────────────────────────
            TextButton(
                onClick = { historialExpandido = !historialExpandido },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (historialExpandido) "▲  Ocultar historial" else "▼  Ver historial (${estado.historialAcciones.size})",
                    fontSize = 12.sp,
                    color = ColorTextoClaro.copy(alpha = 0.5f)
                )
            }

            if (historialExpandido) {
                val listState = rememberLazyListState()
                LaunchedEffect(estado.historialAcciones.size) {
                    if (estado.historialAcciones.isNotEmpty()) {
                        listState.animateScrollToItem(estado.historialAcciones.lastIndex)
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorTarjeta),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(8.dp),
                        state = listState,
                        reverseLayout = true
                    ) {
                        items(estado.historialAcciones.reversed()) { accion ->
                            Text(
                                text = accion,
                                fontSize = 12.sp,
                                color = ColorTextoClaro.copy(alpha = 0.7f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Botón principal ────────────────────────────────────────────
            Button(
                onClick = {
                    lanzando = true
                    viewModel.lanzarDados()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorAccent),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                val textoBoton = when (estado.etapa) {
                    EtapaPartida.BUSCANDO_SENOR -> "🎲  TIRAR DADO"
                    EtapaPartida.RONDA_PARTIDA -> "🎲  TIRAR DADOS"
                    else -> "TIRAR"
                }
                Text(
                    text = textoBoton,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }

    // Reset del flag de lanzando tras un breve delay
    LaunchedEffect(lanzando) {
        if (lanzando) {
            delay(500)
            lanzando = false
        }
    }
}

// ─── Dado con puntos y animación ─────────────────────────────────────────────
@Composable
fun DadoAnimado(
    valor: Int,
    lanzando: Boolean,
    tamano: Dp = 100.dp,
    delayMs: Int = 0
) {
    // Animación de rotación durante el lanzamiento
    val rotacion by animateFloatAsState(
        targetValue = if (lanzando) 360f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = delayMs,
            easing = FastOutSlowInEasing
        ),
        label = "rotacion_dado"
    )

    // Animación de escala (rebote)
    val escala by animateFloatAsState(
        targetValue = if (lanzando) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "escala_dado"
    )

    Box(
        modifier = Modifier
            .size(tamano)
            .rotate(rotacion)
            .shadow(
                elevation = if (lanzando) 16.dp else 8.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFAF5EA),
                        Color(0xFFEDE0C4)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        PuntosDado(valor = valor, tamano = tamano)
    }
}

// ─── Layout de puntos para cada cara del dado ─────────────────────────────────
@Composable
fun PuntosDado(valor: Int, tamano: Dp) {
    val paddingDado = tamano * 0.14f
    val tamPunto = tamano * 0.14f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingDado)
    ) {
        when (valor) {
            1 -> {
                // Centro
                Punto(tamPunto, false, Modifier.align(Alignment.Center))
            }
            2 -> {
                Punto(tamPunto, false, Modifier.align(Alignment.TopEnd))
                Punto(tamPunto, false, Modifier.align(Alignment.BottomStart))
            }
            3 -> {
                Punto(tamPunto, false, Modifier.align(Alignment.TopEnd))
                Punto(tamPunto, true,  Modifier.align(Alignment.Center))
                Punto(tamPunto, false, Modifier.align(Alignment.BottomStart))
            }
            4 -> {
                Punto(tamPunto, false, Modifier.align(Alignment.TopStart))
                Punto(tamPunto, false, Modifier.align(Alignment.TopEnd))
                Punto(tamPunto, false, Modifier.align(Alignment.BottomStart))
                Punto(tamPunto, false, Modifier.align(Alignment.BottomEnd))
            }
            5 -> {
                Punto(tamPunto, false, Modifier.align(Alignment.TopStart))
                Punto(tamPunto, false, Modifier.align(Alignment.TopEnd))
                Punto(tamPunto, false, Modifier.align(Alignment.Center))
                Punto(tamPunto, false, Modifier.align(Alignment.BottomStart))
                Punto(tamPunto, false, Modifier.align(Alignment.BottomEnd))
            }
            6 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Punto(tamPunto, false)
                        Punto(tamPunto, false)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Punto(tamPunto, false)
                        Punto(tamPunto, false)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Punto(tamPunto, false)
                        Punto(tamPunto, false)
                    }
                }
            }
        }
    }
}

@Composable
fun Punto(tamano: Dp, esCentral: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(if (esCentral) ColorPunto3 else ColorPunto)
    )
}