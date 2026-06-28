package com.example.elsenordel3.ui

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// ─── Paleta ───────────────────────────────────────────────────────────────────
private val ColorRosaFuerte = Color(0xFFFF4D8D)
private val ColorRosaOscuro = Color(0xFFD63070)
private val ColorTarjeta   = Color(0x28FFFFFF)   // blanco 16% opacidad — separación sutil
private val ColorTextoOscuro = Color(0xFF20364F) // sigue usándose en texto sobre tarjeta blanca
private val ColorTextoSuave  = Color(0xFF6A7E96)

@Composable
fun PantallaJuego(viewModel: JuegoViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()
    var mostrarSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1000)
        mostrarSplash = false
    }

    if (mostrarSplash) {
        PantallaSplash()
        return
    }

    when (estado.etapa) {
        EtapaPartida.TUTORIAL -> PantallaInstruccionesGenerales(viewModel)
        EtapaPartida.CONFIGURACION -> PantallaConfiguracion(viewModel)
        EtapaPartida.INSTRUCCIONES_MODO -> PantallaInstruccionesModo(viewModel)
        else -> PantallaMesa(viewModel)
    }
}

@Composable
private fun PantallaSplash() {
    Box(
        modifier = Modifier.fillMaxSize().background(ColorRosaFuerte),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "El Señor del 3",
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 1.sp
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
// PANTALLA 1 — INSTRUCCIONES GENERALES (antes de meter nombres)
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun PantallaInstruccionesGenerales(viewModel: JuegoViewModel) {
    Box(modifier = Modifier.fillMaxSize().background(ColorRosaFuerte)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "INSTRUCCIONES",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Text("El Señor del 3", fontSize = 16.sp, color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PasoTutorial(1, "Es un juego de beber por turnos con dados. Vais pasando el móvil entre todos.")
                PasoTutorial(2, "Puedes elegir el modo que quieras de los disponibles (Normal o Hardcore). Cada modo tiene sus propias reglas, que verás antes de empezar.")
                PasoTutorial(3, "Introduce los nombres en el ORDEN en el que vais a tirar. Ese será el orden de los turnos.")
                PasoTutorial(4, "Durante la partida, toca la pantalla para lanzar los dados.")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saltarTutorial() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorRosaOscuro),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("EMPEZAR", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
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
                modifier = Modifier.size(34.dp).clip(CircleShape).background(ColorRosaOscuro),
                contentAlignment = Alignment.Center
            ) {
                Text("$numero", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(texto, fontSize = 14.sp, color = Color.White)
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// PANTALLA 2 — CONFIGURACIÓN (nombres arriba, modos abajo, fondo azul intenso)
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun PantallaConfiguracion(viewModel: JuegoViewModel) {
    val estado by viewModel.estado.collectAsState()
    var nombreInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(ColorRosaFuerte)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("El Señor del 3", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text("Configura la partida", fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))

            Spacer(modifier = Modifier.height(20.dp))

            // ── NOMBRES (arriba) ───────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorTarjeta),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("JUGADORES (${estado.jugadores.size})", fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 2.sp)
                    Text("En orden de tirada", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nombreInput,
                            onValueChange = { nombreInput = it },
                            label = { Text("Nombre", color = Color.White.copy(alpha = 0.7f)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                cursorColor = Color.White
                            )
                        )
                        Button(
                            onClick = { viewModel.agregarJugador(nombreInput); nombreInput = "" },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorRosaOscuro),
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
                                Text("${i + 1}. ${jug.nombre}", color = Color.White, fontSize = 15.sp)
                                TextButton(onClick = { viewModel.quitarJugador(jug) }) {
                                    Text("Quitar", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── MODOS (abajo) ──────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorTarjeta),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("MODO DE JUEGO", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = Color.White, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ModoBoton("Normal", estado.modo == ModoJuego.NORMAL, Modifier.weight(1f)) {
                            viewModel.setModoJuego(ModoJuego.NORMAL)
                        }
                        ModoBoton("Hardcore", estado.modo == ModoJuego.HARDCORE, Modifier.weight(1f)) {
                            viewModel.setModoJuego(ModoJuego.HARDCORE)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.iniciarPartida() },
                enabled = estado.jugadores.size >= 2,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorRosaOscuro,
                    disabledContainerColor = Color.White.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("CONTINUAR", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            if (estado.jugadores.size < 2) {
                Text("Necesitas al menos 2 jugadores", fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ModoBoton(texto: String, seleccionado: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (seleccionado) ColorRosaOscuro else Color.White.copy(alpha = 0.20f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            texto,
            color = Color.White,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
// PANTALLA 3 — INSTRUCCIONES DEL MODO ELEGIDO
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun PantallaInstruccionesModo(viewModel: JuegoViewModel) {
    val estado by viewModel.estado.collectAsState()
    val esNormal = estado.modo == ModoJuego.NORMAL

    Box(modifier = Modifier.fillMaxSize().background(ColorRosaFuerte)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "INSTRUCCIONES",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (esNormal) "Modo Normal" else "Modo Hardcore",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (esNormal) {
                    PasoTutorial(1, "Fase 1: por turnos tiráis 1 dado. Quien saque un 3 se convierte en el Señor del 3 (queda marcado con un 3 junto a su nombre).")
                    PasoTutorial(2, "Cuando hay al menos un Señor del 3, empieza la partida con 2 dados.")
                    PasoTutorial(3, "Con 2 dados, si la suma es 7, 8 o 9, o sale un 3, alguien bebe y sigues tirando. Si no, pasa el móvil.")
                    PasoTutorial(4, "Detalles: con 7 bebe el anterior, con 8 el último, con 9 el siguiente, y un 3 lo bebe el Señor del 3. Los dobles reparten tragos.")
                } else {
                    PasoTutorial(1, "Fase 1: cada jugador tira 1 dado una sola vez. El número que saque será su número para toda la partida.")
                    PasoTutorial(2, "Fase 2: se juega con 2 dados, con un máximo de 3 tiradas por turno.")
                    PasoTutorial(3, "Si en una tirada sale el número de algún jugador, ese jugador bebe y tú conservas el turno.")
                    PasoTutorial(4, "Al completar 3 tiradas, o si no sale ningún número, pasa el móvil al siguiente.")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.empezarPartida() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorRosaOscuro),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("EMPEZAR", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// PANTALLA MESA — fondo rosa fuerte, dados arriba/abajo, tocar para tirar
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun PantallaMesa(viewModel: JuegoViewModel) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    var lanzando by remember { mutableStateOf(false) }
    var bloqueadoPorAudio by remember { mutableStateOf(false) }
    var lanzamientoId by remember { mutableIntStateOf(0) }
    var jugadorMostradoIndex by remember { mutableIntStateOf(estado.jugadorActualIndex) }
    val jugadorActual = estado.jugadores.getOrNull(jugadorMostradoIndex)
    val mpRef = remember { mutableStateOf<MediaPlayer?>(null) }

    val nivelLocura = estado.vecesHaBebidoSenorDel3
    var dado1TerminadoId by remember { mutableIntStateOf(-1) }
    var dado2TerminadoId by remember { mutableIntStateOf(-1) }

    // Audio ZZZ: suena entero, y bloquea el tirar hasta que termina
    LaunchedEffect(estado.reproducirAudioZZZ) {
        if (estado.reproducirAudioZZZ) {
            viewModel.audioZZZConsumido()
            try {
                val resId = context.resources.getIdentifier("zzz", "raw", context.packageName)
                if (resId != 0) {
                    mpRef.value?.release()
                    val mp = MediaPlayer.create(context, resId)
                    if (mp != null) {
                        bloqueadoPorAudio = true
                        mpRef.value = mp
                        mp.setOnCompletionListener {
                            it.release()
                            if (mpRef.value === it) mpRef.value = null
                            bloqueadoPorAudio = false
                        }
                        mp.start()
                    }
                }
            } catch (_: Exception) {
                bloqueadoPorAudio = false
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            mpRef.value?.release()
            mpRef.value = null
        }
    }

    val ocupado = lanzando || bloqueadoPorAudio
    val onTirar = {
        if (!ocupado) {
            jugadorMostradoIndex = estado.jugadorActualIndex
            lanzamientoId++
            lanzando = true
            viewModel.lanzarDados()
        }
    }

    val esDosD = estado.etapa == EtapaPartida.RONDA_PARTIDA
    val dadosTerminadosId = if (
        dado1TerminadoId == lanzamientoId && (!esDosD || dado2TerminadoId == lanzamientoId)
    ) lanzamientoId else -1

    LaunchedEffect(dadosTerminadosId) {
        if (dadosTerminadosId > 0) {
            delay(2000)
            jugadorMostradoIndex = estado.jugadorActualIndex
            lanzando = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(ColorRosaFuerte)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(ocupado) {
                        detectTapGestures(onTap = { onTirar() })
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (estado.etapa == EtapaPartida.RONDA_PARTIDA) {
                    Box(modifier = Modifier.weight(0.42f).fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                        DadoGigante(estado.dado1, lanzamientoId, 0, nivelLocura) { dado1TerminadoId = lanzamientoId }
                    }
                    ZonaSecundaria(estado, jugadorActual, ocupado, Modifier.weight(0.16f))
                    Box(modifier = Modifier.weight(0.42f).fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                        DadoGigante(estado.dado2, lanzamientoId, 1, nivelLocura) { dado2TerminadoId = lanzamientoId }
                    }
                } else {
                    Box(modifier = Modifier.weight(0.60f).fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                        DadoGigante(estado.dado1, lanzamientoId, 0, nivelLocura) { dado1TerminadoId = lanzamientoId }
                    }
                    ZonaSecundaria(estado, jugadorActual, ocupado, Modifier.weight(0.22f))
                    Spacer(modifier = Modifier.weight(0.18f))
                }
            }

            TextButton(
                onClick = { viewModel.terminarPartida() },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text("Terminar partida", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ZonaSecundaria(
    estado: com.example.elsenordel3.data.JuegoEstado,
    jugadorActual: com.example.elsenordel3.data.Jugador?,
    ocupado: Boolean,
    modifier: Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            estado.jugadores.forEach { jug ->
                val esTurno = jug == jugadorActual
                val texto = buildAnnotatedString {
                    append(jug.nombre)
                    when {
                        estado.modo == ModoJuego.NORMAL && jug.esSenorDel3 -> {
                            append(" ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Black)) { append("3") }
                        }
                        estado.modo == ModoJuego.HARDCORE && jug.numerosAsignados.isNotEmpty() ->
                            append(" ${jug.numerosAsignados.joinToString(",")}")
                    }
                }
                val escala by animateFloatAsState(
                    targetValue = if (esTurno) 1f else 0.8f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "escala_nombre"
                )
                Text(
                    text = texto,
                    fontSize = if (esTurno) 18.sp else 12.sp,
                    fontWeight = if (esTurno) FontWeight.Black else FontWeight.Normal,
                    color = if (esTurno) Color.White else Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.scale(escala)
                )
            }
        }

        if (!ocupado) {
            Spacer(modifier = Modifier.height(6.dp))
            Text("Toca para tirar", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
        }
    }
}


// ════════════════════════════════════════════════════════════════════════════
// SISTEMA DE ANIMACIÓN — cuanto más alto el contador del Señor del 3,
// más estilos raros se desbloquean. Todos renderizan un CUBO 3D real.
// ════════════════════════════════════════════════════════════════════════════
private enum class EstiloDado {
    NORMAL_3D, GIRO_RAPIDO_3D, SALTO_3D, GIRO_PLANO, BAMBOLEO,
    GIGANTE, TERREMOTO, LENTO_RARO, BASICA, CAOS,
    MONEDA, ESPIRAL, REBOTE_DOBLE, TORBELLINO
}

private fun estilosPara(nivel: Int): List<EstiloDado> {
    val lista = mutableListOf(EstiloDado.NORMAL_3D, EstiloDado.GIRO_RAPIDO_3D, EstiloDado.MONEDA)
    if (nivel >= 2) { lista += EstiloDado.SALTO_3D; lista += EstiloDado.BAMBOLEO; lista += EstiloDado.ESPIRAL; lista += EstiloDado.REBOTE_DOBLE }
    if (nivel >= 4) { lista += EstiloDado.GIRO_PLANO; lista += EstiloDado.GIGANTE; lista += EstiloDado.TERREMOTO; lista += EstiloDado.TORBELLINO }
    if (nivel >= 7) { lista += EstiloDado.LENTO_RARO; lista += EstiloDado.BASICA; lista += EstiloDado.CAOS }
    return lista
}

private fun rndGiro(base: Float): Float =
    (base + Random.nextInt(0, 200)) * if (Random.nextBoolean()) 1f else -1f

@Composable
fun DadoGigante(
    valorFinal: Int,
    lanzamientoId: Int,
    variante: Int,
    nivelLocura: Int,
    onAnimacionTerminada: () -> Unit = {}
) {
    var valorMostrado by remember(valorFinal) { mutableIntStateOf(valorFinal) }

    // Ángulos del cubo (grados) y transformaciones de pantalla
    val ax = remember { Animatable(0f) }   // giro eje X (vuelco)
    val ay = remember { Animatable(0f) }   // giro eje Y (rodar)
    val az = remember { Animatable(0f) }   // giro en el plano
    val escala = remember { Animatable(1f) }
    val escalaX = remember { Animatable(1f) }
    val escalaY = remember { Animatable(1f) }
    val transX = remember { Animatable(0f) }
    val transY = remember { Animatable(0f) }
    val pulsoTres = remember { Animatable(0f) }  // 0..1, brilla cuando el dado muestra 3

    val delayInicial = variante * 70L

    LaunchedEffect(lanzamientoId) {
        if (lanzamientoId <= 0) return@LaunchedEffect
        pulsoTres.snapTo(0f)
        delay(delayInicial)

        ax.snapTo(0f); ay.snapTo(0f); az.snapTo(0f)
        escala.snapTo(1f); escalaX.snapTo(1f); escalaY.snapTo(1f)
        transX.snapTo(0f); transY.snapTo(0f)

        val estilo = estilosPara(nivelLocura).random()

        coroutineScope {
            val cycle = launch {
                while (isActive) {
                    valorMostrado = Random.nextInt(1, 7)
                    delay(70)
                }
            }

            when (estilo) {
                EstiloDado.NORMAL_3D -> {
                    launch { transY.animateTo(-70f, tween(220, easing = LinearOutSlowInEasing)) }
                    launch { ax.animateTo(rndGiro(630f), tween(640, easing = FastOutSlowInEasing)) }
                    launch { ay.animateTo(rndGiro(540f), tween(640, easing = FastOutSlowInEasing)) }
                    delay(440)
                    transY.animateTo(0f, tween(200, easing = FastOutLinearInEasing))
                }
                EstiloDado.GIRO_RAPIDO_3D -> {
                    launch { ax.animateTo(rndGiro(1080f), tween(700, easing = FastOutSlowInEasing)) }
                    launch { ay.animateTo(rndGiro(1080f), tween(700, easing = FastOutSlowInEasing)) }
                    launch { transY.animateTo(-50f, tween(350)) }
                    delay(700)
                    transY.animateTo(0f, tween(120))
                }
                EstiloDado.SALTO_3D -> {
                    launch { transY.animateTo(-180f, tween(300, easing = LinearOutSlowInEasing)) }
                    launch { ax.animateTo(rndGiro(900f), tween(760, easing = FastOutSlowInEasing)) }
                    launch { ay.animateTo(rndGiro(540f), tween(760, easing = FastOutSlowInEasing)) }
                    launch { escala.animateTo(1.18f, tween(300)) }
                    delay(380)
                    launch { escala.animateTo(1f, tween(360)) }
                    transY.animateTo(0f, tween(300, easing = FastOutLinearInEasing))
                }
                EstiloDado.GIRO_PLANO -> {
                    // Gira sobre todo en el plano (como una peonza), pero sigue siendo un cubo
                    launch { az.animateTo(rndGiro(900f), tween(680, easing = FastOutSlowInEasing)) }
                    launch { ay.animateTo(rndGiro(360f), tween(680, easing = FastOutSlowInEasing)) }
                    delay(680)
                }
                EstiloDado.BAMBOLEO -> {
                    repeat(4) {
                        ax.animateTo(50f, tween(120)); ay.animateTo(-50f, tween(120))
                        ax.animateTo(-50f, tween(120)); ay.animateTo(50f, tween(120))
                    }
                    launch { ax.animateTo(0f, tween(150)) }
                    ay.animateTo(0f, tween(150))
                }
                EstiloDado.GIGANTE -> {
                    launch { escala.animateTo(1.9f, tween(320, easing = FastOutSlowInEasing)) }
                    launch { ax.animateTo(rndGiro(540f), tween(660)) }
                    launch { ay.animateTo(rndGiro(360f), tween(660)) }
                    delay(340)
                    escala.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
                EstiloDado.TERREMOTO -> {
                    repeat(16) {
                        launch { transX.snapTo((Random.nextFloat() - 0.5f) * 60f) }
                        launch { transY.snapTo((Random.nextFloat() - 0.5f) * 60f) }
                        ax.snapTo((Random.nextFloat() - 0.5f) * 80f)
                        ay.snapTo((Random.nextFloat() - 0.5f) * 80f)
                        delay(40)
                    }
                    launch { transX.animateTo(0f, tween(120)) }
                    launch { transY.animateTo(0f, tween(120)) }
                    launch { ax.animateTo(0f, tween(120)) }
                    ay.animateTo(0f, tween(120))
                }
                EstiloDado.LENTO_RARO -> {
                    launch { ax.animateTo(rndGiro(380f), tween(860, easing = LinearEasing)) }
                    launch { az.animateTo(180f, tween(860, easing = LinearEasing)) }
                    launch { escala.animateTo(0.72f, tween(430)) }
                    delay(430)
                    escala.animateTo(1f, tween(430))
                }
                EstiloDado.BASICA -> {
                    launch { ay.animateTo(rndGiro(180f), tween(260)) }
                    escala.animateTo(1.08f, tween(130))
                    escala.animateTo(1f, tween(130))
                }
                EstiloDado.CAOS -> {
                    launch { ax.animateTo(rndGiro(1260f), tween(760, easing = FastOutSlowInEasing)) }
                    launch { ay.animateTo(rndGiro(1260f), tween(760, easing = FastOutSlowInEasing)) }
                    launch { az.animateTo(rndGiro(540f), tween(760, easing = FastOutSlowInEasing)) }
                    launch {
                        repeat(12) {
                            transX.snapTo((Random.nextFloat() - 0.5f) * 50f)
                            transY.snapTo((Random.nextFloat() - 0.5f) * 50f)
                            delay(58)
                        }
                        transX.animateTo(0f, tween(80)); transY.animateTo(0f, tween(80))
                    }
                    launch {
                        escala.animateTo(1.6f, tween(360))
                        escala.animateTo(0.85f, tween(190))
                        escala.animateTo(1f, tween(190))
                    }
                    delay(760)
                }
                EstiloDado.MONEDA -> {
                    // Lanzamiento alto girando como una moneda (eje X)
                    launch { transY.animateTo(-220f, tween(380, easing = LinearOutSlowInEasing)) }
                    launch { ax.animateTo(rndGiro(1800f), tween(760, easing = FastOutSlowInEasing)) }
                    launch { escala.animateTo(1.28f, tween(380)) }
                    delay(380)
                    launch { escala.animateTo(1f, tween(380)) }
                    transY.animateTo(0f, tween(380, easing = FastOutLinearInEasing))
                }
                EstiloDado.ESPIRAL -> {
                    // Sube girando en Y mientras oscila en X como un espiral
                    launch { transY.animateTo(-170f, tween(340, easing = LinearOutSlowInEasing)) }
                    launch { ay.animateTo(rndGiro(1440f), tween(700, easing = FastOutSlowInEasing)) }
                    launch { az.animateTo(rndGiro(270f), tween(700, easing = FastOutSlowInEasing)) }
                    launch {
                        delay(80)
                        repeat(5) { i ->
                            val sign = if (i % 2 == 0) 1f else -1f
                            transX.animateTo(sign * 45f * (1f - i / 5f), tween(110))
                        }
                        transX.animateTo(0f, tween(110))
                    }
                    delay(340)
                    transY.animateTo(0f, tween(360, easing = FastOutLinearInEasing))
                }
                EstiloDado.REBOTE_DOBLE -> {
                    // Primer bote alto
                    launch { transY.animateTo(-160f, tween(260, easing = LinearOutSlowInEasing)) }
                    launch { ax.animateTo(rndGiro(720f), tween(520, easing = FastOutSlowInEasing)) }
                    launch { ay.animateTo(rndGiro(540f), tween(520, easing = FastOutSlowInEasing)) }
                    delay(260)
                    transY.animateTo(0f, tween(190, easing = FastOutLinearInEasing))
                    delay(20)
                    // Segundo bote más pequeño
                    launch { transY.animateTo(-75f, tween(170, easing = LinearOutSlowInEasing)) }
                    launch { ax.animateTo(rndGiro(900f), tween(350, easing = FastOutSlowInEasing)) }
                    launch { ay.animateTo(rndGiro(720f), tween(350, easing = FastOutSlowInEasing)) }
                    delay(170)
                    transY.animateTo(0f, tween(180, easing = FastOutLinearInEasing))
                }
                EstiloDado.TORBELLINO -> {
                    // Giro rapidísimo en Y que frena de golpe
                    launch { ay.animateTo(rndGiro(3600f), tween(900, easing = FastOutSlowInEasing)) }
                    launch { transY.animateTo(-70f, tween(300)) }
                    launch { escala.animateTo(0.78f, tween(450)) }
                    delay(300)
                    launch { transY.animateTo(0f, tween(300)) }
                    delay(200)
                    escala.animateTo(1.12f, tween(220, easing = FastOutSlowInEasing))
                    escala.animateTo(1f, tween(200))
                }
            }

            cycle.cancel()
        }

        // Aterrizaje: cara final mirando al frente, sin rotación
        valorMostrado = valorFinal
        ax.snapTo(0f); ay.snapTo(0f); az.snapTo(0f)
        transX.snapTo(0f); transY.snapTo(0f); escala.snapTo(1f)

        if (estilo != EstiloDado.BASICA) {
            escalaX.animateTo(1.16f, tween(70))
            escalaY.animateTo(0.84f, tween(70))
            coroutineScope {
                launch { escalaX.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
                launch { escalaY.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
            }
        }

        onAnimacionTerminada()

        // Pulso rojo continuo cuando el dado muestra un 3
        if (valorFinal == 3) {
            pulsoTres.animateTo(1f, tween(350))
            while (isActive) {
                pulsoTres.animateTo(0.25f, tween(650, easing = FastOutSlowInEasing))
                pulsoTres.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val lado = minOf(maxWidth, maxHeight) * 0.96f

        // Aura roja cuando el dado muestra un 3
        if (pulsoTres.value > 0f) {
            Box(
                modifier = Modifier
                    .size(lado * 1.05f)
                    .clip(RoundedCornerShape(50))
                    .background(Color(1f, 0.15f, 0.25f, 0.52f * pulsoTres.value))
            )
        }

        // Sombra en la mesa
        Box(
            modifier = Modifier
                .size(lado * 0.78f)
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
                    scaleX = escala.value * escalaX.value
                    scaleY = escala.value * escalaY.value
                    clip = true
                    shape = RoundedCornerShape(18.dp)
                }
        ) {
            DadoCubo3D(
                valor = valorMostrado,
                axDeg = ax.value,
                ayDeg = ay.value,
                azDeg = az.value,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ─── Construye un cuadrilátero con las esquinas redondeadas ───────────────────
private fun puntoHacia(desde: Offset, hacia: Offset, dist: Float): Offset {
    val dx = hacia.x - desde.x
    val dy = hacia.y - desde.y
    val len = sqrt(dx * dx + dy * dy)
    if (len < 0.0001f) return desde
    val t = (dist / len).coerceAtMost(0.5f)
    return Offset(desde.x + dx * t, desde.y + dy * t)
}

private fun caraRedondeada(pts: Array<Offset>): Path {
    val n = pts.size
    var minBorde = Float.MAX_VALUE
    for (i in 0 until n) {
        val a = pts[i]; val b = pts[(i + 1) % n]
        val len = sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y))
        if (len < minBorde) minBorde = len
    }
    val r = minBorde * 0.18f
    val path = Path()
    for (i in 0 until n) {
        val cur = pts[i]
        val prev = pts[(i - 1 + n) % n]
        val next = pts[(i + 1) % n]
        val pIn = puntoHacia(cur, prev, r)
        val pOut = puntoHacia(cur, next, r)
        if (i == 0) path.moveTo(pIn.x, pIn.y) else path.lineTo(pIn.x, pIn.y)
        path.quadraticBezierTo(cur.x, cur.y, pOut.x, pOut.y)
    }
    path.close()
    return path
}

// ─── Geometría del cubo (8 caras como 4 esquinas TL,TR,BR,BL en coords +/-1) ────
private val CARAS: Array<Array<FloatArray>> = arrayOf(
    // 0 front (+Z)
    arrayOf(floatArrayOf(-1f, 1f, 1f), floatArrayOf(1f, 1f, 1f), floatArrayOf(1f, -1f, 1f), floatArrayOf(-1f, -1f, 1f)),
    // 1 back (-Z)
    arrayOf(floatArrayOf(1f, 1f, -1f), floatArrayOf(-1f, 1f, -1f), floatArrayOf(-1f, -1f, -1f), floatArrayOf(1f, -1f, -1f)),
    // 2 right (+X)
    arrayOf(floatArrayOf(1f, 1f, 1f), floatArrayOf(1f, 1f, -1f), floatArrayOf(1f, -1f, -1f), floatArrayOf(1f, -1f, 1f)),
    // 3 left (-X)
    arrayOf(floatArrayOf(-1f, 1f, -1f), floatArrayOf(-1f, 1f, 1f), floatArrayOf(-1f, -1f, 1f), floatArrayOf(-1f, -1f, -1f)),
    // 4 top (+Y)
    arrayOf(floatArrayOf(-1f, 1f, -1f), floatArrayOf(1f, 1f, -1f), floatArrayOf(1f, 1f, 1f), floatArrayOf(-1f, 1f, 1f)),
    // 5 bottom (-Y)
    arrayOf(floatArrayOf(-1f, -1f, 1f), floatArrayOf(1f, -1f, 1f), floatArrayOf(1f, -1f, -1f), floatArrayOf(-1f, -1f, -1f))
)

private fun layoutPuntos(valor: Int): Array<FloatArray> = when (valor) {
    1 -> arrayOf(floatArrayOf(0.5f, 0.5f))
    2 -> arrayOf(floatArrayOf(0.27f, 0.27f), floatArrayOf(0.73f, 0.73f))
    3 -> arrayOf(floatArrayOf(0.27f, 0.27f), floatArrayOf(0.5f, 0.5f), floatArrayOf(0.73f, 0.73f))
    4 -> arrayOf(floatArrayOf(0.27f, 0.27f), floatArrayOf(0.73f, 0.27f), floatArrayOf(0.27f, 0.73f), floatArrayOf(0.73f, 0.73f))
    5 -> arrayOf(floatArrayOf(0.27f, 0.27f), floatArrayOf(0.73f, 0.27f), floatArrayOf(0.5f, 0.5f), floatArrayOf(0.27f, 0.73f), floatArrayOf(0.73f, 0.73f))
    6 -> arrayOf(floatArrayOf(0.28f, 0.25f), floatArrayOf(0.28f, 0.5f), floatArrayOf(0.28f, 0.75f), floatArrayOf(0.72f, 0.25f), floatArrayOf(0.72f, 0.5f), floatArrayOf(0.72f, 0.75f))
    else -> arrayOf()
}

private val ColorPuntoNegro = Color(0xFF161616)

@Composable
fun DadoCubo3D(
    valor: Int,
    axDeg: Float,
    ayDeg: Float,
    azDeg: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val minDim = size.minDimension
        val half = minDim / 2f * 0.70f   // ~10% más pequeño que antes
        val cx = size.width / 2f
        val cy = size.height / 2f
        val d = half * 4.6f   // distancia de cámara (perspectiva)

        val rx = Math.toRadians(axDeg.toDouble())
        val ry = Math.toRadians(ayDeg.toDouble())
        val rz = Math.toRadians(azDeg.toDouble())
        val cxa = cos(rx).toFloat(); val sxa = sin(rx).toFloat()
        val cya = cos(ry).toFloat(); val sya = sin(ry).toFloat()
        val cza = cos(rz).toFloat(); val sza = sin(rz).toFloat()

        // Rota un punto local (+-1) y devuelve (x,y,z) escalado por half
        fun rota(p: FloatArray): FloatArray {
            var x = p[0]; var y = p[1]; var z = p[2]
            // Rx
            val y1 = y * cxa - z * sxa
            val z1 = y * sxa + z * cxa
            // Ry
            val x2 = x * cya + z1 * sya
            val z2 = -x * sya + z1 * cya
            val y2 = y1
            // Rz
            val x3 = x2 * cza - y2 * sza
            val y3 = x2 * sza + y2 * cza
            return floatArrayOf(x3 * half, y3 * half, z2 * half)
        }

        // Proyecta (x,y,z) ya escalado -> Offset en pantalla + factor de perspectiva
        fun proyecta(v: FloatArray): Pair<Offset, Float> {
            val factor = d / (d - v[2])
            return Offset(cx + v[0] * factor, cy - v[1] * factor) to factor
        }

        // Valores por cara: frente = valor; opuesta = 7-valor; resto repartidos
        val frente = valor
        val detras = 7 - valor
        val resto = (1..6).filter { it != frente && it != detras }
        val valoresCara = intArrayOf(frente, detras, resto[0], resto[1], resto[2], resto[3])

        // Calcula geometría de cada cara
        val centrosZ = FloatArray(6)
        val esquinasProyectadas = Array(6) { arrayOfNulls<Offset>(4) }
        val rotadas = Array(6) { Array(4) { FloatArray(3) } }

        for (i in 0 until 6) {
            var sumZ = 0f
            for (j in 0 until 4) {
                val r = rota(CARAS[i][j])
                rotadas[i][j] = r
                esquinasProyectadas[i][j] = proyecta(r).first
                sumZ += r[2]
            }
            centrosZ[i] = sumZ / 4f
        }

        // Painter's algorithm: dibuja de la cara más lejana a la más cercana
        val orden = (0 until 6).sortedBy { centrosZ[it] }

        for (i in orden) {
            val p0 = esquinasProyectadas[i][0]!!
            val p1 = esquinasProyectadas[i][1]!!
            val p2 = esquinasProyectadas[i][2]!!
            val p3 = esquinasProyectadas[i][3]!!

            // Sombreado por profundidad (las caras de delante, más claras)
            val zNorm = ((centrosZ[i] / half) + 1.7f) / 3.4f
            val brillo = (0.80f + 0.20f * zNorm).coerceIn(0.6f, 1f)
            val colorCara = Color(brillo, brillo, brillo, 1f)

            val cara = caraRedondeada(arrayOf(p0, p1, p2, p3))
            drawPath(cara, color = colorCara)

            // Puntos negros de esta cara, mapeados con UV bilineal
            val tl = CARAS[i][0]; val tr = CARAS[i][1]; val bl = CARAS[i][3]
            var factorMedio = 0f
            for (j in 0 until 4) factorMedio += proyecta(rotadas[i][j]).second
            factorMedio /= 4f
            val radio = half * 0.135f * factorMedio

            for (uv in layoutPuntos(valoresCara[i])) {
                val u = uv[0]; val v = uv[1]
                val px = tl[0] + (tr[0] - tl[0]) * u + (bl[0] - tl[0]) * v
                val py = tl[1] + (tr[1] - tl[1]) * u + (bl[1] - tl[1]) * v
                val pz = tl[2] + (tr[2] - tl[2]) * u + (bl[2] - tl[2]) * v
                val r = rota(floatArrayOf(px, py, pz))
                val (off, _) = proyecta(r)
                drawCircle(color = ColorPuntoNegro, radius = radio, center = off)
            }
        }
    }
}