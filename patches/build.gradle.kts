group = "software.zarko"

patches {
    about {
        name = "Zarko Patches"
        description = "Patches for Sunrise Alarm: Wake-Up Light"
        source = "git@github.com:zarko/morphe-patches.git"
        author = "zarko"
        contact = "na"
        website = "na"
        license = "GPLv3"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

// Separate configuration so coroutines are available at runtime for the local
// testApply task (running the patcher directly) but never bundled into the APK.
val testApplyClasspath = configurations.create("testApplyClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    testApplyClasspath("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    register<JavaExec>("testApply") {
        description = "Apply the built patches to an APK given as -PapkPath=... to verify fingerprints match"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + testApplyClasspath
        mainClass.set("util.TestApplyKt")
        args = listOf(project.findProperty("apkPath") as? String ?: error("Pass -PapkPath=<path to apk>"))
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
