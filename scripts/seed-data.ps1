# backend-java：导入种子数据（课程 + 学习任务）
#
# 给小白的说明：
# 1) 这一步是可选的，但强烈建议先导入一次，否则前端页面可能“没有数据”。
# 2) 导入会覆盖演示数据（会删除 learnerId=1 的任务，并删除 courses 1001~1010）。
# 3) 运行时会提示你输入 MySQL 密码，不会把密码写入文件。

$ErrorActionPreference = 'Stop'

function Require-Command([string]$name, [string]$hint) {
  if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
    Write-Host "缺少命令：$name" -ForegroundColor Yellow
    Write-Host "处理建议：$hint" -ForegroundColor Yellow
    exit 1
  }
}

Require-Command 'mysql' '请安装 MySQL 客户端（mysql.exe），并确保 mysql 在 PATH 中可用。'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectDir = Split-Path -Parent $scriptDir
$seedFile = Join-Path $projectDir 'src\main\resources\db\seed_data.sql'

if (-not (Test-Path $seedFile)) {
  throw "Not found: $seedFile"
}

Write-Host '即将导入种子数据到数据库 design。' -ForegroundColor Cyan
Write-Host '注意：会删除 learnerId=1 的学习任务，并删除 courses 1001~1010。' -ForegroundColor Yellow
$confirm = Read-Host '确认继续？输入 y 继续，其它任意键取消'
if ($confirm -ne 'y') {
  Write-Host '已取消。' -ForegroundColor Yellow
  exit 0
}

# 1) 先确保数据库存在（mysql 会提示输入密码）
mysql -uroot -p -e 'CREATE DATABASE IF NOT EXISTS design;'
if ($LASTEXITCODE -ne 0) {
  throw '创建数据库失败（请检查 MySQL 是否启动、账号是否正确）。'
}

# 2) 导入种子数据（用 cmd 的重定向更稳）
$seedFileForCmd = $seedFile -replace '\\', '/'
# 注意：PowerShell 里不能用 \" 来转义双引号（会导致解析异常），这里用字符串拼接生成 cmd 命令
$cmdLine = 'mysql -uroot -p --default-character-set=utf8mb4 < "' + $seedFileForCmd + '"'
cmd /c $cmdLine
if ($LASTEXITCODE -ne 0) {
  throw '导入失败（请检查密码/权限/seed_data.sql 是否可读）。'
}

Write-Host '导入完成 ✅' -ForegroundColor Green
Write-Host '你现在可以：' -ForegroundColor Green
Write-Host '- 重新启动后端（或保持运行）' -ForegroundColor Green
Write-Host '- 打开学员端 /stats 查看统计变化' -ForegroundColor Green

