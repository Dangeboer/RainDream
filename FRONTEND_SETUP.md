# 前端目录说明（完全分离版）

你现在有两种方式把前端从后端仓库里彻底分出去：

## 方式 A：分成两个本地文件夹（你说的 Desktop/Project）

假设你本地路径是：
- 后端：`~/Desktop/Project/RainDream`
- 前端（目标）：`~/Desktop/Project/rain-dream-frontend`

在后端目录执行：

```bash
cd ~/Desktop/Project/RainDream
./scripts_export_frontend.sh ~/Desktop/Project/rain-dream-frontend
```

然后你就能在 VSCode 分别打开两个目录：
- `~/Desktop/Project/RainDream`（后端）
- `~/Desktop/Project/rain-dream-frontend`（前端）

---

## 方式 B：前端推到单独 GitHub 仓库（你可以直接 clone）

1) 先执行方式 A 导出前端。
2) 在 GitHub 新建空仓库（例如 `rain-dream-frontend`）。
3) 在本地前端目录执行：

```bash
cd ~/Desktop/Project/rain-dream-frontend
git init
git add .
git commit -m "feat: init standalone frontend"
git branch -M main
git remote add origin <你的前端仓库URL>
git push -u origin main
```

之后你就可以在任何地方直接：

```bash
git clone <你的前端仓库URL>
```

---

## 前端运行

```bash
cd ~/Desktop/Project/rain-dream-frontend
npm install
cp .env.example .env
npm run dev
```

默认后端地址：`http://localhost:8080`
