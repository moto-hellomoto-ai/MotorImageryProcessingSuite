package ai.hellomoto.mia.tasks.rotation_task

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import ai.hellomoto.mip.tasks.rotation.RotationStreamProcessorGrpc.*
import ai.hellomoto.mip.tasks.rotation.RotationStreamProcessorService.*


class Streamer(
    host: String, port: Int,
    private val errorHandler:(Throwable)->Unit
) {
    private val channel: ManagedChannel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
    private val asyncStub = newStub(channel)
    private val finishLatch = CountDownLatch(1)
    private val responseObserver = object : StreamObserver<ProcessResult> {
        override fun onNext(result: ProcessResult) {
            println("Received result from server.")
        }

        override fun onError(t: Throwable) {
            finishLatch.countDown()
            errorHandler(t)
        }

        override fun onCompleted() {
            println("Finished Streaming")
            finishLatch.countDown()
        }
    }

    private val requestObserver = asyncStub.stream(responseObserver)

    @Throws(InterruptedException::class)
    fun send(timestamp: Long, value:Float) {
        if (finishLatch.count == 0L) {
            return
        }
        try {
            val data = RotationData.newBuilder().setVelocity(value).setTimestamp(timestamp).build()
            requestObserver.onNext(data)
        } catch (e: RuntimeException) {
            requestObserver.onError(e)
            throw e
        }
    }

    @Throws(InterruptedException::class)
    fun shutdown(timeout:Long=3L) {
        requestObserver.onCompleted()
        channel.shutdown().awaitTermination(timeout, TimeUnit.SECONDS)
    }
}