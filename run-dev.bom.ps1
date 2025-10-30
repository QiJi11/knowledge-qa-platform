# 运行 Java 后端（backend-java）
#
# 用法：
#   1) 先按需设置 MySQL 环境变量（至少要有 MYSQL_PASSWORD）
#   2) 在本目录执行： .\run-dev.ps1

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

# 1) 确保 Maven 用的是 JDK（不是 JRE）
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
  $candidate = 'D:\Program Files\Java\jdk-17'
  if (Test-Path (Join-Path $candidate 'bin\javac.exe')) {
    $env:JAVA_HOME = $candidate
  }
}

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
  Write-Host '没找到 javac（需要安装 JDK 17，并让 PATH/JAVA_HOME 指到它）' -ForegroundColor Yellow
  exit 1
}

# 2) MySQL 必要参数检查
if (-not $env:MYSQL_PASSWORD) {
  Write-Host '你还没设置 MYSQL_PASSWORD，先设置一下再跑。例子：' -ForegroundColor Yellow
  Write-Host "$env:MYSQL_PASSWORD='<你的MySQL密码>'" -ForegroundColor Yellow
  exit 1
}

# 3) 启动
mvn -q spring-boot:run

