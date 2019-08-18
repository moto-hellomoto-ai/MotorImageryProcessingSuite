package ai.hellomoto.mia

import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Patterns
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

typealias TextValidator = (String) -> Boolean

class PreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        var key: String

        // Common
        key = "host_addr"
        setSummaryProvider(key, "Address of host machine:")
        setValidator(key) { newVal: String -> Patterns.IP_ADDRESS.matcher(newVal).matches() }
        key = "host_port"
        setSummaryProvider(key, "Port of host machine:")
        setTextType(key, InputType.TYPE_CLASS_NUMBER)

        // Rotation Task
        key = "rotation_speed"
        setSummaryProvider(key, "Rotation Speed:")
        setTextType(key, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        setValidator(key) { newVal: String -> nonZeroFloat(newVal) }
        key = "rotation_decay"
        setSummaryProvider(key, "Rotation Decay:")
        setTextType(key, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        setValidator(key) { newVal: String -> nonZeroFloat(newVal) and lessThan(newVal, 1f) }

    }

    companion object {
        fun nonZeroFloat(text: String): Boolean {
            return try {
                (text.toFloat() > 0f)
            } catch (e: NumberFormatException) {
                false
            }
        }

        fun lessThan(text: String, target: Float): Boolean {
            return try {
                (text.toFloat() < target)
            } catch (e: NumberFormatException) {
                false
            }
        }
    }

    private fun setTextType(key: String, inputType: Int) {
        val preference: EditTextPreference? = findPreference(key)
        preference?.setOnBindEditTextListener { pref ->
            pref.inputType = inputType
        }
    }

    private fun setSummaryProvider(key: String, prefix: String) {
        val preference: EditTextPreference? = findPreference(key)
        preference?.summaryProvider = Preference.SummaryProvider<EditTextPreference> { pref ->
            val text = if (TextUtils.isEmpty(pref.text)) "Not set" else pref.text
            prefix + text
        }
    }

    private fun setValidator(key: String, validator: TextValidator) {
        val preference: EditTextPreference? = findPreference(key)
        preference?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            validator(newValue as String)
        }
    }
}
