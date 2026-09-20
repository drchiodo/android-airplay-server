package io.github.jqssun.airplay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jqssun.airplay.R
import io.github.jqssun.airplay.ui.theme.TvAccent
import io.github.jqssun.airplay.ui.theme.TvGround
import io.github.jqssun.airplay.ui.theme.TvLine
import io.github.jqssun.airplay.ui.theme.TvLive
import io.github.jqssun.airplay.ui.theme.TvRaised
import io.github.jqssun.airplay.ui.theme.TvSurface
import io.github.jqssun.airplay.ui.theme.TvText
import io.github.jqssun.airplay.ui.theme.TvTextDim
import io.github.jqssun.airplay.viewmodel.MainViewModel
import io.github.jqssun.airplay.service.AirPlayService.ServerState
import androidx.compose.ui.res.stringResource
import java.net.Inet4Address
import java.net.NetworkInterface

// Amazon asks for content to stay out of the outer 5% of the screen.
// On a 1920x1080 panel that is 96px by 54px, and a Fire TV is xhdpi.
private val SafeH = 48.dp
private val SafeV = 28.dp

/**
 * Focus on a television has to be visible from three metres, so it inverts
 * the fill instead of tinting a border. Kept separate from [dpadFocus],
 * which the phone layouts use.
 */
fun Modifier.tvFocusFill(
    shape: Shape = RoundedCornerShape(10.dp),
    onFocus: (Boolean) -> Unit = {},
): Modifier = composed {
    var focused by remember { mutableStateOf(false) }
    onFocusChanged { focused = it.isFocused; onFocus(focused) }
        .thenIf(focused) { border(2.dp, TvAccent, shape) }
}

/** First non-loopback IPv4 on an interface that is up, or null. */
private fun localIpv4(): String? = runCatching {
    NetworkInterface.getNetworkInterfaces().asSequence()
        .filter { it.isUp && !it.isLoopback }
        .flatMap { it.inetAddresses.asSequence() }
        .filterIsInstance<Inet4Address>()
        .firstOrNull { !it.isLoopbackAddress }
        ?.hostAddress
}.getOrNull()

@Composable
fun TvOverviewContent(
    viewModel: MainViewModel,
    video: @Composable () -> Unit,
    startFocus: FocusRequester,
) {
    val state by viewModel.serverState.collectAsState()
    val connections by viewModel.connectionCount.collectAsState()
    val serverName by viewModel.serverName.collectAsState()
    val serverPort by viewModel.serverPort.collectAsState()
    val idlePreview by viewModel.idlePreview.collectAsState()
    val mirroringActive by viewModel.mirroringActive.collectAsState()

    val running = state == ServerState.RUNNING
    val showVideo = running && (mirroringActive || idlePreview)
    // The address can change under us on a Wi-Fi reconnect, so recompute it
    // whenever the server is restarted rather than once at composition.
    val ip = remember(state, serverPort) { localIpv4() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .thenIf(!showVideo) { background(TvGround) }
    ) {
        if (showVideo) video()

        if (!mirroringActive) {
            IdleLayout(
                serverName = serverName,
                serverPort = serverPort,
                ip = ip,
                state = state,
                connections = connections,
                onToggle = { if (running) viewModel.stopServer() else viewModel.startServer() },
                startFocus = startFocus,
            )
        }
    }
}

@Composable
private fun IdleLayout(
    serverName: String,
    serverPort: Int,
    ip: String?,
    state: ServerState,
    connections: Int,
    onToggle: () -> Unit,
    startFocus: FocusRequester,
) {
    val running = state == ServerState.RUNNING
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SafeH, vertical = SafeV)
    ) {
        // header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = serverName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TvText,
            )
            Spacer(Modifier.width(12.dp))
            StatusPill(state = state, connections = connections)
            Spacer(Modifier.weight(1f))
            Text(
                text = listOfNotNull(ip, "port $serverPort").joinToString("  ·  "),
                fontSize = 14.sp,
                color = TvTextDim,
            )
        }

        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 48.dp)) {
                Text(
                    text = stringResource(
                        if (running) R.string.tv_ready_to_mirror else R.string.tv_server_off
                    ).uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.4.sp,
                    color = if (running) TvAccent else TvTextDim,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = serverName,
                    fontSize = 48.sp,
                    lineHeight = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TvText,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.tv_pick_this_name),
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    color = TvTextDim,
                    modifier = Modifier.widthIn(max = 410.dp),
                )
            }

            Column(
                modifier = Modifier.width(310.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Step(1, stringResource(R.string.tv_step_control_center))
                Step(2, stringResource(R.string.tv_step_screen_mirroring))
                Step(3, stringResource(R.string.tv_step_choose, serverName))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onToggle,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TvText,
                    contentColor = TvGround,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 28.dp, vertical = 14.dp
                ),
                modifier = Modifier.focusRequester(startFocus).tvFocusFill(),
            ) {
                Text(
                    text = stringResource(if (running) R.string.btn_stop else R.string.btn_start),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = if (connections > 0)
                    stringResource(R.string.connected_count, connections)
                else stringResource(R.string.tv_no_device),
                fontSize = 14.sp,
                color = TvTextDim,
            )
        }
    }
}

@Composable
private fun StatusPill(state: ServerState, connections: Int) {
    val (label, tint) = when {
        state == ServerState.ERROR -> stringResource(R.string.error_label) to MaterialTheme.colorScheme.error
        state == ServerState.STOPPED -> stringResource(R.string.stopped_label) to TvTextDim
        connections > 0 -> stringResource(R.string.connected_count, connections) to TvLive
        else -> stringResource(R.string.tv_listening) to TvLive
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(TvSurface, CircleShape)
            .border(1.dp, TvLine, CircleShape)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Box(Modifier.size(7.dp).background(tint, CircleShape))
        Spacer(Modifier.width(7.dp))
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = tint)
    }
}

@Composable
private fun Step(n: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(TvSurface, RoundedCornerShape(10.dp))
            .border(1.dp, TvLine, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Box(
            modifier = Modifier.size(28.dp).background(TvRaised, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = n.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TvAccent,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(text = text, fontSize = 15.sp, lineHeight = 20.sp, color = TvText)
    }
}
