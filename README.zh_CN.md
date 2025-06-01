# Mindustry Preoxide

![badge](https://img.shields.io/github/commit-activity/m/EmmmM9O/mindustry-preoxide-lib)
[![license](https://img.shields.io/github/license/EmmmM9O/mindustry-preoxide-lib.svg)](LICENSE)

面向mindustry json/js/java modder的前置库,提供图形学等帮助

> preoxide 主要参考oxygen core 因为oxygen core已经变成了改端特化 而且目前进度缓慢
> 所以preoxide提取oxygen部分内容做成前置库给广大开发者使用

## 内容

|    模块    |      依赖模块       | 被哪些模块依赖  |
|----------|-----------------|----------|
| parser   | -               | universe |
| graphics | -               | universe |
| universe | parser,graphics | -        |

### TOOD

- [ ] 修复黑洞边缘衔接问题
- [ ] 更多种类星球

## 示例

- [examples](examples/)
  - [universe](examples/universe)

### 预生成

Fast黑洞的rayMap预生成请使用**oxygen tools**
Fast黑洞的colorMap生成请使用scripts/下的glslSandbox配合disc.glsl使用来生成

## 相关仓库

[Mindustry Oxygen Core](https://github.com/EmmmM9O/mindustry-oxygen-core) - 参考

[Mindustry Oxygen Tools](https://github.com/EmmmM9O/oxygen-tools) - 预生成

## 维护者

[EmmmM9O](https://github.com/EmmmM9O)

## 使用许可

[GPL3.0](LICENSE) EmmmM9O
