package ai.hellomoto.mip.tasks.rotation.app.processors

import ai.hellomoto.mip.openbci.PacketData
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder


fun OutputStream.write32bitInt(value: Int) {
    this.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array())
}

fun InputStream.read32bitInt(): Int {
    val buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
    this.read(buffer.array())
    return buffer.int
}

object TestSizeIO {
    @Throws(IOException::class, InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val name = "foo.bin"
        val oStream = FileOutputStream(name)
        for (i in -1023..1024) {
            oStream.write32bitInt(i)
        }
        oStream.close()

        val iStream = FileInputStream(name)
        for (expected in -1023..1024) {
            val found = iStream.read32bitInt()
            println("${expected}, ${found}")
            assert(expected == found)
        }
    }
}


fun PacketData.writeDelimitedTo(stream: OutputStream?) {
    val data = ProtoBuf.dump(PacketData.serializer(), this)
    stream?.write32bitInt(data.size)
    stream?.write(data)
}

fun InputStream.readPacketData(): PacketData {
    val size = this.read32bitInt()
    val buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN).array()
    this.read(buffer)
    return ProtoBuf.load(PacketData.serializer(), buffer)
}

object TestPacketDataIO {
    private fun getPacketData(i: Int): PacketData {
        return PacketData(
            i.toLong(), i, i.toByte(),
            listOf(i, i, i, i, i, i), listOf(i, i, i, i, i, i),
            listOf(i.toFloat(), i.toFloat(), i.toFloat(), i.toFloat(), i.toFloat(), i.toFloat())
        )
    }

    @Throws(IOException::class, InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val name = "foo.bin"
        val oStream = FileOutputStream(name)
        for (i in 0..1024) {
            val packetData = getPacketData(i)
            packetData.writeDelimitedTo(oStream)
        }
        oStream.close()

        val iStream = FileInputStream(name)
        for (i in 0..1024) {
            val expected = getPacketData(i)
            val found = iStream.readPacketData()
            println("${expected}, ${found}")
            assert(expected == found)
        }
    }
}
