package gyunggi.money.gyunggimoney

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager {
    companion object {
        private var sp: SharedPreferences? = null
        private var editor: SharedPreferences.Editor? = null

        private fun getInstance(context: Context): SharedPreferences {
            synchronized(this) {
                sp = context.getSharedPreferences("userId", Context.MODE_PRIVATE)
                return sp!!
            }
        }

        fun putInt(context: Context, key: String, value: Int) {
            editor = getInstance(context).edit()
            editor!!.putInt(key, value)
            editor!!.commit()
        }

        fun getInt(context: Context, key: String, default: Int): Int {
            return getInstance(context).getInt(key, default)
        }
    }
}