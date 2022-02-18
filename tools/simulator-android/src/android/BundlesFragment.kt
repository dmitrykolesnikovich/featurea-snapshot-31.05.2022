package featurea.android.simulator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import featurea.android.*
import featurea.runtime.import
import featurea.settings.SettingsService
import java.io.File
import java.util.*

class BundlesFragment : FeatureaFragment() {

    private val bundleFiles = mutableListOf<File>()
    private val fileWatcherThread: FileWatcherThread by lazy { import() }
    private val mainActivity: FeatureaActivity by lazy { import(MainActivityProxy) }
    private val simulator: Simulator by lazy { import() }
    private val settingsService: SettingsService by lazy { import() }

    private lateinit var binding: FragmentBundlesLayout

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fileWatcherThread.init(requireActivity().bundlesDir, "bundle") { newFiles ->
            if (bundleFiles.containsAll(newFiles) && newFiles.containsAll(bundleFiles)) return@init
            bundleFiles.clear()
            bundleFiles.addAll(newFiles)
            Collections.sort(bundleFiles, lastModifiedFileComparator)
            binding.bundlesListView.adapter?.notifyDataSetChanged()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bundles, container, false)
        binding.bundlesListView.layoutManager = LinearLayoutManager(requireContext())
        binding.bundlesListView.adapter = FilesAdapter(bundleFiles).apply {
            onClickListener = object : ItemClickListener<File> {
                override fun onClickItem(item: File) {
                    simulator.openBundle(item, false)
                }
            }
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

                override fun onItemRangeRemoved(index: Int, itemCount: Int) {
                    val bundleFile = bundleFiles[index]
                    val targetFile = File(bundleFile.absolutePath.replace("/Projects/", "/Trash/"))
                    targetFile.delete()
                    bundleFile.copyTo(targetFile)
                    bundleFile.delete()
                    binding.bundlesListView.adapter?.notifyDataSetChanged()
                }

                override fun onChanged() {
                    updateLayout()
                }

            })
        }.also {
            it.updateLayout()
        }

        binding.bundlesListView.setHasFixedSize(true)
        setupItemTouchHelper(requireActivity(), binding.bundlesListView)
        setUpAnimationDecoratorHelper(binding.bundlesListView)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val shouldLogin = !settingsService.containsKey(PREFERENCES_USER_EXISTS)
        if (shouldLogin) {
            findNavController().navigate(R.id.loginFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val shouldLogin = !settingsService.containsKey(PREFERENCES_USER_EXISTS)
        if (!shouldLogin) {
            if (settingsService["isFirstTimeCreated", false] && settingsService.containsKey("lastBundlePath")) {
                val lastBundlePath = settingsService["lastBundlePath", ""]
                simulator.openBundle(File(lastBundlePath), withSplash = true)
            }
            settingsService["isFirstTimeCreated"] = false
        }
    }

    override fun onResume() {
        super.onResume()
        mainActivity.supportActionBar?.title = "Projects"
    }

    /*internals*/

    private fun FilesAdapter.updateLayout() {
        binding.bundlesListView.isVisible = itemCount != 0
        binding.noDataMessage.isVisible = !binding.bundlesListView.isVisible
    }

}
