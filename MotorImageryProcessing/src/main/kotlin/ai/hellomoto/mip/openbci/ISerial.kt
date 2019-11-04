package ai.hellomoto.mip.openbci

interface ISerial {
    fun sendCommand(cmd: ByteArray, length: Long)

    fun sendCommand(cmd: Command)

    fun readMessage(timeout: Int =3000): String?

    fun waitByte(value: Byte)

    fun readPacket(): PacketData

    fun close()
}