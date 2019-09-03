package ai.hellomoto.mip.openbci

import sun.misc.Signal
import sun.misc.SignalHandler
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

fun iterateBoardMode(cyton:Cyton) {
    println(cyton.boardMode)
    for (mode in BoardMode.values().asIterable()) {
        cyton.boardMode = mode
        println(cyton.boardMode)
    }
    println(cyton.resetBoard())
    println(cyton.boardMode)
}

fun iterateSampleRate(cyton:Cyton) {
    println(cyton.sampleRate)
    for (mode in SampleRate.values().asIterable()) {
        cyton.sampleRate = mode
        println(cyton.sampleRate)
    }
}

fun testDaisy(cyton:Cyton) {
    cyton.attachDaisy()
    Thread.sleep(1000)
    cyton.detachDaisy()
}


fun main(args: Array<String>) {
    val cyton = Cyton("tty.usbserial-DM00CXN8")
    println(cyton.init())
    println(cyton.sampleRate)
    println(cyton.boardMode)
    println(cyton.firmwareVersion)
    println(cyton.wifiStatus)
    println(cyton.attachDaisy())
    println("is daisy attached: ${cyton.isDaisyAttached}")
    println("is streaming: ${cyton.isStreaming}")
    println("is WiFi attached: ${cyton.isWifiAttached}")

    // cyton.getDefaultSettings()
    // cyton.resetChannels()
    // iterateBoardMode(cyton)
    // testDaisy(cyton)
    // iterateSampleRate(cyton)

    var numSuccess = 0
    var numFail = 0

    val timer = Timer("schedule", true)
    val future = timer.scheduleAtFixedRate(5, 3) {
        cyton.waitForStartByte()
        val result = cyton.readPacket()
        if (result is ReadPacketResult.Fail) {
            numFail += 1
            println(result)
        } else {
            numSuccess += 1
        }
    }
    cyton.startStreaming()
    val startTime = System.currentTimeMillis()

    Signal.handle(Signal("INT"), object : SignalHandler {
        override fun handle(sig: Signal) {
            println("Closing socket.")
            cyton.close()
            println("waiting ...")
            future.cancel()
            val numPacket = numSuccess + numFail
            val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
            println("Elapsed: ${elapsedTime} [sec]")
            println("Total Packets: ${numPacket}")
            println("${numPacket / elapsedTime} [PPS]")
            println("Success Rate ${100F * numSuccess.toFloat() / numPacket.toFloat()}.")
            System.exit(0)
        }
    })

    Thread.sleep(10000000)
}