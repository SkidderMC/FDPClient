group = "org.unlegitmc.fdp"
version = "v1"

plugins.apply("java")

val depsDir = File(projectDir, "deps")

tasks.getByName("jar").doLast {
    val libs = arrayListOf(UILib("mdui/mdui.min.js", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/js/mdui.min.js")),
        UILib("mdui/mdui.min.css", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/css/mdui.min.css")),
        UILib("mdui/LICENSE.txt", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/LICENSE")),
        UILib("icons/material-icons/MaterialIcons-Regular.woff", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/icons/material-icons/MaterialIcons-Regular.woff")),
        UILib("icons/material-icons/MaterialIcons-Regular.woff2", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/icons/material-icons/MaterialIcons-Regular.woff2")),
        UILib("icons/material-icons/LICENSE.txt", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/icons/material-icons/LICENSE.txt")),
        UILib("fonts/roboto/Roboto-Regular.woff2", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-Regular.woff2")),
        UILib("fonts/roboto/Roboto-ThinItalic.woff2", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-ThinItalic.woff2")),
        UILib("fonts/roboto/Roboto-Black.woff", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-Black.woff")),
        UILib("fonts/roboto/Roboto-Thin.woff2", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-Thin.woff2")),
        UILib("fonts/roboto/Roboto-BoldItalic.woff", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-BoldItalic.woff")),
        UILib("fonts/roboto/Roboto-Light.woff", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-Light.woff")),
        UILib("fonts/roboto/Roboto-Medium.woff2", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-Medium.woff2")),
        UILib("fonts/roboto/Roboto-Regular.woff", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-Regular.woff")),
        UILib("fonts/roboto/Roboto-Bold.woff", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-Bold.woff")),
        UILib("fonts/roboto/Roboto-Light.woff2", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-Light.woff2")),
        UILib("fonts/roboto/Roboto-ThinItalic.woff", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-ThinItalic.woff")),
        UILib("fonts/roboto/Roboto-Bold.woff2", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-Bold.woff2")),
        UILib("fonts/roboto/Roboto-MediumItalic.woff", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-MediumItalic.woff")),
        UILib("fonts/roboto/Roboto-BoldItalic.woff2", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-BoldItalic.woff2")),
        UILib("fonts/roboto/Roboto-Black.woff2", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-Black.woff2")),
        UILib("fonts/roboto/Roboto-BlackItalic.woff2", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-BlackItalic.woff2")),
        UILib("fonts/roboto/Roboto-LightItalic.woff", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-LightItalic.woff")),
        UILib("fonts/roboto/Roboto-BlackItalic.woff", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-BlackItalic.woff")),
        UILib("fonts/roboto/Roboto-RegularItalic.woff", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-RegularItalic.woff")),
        UILib("fonts/roboto/Roboto-Thin.woff", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-Thin.woff")),
        UILib("fonts/roboto/Roboto-RegularItalic.woff2", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-RegularItalic.woff2")),
        UILib("fonts/roboto/Roboto-Medium.woff", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-Medium.woff")),
        UILib("fonts/roboto/Roboto-LightItalic.woff2", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-LightItalic.woff2")),
        UILib("fonts/roboto/Roboto-MediumItalic.woff2", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/Roboto-MediumItalic.woff2")),
        UILib("fonts/roboto/LICENSE.txt", java.net.URL("https://cdn.jsdelivr.net/npm/mdui@1.0.2/dist/fonts/roboto/LICENSE.txt")),
        UILib("jquery/jquery.min.js", java.net.URL("https://cdn.jsdelivr.net/npm/jquery@2.2.4/dist/jquery.min.js")),
        UILib("jquery/LICENSE.txt", java.net.URL("https://cdn.jsdelivr.net/npm/jquery@2.2.4/LICENSE.txt")),
        UILib("swal/swal.min.js", java.net.URL("https://cdn.jsdelivr.net/npm/sweetalert@2.1.2/dist/sweetalert.min.js")),
        UILib("swal/LICENSE.md", java.net.URL("https://cdn.jsdelivr.net/npm/sweetalert@2.1.2/LICENSE.md")))

    // write the resource file into a zip
    val zipFile = File(buildDir, "tmp/resources.zip")
    val zos = java.util.jar.JarOutputStream(java.io.BufferedOutputStream(java.io.FileOutputStream(zipFile)))
    fun process(jarDir: String, dir: File) {
        dir.listFiles().forEach {
            if (it.isDirectory) {
                process("$jarDir${it.name}/", it)
            } else if (it.isFile) {
                zos.putNextEntry(java.util.jar.JarEntry("$jarDir${it.name}"))
                zos.write(java.nio.file.Files.readAllBytes(it.toPath()))
                zos.closeEntry()
            }
        }
    }
    process("", File(projectDir, "src"))
    libs.forEach {
        val file = File(depsDir, it.file)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            println("Downloading lib file ${it.file}")
            java.nio.file.Files.copy(it.url.openStream(), file.toPath())
        }
        zos.putNextEntry(java.util.jar.JarEntry("lib/${it.file}"))
        zos.write(java.nio.file.Files.readAllBytes(file.toPath()))
        zos.closeEntry()
    }
    zos.close()

    // then pack it into the jar
    val file = File("$buildDir/libs/${project.name}-$version.jar")
    val jos = java.util.jar.JarOutputStream(java.io.BufferedOutputStream(java.io.FileOutputStream(file)))
    jos.putNextEntry(java.util.jar.JarEntry("ui_resources.zip"))
    jos.write(java.nio.file.Files.readAllBytes(zipFile.toPath()))
    jos.closeEntry()
    jos.close()
}

tasks.register("cleanDeps") {
    depsDir.deleteRecursively()
}

class UILib(val file: String, val url: java.net.URL)