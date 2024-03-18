package com.example.wi_fisecurityanalyzer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WifiScanActivity : AppCompatActivity() {

    private lateinit var wifiScanReceiver: BroadcastReceiver
    private var isReceiverRegistered = false

    companion object {
        const val SELECTED_SCAN_RESULT = "ScanResult"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_scan)

        val btnScanWifi: Button = findViewById(R.id.btnScanWifi)
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        initializeWifiScanReceiver(wifiManager)

        btnScanWifi.setOnClickListener {
            if (!wifiManager.isWifiEnabled) {
                Toast.makeText(this, "Enabling WiFi...", Toast.LENGTH_SHORT).show()
                wifiManager.isWifiEnabled = true
            }

            Toast.makeText(this, "Scanning WiFi...", Toast.LENGTH_SHORT).show()
            val success = wifiManager.startScan()
            if (!success) {
                // Directly trigger the failure handler if startScan indicates failure
                scanFailure()
            }
        }
    }

    private fun initializeWifiScanReceiver(wifiManager: WifiManager) {
        wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) scanSuccess(wifiManager) else scanFailure()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isReceiverRegistered) {
            registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            isReceiverRegistered = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (isReceiverRegistered) {
            unregisterReceiver(wifiScanReceiver)
            isReceiverRegistered = false
        }
    }

    private fun scanSuccess(wifiManager: WifiManager) {
        Toast.makeText(this, "New WiFi have been found!", Toast.LENGTH_SHORT).show()

        val scanResults = wifiManager.scanResults.sortedWith(compareBy({ it.capabilities.contains("WEP") || it.capabilities.contains("WPA") }, { it.SSID }))
            .distinctBy { it.SSID }

        findViewById<ListView>(R.id.listView).apply {
            adapter = MyAdapter(this@WifiScanActivity, scanResults)
            setOnItemClickListener { _, _, position, _ ->
                scanResults[position].also { scanResult ->
                    startActivity(Intent(this@WifiScanActivity, DetailActivity::class.java).apply {
                        putExtra(SELECTED_SCAN_RESULT, scanResult)
                    })
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
        }
    }

    private fun scanFailure() {
        Toast.makeText(this, "No new Wi-Fi have been found, try again...", Toast.LENGTH_SHORT).show()
    }

    class MyAdapter(context: Context, wifiList: List<ScanResult>) : ArrayAdapter<ScanResult>(context, 0, wifiList) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.custom_item_for_list, parent, false)
            getItem(position)?.let { scanResult ->
                view.findViewById<ImageView>(R.id.locklocked).visibility = if (scanResult.capabilities.contains("WEP") || scanResult.capabilities.contains("WPA")) View.VISIBLE else View.GONE
                view.findViewById<TextView>(R.id.eachSSID).text = scanResult.SSID.ifEmpty { "UNKNOWN NETWORK" }
            }
            return view
        }
    }
}
