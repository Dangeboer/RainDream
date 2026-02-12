# 前端目录说明

前端已独立放在仓库根目录下的 `rain-dream-frontend/`，是一个单独的 Vue 3 项目。

- 后端：当前目录其余 Java/Spring 文件（`src/`、`pom.xml`）
- 前端：`rain-dream-frontend/`

## 在 VSCode 里单独打开前端
1. 打开 VSCode
2. `File -> Open Folder`
3. 选择 `rain-dream-frontend`

## 前端运行
```bash
cd rain-dream-frontend
npm install
cp .env.example .env
npm run dev
```

默认后端地址：`http://localhost:8080`
