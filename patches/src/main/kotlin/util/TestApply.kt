package util

import app.morphe.patcher.Patcher
import app.morphe.patcher.PatcherConfig
import app.morphe.patcher.patch.loadPatchesFromJar
import kotlinx.coroutines.runBlocking
import java.io.File

fun main(args: Array<String>) {
    val apkFile = File(args[0])
    val mppFile = File(
        File("build/libs/").listFiles { file ->
            file.name.endsWith(".mpp")
        }!!.first().path
    )

    val patches = loadPatchesFromJar(setOf(mppFile))
    println("Loaded ${patches.size} patch(es): ${patches.map { it.name }}")

    Patcher(PatcherConfig(apkFile = apkFile)).use { patcher ->
        patcher += patches

        runBlocking {
            patcher().collect { result ->
                if (result.exception != null) {
                    println("\"${result.patch}\" FAILED:")
                    result.exception!!.printStackTrace()
                } else {
                    println("\"${result.patch}\" succeeded")
                }
            }
        }
    }
}
