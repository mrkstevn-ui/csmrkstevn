package com.csmrkstevn

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class YabanciDiziPlugin : Plugin() {
    override fun load(context: Context) {
        // İsimler birebir aynı olmak zorunda
        registerMainAPI(YabanciDiziProvider())
    }

}