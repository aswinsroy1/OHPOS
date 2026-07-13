import android.bluetooth.BluetoothAdapter
import android.content.Context

fun getDevices(): List<android.bluetooth.BluetoothDevice> {
    val adapter = BluetoothAdapter.getDefaultAdapter()
    return adapter?.bondedDevices?.toList() ?: emptyList()
}
