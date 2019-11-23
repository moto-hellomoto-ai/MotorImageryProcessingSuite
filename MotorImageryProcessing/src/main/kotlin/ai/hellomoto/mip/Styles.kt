package ai.hellomoto.mip

import javafx.scene.paint.Color
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        const val DEBUG = true;
        val debug1 by cssclass()
        val debug2 by cssclass()
        val debug3 by cssclass()
    }

    init {
        chartSeriesLine {
            strokeWidth = 1.8.px
        }
        if (DEBUG) {
            debug1 {
                borderColor += box(Color.RED)
            }
            debug2 {
                borderColor += box(Color.GREEN)
            }
            debug3 {
                borderColor += box(Color.BLUE)
            }
        }
    }
}