package ai.hellomoto.mip.openbci


class Cyton(private val serial:ISerialWrapper)
{
    constructor(port:String, baudRate:Int=115200): this(SerialWrapper(port, baudRate)) {}

    ////////////////////////////////////////////////////////////////////////////
    fun resetBoard(): String {
        serial.sendCommand(Command.RESET_BOARD)
        return serial.readMessage()
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
        }
    private fun setAndFetchBoardMode(cmd:Command):BoardMode {
        serial.sendCommand(cmd)
        return BoardMode.fromMessage(serial.readMessage()) ?: throw RuntimeException("Failed to fetch Board Mode.")
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
        }
    private fun setAndFetchSampleRate(cmd:Command):SampleRate {
        serial.sendCommand(cmd)
        return SampleRate.fromMessage(serial.readMessage()) ?: throw RuntimeException("Failed to fetch Sample Rate.")
    }

    ////////////////////////////////////////////////////////////////////////////
    var isWifiAttached:Boolean = false
        private set
    fun attachWifi() {
        if (isWifiAttached) { return }
        serial.sendCommand(Command.ATTACH_WIFI)
        if (serial.readMessage().contains("failure", ignoreCase = true)) {
            throw RuntimeException("Failed to attach WiFi shield.")
        }
        isWifiAttached = true
    }
    fun detachWifi() {
        if (!isWifiAttached) { return }
        serial.sendCommand(Command.DETACH_WIFI)
        if (serial.readMessage().contains("failure", ignoreCase=true)) {
            throw RuntimeException("Failed to detach WiFi shield.")
        }
        isWifiAttached = false
    }
    var wifiStatus:String = ""
        get() {
            serial.sendCommand(Command.QUERY_WIFI_STATUS)
            return serial.readMessage()
        }
        private set

    fun resetWifi():String {
        serial.sendCommand(Command.RESET_WIFI_STATUS)
        return serial.readMessage()
    }

    ////////////////////////////////////////////////////////////////////////////
    fun enableChannel(channel:Int) {
        enableChannel(ChannelConfig.Channel.fromInt(channel))
    }
    fun enableChannel(channel:ChannelConfig.Channel) {
        serial.sendCommand(
            when(channel) {
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
        )
    }

    fun disableChannel(channel:Int) {
        disableChannel(ChannelConfig.Channel.fromInt(channel))
    }
    fun disableChannel(channel:ChannelConfig.Channel) {
        serial.sendCommand(
            when(channel) {
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
        )
    }

    ////////////////////////////////////////////////////////////////////////////
    var isStreaming:Boolean = false
        private set
    fun startStreaming() {
        serial.sendCommand(Command.START_STREAMING)
        isStreaming = true
    }

    fun stopStreaming() {
        serial.sendCommand(Command.STOP_STREAMING)
        isStreaming = false
    }

    ////////////////////////////////////////////////////////////////////////////
    var firmwareVersion:String? = null
        get() {
            field = field ?: getFirmWareVersion()
            return field
        }
        private set
    private fun getFirmWareVersion(): String {
        serial.sendCommand(Command.QUERY_FIRMWARE_VERSION)
        return serial.readMessage()
    }

    ////////////////////////////////////////////////////////////////////////////
    var isDaisyAttached:Boolean = false
        private set
    fun attachDaisy():String {
        serial.sendCommand(Command.ATTACH_DAISY)
        val message = serial.readMessage()
        if (message.contains("no daisy to attach!", ignoreCase = true)) {
            throw RuntimeException("Failed to attach Daisy. \"${message}\"")
        }
        isDaisyAttached = true
        return message
    }
    fun detachDaisy():String {
        return if (!isDaisyAttached) "" else {
            serial.sendCommand(Command.DETACH_DAISY)
            isDaisyAttached = false
            serial.readMessage()
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    fun enableTimestamp():String {
        serial.sendCommand(Command.ENABLE_TIMESTAMP)
        return if (isStreaming) "" else serial.readMessage()
    }
    fun disableTimestamp():String {
        serial.sendCommand(Command.DISABLE_TIMESTAMP)
        return if (isStreaming) "" else serial.readMessage()
    }

    ////////////////////////////////////////////////////////////////////////////
    fun resetChannels():String {
        serial.sendCommand(Command.RESET_CHANNELS)
        return serial.readMessage()
    }

    fun getDefaultSettings() {
        serial.sendCommand(Command.QUERY_DEFAULT_SETTINGS)
        val message = serial.readMessage()
    }

    fun configureChannel(
        channel:ChannelConfig.Channel,
        powerDown: ChannelConfig.PowerDown,
        gainSet: ChannelConfig.GainSet,
        inputTypeSet: ChannelConfig.InputTypeSet,
        biasSet: ChannelConfig.BiasSet,
        srb2Set: ChannelConfig.SRB2Set,
        srb1Set: ChannelConfig.SRB1Set
    ) {
        val command = ByteArray(9)
        command[0] = 'x'.toByte()
        command[1] = channel.command
        command[2] = powerDown.command
        command[3] = gainSet.command
        command[4] = inputTypeSet.command
        command[5] = biasSet.command
        command[6] = srb2Set.command
        command[7] = srb1Set.command
        command[8] = 'X'.toByte()
        serial.sendCommand(command, 9)
    }
    fun configureChannel(
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
