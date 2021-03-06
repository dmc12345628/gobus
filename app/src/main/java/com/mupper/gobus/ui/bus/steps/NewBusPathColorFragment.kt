package com.mupper.gobus.ui.bus.steps

import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import com.mupper.gobus.R
import com.mupper.gobus.commons.EventObserver
import com.mupper.gobus.commons.newStepInstance
import com.mupper.gobus.commons.stepper.StepFragment
import com.mupper.gobus.databinding.FragmentBusNewPathColorBinding
import com.stepstone.stepper.VerificationError
import com.thebluealliance.spectrum.SpectrumDialog

/**
 * Created by jesus.medina on 12/2019.
 * Mupper
 */
class NewBusPathColorFragment : StepFragment<FragmentBusNewPathColorBinding>() {
    companion object {
        fun newInstance(): StepFragment<FragmentBusNewPathColorBinding> = newStepInstance(
                NewBusPathColorFragment(),
                R.layout.fragment_bus_new_path_color,
                Bundle()
            )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        busViewModel.showColorPickerDialog.observe(viewLifecycleOwner,
            EventObserver { color ->
                showColorPicker(color)
            })

        binding?.apply {
            bus = busViewModel
            lifecycleOwner = this@NewBusPathColorFragment
        }
    }

    private fun showColorPicker(color: Int?) {
        val colorPicker = SpectrumDialog.Builder(requireActivity())
        colorPicker.setColors(R.array.md_colors)
        color?.let { colorPicker.setSelectedColor(it) }
        colorPicker.setOnColorSelectedListener { _, colorHex ->
            val colorString = convertColorToHexString(colorHex)
            busViewModel.onColorPicked(colorHex, colorString)
        }
        colorPicker.build().show(childFragmentManager, getString(R.string.whats_color_bus_title))
    }

    private fun convertColorToHexString(@ColorInt colorInt: Int): String =
        "#${Integer.toHexString(colorInt)}"

    override fun onSelected() {
        // TOD O: Animation
    }

    override fun verifyStep(): VerificationError? {
        // TOD O: Handle error
        return null
    }

    override fun onError(error: VerificationError) {
        // TOD O: Handle error
    }
}
