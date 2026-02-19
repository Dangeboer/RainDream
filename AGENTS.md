# AGENTS.md

## Scope

- 本文件作用于仓库根目录及其所有子目录。
- 若子目录存在更近的 `AGENTS.md`，就近规则优先。

## Work Style

- 回答简洁直接，先给结论再给细节。
- 修改代码时优先最小改动，不做无关重构。

## Build & Test

- 不要每次改动后都运行 `npm run build`。
- 仅在以下情况运行构建/测试：
  - 我明确要求
  - 改动范围较大（跨多个模块）
  - 你判断回归风险较高

## Frontend Conventions

- 下载文件名优先级：`title > fileName > URL 默认名`
- 展示标题优先级：`title > fileName(去后缀) > 未命名资源`

## Change Safety

- 不要改动与当前需求无关的文件。
- 不要删除现有功能，除非我明确要求。
- 不要执行破坏性 git 命令（如 `reset --hard`）。

## Communication

- 改动前先说计划（一两句话即可），如果不理解我的话或者存疑，先问清楚。
- 改动后列出：
  - 改了哪些文件
  - 为什么这么改
  - 对代码做适当讲解
