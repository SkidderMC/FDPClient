# FDPClient
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FProject-EZ4H%2FFDPClient.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2FProject-EZ4H%2FFDPClient?ref=badge_shield)
[![State-of-the-art Shitcode](https://img.shields.io/static/v1?label=State-of-the-art&message=Shitcode&color=7B5804)](https://github.com/trekhleb/state-of-the-art-shitcode)
![GitHub commit activity](https://img.shields.io/github/commit-activity/w/Project-EZ4H/FDPClient)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/Project-EZ4H/FDPClient)
![GitHub lines of code](https://tokei.rs/b1/github/Project-EZ4H/FDPClient)

A free mixin-based injection hacked-client for Minecraft using Minecraft Forge based on LiquidBounce.

Website: https://fdp.liulihaocai.pw/  
Latest: [github-actions](./actions)

## Issues
If you notice any bugs or missing features, you can let us know by opening an issue [here](https://github.com/Project-EZ4H/FDPClient/issues).

## License
This project is subject to the [GNU General Public License v3.0](LICENSE). This does only apply for source code located directly in this clean repository. During the development and compilation process, additional source code may be used to which we have obtained no rights. Such code is not covered by the GPL license.

For those who are unfamiliar with the license, here is a summary of its main points. This is by no means legal advise nor legally binding.

You are allowed to
- use
- share
- modify

this project entirely or partially for free and even commercially. However, please consider the following:

- **You must disclose the source code of your modified work and the source code you took from this project. This means you are not allowed to use code from this project (even partially) in a closed-source (or even obfuscated) application.**
- **Your modified application must also be licensed under the GPL** 

Do the above and share your source code with everyone; just like we do.


[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2FProject-EZ4H%2FFDPClient.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2FProject-EZ4H%2FFDPClient?ref=badge_large)

## Setting up a Workspace
FDPClient uses gradle, so make sure that it is installed properly. Instructions can be found on [Gradle's website](https://gradle.org/install/).
1. Clone the repository using `git clone https://github.com/Project-EZ4H/FDPClient.git`. 
2. CD into the local repository folder.
3. Depending on which IDE you are using execute either of the following commands:
    - For IntelliJ: `gradlew --debug setupDevWorkspace idea genIntellijRuns build`
    - For Eclipse: `gradlew --debug setupDevWorkspace eclipse build`
4. Open the folder as a Gradle project in your IDE.
5. Select the Forge run configuration.

## Additional libraries
### Mixins
Mixins can be used to modify classes at runtime before they are loaded. FDPClient uses it to inject its code into the Minecraft client. This way, we do not have to ship Mojangs copyrighted code. If you want to learn more about it, check out its [Documentation](https://docs.spongepowered.org/5.1.0/en/plugin/internals/mixins.html).

## Contributing

We appreciate contributions. So if you want to support us, feel free to make changes to FDPClient's source code and submit a pull request.
