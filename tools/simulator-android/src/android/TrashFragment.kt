package featurea.android.simulator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import featurea.android.*
import featurea.runtime.import
import java.io.File
import java.util.*

class TrashFragment : FeatureaFragment() {

    private val fileWatcherThread: FileWatcherThread by lazy { import() }
    private val mainActivity: FeatureaActivity by lazy { import(MainActivityProxy) }

    private lateinit var binding: FragmentTrashLayout
    private val bundleFiles = mutableListOf<File>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fileWatcherThread.init(mainActivity.trashDir, "bundle") { newFiles ->
            if (bundleFiles.containsAll(newFiles) && newFiles.containsAll(bundleFiles)) return@init
            bundleFiles.clear()
            bundleFiles.addAll(newFiles)
            Collections.sort(bundleFiles, lastModifiedFileComparator)
            binding.bundlesListView.adapter?.notifyDataSetChanged()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trash, container, false)
        binding.bundlesListView.layoutManager = LinearLayoutManager(requireContext())
        binding.bundlesListView.adapter = FilesAdapter(bundleFiles).apply {
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeRemoved(index: Int, itemCount: Int) {
                    val bundleFile = bundleFiles[index]
                    AlertDialog.Builder(mainActivity).setTitle("Delete ${bundleFiles[index].name}?")
                        .setPositiveButton("OK") { dialog, which ->
                            bundleFile.delete()
                            binding.bundlesListView.adapter?.notifyDataSetChanged()
                        }
                        .setNegativeButton(android.R.string.no, null)
                        .show()
                }

                override fun onChanged() = updateLayout()
            })
        }.also {
            it.onLongClickListener = object : ItemClickListener<File> {
                override fun onClickItem(file: File) {
                    AlertDialog.Builder(mainActivity).setItems(arrayOf("Restore")) { dialog, item ->
                        when (item) {
                            0 -> {
                                bundleFiles.remove(file)
                                val targetFile = File(file.absolutePath.replace("/Trash/", "/Projects/"))
                                targetFile.delete()
                                file.copyTo(targetFile)
                                file.delete()
                                binding.bundlesListView.adapter?.notifyDataSetChanged()
                            }
                        }
                    }.show()
                }
            }
            it.updateLayout()
        }

        binding.bundlesListView.setHasFixedSize(true)
        setupItemTouchHelper(requireActivity(), binding.bundlesListView)
        setUpAnimationDecoratorHelper(binding.bundlesListView)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mainActivity.supportActionBar?.title = "Trash"
    }

    /*internals*/

    private fun FilesAdapter.updateLayout() {
        if (itemCount != 0) {
            binding.bundlesListView.visibility = View.VISIBLE
            binding.noDataMessage.visibility = View.GONE
        } else {
            binding.bundlesListView.visibility = View.GONE
            binding.noDataMessage.visibility = View.VISIBLE
        }
    }

}