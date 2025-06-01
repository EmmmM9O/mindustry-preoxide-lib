# Mindustry Preoxide

![badge](https://img.shields.io/github/commit-activity/m/EmmmM9O/mindustry-preoxide-lib)
[![license](https://img.shields.io/github/license/EmmmM9O/mindustry-preoxide-lib.svg)](LICENSE)

A library for mindustry json/js/java mod development, providing graphics and other tools.

> Preoxide mainly refers to Oxygen Core. Since Oxygen Core has become client-specific and its progress is currently slow, Preoxide extracts part of Oxygen's content to form a library for the majority of developers.

## Readme

- [en](README.md)
- [简体中文](README.zh_CN.md)

## Content

|  Module  | Dependent Modules | Dependent by Modules |
|----------|-------------------|----------------------|
| parser   | -                 | universe             |
| graphics | -                 | universe             |
| universe | parser, graphics  | -                    |

### TOOD

- [ ] Fix the edge connection issue of black holes
- [ ] More types of planets

## Examples

- [examples](examples/)
  - [universe](examples/universe)

### Pre-generation

For fast black hole rayMap pre-generation, please use **oxygen tools**.
For fast black hole colorMap generation, please use glslSandbox under scripts/ in conjunction with disc.glsl to generate.

## Related Repositories

[Mindustry Oxygen Core](https://github.com/EmmmM9O/mindustry-oxygen-core) - Reference

[Mindustry Oxygen Tools](https://github.com/EmmmM9O/oxygen-tools) - Pre-generation

## Maintainer

[EmmmM9O](https://github.com/EmmmM9O)

## License

[GPL3.0](LICENSE) © EmmmM9O
