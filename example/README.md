# Example

这个目录是 ElfOwl 的完整联调示例，包含：

- 6 个 Linux 动态库
- 明确的依赖链
- 一个 JNI 入口库
- 一个 Java 程序使用 ElfOwl 扫描并加载整个目录

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

预期输出会包含：

- 扫描到的 `.so` 数量
- ElfOwl 计算出的加载顺序
- JNI 返回的拼接消息
