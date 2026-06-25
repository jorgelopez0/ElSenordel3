package com.example.elsenordel3.ui

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elsenordel3.data.EtapaPartida
import com.example.elsenordel3.data.ModoJuego
import com.example.elsenordel3.viewmodel.JuegoViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

// ─── Paleta rosa / azul claro ────────────────────────────────────────────────
private val ColorAzulClaro = Color(0xFFAEDDF5)   // azul cielo suave (arriba)
private val ColorRosa = Color(0xFFFBC7DE)        // rosa suave (abajo)
private val ColorRosaFuerte = Color(0xFFFF5C93)  // acento principal (turno activo)
private val ColorAzulFuerte = Color(0xFF3FA0E0)  // acento secundario
private val ColorTextoOscuro = Color(0xFF20364F) // texto principal sobre fondo claro
private val ColorTextoSuave = Color(0xFF6A7E96)  // texto secundario
private val ColorTarjeta = Color(0xFFFFFFFF)     // tarjetas blancas
private val ColorPunto = Color(0xFF20364F)       // puntos del dado
private val ColorPunto3 = Color(0xFFFF3D7F)      // punto central del 3

@Composable
fun PantallaJuego(viewModel: JuegoViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(estado.reproducirAudioZZZ) {
        if (estado.reproducirAudioZZZ) {
            reproducirAudioZZZ(context)
            viewModel.audioZZZConsumido()
        }
    }

    when (estado.etapa) {
        EtapaPartida.TUTORIAL -> PantallaTutorial(viewModel)
        EtapaPartida.CONFIGURACION -> PantallaConfiguracion(viewModel)
        else -> PantallaMesa(viewModel)
    }
}

private fun reproducirAudioZZZ(context: Context) {
    try {
        val resId = context.resources.getIdentifier("zzz", "raw", context.packageName)
        if (resId != 0) {
            val mp = MediaPlayer.create(context, resId)
            mp?.setOnCompletionListener { it.release() }
            mp?.start()
        }
    } catch (_: Exception) { }
}

private fun fondoClaro() = Brush.verticalGradient(listOf(ColorAzulClaro, ColorRosa))

// ════════════════════════════════════════════════════════════════════════════
// PANTALLA TUTORIAL — se muestra al abrir la app
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun PantallaTutorial(viewModel: JuegoViewModel) {
    Box(modifier = Modifier.fillMaxSize().background(fondoClaro())) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "INSTRUCCIONES",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = ColorTextoOscuro,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "El Señor del 3",
                fontSize = 16.sp,
                color = ColorRosaFuerte,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PasoTutorial(1, "Toca la pantalla para tirar. No hay botones: el toque lanza los dados.")
                PasoTutorial(2, "Modo Normal: por turnos tiráis 1 dado. Quien saque un 3 se convierte en el Señor del 3.")
                PasoTutorial(3, "Después se juega con 2 dados. Si la suma es 7, 8 o 9, o sale un 3, alguien bebe y sigues tirando. Si no, pasa el móvil.")
                PasoTutorial(4, "Modo Hardcore: cada jugador saca su número con 1 dado. Luego, 2 dados con un máximo de 3 tiradas por turno.")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saltarTutorial() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorRosaFuerte),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("EMPEZAR", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            TextButton(
                onClick = { viewModel.saltarTutorial() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Saltar", fontSize = 15.sp, color = ColorTextoSuave, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PasoTutorial(numero: Int, texto: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorTarjeta),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(ColorAzulFuerte),
                contentAlignment = Alignment.Center
            ) {
                Text("$numero", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = texto, fontSize = 14.sp, color = ColorTextoOscuro)
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// PANTALLA CONFIGURACIÓN
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun PantallaConfiguracion(viewModel: JuegoViewModel) {
    val estado by viewModel.estado.collectAsState()
    var nombreInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(fondoClaro())) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "El Señor del 3",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = ColorTextoOscuro,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Juego de dados",
                fontSize = 14.sp,
                color = ColorRosaFuerte,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorTarjeta),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("MODO DE JUEGO", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = ColorAzulFuerte, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ModoBoton("Normal", estado.modo == ModoJuego.NORMAL, Modifier.weight(1f)) {
                            viewModel.setModoJuego(ModoJuego.NORMAL)
                        }
                        ModoBoton("Hardcore", estado.modo == ModoJuego.HARDCORE, Modifier.weight(1f)) {
                            viewModel.setModoJuego(ModoJuego.HARDCORE)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val descripcion = if (estado.modo == ModoJuego.NORMAL)
                        "Busca al Señor del 3. Sumas 7/8/9 o un 3 mantienen el turno."
                    else
                        "Cada jugador tiene su número. 3 tiradas por turno."
                    Text(descripcion, fontSize = 12.sp, color = ColorTextoSuave)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorTarjeta),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("JUGADORES (${estado.jugadores.size})", fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = ColorAzulFuerte, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nombreInput,
                            onValueChange = { nombreInput = it },
                            label = { Text("Nombre", color = ColorTextoSuave) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ColorTextoOscuro,
                                unfocusedTextColor = ColorTextoOscuro,
                                focusedBorderColor = ColorRosaFuerte,
                                unfocusedBorderColor = ColorTextoSuave.copy(alpha = 0.4f),
                                cursorColor = ColorRosaFuerte
                            )
                        )
                        Button(
                            onClick = { viewModel.agregarJugador(nombreInput); nombreInput = "" },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorRosaFuerte),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    if (estado.jugadores.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        estado.jugadores.forEachIndexed { i, jug ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${i + 1}. ${jug.nombre}", color = ColorTextoOscuro, fontSize = 15.sp)
                                TextButton(onClick = { viewModel.quitarJugador(jug) }) {
                                    Text("Quitar", color = ColorRosaFuerte, fontSize = 13.sp)
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
                    containerColor = ColorRosaFuerte,
                    disabledContainerColor = ColorRosaFuerte.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("¡EMPEZAR!", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            if (estado.jugadores.size < 2) {
                Text("Necesitas al menos 2 jugadores", fontSize = 12.sp,
                    color = ColorTextoSuave, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
fun ModoBoton(texto: String, seleccionado: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (seleccionado) ColorRosaFuerte else ColorAzulClaro
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            texto,
            color = if (seleccionado) Color.White else ColorTextoOscuro,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
// PANTALLA MESA — dos dados gigantes (arriba / abajo), tocar para tirar
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun PantallaMesa(viewModel: JuegoViewModel) {
    val estado by viewModel.estado.collectAsState()
    val jugadorActual = estado.jugadores.getOrNull(estado.jugadorActualIndex)

    var lanzando by remember { mutableStateOf(false) }
    var historialExpandido by remember { mutableStateOf(false) }
    var lanzamientoId by remember { mutableIntStateOf(0) }

    val nivelLocura = estado.vecesHaBebidoSenorDel3

    val onTirar = {
        if (!lanzando) {
            lanzamientoId++
            lanzando = true
            viewModel.lanzarDados()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(fondoClaro())) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Zona de juego: tocar para tirar ────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(lanzando) {
                        detectTapGestures(onTap = { onTirar() })
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (estado.etapa == EtapaPartida.RONDA_PARTIDA) {
                    // Dado ARRIBA
                    Box(modifier = Modifier.weight(0.40f).fillMaxWidth()) {
                        DadoGigante(estado.dado1, lanzamientoId, 0, nivelLocura)
                    }
                    // Zona secundaria (contadores + nombres)
                    ZonaSecundaria(estado, jugadorActual, lanzando, Modifier.weight(0.20f))
                    // Dado ABAJO
                    Box(modifier = Modifier.weight(0.40f).fillMaxWidth()) {
                        DadoGigante(estado.dado2, lanzamientoId, 1, nivelLocura)
                    }
                } else {
                    // Fase de búsqueda: un solo dado, grande y centrado
                    Box(modifier = Modifier.weight(0.55f).fillMaxWidth()) {
                        DadoGigante(estado.dado1, lanzamientoId, 0, nivelLocura)
                    }
                    ZonaSecundaria(estado, jugadorActual, lanzando, Modifier.weight(0.25f))
                    Spacer(modifier = Modifier.weight(0.20f))
                }
            }

            // ── Historial (fuera de la zona de toque) ──────────────────────
            HistorialZona(estado, historialExpandido) { historialExpandido = !historialExpandido }
        }
    }

    // Gate de entrada mientras dura la animación más larga
    LaunchedEffect(lanzamientoId) {
        if (lanzamientoId > 0) {
            delay(1300)
            lanzando = false
        }
    }
}

@Composable
fun ZonaSecundaria(
    estado: com.example.elsenordel3.data.JuegoEstado,
    jugadorActual: com.example.elsenordel3.data.Jugador?,
    lanzando: Boolean,
    modifier: Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Contadores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            ContadorChip(if (estado.modo == ModoJuego.NORMAL) "Modo Normal" else "Modo Hardcore")
            if (estado.etapa == EtapaPartida.RONDA_PARTIDA) {
                val t = if (estado.modo == ModoJuego.HARDCORE)
                    "Tirada ${estado.tiradasEnTurnoActual + 1}/3"
                else
                    "Tirada ${estado.tiradasJugadorActual}"
                ContadorChip(t)
            }
            if (estado.modo == ModoJuego.NORMAL) {
                ContadorChip("Señor del 3 bebió: ${estado.vecesHaBebidoSenorDel3}")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nombres de jugadores (el del turno destacado)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            estado.jugadores.forEach { jug ->
                val esTurno = jug == jugadorActual
                val sufijo = when {
                    estado.modo == ModoJuego.NORMAL && jug.esSenorDel3 -> " 3"
                    estado.modo == ModoJuego.HARDCORE && jug.numerosAsignados.isNotEmpty() ->
                        " ${jug.numerosAsignados.joinToString(",")}"
                    else -> ""
                }
                val escala by animateFloatAsState(
                    targetValue = if (esTurno) 1f else 0.8f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "escala_nombre"
                )
                Text(
                    text = "${jug.nombre}$sufijo",
                    fontSize = if (esTurno) 17.sp else 12.sp,
                    fontWeight = if (esTurno) FontWeight.Black else FontWeight.Normal,
                    color = if (esTurno) ColorRosaFuerte else ColorTextoSuave.copy(alpha = 0.7f),
                    modifier = Modifier.scale(escala)
                )
            }
        }

        if (!lanzando) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Toca para tirar",
                fontSize = 12.sp,
                color = ColorTextoSuave,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HistorialZona(
    estado: com.example.elsenordel3.data.JuegoEstado,
    expandido: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (estado.historialAcciones.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorTarjeta),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = estado.historialAcciones.last(),
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    fontSize = 13.sp,
                    color = ColorTextoOscuro,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        TextButton(onClick = onToggle, modifier = Modifier.height(30.dp)) {
            Text(
                text = if (expandido) "Ocultar historial" else "Ver historial (${estado.historialAcciones.size})",
                fontSize = 11.sp,
                color = ColorTextoSuave,
                fontWeight = FontWeight.Bold
            )
        }

        if (expandido) {
            val listState = rememberLazyListState()
            LaunchedEffect(estado.historialAcciones.size) {
                if (estado.historialAcciones.isNotEmpty()) {
                    listState.animateScrollToItem(estado.historialAcciones.lastIndex)
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = CardDefaults.cardColors(containerColor = ColorTarjeta),
                shape = RoundedCornerShape(10.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(8.dp), state = listState, reverseLayout = true) {
                    items(estado.historialAcciones.reversed()) { accion ->
                        Text(accion, fontSize = 11.sp, color = ColorTextoSuave,
                            modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ContadorChip(texto: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ColorTarjeta)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(texto, fontSize = 11.sp, color = ColorTextoOscuro, fontWeight = FontWeight.Medium)
    }
}

// ════════════════════════════════════════════════════════════════════════════
// SISTEMA DE ANIMACIÓN DE DADOS
// Cuanto mayor es el nivel (veces que ha bebido el Señor del 3), más estilos
// raros se desbloquean y pueden salir mezclados, incluso 2D, gigantes o básicos.
// ════════════════════════════════════════════════════════════════════════════
private enum class EstiloDado {
    NORMAL_3D, GIRO_RAPIDO_3D, SALTO_3D, PLANO_2D, BAMBOLEO_3D,
    GIGANTE, TERREMOTO, LENTO_RARO, BASICA, CAOS
}

private fun estilosPara(nivel: Int): List<EstiloDado> {
    val lista = mutableListOf(EstiloDado.NORMAL_3D, EstiloDado.GIRO_RAPIDO_3D)
    if (nivel >= 2) { lista += EstiloDado.SALTO_3D; lista += EstiloDado.BAMBOLEO_3D }
    if (nivel >= 4) { lista += EstiloDado.PLANO_2D; lista += EstiloDado.GIGANTE; lista += EstiloDado.TERREMOTO }
    if (nivel >= 7) { lista += EstiloDado.LENTO_RARO; lista += EstiloDado.BASICA; lista += EstiloDado.CAOS }
    return lista
}

@Composable
fun DadoGigante(
    valorFinal: Int,
    lanzamientoId: Int,
    variante: Int,        // 0 / 1, para desincronizar ambos dados
    nivelLocura: Int
) {
    var valorMostrado by remember(valorFinal) { mutableIntStateOf(valorFinal) }
    var animandoSlot by remember { mutableStateOf(false) }

    val rotX = remember { Animatable(0f) }
    val rotY = remember { Animatable(0f) }
    val rotZ = remember { Animatable(0f) }
    val escala = remember { Animatable(1f) }
    val escalaX = remember { Animatable(1f) }
    val escalaY = remember { Animatable(1f) }
    val transX = remember { Animatable(0f) }
    val transY = remember { Animatable(0f) }

    val delayInicial = variante * 70L

    LaunchedEffect(lanzamientoId) {
        if (lanzamientoId <= 0) return@LaunchedEffect
        delay(delayInicial)

        // Reset
        rotX.snapTo(0f); rotY.snapTo(0f); rotZ.snapTo(0f)
        escala.snapTo(1f); escalaX.snapTo(1f); escalaY.snapTo(1f)
        transX.snapTo(0f); transY.snapTo(0f)

        val estilo = estilosPara(nivelLocura).random()
        animandoSlot = true

        coroutineScope {
            // Cambio rápido de números (efecto tragaperras) mientras se anima
            val cycle = launch {
                val intervalo = if (estilo == EstiloDado.BASICA) 45L else 55L
                while (isActive) {
                    valorMostrado = Random.nextInt(1, 7)
                    delay(intervalo)
                }
            }

            // Animación según el estilo elegido
            when (estilo) {
                EstiloDado.NORMAL_3D -> {
                    launch { transY.animateTo(-70f, tween(200, easing = LinearOutSlowInEasing)) }
                    launch { rotX.animateTo(rndGiro(720f), tween(560, easing = LinearEasing)) }
                    launch { rotY.animateTo(rndGiro(720f), tween(560, easing = LinearEasing)) }
                    launch { escala.animateTo(1.12f, tween(280)) }
                    delay(560)
                    transY.animateTo(0f, tween(160, easing = FastOutLinearInEasing))
                }
                EstiloDado.GIRO_RAPIDO_3D -> {
                    launch { rotX.animateTo(rndGiro(1280f), tween(620, easing = LinearEasing)) }
                    launch { rotY.animateTo(rndGiro(1280f), tween(620, easing = LinearEasing)) }
                    launch { rotZ.animateTo(rndGiro(360f), tween(620, easing = LinearEasing)) }
                    launch { transY.animateTo(-50f, tween(310)) }
                    delay(620)
                    transY.animateTo(0f, tween(120))
                }
                EstiloDado.SALTO_3D -> {
                    launch { transY.animateTo(-180f, tween(300, easing = LinearOutSlowInEasing)) }
                    launch { rotX.animateTo(rndGiro(900f), tween(700, easing = LinearEasing)) }
                    launch { rotY.animateTo(rndGiro(520f), tween(700, easing = LinearEasing)) }
                    launch { escala.animateTo(1.2f, tween(300)) }
                    delay(360)
                    launch { escala.animateTo(1f, tween(360)) }
                    transY.animateTo(0f, tween(280, easing = FastOutLinearInEasing))
                }
                EstiloDado.PLANO_2D -> {
                    // Sin profundidad 3D: solo gira en el plano, como una ficha
                    launch { rotZ.animateTo(rndGiro(1080f), tween(640, easing = FastOutSlowInEasing)) }
                    launch { escala.animateTo(1.15f, tween(320)) }
                    delay(640)
                    escala.animateTo(1f, tween(120))
                }
                EstiloDado.BAMBOLEO_3D -> {
                    repeat(4) {
                        rotX.animateTo(45f, tween(120))
                        rotY.animateTo(-45f, tween(120))
                        rotX.animateTo(-45f, tween(120))
                        rotY.animateTo(45f, tween(120))
                    }
                    launch { rotX.animateTo(0f, tween(150)) }
                    rotY.animateTo(0f, tween(150))
                }
                EstiloDado.GIGANTE -> {
                    launch { escala.animateTo(1.9f, tween(320, easing = FastOutSlowInEasing)) }
                    launch { rotZ.animateTo(rndGiro(360f), tween(620)) }
                    delay(340)
                    escala.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                EstiloDado.TERREMOTO -> {
                    repeat(16) {
                        launch { transX.snapTo((Random.nextFloat() - 0.5f) * 60f) }
                        launch { transY.snapTo((Random.nextFloat() - 0.5f) * 60f) }
                        rotZ.snapTo((Random.nextFloat() - 0.5f) * 30f)
                        delay(38)
                    }
                    launch { transX.animateTo(0f, tween(120)) }
                    launch { transY.animateTo(0f, tween(120)) }
                    rotZ.animateTo(0f, tween(120))
                }
                EstiloDado.LENTO_RARO -> {
                    // Giro raro y lento sobre un eje inclinado
                    launch { rotX.animateTo(540f, tween(820, easing = LinearEasing)) }
                    launch { rotZ.animateTo(180f, tween(820, easing = LinearEasing)) }
                    launch { escala.animateTo(0.7f, tween(410)) }
                    delay(410)
                    escala.animateTo(1f, tween(410))
                }
                EstiloDado.BASICA -> {
                    // La sosa entre tanta locura: apenas se mueve
                    escala.animateTo(1.08f, tween(120))
                    escala.animateTo(1f, tween(120))
                    delay(60)
                }
                EstiloDado.CAOS -> {
                    launch { rotX.animateTo(rndGiro(1440f), tween(720, easing = LinearEasing)) }
                    launch { rotY.animateTo(rndGiro(1440f), tween(720, easing = LinearEasing)) }
                    launch { rotZ.animateTo(rndGiro(720f), tween(720, easing = LinearEasing)) }
                    launch {
                        repeat(12) {
                            transX.snapTo((Random.nextFloat() - 0.5f) * 50f)
                            transY.snapTo((Random.nextFloat() - 0.5f) * 50f)
                            delay(55)
                        }
                        transX.animateTo(0f, tween(80)); transY.animateTo(0f, tween(80))
                    }
                    launch {
                        escala.animateTo(1.6f, tween(360))
                        escala.animateTo(0.8f, tween(180))
                        escala.animateTo(1f, tween(180))
                    }
                    delay(740)
                }
            }

            cycle.cancel()
        }

        // Aterrizaje: fija el valor final y limpia transform
        valorMostrado = valorFinal
        animandoSlot = false
        rotX.snapTo(0f); rotY.snapTo(0f); rotZ.snapTo(0f)
        transX.snapTo(0f); transY.snapTo(0f); escala.snapTo(1f)

        if (estilo != EstiloDado.BASICA) {
            // Rebote elástico de impacto (squash & stretch)
            escalaX.animateTo(1.18f, tween(70))
            escalaY.animateTo(0.82f, tween(70))
            coroutineScope {
                launch { escalaX.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
                launch { escalaY.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val lado = minOf(maxWidth, maxHeight) * 0.92f

        // Sombra en la mesa que se encoge cuando el dado "sube"
        Box(
            modifier = Modifier
                .size(lado * 0.85f)
                .scale(1f - (-transY.value / 360f).coerceIn(0f, 0.35f))
                .clip(RoundedCornerShape(50))
                .background(Color(0x33000000))
        )

        Box(
            modifier = Modifier
                .size(lado)
                .graphicsLayer {
                    translationX = transX.value
                    translationY = transY.value
                    rotationX = rotX.value
                    rotationY = rotY.value
                    rotationZ = rotZ.value
                    scaleX = escala.value * escalaX.value
                    scaleY = escala.value * escalaY.value
                    cameraDistance = 16f * density
                }
                .shadow(
                    elevation = (10 + (-transY.value / 4)).coerceIn(8f, 40f).dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(
                    if (animandoSlot)
                        Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFEAF6FF)))
                    else
                        Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF3F6FA)))
                ),
            contentAlignment = Alignment.Center
        ) {
            PuntosDadoGigante(valor = valorMostrado)
        }
    }
}

private fun rndGiro(base: Float): Float =
    (base + Random.nextInt(0, 280)) * if (Random.nextBoolean()) 1f else -1f

// ─── Puntos del dado ──────────────────────────────────────────────────────────
@Composable
fun PuntosDadoGigante(valor: Int) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val lado = minOf(maxWidth, maxHeight)
        val padding = lado * 0.16f
        val tamPunto = lado * 0.17f

        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (valor) {
                1 -> PuntoG(tamPunto, false, Modifier.align(Alignment.Center))
                2 -> {
                    PuntoG(tamPunto, false, Modifier.align(Alignment.TopEnd))
                    PuntoG(tamPunto, false, Modifier.align(Alignment.BottomStart))
                }
                3 -> {
                    PuntoG(tamPunto, false, Modifier.align(Alignment.TopEnd))
                    PuntoG(tamPunto, true, Modifier.align(Alignment.Center))
                    PuntoG(tamPunto, false, Modifier.align(Alignment.BottomStart))
                }
                4 -> {
                    PuntoG(tamPunto, false, Modifier.align(Alignment.TopStart))
                    PuntoG(tamPunto, false, Modifier.align(Alignment.TopEnd))
                    PuntoG(tamPunto, false, Modifier.align(Alignment.BottomStart))
                    PuntoG(tamPunto, false, Modifier.align(Alignment.BottomEnd))
                }
                5 -> {
                    PuntoG(tamPunto, false, Modifier.align(Alignment.TopStart))
                    PuntoG(tamPunto, false, Modifier.align(Alignment.TopEnd))
                    PuntoG(tamPunto, false, Modifier.align(Alignment.Center))
                    PuntoG(tamPunto, false, Modifier.align(Alignment.BottomStart))
                    PuntoG(tamPunto, false, Modifier.align(Alignment.BottomEnd))
                }
                6 -> {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            PuntoG(tamPunto, false); PuntoG(tamPunto, false)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            PuntoG(tamPunto, false); PuntoG(tamPunto, false)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            PuntoG(tamPunto, false); PuntoG(tamPunto, false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PuntoG(tamano: Dp, esCentral: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(tamano)
            .shadow(2.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = if (esCentral)
                        listOf(Color(0xFFFF7FAE), ColorPunto3)
                    else
                        listOf(Color(0xFF3C5573), ColorPunto),
                    radius = tamano.value * 1.4f
                )
            )
    )
}