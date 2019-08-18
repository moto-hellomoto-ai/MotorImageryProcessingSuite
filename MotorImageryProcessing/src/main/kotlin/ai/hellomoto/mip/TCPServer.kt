package ai.hellomoto.mip

import java.io.IOException
import java.io.PrintWriter
import java.net.*
import java.util.*


class TCPServer(
    private val port:Int=59898,
    private val handler:(String) -> Unit
):Runnable {
    private val server = ServerSocket().apply {
        reuseAddress = true
        soTimeout = 1000
        // To allow Server thread to check if the thread is interrupted,
        // we set timeout and keep running
    }

    override fun run() {
        server.bind(InetSocketAddress(port))
        server.use { keepServing() }
    }

    private fun keepServing() {
        println("Server started.")
        while (!Thread.currentThread().isInterrupted) {
            try {
                server.accept()?.let { processClient(it) }
            }  catch (e: SocketTimeoutException) {

            }
        }
        println("Server stopped.")
    }

    private fun processClient(socket:Socket) {
        try {
            println("$socket: Handling")
            processSocket(socket)
        } catch (e: SocketException) {
            println("$socket: Error")
            println(e)
        } finally {
            try {
                println("$socket: Closing")
                socket.close()
            } catch (e: IOException) {

            }
        }
    }

    private fun processSocket(socket:Socket) {
        val iStream = Scanner(socket.getInputStream())
        val oStream = PrintWriter(socket.getOutputStream(), true)
        while (!Thread.currentThread().isInterrupted && iStream.hasNextLine()) {
            val line = iStream.nextLine()
            handler(line)
            oStream.println(line)
        }
    }
}

class Server (
    port:Int,
    handler:(String) -> Unit
) {
    private var thread:Thread? = null
    private val server:Runnable = TCPServer(port, handler)

    fun start() {
        thread = Thread(server).apply{start()}
    }

    fun stop(wait:Boolean=true) {
        thread?.interrupt()
        if (wait) { thread?.join() }
        thread = null
    }
}

fun getServer(port:Int, handler:(String) -> Unit) : Server {
    return Server(port, handler)
}