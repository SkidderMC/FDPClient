package net.ccbluex.liquidbounce.script.kotlin

import net.ccbluex.liquidbounce.utils.DependencyUtils

object KotlinScriptDependency {
    private var checked=false

    fun check(){
        if(checked)
            return

        // replace minecraft dependency
        // LMAO THIS IS USELESS BUT WILL CAUSE A EXCEPTION
//        DependencyUtils.replaceMcDependency("net.java.dev.jna","jna",
//            DependencyUtils.getMavenDependencyURL("net.java.dev.jna","jna","5.8.0", DependencyUtils.MAVEN_CENTRAL), "jna-5.8.0.jar")
//        DependencyUtils.replaceMcDependency("net.java.dev.jna","platform",
//            DependencyUtils.getMavenDependencyURL("net.java.dev.jna","jna-platform","5.8.0", DependencyUtils.MAVEN_CENTRAL), "jna-platform-5.8.0.jar")

        // load normal dependencies
        DependencyUtils.loadMavenDependency("org.jetbrains.kotlin", "kotlin-reflect", "1.3.72")
        DependencyUtils.loadMavenDependency("org.jetbrains.kotlin", "kotlin-script-runtime", "1.3.72")
        DependencyUtils.loadMavenDependency("org.jetbrains.kotlin", "kotlin-script-util", "1.3.72")
        DependencyUtils.loadMavenDependency("org.jetbrains.kotlin", "kotlin-compiler-embeddable", "1.3.72")
        DependencyUtils.loadMavenDependency("org.jetbrains.kotlin", "kotlin-scripting-jsr223-embeddable", "1.3.72")
        DependencyUtils.loadMavenDependency("org.jetbrains.kotlin", "kotlin-scripting-jvm", "1.3.72")
        DependencyUtils.loadMavenDependency("org.jetbrains.kotlin", "kotlin-scripting-compiler-embeddable", "1.3.72")
        DependencyUtils.loadMavenDependency("org.jetbrains.kotlin", "kotlin-scripting-compiler-impl-embeddable", "1.3.72")
        DependencyUtils.loadMavenDependency("org.jetbrains.kotlin", "kotlin-scripting-common", "1.3.72")
        DependencyUtils.loadMavenDependency("org.jetbrains.intellij.deps", "trove4j", "1.0.20181211")
    }
}