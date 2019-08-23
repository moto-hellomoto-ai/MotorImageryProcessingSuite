package ai.hellomoto.mip.openbci

import com.fazecast.jSerialComm.SerialPort
import java.nio.charset.Charset
import java.util.logging.Logger

private const val DOLLAR_SIGN = '$'.toByte()

private fun isValidMessage(buffer:ByteArray, validLength:Long):Boolean {
    if (validLength >= 3) {
        val index = validLength.toInt()
        return (
            buffer[index - 1] == DOLLAR_SIGN &&
            buffer[index - 2] == DOLLAR_SIGN &&
            buffer[index - 3] == DOLLAR_SIGN
        )
    }
    return false
}

private fun toString(buffer:ByteArray, length:Long):String {
    return buffer.copyOfRange(0, length.toInt()).toString(Charset.defaultCharset())
}

interface ISerialWrapper {
    fun sendCommand(cmd:ByteArray, length:Long)

    fun sendCommand(cmd:Command)

    fun readMessage(): String
}

class SerialWrapper(port:String, baudRate:Int) : ISerialWrapper {
    private val serial = SerialPort.getCommPort(port)
    private val buffer = ByteArray(1024)

    init {
        serial.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 3000, 0)
        serial.baudRate = baudRate
        serial.openPort(2000)
    }

    override fun sendCommand(cmd: ByteArray, length: Long) {
        LOG.info(" --->>> ${cmd.toString(Charset.defaultCharset())}")
        serial.writeBytes(cmd, length)
    }

    override fun sendCommand(cmd:Command) {
        LOG.info(" --->>> ${cmd} (${cmd.value.toString(Charset.defaultCharset())})")
        serial.writeBytes(cmd.value, cmd.value.size.toLong())
    }

    override fun readMessage(): String {
        var numRead:Long = 0
        do {
            val capacity = buffer.size.toLong() - numRead
            numRead += serial.readBytes(buffer, capacity, numRead)
            if (isValidMessage(buffer, numRead)) {
                val message = toString(buffer, numRead)
                for (line in message.lines()) {
                    LOG.info(" <<<--- $line")
                }
                return message
            }
        } while (true)
    }

    companion object {
        val LOG:Logger = Logger.getLogger(SerialWrapper::class.java.name)
    }
}
