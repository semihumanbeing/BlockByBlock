import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))

            implementation(libs.compose.ui)
            implementation(libs.kotlinx.datetime)
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.wasm.binaryen.BinaryenExec>().configureEach {
    binaryenArguments.set(listOf(
        "--enable-gc",
        "--enable-reference-types",
        "--enable-exception-handling",
        "--enable-bulk-memory",
        "--enable-nontrapping-float-to-int",
        "--no-inline=kotlin.wasm.internal.throwValue",
        "--no-inline=kotlin.wasm.internal.getKotlinException",
        "--no-inline=kotlin.wasm.internal.jsToKotlinStringAdapter",
        "--inline-functions-with-loops",
        "--traps-never-happen",
        "--fast-math",
        "--closed-world",
        "--type-ssa",
        "-O2"
    ))
}