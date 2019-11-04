package ai.hellomoto.mip.tasks.rotation.app.processors

import ai.hellomoto.mip.tasks.rotation_task.RotationStreamProcessorGrpc.RotationStreamProcessorImplBase
import ai.hellomoto.mip.tasks.rotation_task.RotationStreamProcessorService.ProcessResult
import ai.hellomoto.mip.tasks.rotation_task.RotationStreamProcessorService.RotationData
import com.google.common.util.concurrent.MoreExecutors
import io.grpc.Server
import io.grpc.netty.NettyServerBuilder
import io.grpc.stub.StreamObserver
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.InetSocketAddress


class RotationStreamProcessorGrpcImpl(
    private val callback: (RotationData) -> Unit,
    private val onErrorCallback: (Throwable) -> Unit = {},
    private val onCompleteCallback: () -> Unit = {}
) : RotationStreamProcessorImplBase() {
    companion object {
        val LOG: Logger = LogManager.getLogger(RotationStreamProcessorGrpcImpl::class.qualifiedName)
    }

    override fun stream(responseObserver: StreamObserver<ProcessResult>?): StreamObserver<RotationData> {
        return object : StreamObserver<RotationData> {
            private var counter = 0
            private val startTime = System.nanoTime()
            override fun onNext(data: RotationData) {
                counter++
                callback(data)
            }

            override fun onError(t: Throwable) {
                LOG.error("Stream stopped.", t)
                onErrorCallback(t)
            }

            override fun onCompleted() {
                responseObserver?.onNext(ProcessResult.newBuilder().build())
                responseObserver?.onCompleted()

                LOG.info("Stream completed.")
                onCompleteCallback()
                val duration = (System.nanoTime() - startTime) / 1e9f
                val speed = counter.toFloat() / duration
                LOG.info("Received $counter points in $duration. $speed(p/s)")
            }
        }
    }
}

abstract class RotationStreamProcessor {
    companion object {
        val LOG: Logger = LogManager.getLogger(RotationStreamProcessor::class.qualifiedName)
    }

    private var server: Server? = null

    fun start(host: String, port: Int) {
        LOG.info("Starting Rotation Stream Receiver {}:{}", host, port)
        server = getServer(host, port);
        server?.start()
    }

    fun stop() {
        server?.let {
            if (!it.isShutdown) {
                LOG.info("Stopping Rotation Stream Receiver")
                it.shutdownNow()
            }
        }
    }

    private fun getServer(host: String, port: Int): Server {
        val builder = NettyServerBuilder.forAddress(InetSocketAddress(host, port))
        val executor = MoreExecutors.directExecutor()
        builder.executor(executor)
        return builder.addService(
            RotationStreamProcessorGrpcImpl(
                this::onNextCallback,
                this::onErrorCallback,
                this::onCompleteCallback
            )
        ).build()
    }

    abstract fun onNextCallback(data: RotationData)

    abstract fun onErrorCallback(t: Throwable)

    abstract fun onCompleteCallback()
}
