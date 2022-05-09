package featurea.android.simulator

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import featurea.android.*
import featurea.runtime.Dependency
import featurea.runtime.import
import featurea.runtime.buildRuntime

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
open class SimulatorActivity(val artifact: Dependency) : FeatureaActivity() {

    lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var layout: MainActivityLayout
    private val mainActivity: SimulatorActivity = this
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. launch
        buildRuntime {
            onInitContainer {
                provide(MainActivityProxy(mainActivity))
            }
            onInitModule { simulatorModule ->
                mainActivity.module = simulatorModule
            }
            SimulatorRuntime()
        }

        // 2. setup
        layout = DataBindingUtil.setContentView(this, R.layout.main_activity)
        setSupportActionBar(layout.toolbarId)
        appBarConfiguration = AppBarConfiguration.Builder(
            R.id.accountFragment,
            R.id.bundlesFragment,
            R.id.logsFragment,
            R.id.trashFragment,
            R.id.settingsFragment,
            R.id.aboutFragment
        ).setOpenableLayout(layout.drawerLayoutId).build()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(layout.navigationviewId, navController)

        val headerView = layout.navigationviewId.getHeaderView(0)
        val navHeader = headerView.findViewById<View>(R.id.navHeader)
        navHeader.setOnClickListener {
            mainActivity.navController.navigate(R.id.accountFragment)
            layout.drawerLayoutId.closeDrawer(Gravity.START)
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatusBarPreference()
        bundlesDir.mkdirs()
        logsDir.mkdirs()
        trashDir.mkdirs()
        cacheDir.mkdirs()
    }

    override fun onBackPressed() {
        val currentFragment: Fragment = getCurrentFragment()
        when (currentFragment) {
            is LoginFragment -> {
                super.onBackPressed()
            }
            is ApplicationFragment -> {
                val bundleLauncher: BundleLauncher = currentFragment.app.import()
                if (!bundleLauncher.isDone) return
                postDelayed(10) {
                    supportActionBar?.show()
                }
                postDelayed(410) {
                    super.onBackPressed()
                }
            }
            else -> {
                if (layout.drawerLayoutId.isDrawerOpen(Gravity.START)) {
                    layout.drawerLayoutId.closeDrawer(Gravity.START)
                } else {
                    super.onBackPressed()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

}
