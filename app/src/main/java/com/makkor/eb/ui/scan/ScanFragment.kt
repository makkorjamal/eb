package com.makkor.eb.ui.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.Context
import android.os.VibrationEffect
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.makkor.eb.R
import com.makkor.eb.databinding.FragmentScanBinding
import com.makkor.eb.ui.permissions.PermissionsFragment
import androidx.navigation.findNavController
import android.os.Vibrator
import com.journeyapps.barcodescanner.DecoratedBarcodeView
class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private lateinit var barcodeViewer: DecoratedBarcodeView
    private lateinit var resultTxt: TextView

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
        resultTxt = binding.resultTextView
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
            resultTxt.text = result.text
            vibrateTwice()
            barcodeViewer.pause()
        }
    }
    private fun vibrateTwice() {
        val vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}