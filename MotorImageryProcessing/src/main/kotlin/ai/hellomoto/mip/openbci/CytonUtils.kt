package ai.hellomoto.mip.openbci

import ai.hellomoto.mip.settings.board_config.BoardConfigFragment


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

    data class Success(override val message:String): OperationResult()
    data class Fail(override val message:String):OperationResult()
    data class Invalid(override val message:String):OperationResult()
    data class TimeOut(override val message:String="TimeOut occurred while reading a message.") : OperationResult() {
        constructor(command:Command):this("TimeOut occurred while checking result from ${command}")
    }
    data class SuccessDefaultConfig(override val message:String, val config: ChannelConfig): OperationResult()

    companion object {
        fun parseDefaultConfig(message:String): OperationResult {
            if (message.length < 6) {
                return Fail("Invalid message received: ${message}")
            }
            return try {
                val config: ChannelConfig = ChannelConfig.fromInts(
                    message.slice(0..0).toInt(), message.slice(1..1).toInt(),
                    message.slice(2..2).toInt(), message.slice(3..3).toInt(),
                    message.slice(4..4).toInt(), message.slice(5..5).toInt())
                SuccessDefaultConfig(message, config)
            } catch (exception:Exception) {
                Fail("Failed to parse message: \"${message}\". Reason: ${exception}")
            }
        }
    }
}


sealed class ReadPacketResult {
    data class Success(val data:PacketData): ReadPacketResult()
    data class Fail(val message:String): ReadPacketResult()
}