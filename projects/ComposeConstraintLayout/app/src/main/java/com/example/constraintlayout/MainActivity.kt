package com.example.constraintlayout

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.constraintlayout.compose.*

class MainActivity : AppCompatActivity() {
    private var mFrameLayout: FrameLayout? = null
    private var composeNum = 13
    private var MAX = 13

    private fun show(com: ComposeView) {
        com.setContent() {
            when (composeNum) {
                0 -> ScreenExample()
                1 -> ScreenExample()
                2 -> ScreenExample2()
                3 -> ScreenExample3()
                4 -> ScreenExample4()
                5 -> ScreenExample5()
                6 -> ScreenExample6()
                7 -> ScreenExample7()
                8 -> ScreenExample8()
                9 -> ScreenExample9()
                10 -> ScreenExample10()
                11 -> ScreenExample11()
                12 -> ScreenExample12()
                13 -> ScreenExample13()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mFrameLayout = findViewById<FrameLayout>(R.id.frame)
        setCompose();
    }

    fun setCompose() {
        if (mFrameLayout!!.childCount > 0) {
            mFrameLayout!!.removeAllViews()
        }
        title = " example " + composeNum;
        findViewById<TextView>(R.id.layoutName).text = " example " + composeNum;
        var com = ComposeView(this);
        mFrameLayout!!.addView(com)
        show(com)
    }

    fun prev(view: View) {
        composeNum = (composeNum + MAX - 1) % MAX
        setCompose()
    }

    fun next(view: View) {
        composeNum = (composeNum + 1) % MAX
        setCompose();
    }
}
