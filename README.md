# ElfOwl

Lightweight native library loading with ELF dependency ordering for JVM services.

ElfOwl 是一个面向 JVM 服务端场景的轻量原生库加载器，适用于 Spring Boot、普通 Java 服务和工具进程。
它专注于对指定目录中的动态库进行扫描、解析 ELF 依赖关系，并以稳定顺序完成加载。

## 特性

- 零运行时第三方依赖
- 纯 Java 解析 ELF `DT_NEEDED` / `DT_SONAME`
- 基于依赖图的稳定加载顺序，优先匹配同目录依赖
- 支持递归扫描和自定义库文件过滤
- 支持严格模式与宽松模式
- 幂等加载，避免同一进程重复 `System.load`
- 提供循环依赖与未解析依赖信息，便于定位问题

## 快速开始

```java
import org.elfowl.loader.NativeLibraryLoader;
import org.elfowl.loader.NativeLoadOptions;

import java.nio.file.Paths;

public class Bootstrap {
    public static void main(String[] args) {
        NativeLibraryLoader.loadDirectory(
            Paths.get("/opt/app/native"),
            NativeLoadOptions.builder()
                .recursive(true)
                .failOnCycle(false)
                .strictDependencyResolution(false)
                .build()
        );
    }
}
```

## Spring Boot

```java
import org.elfowl.loader.NativeLibraryLoader;
import org.elfowl.loader.NativeLoadOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class NativeBootstrap {

    @PostConstruct
    public void init() {
        NativeLibraryLoader.loadDirectory(
            Paths.get("/opt/my-service/native"),
            NativeLoadOptions.builder().recursive(true).build()
        );
    }
}
```

在 Spring Boot 2.x 环境中，可将 `jakarta.annotation.PostConstruct` 替换为 `javax.annotation.PostConstruct`。

## 工作方式

1. 扫描目录下的候选动态库文件，默认匹配：
   - `*.so`
   - `*.so.*`
   - `*.dylib`
   - `*.dll`
2. 解析 ELF 文件中的 `DT_NEEDED` 与 `DT_SONAME`
3. 基于文件名与 SONAME 建立候选映射，并优先匹配同目录依赖
4. 构建依赖图并执行强连通分量压缩与拓扑排序
5. 按依赖层次完成加载，优先底层库，后加载上层库

## 当前边界

- 当前重点支持 Linux ELF 场景
- 对 Mach-O / PE 未提供依赖解析，仅按文件名顺序直接加载
- 暂未包含 JNI 资源解压、签名校验、ABI 选择等增强能力

## 示例

根目录下的 [example](/home/ganjb/project/ElfOwl/example) 提供了一个可直接运行的完整示例，包含：

- 6 个存在依赖关系的 Linux `.so` 动态库
- 一个 JNI 入口库
- 一个调用 ElfOwl 加载整个目录的 Java 演示程序

运行方式：

```bash
./example/run_demo.sh
```
