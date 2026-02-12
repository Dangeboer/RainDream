# RainDream Frontend (Vue 3 + Vite)

## 功能覆盖
- 登录 / 注册（JWT）
- Dashboard（最近 Fanfic + 视觉素材区块）
- 作品列表、详情、新增、编辑、删除
- Fanfic 列表、详情
- Tag 管理（增删改查）
- Platform 管理（增删改查）

## 运行
1. 安装依赖
```bash
npm install
```

2. 配置环境变量
```bash
cp .env.example .env
```

3. 启动开发服务器
```bash
npm run dev
```

默认 API 地址是 `http://localhost:8080`，可在 `.env` 里改 `VITE_API_BASE_URL`。
