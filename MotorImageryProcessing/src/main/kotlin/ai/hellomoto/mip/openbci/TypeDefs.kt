package ai.hellomoto.mip.openbci

import java.util.*

private fun b(command:String):ByteArray = command.toByteArray()

data class PacketData(
    val packetId:Int,
    val date: Date,
    val stopByte: Byte,
    val rawEegs:List<Int>,
    val auxs:List<Int>,
    var eegs:List<Float> = listOf()
)

enum class Command(val value:ByteArray) {
    // COMMON
    RESET_BOARD           (b("v")),
    QUERY_SAMPLE_RATE     (b("~~")),
    SET_SAMPLE_RATE_16000 (b("~0")),
    SET_SAMPLE_RATE_8000  (b("~1")),
    SET_SAMPLE_RATE_4000  (b("~2")),
    SET_SAMPLE_RATE_2000  (b("~3")),
    SET_SAMPLE_RATE_1000  (b("~4")),
    SET_SAMPLE_RATE_500   (b("~5")),
    SET_SAMPLE_RATE_250   (b("~6")),
    ATTACH_WIFI           (b("{")),
    DETACH_WIFI           (b("}")),
    QUERY_WIFI_STATUS     (b(":")),
    RESET_WIFI_STATUS     (b(";")),
    ENABLE_CHANNEL1       (b("!")),
    ENABLE_CHANNEL2       (b("@")),
    ENABLE_CHANNEL3       (b("#")),
    ENABLE_CHANNEL4       (b("$")),
    ENABLE_CHANNEL5       (b("%")),
    ENABLE_CHANNEL6       (b("^")),
    ENABLE_CHANNEL7       (b("&")),
    ENABLE_CHANNEL8       (b("*")),
    ENABLE_CHANNEL9       (b("Q")),
    ENABLE_CHANNEL10      (b("W")),
    ENABLE_CHANNEL11      (b("E")),
    ENABLE_CHANNEL12      (b("R")),
    ENABLE_CHANNEL13      (b("T")),
    ENABLE_CHANNEL14      (b("Y")),
    ENABLE_CHANNEL15      (b("U")),
    ENABLE_CHANNEL16      (b("I")),
    DISABLE_CHANNEL1      (b("1")),
    DISABLE_CHANNEL2      (b("2")),
    DISABLE_CHANNEL3      (b("3")),
    DISABLE_CHANNEL4      (b("4")),
    DISABLE_CHANNEL5      (b("5")),
    DISABLE_CHANNEL6      (b("6")),
    DISABLE_CHANNEL7      (b("7")),
    DISABLE_CHANNEL8      (b("8")),
    DISABLE_CHANNEL9      (b("q")),
    DISABLE_CHANNEL10     (b("w")),
    DISABLE_CHANNEL11     (b("e")),
    DISABLE_CHANNEL12     (b("r")),
    DISABLE_CHANNEL13     (b("t")),
    DISABLE_CHANNEL14     (b("y")),
    DISABLE_CHANNEL15     (b("u")),
    DISABLE_CHANNEL16     (b("i")),
    START_STREAMING       (b("b")),
    STOP_STREAMING        (b("s")),
    // Cyton Specific Commands
    QUERY_FIRMWARE_VERSION(b("V")),
    QUERY_BOARD_MODE      (b("//")),
    SET_BOARD_MODE_DEFAULT(b("/0")),
    SET_BOARD_MODE_DEBUG  (b("/1")),
    SET_BOARD_MODE_ANALOG (b("/2")),
    SET_BOARD_MODE_DIGITAL(b("/3")),
    SET_BOARD_MODE_MARKER (b("/4")),
    ATTACH_DAISY          (b("C")),
    DETACH_DAISY          (b("c")),
    ENABLE_TIMESTAMP      (b("<")),
    DISABLE_TIMESTAMP     (b(">")),
    RESET_CHANNELS        (b("d")),
    QUERY_DEFAULT_SETTINGS(b("D")),
}

enum class BoardMode(val command:Command) {
    DEFAULT(Command.SET_BOARD_MODE_DEFAULT),
    DEBUG  (Command.SET_BOARD_MODE_DEBUG),
    ANALOG (Command.SET_BOARD_MODE_ANALOG),
    DIGITAL(Command.SET_BOARD_MODE_DIGITAL),
    MARKER (Command.SET_BOARD_MODE_MARKER);
}

enum class SampleRate(val command:Command) {
    SR_16000(Command.SET_SAMPLE_RATE_16000),
    SR_8000(Command.SET_SAMPLE_RATE_8000),
    SR_4000(Command.SET_SAMPLE_RATE_4000),
    SR_2000(Command.SET_SAMPLE_RATE_2000),
    SR_1000(Command.SET_SAMPLE_RATE_1000),
    SR_500(Command.SET_SAMPLE_RATE_500),
    SR_250(Command.SET_SAMPLE_RATE_250);

    fun toLong():Long = when (this) {
        SR_16000 -> 16000
        SR_8000 -> 8000
        SR_4000 -> 4000
        SR_2000 -> 2000
        SR_1000 -> 1000
        SR_500 -> 500
        SR_250 -> 250
    }
}

data class ChannelConfig(
    val powerDown:PowerDown,
    val gainSet:GainSet,
    val inputTypeSet:InputTypeSet,
    val biasSet:BiasSet,
    val srb2Set:SRB2Set,
    val srb1Set:SRB1Set
) {
    enum class Channel(val command:Byte) {
        CHANNEL1(1),
        CHANNEL2(2),
        CHANNEL3(3),
        CHANNEL4(4),
        CHANNEL5(5),
        CHANNEL6(6),
        CHANNEL7(7),
        CHANNEL8(8),
        CHANNEL9(9),
        CHANNEL10(10),
        CHANNEL11(11),
        CHANNEL12(12),
        CHANNEL13(13),
        CHANNEL14(14),
        CHANNEL15(15),
        CHANNEL16(16);
        companion object {
            fun fromInt(channel:Int):Channel = when(channel) {
                1  -> Channel.CHANNEL1
                2  -> Channel.CHANNEL2
                3  -> Channel.CHANNEL3
                4  -> Channel.CHANNEL4
                5  -> Channel.CHANNEL5
                6  -> Channel.CHANNEL6
                7  -> Channel.CHANNEL7
                8  -> Channel.CHANNEL8
                9  -> Channel.CHANNEL9
                10 -> Channel.CHANNEL10
                11 -> Channel.CHANNEL11
                12 -> Channel.CHANNEL12
                13 -> Channel.CHANNEL13
                14 -> Channel.CHANNEL14
                15 -> Channel.CHANNEL15
                16 -> Channel.CHANNEL16
                else -> throw IllegalArgumentException("Channel value must be [1, 16]. Found: \"${channel}\"")
            }
        }
    }
    enum class PowerDown(val command:Byte) {
        ON(0),
        OFF(1);
        companion object {
            fun fromInt(value: Int): PowerDown = when (value.toInt()) {
                0 -> ON
                1 -> OFF
                else -> throw IllegalArgumentException("PowerDown value must be [0, 1]. Found: \"${value}\"")
            }
        }
    }
    enum class GainSet(val command:Byte) {
        GAIN1(0),
        GAIN2(1),
        GAIN4(2),
        GAIN6(3),
        GAIN8(4),
        GAIN12(5),
        GAIN24(6);
        companion object {
            fun fromInt(value:Int): GainSet = when(value.toInt()) {
                0 -> GAIN1
                1 -> GAIN2
                2 -> GAIN4
                3 -> GAIN6
                4 -> GAIN8
                5 -> GAIN12
                6 -> GAIN24
                else -> throw IllegalArgumentException("GainSet value must be [0, 6]. Found: \"${value}\"")
            }
        }
    }
    enum class InputTypeSet(val command:Byte) {
        ADSINPUT_NORMAL(0),
        ADSINADSINPUT_SHORTEDPUT_NORMAL(1),
        ADSINPUT_BIAS_MEAS(2),
        ADSINPUT_MVDD(3),
        ADSINPUT_TEMP(4),
        ADSINPUT_TESTSIG(5),
        ADSINPUT_BIAS_DRP(6),
        ADSINPUT_BIAS_DRN(7);

        companion object {
            fun fromInt(value: Int): InputTypeSet = when (value.toInt()) {
                0 -> ADSINPUT_NORMAL
                1 -> ADSINADSINPUT_SHORTEDPUT_NORMAL
                2 -> ADSINPUT_BIAS_MEAS
                3 -> ADSINPUT_MVDD
                4 -> ADSINPUT_TEMP
                5 -> ADSINPUT_TESTSIG
                6 -> ADSINPUT_BIAS_DRP
                7 -> ADSINPUT_BIAS_DRN
                else -> throw IllegalArgumentException("InputTypeSet value must be [0, 7]. Found: \"${value}\"")
            }
        }
    }
    enum class BiasSet(val command:Byte) {
        REMOVE(0),
        INCLUDE(1);
        companion object {
            fun fromInt(value: Int): BiasSet = when (value.toInt()) {
                0 -> REMOVE
                1 -> INCLUDE
                else -> throw IllegalArgumentException("BiasSet value must be [0, 7]. Found: \"${value}\"")
            }
        }
    }
    enum class SRB2Set(val command:Byte) {
        DISCONNECT(0),
        CONNECT(1);
        companion object {
            fun fromInt(value: Int): SRB2Set = when (value.toInt()) {
                0 -> DISCONNECT
                1 -> CONNECT
                else -> throw IllegalArgumentException("SRB2Set value must be [0, 7]. Found: \"${value}\"")
            }
        }
    }
    enum class SRB1Set(val command:Byte) {
        DISCONNECT(0),
        CONNECT(1);
        companion object {
            fun fromInt(value: Int): SRB1Set = when (value.toInt()) {
                0 -> DISCONNECT
                1 -> CONNECT
                else -> throw IllegalArgumentException("SRB1Set value must be [0, 7]. Found: \"${value}\"")
            }
        }
    }

    companion object {
        fun fromInts(powerDown:Int, gainSet:Int, inputTypeSet:Int, biasSet:Int, srb2Set:Int, srb1Set:Int):ChannelConfig {
            return ChannelConfig(
                PowerDown.fromInt(powerDown), GainSet.fromInt(gainSet), InputTypeSet.fromInt(inputTypeSet),
                BiasSet.fromInt(biasSet), SRB2Set.fromInt(srb2Set), SRB1Set.fromInt(srb1Set)
            )
        }
    }
}
