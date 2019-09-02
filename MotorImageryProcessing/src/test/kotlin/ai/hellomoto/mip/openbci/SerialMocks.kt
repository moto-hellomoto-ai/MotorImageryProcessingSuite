package ai.hellomoto.mip.openbci

import org.junit.jupiter.api.Assertions.*
import java.lang.AssertionError

class SerialMocks{
    interface SerialMock : ISerial {}

    class PlaceHolder():SerialMock {
        override fun readMessage(timeout:Int): String {
            throw RuntimeException("Test Serial Mock is used without initialization.")
        }
        override fun sendCommand(cmd: Command) {
            throw RuntimeException("Test Serial Mock is used without initialization.")
        }
        override fun sendCommand(cmd: ByteArray, length: Long) {
            throw RuntimeException("Test Serial Mock is used without initialization.")
        }
        override fun waitByte(value: Byte) {
            throw RuntimeException("Test Serial Mock is used without initialization.")
        }
        override fun readPacket(): PacketData {
            throw RuntimeException("Test Serial Mock is used without initialization.")
        }
        override fun close() {
            throw RuntimeException("Test Serial Mock is used without initialization.")
        }
    }

    class SimpleResponsive (
        private var command: Command?=null,
        private var message: String?=null
    ) :
        ISerial,
        SerialMock
    {
        override fun readPacket(): PacketData {
            throw RuntimeException("SimpleResponsive Mock is not intended for stream testing.")
        }
        override fun waitByte(value: Byte) {
            throw RuntimeException("SimpleResponsive Mock is not intended for stream testing.")
        }
        override fun sendCommand(cmd: ByteArray, length: Long) {
            throw RuntimeException("SimpleResponsive Mock is not intended for variable command testing.")
        }
        override fun sendCommand(cmd: Command) {
            if (command == null) {
                throw AssertionError("Receiving a command more than once.")
            } else {
                assertEquals(command, cmd, "Unexpected command. Expected:`${command}`. Found:`${cmd}`.")
                command = null
            }
        }
        override fun readMessage(timeout:Int): String {
            val msg:String = message ?: throw AssertionError("No message is available.")
            message = null
            return msg
        }
        override fun close() {
            command = null
            message = null
        }
    }
}