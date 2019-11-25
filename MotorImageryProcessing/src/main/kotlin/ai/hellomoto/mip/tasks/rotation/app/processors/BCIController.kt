package ai.hellomoto.mip.tasks.rotation.app.processors

import ai.hellomoto.mip.openbci.Cyton
import ai.hellomoto.mip.openbci.ICyton
import ai.hellomoto.mip.openbci.OperationResult
import ai.hellomoto.mip.openbci.ReadPacketResult
import ai.hellomoto.mip.tasks.rotation.app.AppConfig
import ai.hellomoto.mip.tasks.rotation.app.CytonMock
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class BCIController(private val dataProcessor: DataProcessor) {
    companion object {
        val LOG: Logger = LogManager.getLogger(BCIController::class.qualifiedName)
    }

    private var cyton: ICyton = CytonMock()

    fun init(serialPort: String, numChannels: Int): OperationResult {
        LOG.info("Initializing BCI from {}", serialPort)
        cyton = if (serialPort == AppConfig.CYTON_MOCK_PORT) CytonMock() else Cyton(serialPort)
        val result = cyton.initBoard()
        when (result) {
            is OperationResult.Success -> {
                if (numChannels == 16) {
                    cyton.attachDaisy()
                }
            }
        }
        return result
    }

    fun start() {
        LOG.info("Starting BCI stream")
        LOG.info("{}", cyton)
        cyton.startStreaming { result ->
            when (result) {
                is ReadPacketResult.Success -> dataProcessor.addBCIData(result.data)
            }
        }
    }

    fun stop() {
        LOG.info("Stopping BCI stream")
        cyton.stopStreaming()
        cyton.close()
    }
}
