package org.airella.airella.ui.station.config.name

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import org.airella.airella.R
import org.airella.airella.config.Characteristic
import org.airella.airella.config.RefreshAction
import org.airella.airella.data.bluetooth.BluetoothCallback
import org.airella.airella.data.bluetooth.BluetoothRequest
import org.airella.airella.data.bluetooth.WriteRequest
import org.airella.airella.data.service.BluetoothService
import org.airella.airella.ui.OnBackPressed
import org.airella.airella.ui.station.config.ConfigViewModel
import org.airella.airella.utils.FragmentUtils.btConnectionFailed
import org.airella.airella.utils.FragmentUtils.configFailed
import org.airella.airella.utils.FragmentUtils.configSuccessful
import org.airella.airella.utils.Log
import java.util.*

class StationNameProgressFragment : Fragment(), OnBackPressed {

    private val viewModel: ConfigViewModel by activityViewModels()

    private val locationViewModelStation: StationNameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_configuration_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveStationName(locationViewModelStation.stationName.value!!)
    }

    private fun saveStationName(stationName: String) {
        Log.i("Save station name")
        val bluetoothRequests: Queue<BluetoothRequest> = LinkedList<BluetoothRequest>(
            listOf(
                WriteRequest(Characteristic.STATION_NAME, stationName),
                WriteRequest(Characteristic.REFRESH_ACTION, RefreshAction.NAME.code)
            )
        ).apply {
            addAll(viewModel.getStatusReadRequest())
            addAll(viewModel.getStationNameReadRequest())
            addAll(viewModel.getLastOperationStateReadRequest())
        }
        BluetoothService.connectGatt(
            viewModel.btDevice,
            object : BluetoothCallback(bluetoothRequests) {
                override fun onSuccess() {
                    when (viewModel.lastOperationStatus.value) {
                        "name|ok" -> configSuccessful()
                        else -> configFailed()
                    }
                }

                override fun onFailure() {
                    btConnectionFailed()
                }
            })
    }

}
