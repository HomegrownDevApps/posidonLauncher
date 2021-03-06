package posidon.launcher.customizations

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import posidon.launcher.BuildConfig
import posidon.launcher.R
import posidon.launcher.tools.Loader
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Tools
import posidon.launcher.tools.applyFontSetting

class About : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFontSetting()
        setContentView(R.layout.custom_about)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        findViewById<View>(R.id.settings).setPadding(0, 0, 0, Tools.navbarHeight)
        val description = findViewById<TextView>(R.id.appname)
        description.text = getString(R.string.app_name) + " - " + BuildConfig.VERSION_NAME
        Loader.Text("https://posidon.io/launcher/contributors/pictureUrls") {
            var leoLink: String? = null
            var sajidShaikLink: String? = null
            for (line in it.split('\n')) {
                if (line.startsWith("Leo: "))
                    leoLink = line.substring(5)
                else if (line.startsWith("SajidShaik: "))
                    sajidShaikLink = line.substring(12)
            }
            leoLink?.let { link -> Loader.Bitmap(link) { img -> findViewById<ImageView>(R.id.leoProfile).setImageBitmap(img) }.execute() }
            sajidShaikLink?.let { link -> Loader.Bitmap(link) { img -> findViewById<ImageView>(R.id.sajidShaikProfile).setImageBitmap(img) }.execute() }
        }.execute()

        findViewById<View>(R.id.maincard).setOnLongClickListener {
            if (Settings["dev:enabled", false]) {
                Settings["dev:enabled"] = false
                Toast.makeText(this@About, "Developer mode disabled", Toast.LENGTH_SHORT).show()
            } else {
                Settings["dev:enabled"] = true
                Toast.makeText(this@About, "Developer mode enabled", Toast.LENGTH_SHORT).show()
            }
            true
        }

        try { findViewById<ImageView>(R.id.img).setImageResource(R.drawable.logo_wide) } catch (ignore: Exception) {}
    }

    fun openTwitter(v: View) {
        val uri = Uri.parse("https://twitter.com/posidon")
        val i = Intent(Intent.ACTION_VIEW, uri)
        startActivity(i, ActivityOptions.makeCustomAnimation(this, R.anim.slideup, R.anim.slidedown).toBundle())
    }

    fun openTelegram(v: View) { try {
        val uri = Uri.parse("https://t.me/posidonlauncher")
        val i = Intent(Intent.ACTION_VIEW, uri)
        startActivity(i, ActivityOptions.makeCustomAnimation(this, R.anim.slideup, R.anim.slidedown).toBundle())
    } catch (ignore: Exception) {} }

    fun openGitHub(v: View) { try {
        val uri = Uri.parse("https://github.com/leoxshn/posidonLauncher")
        val i = Intent(Intent.ACTION_VIEW, uri)
        startActivity(i, ActivityOptions.makeCustomAnimation(this, R.anim.slideup, R.anim.slidedown).toBundle())
    } catch (ignore: Exception) {} }

    fun openWebsite(v: View) {
        val uri = Uri.parse("https://posidon.io/launcher")
        val i = Intent(Intent.ACTION_VIEW, uri)
        startActivity(i, ActivityOptions.makeCustomAnimation(this, R.anim.slideup, R.anim.slidedown).toBundle())
    }
}