package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.LiquidBounce
import net.minecraft.launchwrapper.LaunchClassLoader
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.ModClassLoader
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object DependencyUtils {
    val MAVEN_CENTRAL="https://repo.maven.apache.org/maven2/"
    val cacheDir=File("./.cache/${LiquidBounce.CLIENT_NAME}/dependency")

    init {
        if(!cacheDir.exists())
            cacheDir.mkdirs()
    }

    fun loadMavenDependency(groupID: String, artifactID: String, version: String, repo: String = MAVEN_CENTRAL){
        val file=File(cacheDir,"$artifactID-$version.jar")

        if(!file.exists())
            FileUtils.downloadFile(file, getMavenDependencyURL(groupID, artifactID, version, repo))

        loadJarClasses(file)
    }

    fun getMavenDependencyURL(groupID: String, artifactID: String, version: String, repo: String):URL{
        return URL("${if(repo.endsWith("/")){repo}else{"$repo/"}}${groupID.replace(".","/")}/$artifactID/$version/$artifactID-$version.jar")
    }

//    fun replaceMcDependency(groupID: String, artifactID: String, url: URL, fileStr: String){
//        val file=File(cacheDir, fileStr)
//        val loader=ClassLoader.getSystemClassLoader() as URLClassLoader
//        loader.urLs.forEach {
//            if(it.path.contains("/${groupID.replace(".","/")}/$artifactID/")){
//                if(!file.exists())
//                    FileUtils.downloadFile(file, url)
//
//                Files.copy(file.toPath(), File(it.path).toPath(), StandardCopyOption.REPLACE_EXISTING)
//
//                loadJarClasses(file)
//                return@forEach
//            }
//        }
//    }

    private fun loadJarClasses(file: File){
        val classLoader=Loader.instance().modClassLoader
        classLoader.addFile(file)
    }
}