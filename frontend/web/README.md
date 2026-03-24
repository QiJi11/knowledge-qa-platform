# Todo 联调前端（已简化）

这个前端只保留一件事：打开一个页面，直接把后端的 Todo 接口跑通（列表/创建/更新/删除）。

约定：开发环境通过 Vite 代理访问后端：`/api -> http://localhost:3001`。

## 先启动后端（Node 版：backend）

```powershell
cd D:\soft\design\backend
npm install

# 必配：MySQL 密码（按你自己的改）
$env:MYSQL_PASSWORD="root"

# 可选：不配就走默认值（127.0.0.1:3306, root, design）
# $env:MYSQL_HOST="127.0.0.1"
# $env:MYSQL_PORT="3306"
# $env:MYSQL_USER="root"
# $env:MYSQL_DATABASE="design"

# 初始化表结构
npm run db:init

# 启动（默认端口 3001）
npm run dev
```

## 怎么跑

```bash
cd D:\soft\design\frontend\web
npm install
npm run dev
```

然后打开：

- `http://localhost:5173/todos`
