package ai.hellomoto.mia.tasks.rotation_task

import ai.hellomoto.mia.R
import ai.hellomoto.mia.tasks.TCPClient
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import org.jetbrains.anko.doAsync
import kotlin.math.abs

class RotationFragment : Fragment() {
    private var mVelocity: Float = 0f
    private var mUnitRotationSpeed: Float = 30f
    private var mVelocityDecay: Float = 0.95f

    private var mStream: Boolean = false
    private var mClient: TCPClient? = null
    private var mAddress: String = ""
    private var mPort: Int = 0

    private var mHandler: Handler = Handler(Looper.getMainLooper())
    private lateinit var mImage: ImageView
    private lateinit var mRotation: RotationHandler
    private lateinit var mUIUpdater: Scheduler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rotation_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mImage = view.findViewById(R.id.rotary_image)
        mImage.viewTreeObserver.addOnGlobalLayoutListener { mRotation = RotationHandler(mImage) }
        view.setOnTouchListener { v, e -> onTouch(v, e) }
    }

    private fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mRotation.init(event)
            }
            MotionEvent.ACTION_MOVE -> {
                mVelocity += mUnitRotationSpeed * mRotation.track(event)
                mImage.rotation += mVelocity
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // mVelocity += (mRotationTracker?.onUp(event) ?: 0f)
            }
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        loadSettings()
        initUI()
        startUIUpdater()
    }

    override fun onStop() {
        super.onStop()
        deinitUI()
        stopUIUpdater()
    }

    // Load configuration
    private fun loadSettings() {
        val pref = PreferenceManager.getDefaultSharedPreferences(activity as AppCompatActivity)
        mUnitRotationSpeed = pref.getString("rotation_speed", "30")?.toFloat() ?: 30f
        mVelocityDecay = pref.getString("rotation_decay", "0.95")?.toFloat() ?: 0.95f
        mStream = pref.getBoolean("streaming", false)

        if (!mStream) {
            showToast("Streaming is OFF.", Toast.LENGTH_SHORT)
        }
        mAddress = pref.getString("host_addr", "") ?: ""
        mPort = pref.getString("host_port", "59898")?.toInt() ?: 59898
    }

    // Enter Fullscreen and hide Toolbar
    private fun initUI() {
        val act = (activity as AppCompatActivity)
        act.supportActionBar!!.hide()
        act.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Exit Fullscreen and show Toolbar
    private fun deinitUI() {
        val act = (activity as AppCompatActivity)
        act.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        act.supportActionBar!!.show()
    }

    //
    private fun updateUI() {
        mVelocity *= mVelocityDecay
        // Log.d("", "%f".format(mVelocity))
        mHandler.post {
            if (abs(mVelocity) > 0.001) {
                mImage.rotation += mVelocity
            }
        }
        if (mStream) {
            try {
                mClient?.sendMessage("%f".format(mVelocity))
            } catch (e: Exception) {
                Log.e("MIA", "exception", e)
                mStream = false
                mClient = null
                showToast("Network Error. Stream stopped.")
            }
        }
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
        mHandler.post {
            val toast = Toast.makeText(activity, message, duration)
            toast.show()
        }
    }

    private fun startUIUpdater() {
        if (mStream) {
            doAsync {
                try {
                    mClient = TCPClient(mAddress, mPort)
                    showToast("Streaming started: $mAddress:$mPort.")
                } catch (e: Exception) {
                    Log.e("MIA", "exception", e)
                    mStream = false
                    showToast("Network Error. Stream stopped.")
                }
            }
        }
        mUIUpdater = Scheduler({ updateUI() }, 33)
    }

    private fun stopUIUpdater() {
        this.mUIUpdater.stop()
        this.mClient?.close()
        this.mClient = null
    }
}
