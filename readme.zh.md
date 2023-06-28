# Forge & Fabric Mixin One

中文 | [English](readme.md)

这个项目旨在让模组可以同时运行在Forge与Fabric平台上，甚至是兼容1.16-1.19+等多版本。  
比起框架，其实这更像是一种开发规范或者约定，通过增加与模组加载器交互的中间层，将模组本身的逻辑独立出来，以此提高兼容性。  

虽然其实可以兼容更老的版本……好麻烦，不想搞。  

使用方法：  
首先配置`mod.properties`文件，填入模组信息以及模组包名`mod_package`，然后执行`gradle initialization`，就可以进行开发了。  
使用`gradle buildMod`来编译模组，模组会被打包至`dists`文件夹。  
如果需要更改包名，请修改mod.properties中的`mod_package`配置（不要修改`mod_entrypoint`），然后执行`gradle renamePackage`。  
`mod_entrypoint`应始终在`mod_package`包下，不要放在子包中。  

**啊……我没有在开发环境直接启动调试的习惯，所以我没写那些IDE启动项……坏习惯呢，我的锅。**

模组逻辑请在`src`中直接开发，需要调用游戏接口时，请在`game`模块中添加相应`api`并在各版本模块中编写接口逻辑。  

如果需要使用mixin，Forge请在对应版本模块中编写，Fabric请在`fabric-mixin`模块编写。  

如果需要增加新的版本适配，除了在`game`中新增文件夹外，还要在`modloader/loader`中增加映射。  