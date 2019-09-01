package ai.hellomoto.mip.settings.board_config

import ai.hellomoto.mip.openbci.SampleRate
import com.fazecast.jSerialComm.SerialPort

fun getPorts():List<String> {
    return SerialPort.getCommPorts().map{ it.systemPortName }
}

fun getSampleRates():List<String> {
    return SampleRate.values().map { it.toString() }
}

class BoardConfig {

}