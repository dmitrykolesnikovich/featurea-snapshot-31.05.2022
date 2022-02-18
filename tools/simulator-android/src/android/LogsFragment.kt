package featurea.android.simulator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import featurea.android.*
import featurea.runtime.import
import featurea.android.FeatureaFragment
import java.io.File
import java.util.*

class LogsFragment : FeatureaFragment() {

    private val fileWatcherThread: FileWatcherThread by lazy { import() }
    private val mainActivity: FeatureaActivity by lazy { import(MainActivityProxy) }

    private lateinit var binding: FragmentLogsLayout
    private val logFiles = mutableListOf<File>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fileWatcherThread.init(requireActivity().logsDir, "log") { newFiles ->
            if (logFiles.containsAll(newFiles) && newFiles.containsAll(logFiles)) return@init
            logFiles.clear()
            logFiles.addAll(newFiles)
            Collections.sort(logFiles, lastModifiedFileComparator)
            binding.logsListView.adapter?.notifyDataSetChanged()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = inflater.inflate<FragmentLogsLayout>(R.layout.fragment_logs, container)
        binding.logsListView.layoutManager = LinearLayoutManager(requireContext())
        binding.logsListView.adapter = FilesAdapter(logFiles).apply {
            onClickListener = object : ItemClickListener<File> {
                override fun onClickItem(logFile: File) {
                    openLogFile(logFile)
                }
            }
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeRemoved(index: Int, itemCount: Int) {
                    val logFile = logFiles[index]
                    logFile.delete()
                    binding.logsListView.adapter?.notifyDataSetChanged()
                }

                override fun onChanged() {
                    updateLayout()
                }
            })
        }.also {
            it.updateLayout()
        }

        binding.logsListView.setHasFixedSize(true)
        setupItemTouchHelper(requireActivity(), binding.logsListView)
        setUpAnimationDecoratorHelper(binding.logsListView)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mainActivity.supportActionBar?.title = "Logs"
    }

    /*internals*/

    private fun FilesAdapter.updateLayout() {
        if (itemCount != 0) {
            binding.logsListView.visibility = View.VISIBLE
            binding.noDataMessage.visibility = View.GONE
        } else {
            binding.logsListView.visibility = View.GONE
            binding.noDataMessage.visibility = View.VISIBLE
        }
    }

    private fun openLogFile(file: File) {
        findNavController().navigate(R.id.action_logsFragment_to_viewLogFragment, bundleOf("filePath" to file.absolutePath))
    }

}
