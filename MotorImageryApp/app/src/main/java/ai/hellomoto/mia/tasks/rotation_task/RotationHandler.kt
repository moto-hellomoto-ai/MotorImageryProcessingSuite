package ai.hellomoto.mia.tasks.rotation_task

import android.view.MotionEvent
import android.view.View


class RotationHandler(private val view: View) {
    private val centerX: Float
    private val centerY: Float
    private var coord0: Pair<Float, Float> = Pair(0f, 0f)
    private val width = kotlin.math.min(view.width, view.height)

    init {
        val loc: IntArray = intArrayOf(0, 0)
        view.getLocationOnScreen(loc)
        centerX = loc[0] + view.width / 2f
        centerY = loc[1] + view.height / 2f
    }

    fun init(event: MotionEvent) {
        this.coord0 = getRelativeCoord(event)
    }

    fun track(event: MotionEvent): Float {
        val coord1 = getRelativeCoord(event)
        val cross = this.coord0.first * coord1.second - this.coord0.second * coord1.first
        this.coord0 = coord1
        return cross
    }

    private fun getRelativeCoord(event: MotionEvent): Pair<Float, Float> {
        val x = 2f * (event.rawX - centerX) / width
        val y = 2f * (event.rawY - centerY) / width
        /*
        Log.d("", "(%f, %f) <- ((%f - %f) / %d, (%f - %f) / %d)".format(
            x, y, event.rawX, centerX, view.width, event.rawY, centerY, view.height))
         */
        return Pair(x, y)
    }
}
