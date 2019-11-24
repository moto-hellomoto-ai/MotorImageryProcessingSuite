package ai.hellomoto.mip.tasks.rotation

import ai.hellomoto.mip.tasks.rotation_task.RotationStreamProcessorGrpc
import ai.hellomoto.mip.tasks.rotation_task.RotationStreamProcessorService.ProcessResult
import ai.hellomoto.mip.tasks.rotation_task.RotationStreamProcessorService.RotationData
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class StreamClient(host: String, port: Int) {
    private val channel: ManagedChannel = ManagedChannelBuilder.forTarget("${host}:${port}/").usePlaintext().build()
    private val asyncStub = RotationStreamProcessorGrpc.newStub(channel)

    private val finishLatch = CountDownLatch(1)
    private val responseObserver = object : StreamObserver<ProcessResult> {
        override fun onNext(result: ProcessResult) {
            println("Received result from server.")
        }

        override fun onError(t: Throwable) {
            println("Failed: ${Status.fromThrowable(t)}")
            finishLatch.countDown()
        }

        override fun onCompleted() {
            println("Finished Streaming")
            finishLatch.countDown()
        }
    }

    private val requestObserver = asyncStub.stream(responseObserver)

    @Throws(InterruptedException::class)
    fun send(value:Float) {
        if (finishLatch.count == 0L) {
            return
        }
        val now = System.currentTimeMillis()
        try {
            val data = RotationData.newBuilder().setVelocity(value).setTimestamp(now).build()
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

object Client {
    @Throws(IOException::class, InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val client = StreamClient("localhost", 59898)
        val random = Random()
        try {
            while(true) {
                client.send(random.nextFloat())
                Thread.sleep(33)
            }
        } finally {
            client.shutdown()
        }
    }
}