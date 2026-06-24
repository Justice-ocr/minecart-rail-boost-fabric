# Minecart Rail Boost

一个 Fabric 服务端模组，也可用于单人存档的集成服务器。

## 功能

- 只有真实玩家乘坐矿车时才生效
- 当矿车正下方的方块命中配置列表时，提高矿车速度上限
- 加速方块和速度上限都写在游戏外配置文件里
- 修改配置后需要重启服务器或单人世界生效

## 配置文件

路径：`config/minecart-rail-boost.json`

默认内容：

```json
{
  "boostedMaxSpeed": 1.0,
  "boostBlocks": [
    "minecraft:gold_block"
  ]
}
```

## 字段说明

- `boostedMaxSpeed`：矿车速度上限，数值越大越快
- `boostBlocks`：触发加速的方块 ID 列表

示例：

```json
{
  "boostedMaxSpeed": 1.5,
  "boostBlocks": [
    "minecraft:gold_block",
    "minecraft:diamond_block",
    "minecraft:emerald_block"
  ]
}
```

## 使用

1. 把模组放进 `mods` 文件夹
2. 启动一次游戏或服务器，让配置文件自动生成
3. 按需修改 `config/minecart-rail-boost.json`
4. 重启服务器或单人世界

## 说明

- 当前逻辑只看方块种类，不区分朝向、半砖上下半等方块状态
- Carpet 假人不会被当作真实玩家
