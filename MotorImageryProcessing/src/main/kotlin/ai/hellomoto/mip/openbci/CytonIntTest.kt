package ai.hellomoto.mip.openbci

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import sun.misc.Signal
import kotlin.system.exitProcess

val LOG: Logger = LogManager.getLogger("CytonIntTest")

fun iterateBoardMode(cyton:Cyton) {
    LOG.info(cyton.boardMode)
    for (mode in BoardMode.values().asIterable()) {
        cyton.boardMode = mode
        LOG.info(cyton.boardMode)
    }
    LOG.info(cyton.resetBoard())
    LOG.info(cyton.boardMode)
}

fun iterateSampleRate(cyton:Cyton) {
    LOG.info(cyton.sampleRate)
    for (mode in SampleRate.values().asIterable()) {
        cyton.sampleRate = mode
        LOG.info(cyton.sampleRate)
    }
}

fun testDaisy(cyton:Cyton) {
    cyton.attachDaisy()
    Thread.sleep(1000)
    cyton.detachDaisy()
}

class StreamMonitor {
    private var numFail = 0
    private var numSuccess = 0
    private val startTime = System.currentTimeMillis()

    fun append(result:ReadPacketResult) {
        if (result is ReadPacketResult.Fail) {
            numFail += 1
            LOG.info(result)
        } else {
            numSuccess += 1
        }
    }

    fun print() {
        val numPacket = numSuccess + numFail
        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
        LOG.info("Elapsed: ${elapsedTime} [sec]")
        LOG.info("Total Packets: ${numPacket}")
        LOG.info("${numPacket / elapsedTime} [PPS]")
        LOG.info("Success Rate ${100F * numSuccess.toFloat() / numPacket.toFloat()}.")
    }
}

fun runTest(cyton:Cyton) {
    cyton.initBoard().message.lines().map { LOG.info(it) }
    LOG.info(cyton.sampleRate)
    LOG.info(cyton.boardMode)
    LOG.info(cyton.firmwareVersion)
    LOG.info(cyton.wifiStatus)
    LOG.info(cyton.attachDaisy())
    LOG.info("is daisy attached: ${cyton.isDaisyAttached}")
    LOG.info("is streaming: ${cyton.isStreaming}")
    LOG.info("is WiFi attached: ${cyton.isWifiAttached}")

    // cyton.getDefaultSettings()
    // cyton.resetChannels()
    // iterateBoardMode(cyton)
    // testDaisy(cyton)
    // iterateSampleRate(cyton)

    val monitor = StreamMonitor()
    cyton.startStreaming(monitor::append)

    val syncObject = Object();
    Signal.handle(Signal("INT")) { synchronized(syncObject) { syncObject.notify() } }
    synchronized(syncObject) { syncObject.wait() }
    cyton.stopStreaming();
    monitor.print();
}

fun main(args: Array<String>) {
    val cyton = Cyton("tty.usbserial-DM00CXN8")
    cyton.use { runTest(cyton) }
}