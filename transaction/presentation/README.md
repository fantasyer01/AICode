# 数据库事务基础技术分享 PPT

一个基于 HTML/CSS/JavaScript 的交互式技术演示文稿，用于介绍数据库事务的基础知识、实现原理和实践应用。

## 特性

✅ **交互式导航** - 支持键盘、鼠标、触摸操作  
✅ **精美动画** - 流畅的过渡效果和元素动画  
✅ **响应式设计** - 适配各种屏幕尺寸  
✅ **ECharts 图表** - 丰富的数据可视化  
✅ **浅蓝色主题** - 清淡柔和的配色方案  
✅ **打印友好** - 支持导出为 PDF  

## 目录结构

```
presentation/
├── index.html          # 主 HTML 文件
├── css/
│   ├── main.css       # 主样式文件
│   ├── theme.css      # 主题配色
│   └── animations.css # 动画效果
├── js/
│   ├── app.js         # 应用逻辑
│   ├── navigation.js  # 导航控制
│   ├── charts.js      # 图表渲染
│   └── animations.js  # 动画控制
├── assets/
│   └── diagrams/      # 图表数据
└── README.md          # 使用说明
```

## 快速开始

### 方法 1：直接打开（推荐）

```powershell
# Windows PowerShell
cd D:\code\AICode\transaction\presentation
Start-Process index.html
```

### 方法 2：使用本地服务器

```powershell
# 使用 Python
cd D:\code\AICode\transaction\presentation
python -m http.server 8000

# 使用 Node.js (需要安装 http-server)
npx http-server -p 8000

# 使用 VS Code Live Server
# 右键 index.html -> Open with Live Server
```

然后在浏览器中访问：`http://localhost:8000`

## 使用指南

### 导航操作

| 操作 | 快捷键 |
|-----|-------|
| 下一张幻灯片 | `→` / `Space` / 点击页面 |
| 上一张幻灯片 | `←` |
| 第一张幻灯片 | `Home` |
| 最后一张幻灯片 | `End` |
| 打开目录导航 | 点击左上角 `☰` |
| 显示帮助 | `H` / `?` |
| 全屏模式 | `F11` |
| 打印/导出 PDF | `Ctrl + P` |

### 交互功能

- **鼠标悬停**：在卡片、图表上悬停查看详细信息
- **侧边栏导航**：点击左上角菜单图标快速跳转章节
- **进度指示**：底部进度条显示当前位置
- **章节指示器**：顶部显示当前章节名称

## 内容章节

### 第一章：事务基础与并发挑战
- 事务的定义与 ACID 特性
- 并发引发的问题（脏读、不可重复读、幻读）
- 四种事务隔离级别对比

### 第二章：深入单机事务引擎
- ACID 落地原理（Redo Log、Undo Log、MVCC、锁）
- WAL 机制详解
- MVCC 多版本并发控制
- 锁机制（行锁、间隙锁、Next-Key Lock）

### 第三章：Spring 事务实战与避坑
- Spring 声明式事务原理
- 传播行为详解
- 经典踩坑场景与解决方案

### 第四章：分布式事务概览
- CAP 定理与 BASE 理论
- 2PC/3PC 协议
- TCC、Saga、事务消息
- Seata 框架介绍

### 第五章：总结与参考
- 知识点回顾
- 实践建议
- 推荐书籍与资源

## 打印和导出

### 导出为 PDF

1. 打开演示文稿
2. 按 `Ctrl + P` 打开打印对话框
3. 选择 "打印到 PDF" 或 "Microsoft Print to PDF"
4. 调整设置：
   - 布局：横向
   - 边距：无
   - 背景图形：启用
5. 点击打印/保存

### 打印样式优化

- 每张幻灯片自动占一页
- 隐藏导航元素和交互控件
- 保留所有颜色和图表
- 优化字体大小以适应打印

## 浏览器兼容性

| 浏览器 | 最低版本 | 状态 |
|-------|---------|------|
| Chrome | 90+ | ✅ 完全支持 |
| Firefox | 88+ | ✅ 完全支持 |
| Edge | 90+ | ✅ 完全支持 |
| Safari | 14+ | ✅ 支持（部分 CSS 特性需要前缀） |

## 自定义和扩展

### 修改主题颜色

编辑 `css/theme.css` 中的 CSS 变量：

```css
:root {
    --primary-blue: #4A90E2;    /* 主题色 */
    --primary-bg: #F0F7FF;      /* 背景色 */
    /* ... 其他颜色变量 */
}
```

### 添加新幻灯片

在 `index.html` 中添加新的 `<section>` 元素：

```html
<section class="slide" data-slide-id="X-Y" data-chapter="X">
    <div class="slide-content">
        <!-- 你的内容 -->
    </div>
</section>
```

### 添加新图表

在 `js/charts.js` 中定义新的图表配置和渲染方法。

## 技术栈

- **HTML5** - 语义化标记
- **CSS3** - Flexbox/Grid 布局、动画、变量
- **JavaScript (ES6+)** - 模块化、类、Promise
- **ECharts 5.4** - 数据可视化

## 性能优化

- ✅ 懒加载图表（进入视口时渲染）
- ✅ CSS 动画（硬件加速）
- ✅ 响应式图片
- ✅ 最小化 DOM 操作
- ✅ 事件委托

## 故障排除

### 幻灯片不显示
- 检查浏览器控制台是否有错误
- 确保 ECharts CDN 加载成功
- 清除浏览器缓存后重试

### 导航不工作
- 确保 JavaScript 文件加载成功
- 检查是否有 JavaScript 错误
- 尝试在无痕模式下打开

### 动画卡顿
- 关闭浏览器的硬件加速
- 减少同时播放的动画数量
- 使用性能更好的浏览器（如 Chrome）

## 许可证

本演示文稿用于教育和技术分享目的。

## 联系方式

如有问题或建议，请联系：
- 📧 邮箱：your-email@example.com
- 🔗 GitHub：github.com/yourusername

---

**Built with ❤️ for database transaction education**
