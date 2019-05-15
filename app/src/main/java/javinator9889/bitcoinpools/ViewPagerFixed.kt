package javinator9889.bitcoinpools

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent

import androidx.viewpager.widget.ViewPager

/**
 * Created by Javinator9889 on 07/08/2018. Based on: https://github.com/chrisbanes/PhotoView/issues/31
 */
class ViewPagerFixed : ViewPager {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
            Log.e("ViewPagerFX", "Error handling \"onTouchEvent\". Full trace: " + ex.message)
        }

        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }

        return false
    }
}
