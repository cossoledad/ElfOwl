# Publishing

当前项目推荐通过 JitPack 分发，不再默认使用 GitHub Packages Maven registry。

原因很直接：

- JitPack 更适合还没有 Maven Central 发布条件的开源 Java 库
- 使用 Git tag / GitHub Release 即可生成可被 Maven/Gradle 消费的依赖
- 不需要维护额外的 Maven 包仓库上传流程

## 发布流程

### 1. 更新版本

先在 [pom.xml](/home/ganjb/project/ElfOwl/pom.xml) 中维护正式版本号，例如：

```xml
<version>0.1.1</version>
```

### 2. 打 tag

推荐使用带 `v` 前缀的 tag：

```bash
git tag v0.1.1
git push origin v0.1.1
```

### 3. 可选：创建 GitHub Release

JitPack 可以直接基于 tag 构建，不强制要求 Release。

如果你希望用户更方便下载源码包、jar 或查看版本说明，可以再创建 GitHub Release。

## 使用方式

JitPack 官方文档：

- https://docs.jitpack.io/
- https://jitpack.io/

### Maven

在使用方项目中添加仓库：

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

再添加依赖：

```xml
<dependency>
  <groupId>com.github.cossoledad</groupId>
  <artifactId>ElfOwl</artifactId>
  <version>v0.1.1</version>
</dependency>
```

说明：

- `groupId` 采用 JitPack 规则，对应 GitHub owner
- `artifactId` 通常对应仓库名
- `version` 通常对应 Git tag

如果你后续修改了仓库名、owner 或 tag 规则，依赖坐标也会随之变化。

### Gradle

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.cossoledad:ElfOwl:v0.1.1'
}
```

## GitHub Actions

当前工作流默认只做构建校验，不再自动发布到 GitHub Packages。

如果后续你准备好了 Maven Central，再补专门的正式发布工作流更合适。
