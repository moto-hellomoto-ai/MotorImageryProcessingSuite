package ai.hellomoto.mip.tasks.rotation.app

import ai.hellomoto.mip.openbci.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

class CytonMock: ICyton {
    companion object {
        val LOG: Logger = LogManager.getLogger(CytonMock::class.java.name)
    }
    var numChannels = 8
    var sampleRate = SampleRate.SR_250

    override fun toString(): String {
        return "${super.toString()}: ${numChannels} Channels, ${sampleRate}"
    }

    override fun initBoard(): OperationResult {
        numChannels = 8
        sampleRate = SampleRate.SR_250
        return OperationResult.Success("Successfully initialized Cyton Mock: ${this}")
    }

    override fun readMessage(timeout: Int): OperationResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resetBoard(): OperationResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun attachWifi(): OperationResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun detachWifi(): OperationResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resetWifi(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableChannel(channel: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableChannel(channel: ChannelConfig.Channel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun disableChannel(channel: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun disableChannel(channel: ChannelConfig.Channel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val streamingTimer = Timer(true)
    private var streamingTimerTask: TimerTask? = null
    override fun startStreaming(callback: (ReadPacketResult) -> Unit): Boolean {
        LOG.info("Starting Streaming");
        val rate = sampleRate.toLong()
        if (rate > 1000) {
            LOG.error("Sampling rate is higher than 1000. Cannot stream via serial COM.");
            return false;
        }
        val period: Long = 1000 / sampleRate.toLong()
        streamingTimerTask = streamingTimer.scheduleAtFixedRate(1000, period) {
            callback(readPacket())
        }
        return true
    }

    override fun stopStreaming() {
        LOG.info("Stoping Streaming");
        streamingTimerTask?.cancel()
        streamingTimerTask = null
    }

    var packetId = 0
    val stopByte = 0xC0.toByte()
    override fun readPacket(): ReadPacketResult {
        packetId += 1;
        val rawEegs = List(numChannels) { (0..255).random() }
        val eegs = rawEegs.map { parseEeg(it) }
        val auxs: List<Int> = listOf()
        return ReadPacketResult.Success(PacketData(Date().time, packetId, stopByte, rawEegs, auxs, eegs));
    }

    override fun attachDaisy(): OperationResult {
        LOG.info("Attaching Daisy")
        numChannels = 16
        return OperationResult.Success("Successfully attached Daisy. ${this}");
    }

    override fun detachDaisy(): OperationResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableTimestamp(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun disableTimestamp(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resetChannels(): OperationResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDefaultSettings(): OperationResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configureChannel(
        channel: ChannelConfig.Channel,
        powerDown: ChannelConfig.PowerDown,
        gainSet: ChannelConfig.GainSet,
        inputTypeSet: ChannelConfig.InputTypeSet,
        biasSet: ChannelConfig.BiasSet,
        srb2Set: ChannelConfig.SRB2Set,
        srb1Set: ChannelConfig.SRB1Set
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configureChannel(
        channel: Int,
        powerDown: ChannelConfig.PowerDown,
        gainSet: ChannelConfig.GainSet,
        inputTypeSet: ChannelConfig.InputTypeSet,
        biasSet: ChannelConfig.BiasSet,
        srb2Set: ChannelConfig.SRB2Set,
        srb1Set: ChannelConfig.SRB1Set
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun close() {
    }
}