package ai.hellomoto.mip.openbci

object Messages {
        val CYTON_8BIT_INFO = """OpenBCI V3 8bit Board
Setting ADS1299 Channel Values
ADS1299 Device ID: 0x3E
LIS3DH Device ID: 0x33
$$$"""

        val CYTON_V1_INFO = """OpenBCI V3 16 channel
ADS1299 Device ID: 0x3E
LIS3DH Device ID: 0x33
$$$"""

        val CYTON_V2_INFO = """OpenBCI V3 8-16 channel
ADS1299 Device ID: 0x3E
LIS3DH Device ID: 0x33
Firmware: v2.0.0
$$$"""

        val CYTON_V3_INFO = """OpenBCI V3 8-16 channel
On Board ADS1299 Device ID: 0x3E
LIS3DH Device ID: 0x33
Firmware: v3.1.1
$$$"""

        val CYTON_V3_WITH_DAISY_INFO = """OpenBCI V3 8-16 channel
On Board ADS1299 Device ID: 0x3E
On Daisy ADS1299 Device ID: 0x3E
LIS3DH Device ID: 0x33
Firmware: v3.1.1
$$$"""

        val GANGLION_V2_INFO = """OpenBCI Ganglion v2.0.0
LIS2DH ID: 0x33
MCP3912 CONFIG_1: 0xXX
$$$"""

        val RESET_CHANNEL = "updating channel settings to default$$$"
        val DEFAULT_CHANNEL = "060110$$$"

        val STREAM_STARTED = "Stream started$$$"
        val STREAM_STOPPED = "Stream stopped$$$"

        val DAISY_ALREADY_ATTACHED = "16$$$"
        val DAISY_ATTACHED = "daisy attached16$$$"
        val NO_DAISY_TO_ATTACH = "no daisy to attach!8$$$"
        val DAISY_REMOVED = "daisy removed$$$"

        val TIMESTAMP_ON = "Time stamp ON$$$"
        val TIMESTAMP_OFF = "Time stamp OFF$$$"

        val SAMPLE_RATE_250 = "Success: Sample rate is 250Hz$$$"
        val SAMPLE_RATE_500 = "Success: Sample rate is 500Hz$$$"
        val SAMPLE_RATE_1000 = "Success: Sample rate is 1000Hz$$$"
        val SAMPLE_RATE_2000 = "Success: Sample rate is 2000Hz$$$"
        val SAMPLE_RATE_4000 = "Success: Sample rate is 4000Hz$$$"
        val SAMPLE_RATE_8000 = "Success: Sample rate is 8000Hz$$$"
        val SAMPLE_RATE_16000 = "Success: Sample rate is 16000Hz$$$"

        val BOARD_MODE_DEFAULT = "Success: default$$$"
        val BOARD_MODE_DEBUG = "Success: debug$$$"
        val BOARD_MODE_ANALOG = "Success: analog$$$"
        val BOARD_MODE_DIGITAL = "Success: digital$$$"
        val BOARD_MODE_MARKER = "Success: marker$$$"

        val WIFI_ATTACH_SUCCESS = "Success: Wifi attached$$$"
        val WIFI_ATTACH_FAILURE = "Failure: Wifi not attached$$$"
        val WIFI_REMOVE_SUCCESS = "Success: Wifi removed$$$"
        val WIFI_REMOVE_FAILURE = "Failure: Wifi not removed$$$"
        val WIFI_PRESENT = "Wifi present$$$"
        val WIFI_NOT_PRESENT = "Wifi not present, send { to attach the shield$$$"
        val WIFI_RESET = "Wifi soft reset$$$"

        val SET_CHANNEL_1 = "Success: Channel set for 1$$$"
        val SET_CHANNEL_2 = "Success: Channel set for 2$$$"
        val SET_CHANNEL_3 = "Success: Channel set for 3$$$"
        val SET_CHANNEL_4 = "Success: Channel set for 4$$$"
        val SET_CHANNEL_5 = "Success: Channel set for 5$$$"
        val SET_CHANNEL_6 = "Success: Channel set for 6$$$"
        val SET_CHANNEL_7 = "Success: Channel set for 7$$$"
        val SET_CHANNEL_8 = "Success: Channel set for 8$$$"
        val SET_CHANNEL_9 = "Success: Channel set for 9$$$"
        val SET_CHANNEL_10 = "Success: Channel set for 10$$$"
        val SET_CHANNEL_11 = "Success: Channel set for 11$$$"
        val SET_CHANNEL_12 = "Success: Channel set for 12$$$"
        val SET_CHANNEL_13 = "Success: Channel set for 13$$$"
        val SET_CHANNEL_14 = "Success: Channel set for 14$$$"
        val SET_CHANNEL_15 = "Success: Channel set for 15$$$"
        val SET_CHANNEL_16 = "Success: Channel set for 16$$$"
}