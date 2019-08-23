package ai.hellomoto.mip.openbci

import org.junit.jupiter.api.Assertions.*
import java.lang.AssertionError

class SerialMocks{
    interface SerialMock : ISerialWrapper {}

    class PlaceHolder():SerialMock {
        override fun readMessage(): String {
            throw RuntimeException("Test Serial Mock is used without initialization.")
        }
        override fun sendCommand(cmd: Command) {
            throw RuntimeException("Test Serial Mock is used without initialization.")
        }
        override fun sendCommand(cmd: ByteArray, length: Long) {
            throw RuntimeException("Test Serial Mock is used without initialization.")
        }
    }

    class SimpleResponsive (
        private var command: Command?=null,
        private var message: String?=null
    ) :
        ISerialWrapper,
        SerialMock
    {
        override fun sendCommand(cmd: ByteArray, length: Long) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
        override fun sendCommand(cmd: Command) {
            if (command == null) {
                throw AssertionError("Receiving a command more than once.")
            } else {
                assertEquals(command, cmd, "Unexpected command. Expected:`${command}`. Found:`${cmd}`.")
                command = null
            }
        }
        override fun readMessage(): String {
            val msg:String = message ?: throw AssertionError("No message is available.")
            message = null
            return msg
        }
    }
}