import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.videodownloader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter

    private val formatMap = mapOf(
        "Best (Video + Audio)" to "best",
        "Audio Only (MP3)" to "bestaudio",
        "Data Saver (Worst)" to "worst"
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Notifications disabled. App will still download.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askForPermissions()
        setupUI()
        setupObservers()
    }

    private fun askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun setupUI() {
        val formats = formatMap.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, formats)
        binding.dropdownFormat.setAdapter(adapter)
        binding.dropdownFormat.setText(formats[0], false)

        historyAdapter = HistoryAdapter(mutableListOf())
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = historyAdapter

        binding.btnPaste.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip()) {
                val text = clipboard.primaryClip?.getItemAt(0)?.text
                if (!text.isNullOrEmpty()) {
                    binding.etUrl.setText(text)
                }
            }
        }

        binding.btnDownload.setOnClickListener {
            val url = binding.etUrl.text.toString().trim()
            if (url.isEmpty()) {
                binding.inputLayoutUrl.error = "Please enter a URL"
                return@setOnClickListener
            }
            binding.inputLayoutUrl.error = null

            val selectedFriendlyName = binding.dropdownFormat.text.toString()
            val formatCode = formatMap[selectedFriendlyName] ?: "best"

            startDownload(url, formatCode)
            binding.etUrl.text?.clear()
        }
    }

    private fun setupObservers() {
        viewModel.historyList.observe(this) { items ->
            historyAdapter.updateItems(items)
        }
    }

    private fun startDownload(url: String, formatCode: String) {
        val inputData = Data.Builder()
            .putString("url", url)
            .putString("format", formatCode)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .build()

        viewModel.addDownload(workRequest.id, url)

        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(workRequest)

        workManager.getWorkInfoByIdLiveData(workRequest.id).observe(this) { workInfo ->
            if (workInfo != null) {
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> {
                        viewModel.updateStatus(workRequest.id, "Queued")
                    }
                    WorkInfo.State.RUNNING -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvProgressText.visibility = View.VISIBLE
                        
                        val progress = workInfo.progress.getInt("progress", 0)
                        binding.progressBar.progress = progress
                        binding.tvProgressText.text = "$progress%"
                        viewModel.updateStatus(workRequest.id, "Downloading", progress)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        hideProgress()
                        viewModel.updateStatus(workRequest.id, "Completed ✅")
                        Toast.makeText(this, "Download finished!", Toast.LENGTH_SHORT).show()
                    }
                    WorkInfo.State.FAILED -> {
                        hideProgress()
                        viewModel.updateStatus(workRequest.id, "Failed ❌")
                        val errorMsg = workInfo.outputData.getString("error")
                        Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                    WorkInfo.State.CANCELLED -> {
                        hideProgress()
                        viewModel.updateStatus(workRequest.id, "Cancelled")
                    }
                    else -> {}
                }
            }
        }
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.tvProgressText.visibility = View.INVISIBLE
        binding.progressBar.progress = 0
    }
}
