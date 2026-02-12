# RainDream Frontend (Vue 3 + Vite)

这个目录是**独立前端项目**，和后端分开。

## 功能覆盖
- 登录 / 注册（JWT）
- Dashboard（最近 Fanfic + 视觉素材区块）
- 作品列表、详情、新增、编辑、删除
- Fanfic 列表、详情
- Tag 管理（增删改查）
- Platform 管理（增删改查）

## 运行
1. 进入前端目录
```bash
cd rain-dream-frontend
```

2. 安装依赖
```bash
npm install
```

3. 配置环境变量
```bash
cp .env.example .env
```

4. 启动开发服务器
```bash
npm run dev
```

默认 API 地址是 `http://localhost:8080`，可在 `.env` 里改 `VITE_API_BASE_URL`。

## 常见报错：`Failed to parse source ... Install @vitejs/plugin-vue`
如果你遇到 `.vue` 文件解析失败，通常是依赖没装完整或版本缓存冲突，按下面重装：

```bash
cd rain-dream-frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

如果你在仓库根目录误执行了 `npm run dev`，也会出现配置未生效，请务必在 `rain-dream-frontend/` 目录执行。

## 后端响应结构说明
当前后端会统一返回 `ApiResponse` 包装：

```json
{ "code": 0, "message": "success", "data": ... }
```

前端已在 `src/api/http.js` 里自动解包 `data`。如果你改了后端返回结构，需要同步调整这里。
