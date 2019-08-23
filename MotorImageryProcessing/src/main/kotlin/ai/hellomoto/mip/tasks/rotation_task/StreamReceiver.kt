package ai.hellomoto.mip.tasks.rotation_task

import ai.hellomoto.mip.tasks.rotation_task.RotationStreamProcessorGrpc.RotationStreamProcessorImplBase
import ai.hellomoto.mip.tasks.rotation_task.RotationStreamProcessorService.ProcessResult
import ai.hellomoto.mip.tasks.rotation_task.RotationStreamProcessorService.RotationData
import com.google.common.util.concurrent.MoreExecutors
import io.grpc.Server
import io.grpc.netty.NettyServerBuilder
import io.grpc.stub.StreamObserver
import java.net.InetSocketAddress
import java.util.logging.Level
import java.util.logging.Logger

class RotationStreamProcessorGrpcImpl(
    private val callback: (RotationData)->Unit
):
    RotationStreamProcessorImplBase()
{
    private val logger = Logger.getLogger(RotationStreamProcessorGrpcImpl::class.java.name)

    override fun stream(
        responseObserver: StreamObserver<ProcessResult>?
    ):StreamObserver<RotationData>
    {
        return object : StreamObserver<RotationData> {
            private var counter = 0u
            private val startTime = System.nanoTime()
            override fun onNext(data: RotationData) {
                counter++
                callback(data)
            }

            override fun onError(t: Throwable) {
                logger.log(Level.WARNING, "Stream stopped.")
            }

            override fun onCompleted() {
                val result = ProcessResult.newBuilder().build()
                responseObserver?.onNext(result)
                responseObserver?.onCompleted()
                val duration = (System.nanoTime() - startTime) / 1e9f
                val speed = counter.toFloat() / duration
                logger.log(Level.INFO, "Received $counter points in $duration. $speed(p/s)")
            }
        }
    }
}

fun getServer(host:String="0.0.0.0", port:Int=59898, callback:(RotationData)->Unit): Server? {
    val builder = NettyServerBuilder.forAddress(InetSocketAddress(host, port))
    val executor = MoreExecutors.directExecutor()
    builder.executor(executor)
    return builder.addService(RotationStreamProcessorGrpcImpl(callback)).build()
}