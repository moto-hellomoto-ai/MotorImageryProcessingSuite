package ai.hellomoto.mip.openbci

interface ICyton: AutoCloseable {
    ////////////////////////////////////////////////////////////////////////////
    fun initBoard(): OperationResult

    ////////////////////////////////////////////////////////////////////////////
    fun readMessage(timeout: Int =3000): OperationResult

    ////////////////////////////////////////////////////////////////////////////
    fun resetBoard(): OperationResult

    fun attachWifi(): OperationResult
    fun detachWifi(): OperationResult
    fun resetWifi(): String?
    ////////////////////////////////////////////////////////////////////////////
    fun enableChannel(channel: Int)

    fun enableChannel(channel: ChannelConfig.Channel)
    fun disableChannel(channel: Int)
    fun disableChannel(channel: ChannelConfig.Channel)
    fun startStreaming(callback: (ReadPacketResult) -> Unit): Boolean
    fun stopStreaming()
    fun readPacket(): ReadPacketResult
    fun attachDaisy(): OperationResult
    fun detachDaisy(): OperationResult
    ////////////////////////////////////////////////////////////////////////////
    fun enableTimestamp(): String?

    fun disableTimestamp(): String?
    ////////////////////////////////////////////////////////////////////////////
    fun resetChannels(): OperationResult

    fun getDefaultSettings(): OperationResult

    fun configureChannel(
        channel: ChannelConfig.Channel,
        powerDown: ChannelConfig.PowerDown,
        gainSet: ChannelConfig.GainSet,
        inputTypeSet: ChannelConfig.InputTypeSet,
        biasSet: ChannelConfig.BiasSet,
        srb2Set: ChannelConfig.SRB2Set,
        srb1Set: ChannelConfig.SRB1Set
    )

    fun configureChannel(
        channel: Int,
        powerDown: ChannelConfig.PowerDown,
        gainSet: ChannelConfig.GainSet,
        inputTypeSet: ChannelConfig.InputTypeSet,
        biasSet: ChannelConfig.BiasSet,
        srb2Set: ChannelConfig.SRB2Set,
        srb1Set: ChannelConfig.SRB1Set
    )
}