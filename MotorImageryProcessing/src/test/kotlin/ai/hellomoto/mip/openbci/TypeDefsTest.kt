package ai.hellomoto.mip.openbci

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import java.util.stream.Stream

internal class TypeDefsTest {
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    internal class SampleRateTest {
        private fun validSampleRatePatterns(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(SampleRate.SR_250, "Success: Sample rate is 250Hz$$$"),
                Arguments.of(SampleRate.SR_500, "Success: Sample rate is 500Hz$$$"),
                Arguments.of(SampleRate.SR_1000, "Success: Sample rate is 1000Hz$$$"),
                Arguments.of(SampleRate.SR_2000, "Success: Sample rate is 2000Hz$$$"),
                Arguments.of(SampleRate.SR_4000, "Success: Sample rate is 4000Hz$$$"),
                Arguments.of(SampleRate.SR_8000, "Success: Sample rate is 8000Hz$$$"),
                Arguments.of(SampleRate.SR_16000, "Success: Sample rate is 16000Hz$$$")
            )
        }

        @ParameterizedTest
        @MethodSource("validSampleRatePatterns")
        fun test_SampleRateFromMessage_success(rate:SampleRate, msg:String) {
            val actual = msg.toSampleRate()
            assertEquals(rate, actual)
        }
        @Test
        fun test_SampleRateFromMessage_failure() {
            assertNull("36Hz".toSampleRate())
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class BoardModeTest {
        private fun validBoardModePatterns():Stream<Arguments> {
            return Stream.of(
                Arguments.of(BoardMode.DEFAULT, "Success: default$$$"),
                Arguments.of(BoardMode.DEBUG, "Success: debug$$$"),
                Arguments.of(BoardMode.ANALOG, "Success: analog$$$"),
                Arguments.of(BoardMode.DIGITAL, "Success: digital$$$"),
                Arguments.of(BoardMode.MARKER, "Success: marker$$$")
            )
        }
        @ParameterizedTest
        @MethodSource("validBoardModePatterns")
        fun test_BoardModeFromMessage_success(mode:BoardMode, msg:String) {
            assertEquals(mode, msg.toBoardMode())
        }
        @Test
        fun test_BoardModeFromMessage_failure() {
            assertNull("Success: unknown$$$".toBoardMode())
        }
    }
}
