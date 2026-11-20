package com.example.soundbutton

import android.app.Activity
import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var soundPool: SoundPool
    private var soundId = 0
    private var loaded = false
    private var volume = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        volume = getSharedPreferences("prefs", MODE_PRIVATE).getFloat("vol", 1.0f)

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(16)
            .setAudioAttributes(attrs)
            .build()
        soundPool.setOnLoadCompleteListener { _, _, status -> loaded = (status == 0) }
        soundId = soundPool.load(this, R.raw.sound, 1)

        val dp = resources.displayMetrics.density

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding((24 * dp).toInt(), (24 * dp).toInt(), (24 * dp).toInt(), (24 * dp).toInt())
        }

        val buttonArea = FrameLayout(this)
        root.addView(buttonArea, LinearLayout.LayoutParams(-1, 0, 1f))

        val button = View(this).apply {
            setBackgroundColor(Color.parseColor("#FFD600"))
        }
        val size = (240 * dp).toInt()
        buttonArea.addView(button, FrameLayout.LayoutParams(size, size, Gravity.CENTER))

        // التشغيل بمجرد اللمس (ACTION_DOWN) عشان مفيش أي تأخير، ويدعم الضغط السريع المتكرر
        button.setOnTouchListener { v, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    if (loaded) soundPool.play(soundId, volume, volume, 1, 0, 1f)
                    v.alpha = 0.7f
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.alpha = 1f
            }
            true
        }

        val label = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 18f
            gravity = Gravity.CENTER
            text = "مستوى الصوت: ${(volume * 100).toInt()}%"
        }
        root.addView(label, LinearLayout.LayoutParams(-1, -2))

        val seek = SeekBar(this).apply {
            max = 100
            progress = (volume * 100).toInt()
            setPadding((8 * dp).toInt(), (16 * dp).toInt(), (8 * dp).toInt(), (24 * dp).toInt())
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) {
                    volume = p / 100f
                    label.text = "مستوى الصوت: $p%"
                }
                override fun onStartTrackingTouch(s: SeekBar) {}
                override fun onStopTrackingTouch(s: SeekBar) {
                    getSharedPreferences("prefs", MODE_PRIVATE).edit().putFloat("vol", volume).apply()
                }
            })
        }
        root.addView(seek, LinearLayout.LayoutParams(-1, -2))

        setContentView(root)
    }

    override fun onDestroy() {
        soundPool.release()
        super.onDestroy()
    }
}
