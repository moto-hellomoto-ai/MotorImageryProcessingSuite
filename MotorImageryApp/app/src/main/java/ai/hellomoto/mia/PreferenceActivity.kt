package ai.hellomoto.mia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen

class PreferenceActivity :
    AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.preference_layout, PreferenceFragment(), "preference_root")
            .commit()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Settings
    ///////////////////////////////////////////////////////////////////////////
    /* This will enable preference subscreen for androidx.preference 1.1.0.
        In future version of preference library, this might not be necessary.
     */
    override fun onPreferenceStartScreen(
        preferenceFragmentCompat: PreferenceFragmentCompat,
        preferenceScreen: PreferenceScreen
    ): Boolean {
        val fragment = PreferenceFragment()
        val args = Bundle()
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.key)
        fragment.arguments = args
        supportFragmentManager.beginTransaction()
            .replace(R.id.preference_layout, fragment, preferenceScreen.key)
            .addToBackStack(preferenceScreen.key)
            .commit()
        return true
    }
}
