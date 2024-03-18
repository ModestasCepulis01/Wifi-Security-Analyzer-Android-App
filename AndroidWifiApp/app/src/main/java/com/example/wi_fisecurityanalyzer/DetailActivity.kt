package com.example.wi_fisecurityanalyzer

import android.app.Activity
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ReportFragment.Companion.reportFragment

class DetailActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_details)

        val scanResult = intent.getParcelableExtra<ScanResult>(WifiScanActivity.SELECTED_SCAN_RESULT)
        scanResult?.let {
            val listOfWifiDetails = createWifiList(it)
            val adapter = WifiDetailsViewAdapter(this, listOfWifiDetails)
            findViewById<ListView>(R.id.WifiListDetails).adapter = adapter

            val editTextPassword: EditText = findViewById(R.id.inputWifiPass)
            disableOrEnablePassInput(it, editTextPassword)

            findViewById<Button>(R.id.btnSuggestWifi).setOnClickListener {
                val password: String = editTextPassword.text.toString()
                connectToWifi(scanResult.SSID ?: "", password)
            }
        } ?: run {
            Toast.makeText(this, "No Wi-Fi details found.", Toast.LENGTH_LONG).show()
        }
    }

    private fun connectToWifi(ssid: String, password: String) {
        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()

        val suggestionsList: MutableList<WifiNetworkSuggestion> = ArrayList()
        suggestionsList.add(suggestion)

        val wifiManager = getSystemService(WIFI_SERVICE) as WifiManager
        val status = wifiManager.addNetworkSuggestions(suggestionsList)

        if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            Toast.makeText(this, "Network Suggestions Success", Toast.LENGTH_SHORT).show()
        }
    }

    private fun disableOrEnablePassInput(scanResult: ScanResult, editTextPassword: EditText) {
        editTextPassword.visibility = if (scanResult.capabilities.contains("WEP") || scanResult.capabilities.contains("WPA")) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    private fun createWifiList(scanResult: ScanResult): List<String> {
        val ssidDetails = scanResult.SSID
        val bssidDetails = scanResult.BSSID
        val levelDetails = scanResult.level
        val wifiStandard = scanResult.wifiStandard
        val capabilitiesDetails = scanResult.capabilities
        val passpointDetails = scanResult.isPasspointNetwork
        val frequencyDetails = scanResult.frequency
        val channelWidthDetails = scanResult.channelWidth
        val channelWidthDetails = scanResult.

        val formattedWifiStandard = ReturnWifiStandard(wifiStandard)

        val range = 100
        val signalStrength = WifiManager.calculateSignalLevel(levelDetails, range)
        val formattedFreq = frequencyDetails.div(1000.0)
        val formattedPasspoint = if (passpointDetails) "YES" else "NO"
        val formattedBSSID = bssidDetails.toUpperCase()
        val formattedSSID = ReturnSSIDifEmpty(ssidDetails).toUpperCase()
        val formattedChannelWidth = ReturnChannelWidth(channelWidthDetails)

        return listOf(
            "WIFI NAME: $formattedSSID",
            "MAC ADDRESS: $formattedBSSID",
            "SIGNAL STRENGTH: $signalStrength /100",
            "WIFI STANDARD: $formattedWifiStandard",
            "SECURITY LEVELS: $capabilitiesDetails",
            "PASSPOINT ACTIVE? $formattedPasspoint",
            "FREQUENCY: ${formattedFreq}GHZ",
            "CHANNEL WIDTH: ${formattedChannelWidth}"
        )
    }

    private fun ReturnChannelWidth(channelWidthIn: Int): String{
        var inputChannelWidth = "UNKNOWN"
        when(channelWidthIn){
            0 -> inputChannelWidth = "20 MHZ"
            1 -> inputChannelWidth = "40 MHZ"
            2 -> inputChannelWidth = "80 MHZ"
            3 -> inputChannelWidth = "160 MHZ"
            4 -> inputChannelWidth = "160(80+80) MHZ"
            5 -> inputChannelWidth = "320 MHZ"
            6 -> inputChannelWidth = "UNKNOWN"
            7 -> inputChannelWidth = "UNKNOWN"
            8 -> inputChannelWidth = "UNKNOWN"
            else -> inputChannelWidth = "UNKNOWN"
        }

        return inputChannelWidth
    }

    private fun ReturnWifiStandard(wifiStandard: Int):String {

        var actualWifiStandard = "UNKNOWN"

        when(wifiStandard){
            0 -> actualWifiStandard = "WIFI UNKOWN"
            1 -> actualWifiStandard = "WIFI 802.11A/B/G"
            2 -> actualWifiStandard = "WIFI UNKOWN"
            3 -> actualWifiStandard = "WIFI UNKOWN"
            4 -> actualWifiStandard = "WIFI 802.11N"
            5 -> actualWifiStandard = "WIFI 802.11AC"
            6 -> actualWifiStandard = "WIFI 802.11AX"
            7 -> actualWifiStandard = "WIFI 802.11AD"
            8 -> actualWifiStandard = "WIFI 802.11BE"
            else -> actualWifiStandard = "WIFI UNKOWN"
        }

        return actualWifiStandard
    }

    private fun ReturnSSIDifEmpty(sSIDname: String): String{
        if(sSIDname == ""){
            return "UNKNOWN NETWORK"
        }
        return sSIDname
    }

    class WifiDetailsViewAdapter(context: Context, private val wifiDetailsList: List<String>) : ArrayAdapter<String>(context, 0, wifiDetailsList) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater = LayoutInflater.from(context)
            val view = convertView ?: inflater.inflate(R.layout.custom_item_for_wifi_details, parent, false)
            val textView: TextView = view.findViewById(R.id.wifiDetailsItem)
            val wifiDetail = getItem(position)
            textView.text = wifiDetail
            return view
        }
    }
}
