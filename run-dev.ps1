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
if (-not $env:MYSQL_HOST) { $env:MYSQL_HOST = '127.0.0.1' }
if (-not $env:MYSQL_PORT) { $env:MYSQL_PORT = '3306' }
if (-not $env:MYSQL_USER) { $env:MYSQL_USER = 'root' }
if (-not $env:MYSQL_DATABASE) { $env:MYSQL_DATABASE = 'design' }

# 小白友好：没设置就交互输入（不回显，不写入文件）
if (-not $env:MYSQL_PASSWORD) {
  $secure = Read-Host '请输入 MySQL 密码（不会回显）' -AsSecureString
  $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
  try {
    $plain = [System.Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
  } finally {
    [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr) | Out-Null
  }

  if (-not $plain -or $plain.Trim().Length -eq 0) {
    Write-Host 'MySQL 密码不能为空。' -ForegroundColor Yellow
    exit 1
  }

  $env:MYSQL_PASSWORD = $plain
}

# 3) 启动
mvn -q spring-boot:run

