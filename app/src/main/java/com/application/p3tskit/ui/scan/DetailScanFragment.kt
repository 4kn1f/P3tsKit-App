package com.application.p3tskit.ui.scan

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.application.p3tskit.R
import com.application.p3tskit.data.pref.AuthPreferences
import com.application.p3tskit.data.remote.repository.DiagnoseRepository
import com.application.p3tskit.data.remote.response.ModelScanResponse
import com.bumptech.glide.Glide
import com.google.gson.Gson

class DetailScanFragment : Fragment() {

    private lateinit var predictedClassTextView: TextView
    private lateinit var diseaseInfoTextView: TextView
    private lateinit var symptomsTextView: TextView
    private lateinit var treatmentTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var sourceTextView: TextView
    private lateinit var noteTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: DetailScanViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detail_scan_page, container, false)

        predictedClassTextView = view.findViewById(R.id.tv_predicted_class)
        diseaseInfoTextView = view.findViewById(R.id.tv_result_description)
        symptomsTextView = view.findViewById(R.id.symptoms)
        treatmentTextView = view.findViewById(R.id.tv_treatment)
        imageView = view.findViewById(R.id.result_image)
        sourceTextView = view.findViewById(R.id.tv_source)
        noteTextView = view.findViewById(R.id.tv_note)
        progressBar = view.findViewById(R.id.progress_bar)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = DetailScanViewModelFactory(
            DiagnoseRepository.getInstance(requireContext()),
            AuthPreferences.getInstance(requireContext()),
            requireActivity().application
        )
        viewModel = ViewModelProvider(this, factory)[DetailScanViewModel::class.java]

        progressBar.visibility = View.VISIBLE

        viewModel.scanResult.observe(viewLifecycleOwner) { result ->
            progressBar.visibility = View.GONE
            result?.let {
                if (it.predictedClass != null) {
                    updateUIWithScanResult(it)
                } else {
                    Log.e("DetailScanFragment", "No disease detected.")
                }
            } ?: run {
                Log.e("DetailScanFragment", "Scan result is null")
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            progressBar.visibility = View.GONE
            if (!error.isNullOrEmpty()) {
                Log.e("DetailScanFragment", "Error: $error")
            }
        }

        val scanResultJson = arguments?.getString("scan_result_json")
        val imageUri: Uri? = arguments?.getParcelable("image_uri")

        if (scanResultJson != null) {
            val scanResult = Gson().fromJson(scanResultJson, ModelScanResponse::class.java)
            updateUIWithScanResult(scanResult)
            progressBar.visibility = View.GONE
        } else if (imageUri != null) {
            viewModel.analyzeImage(imageUri)
        }

        imageUri?.let { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(imageView)
        }
    }
    private fun updateUIWithScanResult(result: ModelScanResponse) {
        Log.d("DetailScanFragment", "Received result: $result")

        predictedClassTextView.text =
            getString(R.string.diagnosis, result.predictedClass ?: "Not available")

        if (result.diseaseInfo != null) {
            val diseaseInfo = result.diseaseInfo

            diseaseInfoTextView.text =
                getString(R.string.descriptions, diseaseInfo.description ?: "Not Available")

            val symptoms = diseaseInfo.symptoms.takeIf { it.isNotEmpty() }?.joinToString("\n") ?: "Not Available"
            symptomsTextView.text = "Symptoms: $symptoms"

            val treatment = diseaseInfo.treatment.takeIf { it.isNotEmpty() }?.joinToString("\n") ?: "Not Available"
            treatmentTextView.text = "Treatment: $treatment"

            noteTextView.text = "Note: ${diseaseInfo.note ?: "Not Available"}"

            val source = diseaseInfo.source.takeIf { it.isNotEmpty() }?.joinToString("\n") ?: "Not Available"
            sourceTextView.text = "Source: $source"

        } else {
            Log.e("DetailScanFragment", "Disease info is null, using default values")

            diseaseInfoTextView.text = "Description: ${getString(R.string.description_not_available)}"
            symptomsTextView.text = "Symptoms: ${getString(R.string.symptoms_not_available)}"
            treatmentTextView.text = "Treatment: ${getString(R.string.treatment_not_available)}"
            noteTextView.text = "Note: ${getString(R.string.note_not_available)}"
            sourceTextView.text = "Source: ${getString(R.string.source_not_available)}"
        }
    }

}
