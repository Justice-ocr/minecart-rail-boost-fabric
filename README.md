# Minecart Rail Boost

一个 Fabric 服务端模组，也可用于单人存档的集成服务器。

## 功能

- 只有真实玩家乘坐矿车时才生效
- 当矿车正下方的方块命中配置表时，提高矿车速度上限
- 不同方块可以对应不同速度上限
- 修改配置后需要重启服务器或单人世界生效

## 配置文件

路径：`config/minecart-rail-boost.json`

### 新版配置

```json
{
  "defaultMaxSpeed": 0.4,
  "blockSpeeds": {
    "minecraft:gold_block": 1.0,
    "minecraft:diamond_block": 1.5,
    "minecraft:emerald_block": 2.0
  }
}
```

### 字段说明

- `defaultMaxSpeed`：未命中配置方块时的默认速度上限
- `blockSpeeds`：方块 ID 到速度上限的对应表

例如：

- 金块下是 `1.0`
- 钻石块下是 `1.5`
- 绿宝石块下是 `2.0`
- 其他方块走 `defaultMaxSpeed`

## 旧配置兼容

旧版这两项也能继续读：

```json
{
  "boostedMaxSpeed": 1.0,
  "boostBlocks": [
    "minecraft:gold_block"
  ]
}
```

升级后建议改成新版格式。

## 使用

1. 把模组放进 `mods` 文件夹
2. 启动一次游戏或服务器，让配置文件自动生成
3. 按需修改 `config/minecart-rail-boost.json`
4. 重启服务器或单人世界

## 说明

- 当前逻辑只看方块种类，不区分朝向、半砖上下半等方块状态
- Carpet 假人不会被当作真实玩家
