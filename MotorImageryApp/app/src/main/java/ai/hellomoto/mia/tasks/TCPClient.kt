package ai.hellomoto.mia.tasks

import android.util.Log
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket
import java.nio.charset.Charset
import java.util.*


class TCPClient(address: String, port: Int) {
    private val socket: Socket
    private val oStream: OutputStream
    private val iStream: Scanner

    init {
        Log.d("", "Connecting... %s:%d".format(address, port))
        socket = Socket(InetAddress.getByName(address), port)
        oStream = socket.getOutputStream()
        iStream = Scanner(socket.getInputStream())
    }

    fun sendMessage(message: String): String {
        oStream.write((message + '\n').toByteArray(Charset.defaultCharset()))
        val returnVal = iStream.nextLine()
        //Log.d("", returnVal)
        return returnVal
    }

    fun close() {
        oStream.close()
        iStream.close()
        socket.close()
    }

    /*
    public static void main(String[] args) throws Exception {
        try (var socket = new Socket(args[0], 59898)) {
            System.out.println("Enter lines of text then Ctrl+D or Ctrl+C to quit");
            var scanner = new Scanner(System.in);
            var in = new Scanner(socket.getInputStream());
            var out = new PrintWriter(socket.getOutputStream(), true);
            while (scanner.hasNextLine()) {
                out.println(scanner.nextLine());
                System.out.println(in.nextLine());
            }
        }
    }
    */
}
