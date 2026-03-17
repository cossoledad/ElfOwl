# Publishing

这个项目已经内置了两套发布配置：

- GitHub Packages
- Maven Central

## 先决条件

### GitHub Packages

`pom.xml` 已经提供 `release-github-packages` profile，对应仓库：

```text
https://maven.pkg.github.com/ganjb/ElfOwl
```

本地发布时，你需要在 `~/.m2/settings.xml` 中配置 `github` 服务器凭证：

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

发布命令：

```bash
mvn -P release-github-packages clean deploy
```

### Maven Central

这个项目使用 Sonatype Central Portal 的 Maven 官方插件发布。

在真正发布前，你还需要先完成这几件事：

1. 在 Central Portal 注册账号
2. 验证并拥有一个可发布的 namespace
3. 生成 Portal User Token
4. 准备好 GPG 私钥

注意：

- 你当前的 `groupId` 是 `org.elfowl`
- 只有当 `org.elfowl` 是你在 Central Portal 中已验证并可发布的 namespace 时，才能成功发布
- 如果你还没有这个 namespace，建议先改成你实际拥有的 namespace 再发布

本地 `settings.xml` 示例：

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>YOUR_CENTRAL_TOKEN_USERNAME</username>
      <password>YOUR_CENTRAL_TOKEN_PASSWORD</password>
    </server>
  </servers>
</settings>
```

发布命令：

```bash
mvn -P release-maven-central clean deploy
```

## GitHub Actions Secrets

工作流需要以下 secrets：

### GitHub Packages

- `GITHUB_TOKEN`
  GitHub Actions 自动提供，无需手动创建

### Maven Central

- `CENTRAL_TOKEN_USERNAME`
- `CENTRAL_TOKEN_PASSWORD`
- `GPG_PRIVATE_KEY`
- `GPG_PASSPHRASE`

其中：

- `CENTRAL_TOKEN_USERNAME` 和 `CENTRAL_TOKEN_PASSWORD` 来自 Central Portal User Token
- `GPG_PRIVATE_KEY` 建议放 ASCII armor 格式的私钥全文
- `GPG_PASSPHRASE` 是对应私钥口令

## 工作流使用方式

仓库已内置发布工作流：

- [publish.yml](/home/ganjb/project/ElfOwl/.github/workflows/publish.yml)

触发方式：

1. 创建 GitHub Release
2. 或手动触发 `workflow_dispatch`

默认行为：

- `publish-github-packages` 会发布到 GitHub Packages
- `publish-maven-central` 会在 Central + GPG secrets 全部存在时发布到 Maven Central

## 消费方式

### 从 GitHub Packages 使用

使用方项目需要配置仓库：

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/ganjb/ElfOwl</url>
    </repository>
</repositories>
```

再添加依赖：

```xml
<dependency>
    <groupId>org.elfowl</groupId>
    <artifactId>elfowl-native-loader</artifactId>
    <version>0.1.0</version>
</dependency>
```

### 从 Maven Central 使用

发布成功后，使用方只需要：

```xml
<dependency>
    <groupId>org.elfowl</groupId>
    <artifactId>elfowl-native-loader</artifactId>
    <version>0.1.0</version>
</dependency>
```

Maven 默认就会从 Central 拉取，无需额外仓库配置。
