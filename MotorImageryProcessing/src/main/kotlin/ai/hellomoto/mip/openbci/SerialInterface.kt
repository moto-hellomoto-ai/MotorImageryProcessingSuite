package ai.hellomoto.mip.openbci

import com.fazecast.jSerialComm.SerialPort
import java.nio.charset.Charset
import java.util.logging.Logger

interface ISerial {
    fun sendCommand(cmd:ByteArray, length:Long)

    fun sendCommand(cmd:Command)

    fun readMessage(timeout:Int=3000): String?

    fun waitByte(value:Byte)

    fun readPacket(): PacketData

    fun close()
}

class Serial(port:String, baudRate:Int) : ISerial {
    private val serial = SerialPort.getCommPort(port)
    private val buffer = ByteArray(1024)

    init {
        serial.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0)
        serial.baudRate = baudRate
        serial.openPort(3000)
    }

    override fun sendCommand(cmd: ByteArray, length: Long) {
        LOG.info(" --->>> ${cmd.toString(Charset.defaultCharset())}")
        serial.writeBytes(cmd, length)
    }

    override fun sendCommand(cmd:Command) {
        LOG.info(" --->>> ${cmd} (${cmd.value.toString(Charset.defaultCharset())})")
        serial.writeBytes(cmd.value, cmd.value.size.toLong())
    }

    override fun readMessage(timeout:Int): String? {
        var numRead:Long = 0
        val start = System.currentTimeMillis()
        do {
            val capacity = buffer.size.toLong() - numRead
            numRead += serial.readBytes(buffer, capacity, numRead)
            when {
                isValidMessage(buffer, numRead) -> {
                    val message = toString(buffer, numRead)
                    for (line in message.lines()) {
                        LOG.info(" <<<--- $line")
                    }
                    return message
                }
                (System.currentTimeMillis() - start > timeout) -> {
                    return null
                }
                else -> {
                    Thread.sleep(4)
                }
            }
        } while (true)
    }

    override fun waitByte(value:Byte) {
        do {
            val numRead = serial.readBytes(buffer, 1)
        } while (numRead != 1 || buffer[0] != value)
    }

    override fun readPacket():PacketData {
        var numRead:Long = 32
        do {
            numRead -= serial.readBytes(buffer, numRead, 32-numRead)
            Thread.sleep(1)
        } while (numRead > 0)
        return parsePacket(buffer)
    }

    override fun close() {
        serial.closePort()
    }

    companion object {
        val LOG:Logger = Logger.getLogger(this::class.qualifiedName)
    }
}
