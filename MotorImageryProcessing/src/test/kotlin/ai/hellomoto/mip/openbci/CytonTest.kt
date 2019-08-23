package ai.hellomoto.mip.openbci

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import java.util.stream.Stream

internal class CytonTest {
    open class SimpleTestSuite {
        protected var mock: SerialMocks.SerialMock = SerialMocks.PlaceHolder()
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    internal class CytonBoardTest : SimpleTestSuite() {
        @Test
        fun test_resetBoardMode() {
            mock = SerialMocks.SimpleResponsive(Command.RESET_BOARD, Messages.CYTON_V3_WITH_DAISY_INFO)
            val cyton = Cyton(mock)
            cyton.resetBoard()
        }
        private fun validBoardModePatterns(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Command.SET_BOARD_MODE_DEFAULT, Messages.BOARD_MODE_DEFAULT, BoardMode.DEFAULT),
                Arguments.of(Command.SET_BOARD_MODE_DEBUG, Messages.BOARD_MODE_DEBUG, BoardMode.DEBUG),
                Arguments.of(Command.SET_BOARD_MODE_DIGITAL, Messages.BOARD_MODE_DIGITAL, BoardMode.DIGITAL),
                Arguments.of(Command.SET_BOARD_MODE_ANALOG, Messages.BOARD_MODE_ANALOG, BoardMode.ANALOG),
                Arguments.of(Command.SET_BOARD_MODE_MARKER, Messages.BOARD_MODE_MARKER, BoardMode.MARKER)
            )
        }
        @ParameterizedTest
        @MethodSource("validBoardModePatterns")
        fun test_setBoardMode(cmd:Command, msg:String, mode:BoardMode) {
            mock = SerialMocks.SimpleResponsive(cmd, msg)
            val cyton = Cyton(mock)
            cyton.boardMode = mode
            assertEquals(cyton.boardMode, mode)
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    internal class SamplingRateTest : SimpleTestSuite() {
        private fun validSampleRateArguments(): Stream<Arguments>  {
            return Stream.of(
                Arguments.of(Command.SET_SAMPLE_RATE_250, Messages.SAMPLE_RATE_250, SampleRate.SR_250),
                Arguments.of(Command.SET_SAMPLE_RATE_500, Messages.SAMPLE_RATE_500, SampleRate.SR_500),
                Arguments.of(Command.SET_SAMPLE_RATE_1000, Messages.SAMPLE_RATE_1000, SampleRate.SR_1000),
                Arguments.of(Command.SET_SAMPLE_RATE_2000, Messages.SAMPLE_RATE_2000, SampleRate.SR_2000),
                Arguments.of(Command.SET_SAMPLE_RATE_4000, Messages.SAMPLE_RATE_4000, SampleRate.SR_4000),
                Arguments.of(Command.SET_SAMPLE_RATE_8000, Messages.SAMPLE_RATE_8000, SampleRate.SR_8000),
                Arguments.of(Command.SET_SAMPLE_RATE_16000, Messages.SAMPLE_RATE_16000, SampleRate.SR_16000)
            )
        }
        @ParameterizedTest
        @MethodSource("validSampleRateArguments")
        fun test_enableChannel(cmd:Command, msg:String, rate:SampleRate) {
            mock = SerialMocks.SimpleResponsive(cmd, msg)
            val cyton = Cyton(mock)
            cyton.sampleRate = rate
            assertEquals(cyton.sampleRate, rate)
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    internal class EnableChannelTest : SimpleTestSuite() {
        private fun enableChannelArguments(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1, Command.ENABLE_CHANNEL1),
                Arguments.of(2, Command.ENABLE_CHANNEL2),
                Arguments.of(3, Command.ENABLE_CHANNEL3),
                Arguments.of(4, Command.ENABLE_CHANNEL4),
                Arguments.of(5, Command.ENABLE_CHANNEL5),
                Arguments.of(6, Command.ENABLE_CHANNEL6),
                Arguments.of(7, Command.ENABLE_CHANNEL7),
                Arguments.of(8, Command.ENABLE_CHANNEL8),
                Arguments.of(9, Command.ENABLE_CHANNEL9),
                Arguments.of(10, Command.ENABLE_CHANNEL10),
                Arguments.of(11, Command.ENABLE_CHANNEL11),
                Arguments.of(12, Command.ENABLE_CHANNEL12),
                Arguments.of(13, Command.ENABLE_CHANNEL13),
                Arguments.of(14, Command.ENABLE_CHANNEL14),
                Arguments.of(15, Command.ENABLE_CHANNEL15),
                Arguments.of(16, Command.ENABLE_CHANNEL16)
            )
        }
        @ParameterizedTest
        @MethodSource("enableChannelArguments")
        fun test_enableChannel(index:Int, cmd:Command) {
            mock = SerialMocks.SimpleResponsive(cmd, "")
            val cyton = Cyton(mock)
            cyton.enableChannel(index)
        }

        private fun disableChannelArguments(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(1, Command.DISABLE_CHANNEL1),
                Arguments.of(2, Command.DISABLE_CHANNEL2),
                Arguments.of(3, Command.DISABLE_CHANNEL3),
                Arguments.of(4, Command.DISABLE_CHANNEL4),
                Arguments.of(5, Command.DISABLE_CHANNEL5),
                Arguments.of(6, Command.DISABLE_CHANNEL6),
                Arguments.of(7, Command.DISABLE_CHANNEL7),
                Arguments.of(8, Command.DISABLE_CHANNEL8),
                Arguments.of(9, Command.DISABLE_CHANNEL9),
                Arguments.of(10, Command.DISABLE_CHANNEL10),
                Arguments.of(11, Command.DISABLE_CHANNEL11),
                Arguments.of(12, Command.DISABLE_CHANNEL12),
                Arguments.of(13, Command.DISABLE_CHANNEL13),
                Arguments.of(14, Command.DISABLE_CHANNEL14),
                Arguments.of(15, Command.DISABLE_CHANNEL15),
                Arguments.of(16, Command.DISABLE_CHANNEL16)
            )
        }
        @ParameterizedTest
        @MethodSource("disableChannelArguments")
        fun test_disableChannel(index:Int, cmd:Command) {
            mock = SerialMocks.SimpleResponsive(cmd, "")
            val cyton = Cyton(mock)
            cyton.disableChannel(index)
        }
    }
}
