package ai.hellomoto.mip.openbci

import java.nio.charset.Charset

private const val DOLLAR_SIGN = '$'.toByte()

////////////////////////////////////////////////////////////////////////////////
// Helper functions for parsing message related to command
fun isValidMessage(buffer:ByteArray, validLength:Long):Boolean {
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

fun toString(buffer:ByteArray, length:Long):String {
    return buffer.copyOfRange(0, length.toInt()).toString(Charset.defaultCharset())
}


////////////////////////////////////////////////////////////////////////////////
// Helper function for parsing data stream
fun interpret24bitAsInt32(ba:ByteArray, offset:Int): Int {
    val int = (
        (0xFF and ba[offset+0].toInt() shl 16) or
        (0xFF and ba[offset+1].toInt() shl 8) or
        (0xFF and ba[offset+2].toInt())
    )
    return if (int and 0x00800000 > 0) {
        int or -0x1000000
    } else {
        int and 0x00FFFFFF
    }
}

fun interpret16bitAsInt32(ba:ByteArray, offset:Int): Int {
    val int = (
        (0xFF and ba[offset+0].toInt() shl 8) or
        (0xFF and ba[offset+1].toInt())
    )
    return if (int and 0x00008000 > 0) {
        int or -0x10000
    } else {
        int and 0x0000FFFF
    }
}

fun parsePacket(buffer:ByteArray):PacketData {
    val packetId = 0xFF and buffer[0].toInt()
    val eegs = (0 until 8).map{interpret24bitAsInt32(buffer, 1 + 3 * it)}
    val auxs = (0 until 3).map{interpret16bitAsInt32(buffer, 25 + 2 * it)}
    val stopByte = buffer[31]
    return PacketData(packetId, stopByte, eegs, auxs)
}
