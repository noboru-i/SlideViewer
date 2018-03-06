package hm.orz.chaos114.android.slideviewer.widget

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View

class BottomBarBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<View>(context, attrs) {

    private var defaultDependencyTop = -1

    override fun layoutDependsOn(parent: CoordinatorLayout?, bottomBar: View?, dependency: View?): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, bottomBar: View, dependency: View): Boolean {
        if (defaultDependencyTop == -1) {
            defaultDependencyTop = dependency.top
        }
        ViewCompat.setTranslationY(bottomBar, (-dependency.top + defaultDependencyTop).toFloat())
        return true
    }
}
