# FDPClient 
[![State-of-the-art Shitcode](https://img.shields.io/static/v1?label=State-of-the-art&message=Shitcode&color=7B5804)](https://github.com/trekhleb/state-of-the-art-shitcode)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/SkidderMC/FDPClient)
![GitHub lines of code](https://tokei.rs/b1/github/SkidderMC/FDPClient)
![Minecraft](https://img.shields.io/badge/game-Minecraft-brightgreen)  
A free mixin-based injection hacked-client for Minecraft using Minecraft Forge based on LiquidBounce.

Website: [fdpinfo.github.io](https://fdpinfo.github.io)  
Latest [github-actions](https://github.com/SkidderMC/FDPClient/actions/workflows/build.yml?query=event%3Apush)  
Discord: [dsc.gg/fdpdiscord](https://dsc.gg/fdpdiscord)


## How To Install FDP?
- **Step 1:** Install Java [here](https://www.java.com/en/download/) (Skip if you have Java)
- **Step 2:** Install Forge 1.8.9 [here](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.8.9.html) (Skip if you have Forge)
- **Step 3:** Start Forge and then close it
- **Step 4:** Put the FDP jar in the mods folder in your Minecraft directory (%appdata%\\.minecraft\mods or if you are using the official launcher, click the installations tab then click the folder icon next to forge)
- **Step 5:** Enjoy!

## Issues
Found bugs or a missing features? You can let us know by opening an issue [here](https://github.com/SkidderMC/FDPClient/issues)!

## License
This project is subject to the [GNU General Public License v3.0](LICENSE). This does only apply for source code located directly in this clean repository. During the development and compilation process, additional source code may be used to which we have obtained no rights. Such code is not covered by the GPL license.

For those who are unfamiliar with the license, here is a summary of its main points. This is by no means legal advise nor legally binding.

You are allowed to:
- use
- share
- modify

this project entirely or partially for free and even commercially. However, please consider the following:

- **You must disclose the source code of your modified work and the source code you took from this project. This means you are not allowed to use code from this project (even partially) in a closed-source (or even obfuscated) application.**
- **Your modified application must also be licensed under the GPL.**

Do the above and share your source code with everyone; just like we do!

## Setting up a Workspace
FDPClient uses gradle, so make sure that it is installed properly. Instructions can be found on [Gradle's website](https://gradle.org/install/).
1. Clone the repository using `git clone --recurse-submodules https://github.com/SkidderMC/FDPClient.git` (Make sure you have git or Github Desktop installed on your system).
2. CD into the local repository folder.
3. Depending on which IDE you are using, execute either of the following commands:
    - For IntelliJ: `gradlew --debug setupDevWorkspace idea genIntellijRuns build`
    - For Eclipse: `gradlew --debug setupDevWorkspace eclipse build`
4. Open the folder as a Gradle project in your IDE. (make sure your ide is using java 8, you will face issues if it isnt)
5. Select the Forge run configuration.
### Troubleshooting Workspace Errors
If you get a "cannot find forgebin" error, download forge 1.8.9 universal from the forge site and place it in `./FDPClient-main/.gradle/minecraft`<br>.

## Additional Libraries
### Mixins
Mixins can be used to modify classes at runtime before they are loaded. FDPClient uses them to inject its code into the Minecraft client. This way, we do not have to ship Mojang's copyrighted code. If you want to learn more about it, check out their [documentation](https://docs.spongepowered.org/5.1.0/en/plugin/internals/mixins.html).

## Contributions
We welcome contributions, but you have to follow the following rules in order for us to merge your pull request.

You can make a pull request [here](https://github.com/SkidderMC/FDPClient/issues)!

### Language and Code Quality
Your code needs to be able to build, please ensure your code has little to no bugs!  
You also need to use kotlin features to make coding easier and faster, so please use kotlin and pass the [Detekt](https://github.com/detekt/detekt) code quality check, use kotlin features if you can, because we will never merge terrible code.

#### Kotlin Features
Help enhance the code readability by using kotlin features.

Using kotlin features:
~~~kotlin
Timer().schedule(2000L) { 
    // your code
}
~~~
Not using kotlin features:
~~~kotlin
Timer().schedule(object : TimerTask() {
    override fun run() {
        // your code
    }
}, 2000L)
~~~

### A note about Skidding
Please use original code if you can and do not directly steal code, however we welcome skidding with a packet logger or anything similar in order to skid from a closed source client and make the cheating community more open!

### Useless Features
Useless features are features only you think are useful and/or features can be added with a config change.  
Like the "TimerSpeed" option to InfiniteAura, this feature can be added by binding Timer to the key with InfiniteAura, or use the macro system in FDP Client.

### Old Contributors
@Liulihaocai(kotonemywaifu) | Quitted Minecraft Cheating    
@XiGuaHanHan(Wlenk) | Got Exams
  
To be continued...      
Thanking them for their amazing contributions.
