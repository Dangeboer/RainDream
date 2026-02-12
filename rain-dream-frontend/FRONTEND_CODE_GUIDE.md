# 前端代码改动指南（我该改哪里）

这份文档是给你后续自己改功能用的「定位地图」。

## 1. 启动与入口
- `src/main.js`：前端总入口，挂载 Vue/Pinia/Router/Element Plus。
- `src/App.vue`：只负责渲染路由出口（`<router-view />`）。

## 2. 路由与页面跳转
- `src/router/index.js`
  - 所有页面路由都在这里。
  - 登录拦截逻辑也在这里（没 token 跳回 `/login`）。

常见改动：
- 新页面：在 `views/` 新建 `.vue` 文件，然后在这里加一条 route。

## 3. 后端请求
- `src/api/http.js`
  - 全局 axios 配置。
  - 自动加 JWT 到请求头。
  - 自动处理后端统一返回 `{ code, message, data }`。
  - 401 时自动清理登录态并跳登录。

- `src/api/auth.js`：登录、注册接口。
- `src/api/item.js`：作品 / fanfic 的增删改查。
- `src/api/meta.js`：标签、平台增删改查。

常见改动：
- 后端新增接口：优先在 `api/*.js` 补一个函数，再到页面调用。

## 4. 登录状态
- `src/stores/auth.js`
  - 登录成功后把 token 存到 `localStorage`。
  - 退出登录时清 token。

常见改动：
- 要改 token key / 用户信息存储：在这里改。

## 5. 页面结构（你最常改）
- `src/components/LayoutShell.vue`
  - 左侧菜单、顶部搜索、主区域壳子。

- `src/views/LoginView.vue`：登录/注册页。
- `src/views/DashboardView.vue`：首页仪表盘。
- `src/views/ItemListView.vue`：作品列表。
- `src/views/ItemFormView.vue`：新增/编辑作品表单。
- `src/views/ItemDetailView.vue`：作品详情。
- `src/views/FanficListView.vue`：fanfic 列表。
- `src/views/FanficDetailView.vue`：fanfic 详情。
- `src/views/TagManageView.vue`：标签管理。
- `src/views/PltManageView.vue`：平台管理。

常见改动：
- 改页面显示文案、按钮、布局：去对应 `views/*.vue`。
- 改菜单项：`LayoutShell.vue`。

## 6. 主题样式
- `src/assets/theme.css`
  - 全局颜色变量、暗色风格、Element Plus 覆盖样式都在这。

常见改动：
- 改整体配色：优先改 `:root` 变量。

## 7. 你可以按这个顺序改功能（推荐）
1) 先确认后端接口路径和字段。  
2) 在 `src/api/*.js` 写请求函数。  
3) 在 `src/views/*.vue` 调接口、绑定到页面。  
4) 样式最后去 `theme.css` 微调。  

---

如果你想，我下一步可以再给你补一份「按你的业务术语（同人文/图/混剪/链接）对应字段解释」文档。
