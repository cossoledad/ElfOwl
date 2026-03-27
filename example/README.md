# Example

这个目录是 ElfOwl 的完整联调示例，包含：

- 6 个 Linux 动态库
- 明确的依赖链
- 一个 JNI 入口库
- 一个独立 Maven 示例项目，通过 JitPack 依赖 ElfOwl 并扫描加载整个目录

依赖关系如下：

- `libowl_seed.so`
- `libowl_branch.so -> libowl_seed.so`
- `libowl_leaf.so -> libowl_branch.so`
- `libowl_song.so -> libowl_seed.so + libowl_leaf.so`
- `libowl_bridge.so -> libowl_branch.so`
- `libowl_entry.so -> libowl_song.so + libowl_bridge.so`

运行：

```bash
./example/run_demo.sh
```

默认会从 JitPack 拉取 `v0.1.1`。如果你要验证其他 tag，可覆盖版本：

```bash
ELFOWL_VERSION=v0.1.1 ./example/run_demo.sh
```

预期输出会包含：

- 扫描到的 `.so` 数量
- ElfOwl 计算出的加载顺序
- JNI 返回的拼接消息
