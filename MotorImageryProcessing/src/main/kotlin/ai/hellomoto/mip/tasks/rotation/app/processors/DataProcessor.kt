package ai.hellomoto.mip.tasks.rotation.app.processors

import ai.hellomoto.mip.openbci.PacketData
import ai.hellomoto.mip.tasks.rotation.app.AppView
import ai.hellomoto.mip.tasks.rotation_task.RotationStreamProcessorService.RotationData
import javafx.application.Platform
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

class DataProcessor(private val appView: AppView) {
    private val timer = Timer(true)
    private var flushTask: TimerTask? = null
    private var plotTask: TimerTask? = null

    private var bciOutputStream: OutputStream? = null
    private val bciBuffer: ArrayList<PacketData> = arrayListOf()
    private val bciBufferLock = Object()

    private var rotOutputStream: OutputStream? = null
    private val rotBuffer: ArrayList<RotationData> = arrayListOf()
    private val rotBufferLock = Object()

    fun addBCIData(data: PacketData) {
        synchronized(bciBufferLock) {
            bciBuffer.add(data)
        }
    }

    fun addRotationData(data: RotationData) {
        synchronized(rotBufferLock) {
            rotBuffer.add(data)
        }
    }

    private fun flush() {
        synchronized(bciBufferLock) {
            for (packetData in bciBuffer) {
                packetData.writeDelimitedTo(bciOutputStream)
            }
            bciBuffer.clear()
            bciOutputStream?.flush()
        }
        synchronized(rotBufferLock) {
            for (rotationData in rotBuffer) {
                rotationData.writeDelimitedTo(rotOutputStream)
            }
            rotBuffer.clear()
            rotOutputStream?.flush()
        }
    }

    private fun initSaveDir() {
        File(appView.scope.ioConfig.saveDir).mkdirs()
    }

    private fun getSavePathPrefix(): String {
        val date = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
        return File(appView.scope.ioConfig.saveDir, dateFormat.format(date).toString()).toString()
    }

    fun startFlush() {
        initSaveDir()
        val prefix = getSavePathPrefix()
        rotOutputStream = FileOutputStream(prefix + "_rot.bin")
        bciOutputStream = FileOutputStream(prefix + "_bci.bin")
        flushTask = flushTask ?: timer.scheduleAtFixedRate(0, 5000) { flush() }
    }

    fun stopFlush() {
        rotOutputStream?.close()
        bciOutputStream?.close()
        flushTask?.cancel()
        flushTask = null
    }

    fun startPlot() {
        plotTask = plotTask ?: timer.scheduleAtFixedRate(0, 50) { plot() }
    }

    fun stopPlot() {
        plotTask?.cancel()
        plotTask = null
    }

    private fun plot() {
        var bciData: PacketData? = null
        var rotData: RotationData? = null
        synchronized(bciBufferLock) {
            if (bciBuffer.size > 0) {
                bciData = bciBuffer[bciBuffer.size - 1]
            }
        }
        synchronized(rotBufferLock) {
            if (rotBuffer.size > 0) {
                rotData = rotBuffer[rotBuffer.size - 1]
            }
        }
        Platform.runLater {
            bciData?.let {
                appView.bciPlotView.addData(it)
            }
            rotData?.let{
                appView.rotationView.addData(it.timestamp, it.velocity)
                appView.rotationView.rotate(it.velocity)
            }
            appView.bciPlotView.updateTimeRange()
            appView.rotationView.updateChart()
        }
    }
}
