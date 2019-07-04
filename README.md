# AR Patch
Airless Dungeon Patching Plugin

无尽地牢补丁插件

Tested on: AR 3.7.2

测试可用版本： AR 3.7.2

## What is AR/Airless Dungeion? / AR/无尽地牢是什么？
See (in Chinese): http://www.mcbbs.net/thread-611815-1-1.html

详情请见：http://www.mcbbs.net/thread-611815-1-1.html

## What is it for? / 这是用来干啥的？
This is a plugin to patch some missing behaviors in Airless Dungeon when running on Spigot/Bukkit. This was meant to replace the functionalities of `ad:item/_main/game/chest/drops` . The cause is probably because of the use of non-ASCII characters in the target selector. Since the newer version (3.8+) of AR will no longer be using non-ASCII characters in the detection, this plugin is almost already obsolete.

这是一个用来修复一些在Spigot/Bukkit里运行无尽地牢时缺失功能的插件。这个插件是设计来替代 `ad:item/_main/game/chest/drops` 来执行所需功能的。问题本身大概是因为在目标选择器里使用了非ASCII文字所导致的。但在新版本(3.8+)的AR里将不再使用非ASCII在物品检测中，所以这个插件已经可以说是没用的了（

## So what did you fix? / 于是这个插件修了什么呢？
By the time I get the updated version of AR, I already implemented the fixes to the spawning of regular drop chest and rare drop chest.

当咱知道AR的更新时，咱已经弄好了普通掉落箱和稀有掉落箱的生成了。

## What other changes did you make? / 还有些什么其他修改吗？
I have seen drop chest getting stuck under solid block when the drop location was not ideal, so I added an extra check to see if there is anything blocking the chest from spawning. Chests will not be able to spawn if either the spawning location or the block right above the chest is solid.

咱见过有一些箱子因为扔的位置不好被卡在了固体方块下，所以咱加了个额外的检查来避免这种情况。如果检测到箱子将要生成的位置或者生成位置之上的一个方块是固体方块的话，箱子则不会生成。
