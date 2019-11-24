package ai.hellomoto.mip.openbci

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.concurrent.scheduleAtFixedRate

fun <T> concatenate(vararg lists: List<T>): List<T> {
    return Stream.of(*lists).flatMap {x -> x.stream()}.collect(Collectors.toList())
}

fun PacketData.merge(other: PacketData): PacketData {
    return PacketData(
        packetId = this.packetId,
        date = this.date,
        stopByte = other.stopByte,
        rawEegs = concatenate(this.rawEegs, other.rawEegs),
        auxs = concatenate(this.auxs, other.auxs),
        eegs = concatenate(this.eegs, other.eegs)
    )
}

class Cyton(private val serial:ISerial) : ICyton {
    companion object {
        val LOG:Logger = LogManager.getLogger(Cyton::class.java.name)
    }

    constructor(port:String, baudRate:Int=115200): this(SerialImpl(port, baudRate)) {}

    private fun closeSocket() {
        LOG.info("* Closing the socket.")
        serial.close()
    }

    ////////////////////////////////////////////////////////////////////////////
    override fun initBoard():OperationResult {
        var res:OperationResult?
        LOG.info("* Resetting Board.")
        res = resetBoard()
        if (res !is OperationResult.Success) { return res }
        val boardInfo = res.message
        LOG.info("* Resetting Channels.")
        res = resetChannels()
        if (res !is OperationResult.Success) { return res }
        return OperationResult.Success(message=boardInfo)
    }

    override fun close() {
        stopStreaming()
        closeSocket()
    }

    ////////////////////////////////////////////////////////////////////////////
    override fun readMessage(timeout:Int):OperationResult {
        return when (val message = serial.readMessage(timeout)) {
            null -> OperationResult.TimeOut()
            else -> OperationResult.Success(message)
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    override fun resetBoard(): OperationResult {
        serial.sendCommand(Command.RESET_BOARD)
        val message = serial.readMessage()
        return when {
            message == null -> OperationResult.TimeOut(Command.RESET_BOARD)
            message.endsWith("$$$") -> OperationResult.Success(trimBeforePrefix(message, "OpenBCI"))
            else -> OperationResult.Fail(message)
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    var boardMode:BoardMode? = null
        get() {
            field = field ?: setAndFetchBoardMode(Command.QUERY_BOARD_MODE)
            return field
        }
        set(mode) {
            requireNotNull(mode) { "Board Mode to set cannot be null." }
            field = setAndFetchBoardMode(mode.command)
            if (field == null) { LOG.warn("Board mode was not set correctly.") }
        }
    private fun setAndFetchBoardMode(cmd:Command):BoardMode? {
        serial.sendCommand(cmd)
        return serial.readMessage().toBoardMode()
    }

    ////////////////////////////////////////////////////////////////////////////
    var sampleRate: SampleRate? = null
        get() {
            field = field ?: setAndFetchSampleRate(Command.QUERY_SAMPLE_RATE)
            return field
        }
        set(rate) {
            requireNotNull(rate) {"Sample Rate to set cannot be null."}
            field = setAndFetchSampleRate(rate.command)
            if (field == null) { LOG.warn("Sample rate was not set correctly.") }
        }
    private fun setAndFetchSampleRate(cmd:Command):SampleRate? {
        serial.sendCommand(cmd)
        return serial.readMessage().toSampleRate()
    }

    ////////////////////////////////////////////////////////////////////////////
    var isWifiAttached:Boolean = false
        private set
    override fun attachWifi():OperationResult = when {
        isWifiAttached -> OperationResult.Invalid("WiFi is attached.")
        else -> {
            serial.sendCommand(Command.ATTACH_WIFI)
            val message = serial.readMessage()
            when {
                message == null -> OperationResult.TimeOut(Command.ATTACH_WIFI)
                message.contains("failure", ignoreCase = true) -> OperationResult.Fail(message)
                else -> {
                    isWifiAttached = true
                    OperationResult.Success(message)
                }
            }
        }
    }
    override fun detachWifi():OperationResult = when {
        !isWifiAttached -> OperationResult.Invalid("WiFi not attached.")
        else -> {
            serial.sendCommand(Command.DETACH_WIFI)
            val message = serial.readMessage()
            when {
                message == null -> OperationResult.TimeOut(Command.DETACH_WIFI)
                message.contains("failure", ignoreCase = true) -> OperationResult.Fail(message)
                else -> {
                    isWifiAttached = false
                    OperationResult.Success(message)
                }
            }
        }
    }
    var wifiStatus:String? = ""
        get() {
            serial.sendCommand(Command.QUERY_WIFI_STATUS)
            return serial.readMessage()
        }
        private set

    override fun resetWifi():String? {
        serial.sendCommand(Command.RESET_WIFI_STATUS)
        return serial.readMessage()
    }

    ////////////////////////////////////////////////////////////////////////////
    override fun enableChannel(channel:Int) {
        enableChannel(ChannelConfig.Channel.fromInt(channel))
    }
    override fun enableChannel(channel:ChannelConfig.Channel) {
        serial.sendCommand(channel.toEnableCommand())
    }

    override fun disableChannel(channel:Int) {
        disableChannel(ChannelConfig.Channel.fromInt(channel))
    }
    override fun disableChannel(channel:ChannelConfig.Channel) {
        serial.sendCommand(channel.toDisableCommand())
    }

    ////////////////////////////////////////////////////////////////////////////
    var isStreaming:Boolean = false
        private set
    private fun startStreaming() {
        serial.sendCommand(Command.START_STREAMING)
        isStreaming = true
    }

    private var streamingTimer:Timer? = null
    private var streamingTimerTask: TimerTask? = null
    override fun startStreaming(callback:(ReadPacketResult)->Unit):Boolean {
        this.sampleRate?.let schedule@ {
            val rate:Long = it.toLong()
            if (rate > 1000) {
                LOG.error("Sampling rate is higher than 1000. Cannot stream via serial COM.");
                return@schedule
            }
            val period:Long = 1000 / rate
            streamingTimer = Timer(true)
            streamingTimerTask = streamingTimer?.scheduleAtFixedRate(0, period) {
                callback(readPacket());
            }
            startStreaming()
        }
        return isStreaming
    }

    override fun stopStreaming() {
        if (isStreaming) {
            serial.sendCommand(Command.STOP_STREAMING)
            streamingTimerTask?.cancel()
            streamingTimerTask = null
            streamingTimer?.cancel()
            streamingTimer = null
            isStreaming = false
        }
    }

    private fun waitForStartByte() {
        serial.waitByte(START_BYTE)
    }
    override fun readPacket():ReadPacketResult {
        return if (isDaisyAttached) readTwoPackets() else readOnePacket()
    }

    private fun readOnePacket(): ReadPacketResult {
        waitForStartByte()
        val packet = serial.readPacket()
        if (packet.stopByte == STOP_BYTE) {
            packet.eegs = packet.rawEegs.map{parseEeg(it)}
            return ReadPacketResult.Success(packet)
        }
        return ReadPacketResult.Fail(
            "Invalid Stop Byte. Packet ID: ${packet.packetId}, Stop Byte: ${packet.stopByte}.")
    }

    private fun readTwoPackets(): ReadPacketResult {
        return when (val res1 = readOnePacket()) {
            is ReadPacketResult.Fail -> res1
            is ReadPacketResult.Success ->
                when (val res2 = readOnePacket()) {
                    is ReadPacketResult.Fail -> res2
                    is ReadPacketResult.Success ->
                        ReadPacketResult.Success(res1.data.merge(res2.data))
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    var firmwareVersion:String? = null
        get() {
            field = field ?: getFirmWareVersion()
            return field
        }
        private set
    private fun getFirmWareVersion(): String? {
        serial.sendCommand(Command.QUERY_FIRMWARE_VERSION)
        return serial.readMessage()
    }

    ////////////////////////////////////////////////////////////////////////////
    var isDaisyAttached:Boolean = false
        private set
    override fun attachDaisy():OperationResult = when {
        isDaisyAttached -> OperationResult.Invalid("Daisy is attached.")
        else -> {
            serial.sendCommand(Command.ATTACH_DAISY)
            val message = serial.readMessage()
            when {
                message == null -> OperationResult.TimeOut(Command.ATTACH_DAISY)
                message.contains("no daisy to attach!", ignoreCase = true) -> OperationResult.Fail(message)
                else -> {
                    isDaisyAttached = true
                    OperationResult.Success(message)
                }
            }
        }
    }
    override fun detachDaisy():OperationResult = when {
        !isDaisyAttached -> OperationResult.Invalid("Daisy is not attached.")
        else -> {
            serial.sendCommand(Command.DETACH_DAISY)
            val message = serial.readMessage()
            when (message) {
                null -> OperationResult.TimeOut(Command.DETACH_DAISY)
                else -> {
                    isDaisyAttached = false
                    OperationResult.Success(message)
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    override fun enableTimestamp():String? {
        serial.sendCommand(Command.ENABLE_TIMESTAMP)
        return if (isStreaming) "" else serial.readMessage()
    }
    override fun disableTimestamp():String? {
        serial.sendCommand(Command.DISABLE_TIMESTAMP)
        return if (isStreaming) "" else serial.readMessage()
    }

    ////////////////////////////////////////////////////////////////////////////
    override fun resetChannels():OperationResult {
        serial.sendCommand(Command.RESET_CHANNELS)
        return when (val message = serial.readMessage()) {
            null -> OperationResult.TimeOut(Command.RESET_CHANNELS)
            // TODO: Add failure case
            else -> OperationResult.Success(message)
        }
    }

    override fun getDefaultSettings():OperationResult {
        serial.sendCommand(Command.QUERY_DEFAULT_SETTINGS)
        return serial.readMessage().toDefaultConfig()
    }

    override fun configureChannel(
        channel:ChannelConfig.Channel,
        powerDown: ChannelConfig.PowerDown,
        gainSet: ChannelConfig.GainSet,
        inputTypeSet: ChannelConfig.InputTypeSet,
        biasSet: ChannelConfig.BiasSet,
        srb2Set: ChannelConfig.SRB2Set,
        srb1Set: ChannelConfig.SRB1Set
    ) {
        val command = byteArrayOf(
            'x'.toByte(),
            channel.command, powerDown.command, gainSet.command,
            inputTypeSet.command, biasSet.command, srb2Set.command, srb1Set.command,
            'X'.toByte()
        )
        serial.sendCommand(command, 9)
    }
    override fun configureChannel(
        channel:Int,
        powerDown: ChannelConfig.PowerDown,
        gainSet: ChannelConfig.GainSet,
        inputTypeSet: ChannelConfig.InputTypeSet,
        biasSet: ChannelConfig.BiasSet,
        srb2Set: ChannelConfig.SRB2Set,
        srb1Set: ChannelConfig.SRB1Set
    ) {
        configureChannel(
            ChannelConfig.Channel.fromInt(channel),
            powerDown, gainSet, inputTypeSet, biasSet, srb2Set, srb1Set
        )
    }
    ////////////////////////////////////////////////////////////////////////////
}
