package ai.hellomoto.mip.openbci

const val START_BYTE = 0xA0.toByte()
const val STOP_BYTE = 0xC0.toByte()

private const val ADS1299VREF:Float = 4.5F
private val EEG_SCALE:Float = 1000000.0F * ADS1299VREF / (Math.pow(2.0, 23.0).toFloat() - 1)

internal fun parseEeg(raw:Int, gain:Float=24F):Float {
    return raw * EEG_SCALE / gain
}

internal fun trimBeforePrefix(message:String, prefix:String):String = when (val index = message.indexOf(prefix)) {
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
}


sealed class ReadPacketResult {
    data class Success(val data:PacketData): ReadPacketResult()
    data class Fail(val message:String): ReadPacketResult()
}

internal fun findPattern(pattern:Regex, message:String?):String? {
    return if (message == null) null else pattern.find(message)?.groups?.get(1)?.value
}

internal fun String?.toSampleRate():SampleRate? {
    val pattern = """Sample rate is (\d+)Hz""".toRegex()
    return when (findPattern(pattern, this)) {
        "250" -> SampleRate.SR_250
        "500" -> SampleRate.SR_500
        "1000" -> SampleRate.SR_1000
        "2000" -> SampleRate.SR_2000
        "4000" -> SampleRate.SR_4000
        "8000" -> SampleRate.SR_8000
        "16000" -> SampleRate.SR_16000
        else -> null
    }
}

internal fun String?.toBoardMode(): BoardMode? {
    val pattern = """(default|debug|analog|digital|marker)""".toRegex()
    return when (findPattern(pattern, this)) {
        "default" -> BoardMode.DEFAULT
        "debug" -> BoardMode.DEBUG
        "analog" -> BoardMode.ANALOG
        "digital" -> BoardMode.DIGITAL
        "marker" -> BoardMode.MARKER
        else -> null
    }
}

internal fun String?.toDefaultConfig(): OperationResult {
    if (this == null) return OperationResult.Fail("Failed to fetch default setting.")
    if (this.length < 6) return OperationResult.Fail("Invalid message received: ${this}")
    return try {
        val config: ChannelConfig = ChannelConfig.fromInts(
            this.slice(0..0).toInt(), this.slice(1..1).toInt(),
            this.slice(2..2).toInt(), this.slice(3..3).toInt(),
            this.slice(4..4).toInt(), this.slice(5..5).toInt())
        OperationResult.SuccessDefaultConfig(this, config)
    } catch (exception:Exception) {
        OperationResult.Fail("Failed to parse message: \"${this}\". Reason: ${exception}")
    }
}

internal fun ChannelConfig.Channel.toDisableCommand(): Command {
    return when(this) {
        ChannelConfig.Channel.CHANNEL1  -> Command.DISABLE_CHANNEL1
        ChannelConfig.Channel.CHANNEL2  -> Command.DISABLE_CHANNEL2
        ChannelConfig.Channel.CHANNEL3  -> Command.DISABLE_CHANNEL3
        ChannelConfig.Channel.CHANNEL4  -> Command.DISABLE_CHANNEL4
        ChannelConfig.Channel.CHANNEL5  -> Command.DISABLE_CHANNEL5
        ChannelConfig.Channel.CHANNEL6  -> Command.DISABLE_CHANNEL6
        ChannelConfig.Channel.CHANNEL7  -> Command.DISABLE_CHANNEL7
        ChannelConfig.Channel.CHANNEL8  -> Command.DISABLE_CHANNEL8
        ChannelConfig.Channel.CHANNEL9  -> Command.DISABLE_CHANNEL9
        ChannelConfig.Channel.CHANNEL10 -> Command.DISABLE_CHANNEL10
        ChannelConfig.Channel.CHANNEL11 -> Command.DISABLE_CHANNEL11
        ChannelConfig.Channel.CHANNEL12 -> Command.DISABLE_CHANNEL12
        ChannelConfig.Channel.CHANNEL13 -> Command.DISABLE_CHANNEL13
        ChannelConfig.Channel.CHANNEL14 -> Command.DISABLE_CHANNEL14
        ChannelConfig.Channel.CHANNEL15 -> Command.DISABLE_CHANNEL15
        ChannelConfig.Channel.CHANNEL16 -> Command.DISABLE_CHANNEL16
    }
}

internal fun ChannelConfig.Channel.toEnableCommand(): Command {
    return when(this) {
        ChannelConfig.Channel.CHANNEL1  -> Command.ENABLE_CHANNEL1
        ChannelConfig.Channel.CHANNEL2  -> Command.ENABLE_CHANNEL2
        ChannelConfig.Channel.CHANNEL3  -> Command.ENABLE_CHANNEL3
        ChannelConfig.Channel.CHANNEL4  -> Command.ENABLE_CHANNEL4
        ChannelConfig.Channel.CHANNEL5  -> Command.ENABLE_CHANNEL5
        ChannelConfig.Channel.CHANNEL6  -> Command.ENABLE_CHANNEL6
        ChannelConfig.Channel.CHANNEL7  -> Command.ENABLE_CHANNEL7
        ChannelConfig.Channel.CHANNEL8  -> Command.ENABLE_CHANNEL8
        ChannelConfig.Channel.CHANNEL9  -> Command.ENABLE_CHANNEL9
        ChannelConfig.Channel.CHANNEL10 -> Command.ENABLE_CHANNEL10
        ChannelConfig.Channel.CHANNEL11 -> Command.ENABLE_CHANNEL11
        ChannelConfig.Channel.CHANNEL12 -> Command.ENABLE_CHANNEL12
        ChannelConfig.Channel.CHANNEL13 -> Command.ENABLE_CHANNEL13
        ChannelConfig.Channel.CHANNEL14 -> Command.ENABLE_CHANNEL14
        ChannelConfig.Channel.CHANNEL15 -> Command.ENABLE_CHANNEL15
        ChannelConfig.Channel.CHANNEL16 -> Command.ENABLE_CHANNEL16
    }
}