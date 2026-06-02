package com.nikolayux.masterchariot.data.obd2

import android.util.Log
import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse

class RawObdCommand(
    private val command: String,
    override val tag: String = command,
    override val name: String = command
) : ObdCommand() {

    override val mode: String =
        command.substringBefore(" ")

    override val pid: String =
        command.substringAfter(" ")

    override val handler: (ObdRawResponse) -> String = { raw ->

        Log.d(
            "RawObdCommand",
            "CMD=$command RAW=${raw.value}"
        )

        raw.value
            .replace("\r", " ")
            .replace("\n", " ")
            .trim()
    }
}