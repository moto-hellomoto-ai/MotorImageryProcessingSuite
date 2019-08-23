package ai.hellomoto.mip.openbci

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

    cyton.getDefaultSettings()
    cyton.resetChannels()

    iterateBoardMode(cyton)

    println(cyton.firmwareVersion)

    testDaisy(cyton)

    iterateSampleRate(cyton)
}