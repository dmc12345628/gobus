package com.mupper.gobus.ui.bus.steps

import android.os.Bundle
import android.view.View
import com.mupper.gobus.R
import com.mupper.gobus.commons.newStepInstance
import com.mupper.gobus.commons.stepper.StepFragment
import com.mupper.gobus.databinding.FragmentBusNewPathNameBinding
import com.stepstone.stepper.VerificationError

/**
 * Created by jesus.medina on 12/2019.
 * Mupper
 */
class NewBusPathNameFragment : StepFragment<FragmentBusNewPathNameBinding>() {
    companion object {
        fun newInstance(): StepFragment<FragmentBusNewPathNameBinding> {
            return newStepInstance(
                NewBusPathNameFragment(),
                R.layout.fragment_bus_new_path_name,
                Bundle()
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            bus = busViewModel
            lifecycleOwner = this@NewBusPathNameFragment
        }
    }

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
