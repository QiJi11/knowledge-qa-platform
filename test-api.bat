@echo off
chcp 65001 >nul
echo ========================================
echo 在线学习平台 - 接口测试脚本
echo ========================================
echo.

where curl >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 没找到 curl，请先安装或确保 curl 在 PATH 里
    pause
    exit /b 1
)

set HAS_JQ=0
where jq >nul 2>&1
if %errorlevel% equ 0 (
    set HAS_JQ=1
) else (
    echo ⚠️  没找到 jq，将直接打印原始 JSON（不影响接口是否可用）
)

set BASE_URL=http://localhost:3001/api/v1

call :test_api "[测试 1/5] 测试课程详情接口..." "%BASE_URL%/courses/1001"
call :test_api "[测试 2/5] 测试课程列表接口..." "%BASE_URL%/courses?page=1&pageSize=5"
call :test_api "[测试 3/5] 测试总览统计接口..." "%BASE_URL%/learners/1/stats/overview"
call :test_api "[测试 4/5] 测试按课程聚合统计接口..." "%BASE_URL%/learners/1/stats/by-course"
call :test_api "[测试 5/5] 测试趋势统计接口..." "%BASE_URL%/learners/1/stats/trend?from=2026-01-10&to=2026-01-17"

echo ========================================
echo 测试完成
echo ========================================
pause
goto :eof

:test_api
set "TITLE=%~1"
set "URL=%~2"
set "TMP_FILE=%TEMP%\design_api_test_%RANDOM%_%RANDOM%.json"
set "HTTP_STATUS="

echo %TITLE%

REM 用 HTTP 状态码判断是否成功，避免 curl 只看 errorlevel 误判
for /f %%s in ('curl -s -o "%TMP_FILE%" -w "%%{http_code}" "%URL%"') do set "HTTP_STATUS=%%s"

if "%HTTP_STATUS%"=="200" (
    if "%HAS_JQ%"=="1" (
        type "%TMP_FILE%" | jq .
    ) else (
        type "%TMP_FILE%"
    )
    echo ✅ HTTP 200
) else (
    if "%HTTP_STATUS%"=="000" (
        echo ❌ 请求失败（大概率是后端没启动，或者端口/地址不对）
    ) else (
        echo ❌ HTTP %HTTP_STATUS%
    )
    if exist "%TMP_FILE%" type "%TMP_FILE%"
)

del "%TMP_FILE%" >nul 2>&1
echo.
exit /b 0
