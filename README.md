# ElfOwl

ElfOwl 是一个轻巧的 JVM 原生库加载器，面向 Spring Boot、普通 Java 服务和工具进程。

它吸收了 ReLinker 的“替代系统默认行为、增强可控性”的思路，也借鉴了 SoLoader 的“依赖有序、来源可控”的经验，但目标更聚焦：

- 用户指定一个目录
- 库扫描该目录中的动态库文件
- 解析 ELF `DT_NEEDED` / `DT_SONAME`
- 按依赖顺序稳定加载
- 对循环依赖、缺失依赖、重复加载给出清晰结果

## 特性

- 零运行时第三方依赖
- 纯 Java ELF 动态段解析
- 稳定拓扑排序，优先同目录依赖解析
- 支持递归扫描
- 支持严格模式与宽松模式
- 幂等加载，避免同一进程重复 `System.load`
- 适合作为 Spring Boot 启动前或启动中的 native bootstrap 组件

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

## Spring Boot 用法

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

如果你使用的是 Spring Boot 2.x，可把 `jakarta.annotation.PostConstruct` 换成 `javax.annotation.PostConstruct`。

## 设计说明

1. 扫描目录下的候选动态库文件，默认匹配：
   - `*.so`
   - `*.so.*`
   - `*.dylib`
   - `*.dll`
2. 对 ELF 文件解析 `DT_NEEDED` 与 `DT_SONAME`
3. 用“文件名 + SONAME”建立候选映射，优先同目录匹配
4. 建立依赖图后做强连通分量压缩和拓扑排序
5. 先加载底层依赖，再加载上层库

## 与 ReLinker / SoLoader 的关系

- ReLinker 更擅长 Android 场景中的提取、修复和兼容加载
- SoLoader 更偏向 Android/Meta 体系中的多来源 native 解析和加载
- ElfOwl 则专注于服务端/JVM 进程中“指定目录、分析依赖、有序加载”的核心问题

它不是二者的替代品，而是一个更适合服务端部署目录模型的轻量实现。

## 当前版本边界

- 重点支持 Linux ELF 场景
- 对 Mach-O / PE 未做依赖解析，仅可按文件名顺序直接加载
- 当前未实现 JNI 资源解压、签名校验、ABI 选择等增强能力

这些能力很适合后续演进到 `0.2.x` 和 `0.3.x`。

## 示例工程

根目录下的 [example](/home/ganjb/project/ElfOwl/example) 提供了一个完整可运行的示例：

- 6 个存在依赖关系的 Linux `.so` 动态库
- 一个 JNI 入口库
- 一个 Java 演示程序，直接调用 ElfOwl 加载整个目录

可直接运行：

```bash
./example/run_demo.sh
```

## 发布

发布到 GitHub Packages、Maven Central 以及对应的 GitHub Actions 工作流说明见：

- [PUBLISHING.md](/home/ganjb/project/ElfOwl/PUBLISHING.md)
