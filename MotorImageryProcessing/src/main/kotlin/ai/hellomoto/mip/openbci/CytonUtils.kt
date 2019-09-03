package ai.hellomoto.mip.openbci


const val START_BYTE = 0xA0.toByte()
const val STOP_BYTE = 0xC0.toByte()

private const val ADS1299VREF:Float = 4.5F
private val EEG_SCALE:Float = 1000000.0F * ADS1299VREF / (Math.pow(2.0, 23.0).toFloat() - 1)

fun parseEeg(raw:Int, gain:Float=24F):Float {
    return raw * EEG_SCALE / gain
}

fun trimBeforePrefix(message:String, prefix:String):String = when (val index = message.indexOf(prefix)) {
    -1 -> message
    else -> message.substring(index)
}


sealed class OperationResult {
    abstract val message:String

    data class Success(override val message:String): OperationResult() {}
    data class Fail(override val message:String):OperationResult() {}
    data class Invalid(override val message:String):OperationResult() {}
    data class TimeOut(override val message:String="TimeOut occurred while reading a message.") : OperationResult() {
        constructor(command:Command):this("TimeOut occurred while checking result from ${command}") {}
    }
}

sealed class ReadPacketResult {
    data class Success(val data:PacketData): ReadPacketResult()
    data class Fail(val message:String): ReadPacketResult()
}