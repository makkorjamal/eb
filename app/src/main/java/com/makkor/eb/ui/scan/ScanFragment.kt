package com.makkor.eb.ui.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.makkor.eb.R
import com.makkor.eb.databinding.FragmentScanBinding
import com.makkor.eb.ui.permissions.PermissionsFragment
import androidx.navigation.findNavController
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private lateinit var barcodeViewer: DecoratedBarcodeView

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val scanViewModel =
            ViewModelProvider(this).get(ScanViewModel::class.java)

        _binding = FragmentScanBinding.inflate(inflater, container, false)
        val root: View = binding.root
        barcodeViewer = root.findViewById(binding.barcodeScannerView.id)
        val resultTxt: TextView = binding.resultTextView
        scanViewModel.text.observe(viewLifecycleOwner) {
            resultTxt.text = it
        }
        startScanning()
        return root
    }
    override fun onResume() {
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            requireActivity().findNavController(R.id.nav_host_fragment_activity_main).navigate(
                ScanFragmentDirections.actionScanToPermissions()
            )
        }
        else{
            barcodeViewer.resume()
        }
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        barcodeViewer.pause()
    }
    private fun startScanning() {
        barcodeViewer.decodeContinuous { result ->
            Toast.makeText(activity, "Scanned: ${result.text}", Toast.LENGTH_LONG).show()
            barcodeViewer.pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}