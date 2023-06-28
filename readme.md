# Forge & Fabric Mixin One

[中文](readme.zh.md) | English

This project aims to allow mod to run on both Forge and Fabric platforms,  even compatible with multiple versions such as 1.16-1.19+.  
This is actually more of a development specification or convention than a framework, improving compatibility by adding an intermediate layer to interact with the modloader and separating out the logic of the mod itself.  

Although in fact it can be compatible with older versions ...... i do not want to do that.  

Usage:  
Firstly you should configure the `mod.properties` file, fill in the mod information and the module package name, then execute `gradle initialization` and you are ready to develop.  
Use `gradle buildMod` to compile the mod, the mod will be packaged into `dists` folder.  
If you want to change the package name, please modify the `mod_package` configuration in mod.properties (don't touch `mod_entrypoint`) and then execute `gradle renamePackage`.  
`mod_entrypoint` should always be in `mod_package` package, do not put it in the sub-package.

**Because I don't have the habit of debugging in the IDE, I didn't let it create a run that can be used in the IDE. my bad.**

If you need to call the game interface, please add `api` in `game` module and implements them in each version module.  

If you need to use mixin, Forge should be written in the corresponding version module, and Fabric should be written in the `fabric-mixin` module.  

If you need to add a new version adaptation, please add a mapping in `modloader/loader` in addition to the new folder in `game`.  