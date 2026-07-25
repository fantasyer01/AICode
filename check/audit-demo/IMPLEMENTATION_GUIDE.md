# 方案 B 实现指南 - 后端增强的业务友好审核展示

## 📋 实现概述

本方案通过**后端增强快照数据**和**前端智能渲染**，将审核页面从技术化的 JSON 展示升级为业务友好的可视化界面，并支持 UPDATE 操作的前后对比展示（差异红色高亮）。

---

## 🏗️ 架构设计

### 核心组件

```
┌─────────────────────────────────────────────────────┐
│                  AuditAspect                        │
│  拦截 @AuditRequired 方法                            │
│  调用 SnapshotBuilder 生成业务快照                   │
└──────────────┬──────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────┐
│              SnapshotBuilder (接口)                  │
│  - UserSnapshotBuilder (单表: 用户管理)              │
│  - OrderSnapshotBuilder (多表: 订单管理)             │
└──────────────┬──────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────┐
│              SnapshotData (数据模型)                 │
│  - DisplayField: 字段展示 (label/value/oldValue)    │
│  - SubSection: 子表数据 (订单明细)                   │
└──────────────┬──────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────┐
│         存储到 sys_audit_request.snapshot_data       │
└──────────────┬──────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────┐
│         AuditController 传递到前端                   │
└──────────────┬──────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────┐
│    前端 JavaScript 渲染业务友好视图                  │
│  - 单表: 字段卡片展示                                │
│  - 多表: 主表 + 明细表格                             │
│  - UPDATE: 前后对比 + 红色差异高亮                   │
└─────────────────────────────────────────────────────┘
```

---

## 📁 新增文件列表

### 后端 Java 文件

1. **`DisplayField.java`** - 字段展示模型
   - 位置: `audit/model/DisplayField.java`
   - 功能: 表示单个业务字段（标签、值、旧值、是否变更）

2. **`SnapshotData.java`** - 快照数据模型
   - 位置: `audit/model/SnapshotData.java`
   - 功能: 完整的业务快照结构（主字段 + 子表数据）

3. **`SnapshotBuilder.java`** - 快照构建器接口
   - 位置: `audit/snapshot/SnapshotBuilder.java`
   - 功能: 定义快照构建规范

4. **`UserSnapshotBuilder.java`** - 用户快照构建器
   - 位置: `audit/snapshot/UserSnapshotBuilder.java`
   - 功能: 生成用户管理的业务快照（含 UPDATE 对比）

5. **`OrderSnapshotBuilder.java`** - 订单快照构建器
   - 位置: `audit/snapshot/OrderSnapshotBuilder.java`
   - 功能: 生成订单管理的业务快照（含订单明细对比）

### 修改的文件

1. **`AuditAspect.java`** - 切面类
   - 修改: 集成 SnapshotBuilder，生成业务友好快照

2. **`AuditController.java`** - 控制器
   - 修改: 传递 snapshotData 到前端

3. **`detail.html`** - 审核详情页
   - 修改: 添加业务数据展示卡片 + JavaScript 渲染逻辑

4. **`style.css`** - 样式文件
   - 修改: 添加对比视图样式（红色高亮、删除线等）

---

## 🎯 核心功能特性

### 1. INSERT 操作 - 新增数据展示

**用户新增示例：**
```
╔══════════════ 用户信息 ══════════════╗
║  用户名：zhangsan                   ║
║  姓名：张三                         ║
║  邮箱：zhangsan@example.com        ║
║  电话：13800138000                  ║
║  部门：技术部                       ║
║  状态：[启用] (绿色徽章)            ║
╚═════════════════════════════════════╝
```

**订单新增示例：**
```
╔══════════════ 订单主信息 ═══════════╗
║  订单号：ORD20260121001             ║
║  客户名称：阿里巴巴                 ║
║  收货地址：杭州市西湖区...          ║
║  订单状态：[待确认] (黄色徽章)      ║
╚═════════════════════════════════════╝

╔══════════════ 订单明细 ═════════════╗
║  商品名称  | 规格  | 单价  | 数量   ║
║─────────────┼──────┼──────┼───────║
║  iPhone15  | 256G | ¥5999| 1     ║
║  AirPods   | Pro  | ¥1299| 2     ║
╚═════════════════════════════════════╝
总金额: ¥8597.00
```

### 2. UPDATE 操作 - 前后对比 + 红色差异高亮

**用户修改示例：**
```
╔══════════════ 用户信息 (修改) ═══════╗
║  用户名：zhangsan                   ║
║                                     ║
║  邮箱：                             ║
║    old@example.com (删除线灰色)     ║
║    new@example.com (红色粗体) ✗    ║
║    [Changed] (红色徽章)             ║
║                                     ║
║  电话：                             ║
║    13800138000 (删除线灰色)         ║
║    13900139000 (红色粗体) ✗        ║
║    [Changed] (红色徽章)             ║
║                                     ║
║  部门：技术部 (无变化)              ║
╚═════════════════════════════════════╝
```

**订单修改示例（含明细对比）：**
```
╔════ 订单明细 (Before vs After) ═════╗
║                                     ║
║  Before (Original)    After (New)   ║
║  ──────────────────  ───────────────║
║  iPhone 15 × 1       iPhone 15 × 2  ║
║  AirPods × 2         MacBook × 1    ║
║                                     ║
║  总金额: ¥8597       总金额: ¥21997 ║
╚═════════════════════════════════════╝
```

### 3. DELETE 操作 - 删除数据展示

显示即将被删除的完整数据信息，样式同 INSERT。

---

## 🔧 关键实现细节

### 后端 - UPDATE 对比如何实现

**UserSnapshotBuilder.java 示例：**
```java
private SnapshotData buildUpdateSnapshot(BizUser newUser) {
    // 1. 从数据库查询旧数据
    BizUser oldUser = userMapper.selectById(newUser.getId());
    
    // 2. 逐字段对比
    fields.add(createCompareField("邮箱", 
            oldUser.getEmail(),  // 旧值
            newUser.getEmail())); // 新值
    
    // 3. 标记是否变更
    private DisplayField createCompareField(String label, String oldValue, String newValue) {
        DisplayField field = new DisplayField();
        field.setLabel(label);
        field.setValue(newValue);
        field.setOldValue(oldValue);
        field.setChanged(!Objects.equals(oldValue, newValue)); // 自动检测变更
        return field;
    }
}
```

### 前端 - 红色差异高亮渲染

**detail.html JavaScript 示例：**
```javascript
function renderField(field, operationType) {
    if (operationType === 'UPDATE' && field.changed === true) {
        // UPDATE 模式且字段有变化
        return `
            <div class="comparison-view">
                <div class="old-value text-muted text-decoration-line-through">
                    ${field.oldValue}
                </div>
                <div class="new-value text-danger fw-bold">
                    ${field.value}
                </div>
                <span class="badge bg-danger">Changed</span>
            </div>
        `;
    } else {
        // 无变化，正常显示
        return escapeHtml(field.value);
    }
}
```

**CSS 样式 (style.css)：**
```css
.comparison-view .old-value {
    font-size: 0.9rem;
    text-decoration: line-through; /* 删除线 */
    color: #6c757d; /* 灰色 */
}

.comparison-view .new-value {
    font-size: 1.05rem;
    font-weight: bold !important;
    color: #dc3545 !important; /* 红色 */
}
```

---

## 🚀 使用方式

### 1. 查看审核详情

1. 进入"待审核"页面
2. 点击任一记录的"查看"按钮
3. 页面自动显示：
   - **业务数据卡片**（默认显示）
   - **技术详情卡片**（点击"Show Technical Details"按钮切换）

### 2. UPDATE 对比查看

- **单字段对比**：
  - 旧值：灰色 + 删除线
  - 新值：红色 + 粗体
  - 变更标记：红色 "Changed" 徽章

- **多表对比**（订单明细）：
  - 左右分栏：Before / After
  - 表格对比：逐行查看差异

### 3. 技术人员调试

点击"Show Technical Details"按钮，查看：
- Bean Name / Method Name
- 原始 JSON 参数

---

## 📊 数据流向

```
用户提交表单
    ↓
Service 方法被 @AuditRequired 拦截
    ↓
AuditAspect 调用 SnapshotBuilder
    ↓
SnapshotBuilder 生成 SnapshotData
    - INSERT: 直接格式化新数据
    - UPDATE: 查询旧数据 + 逐字段对比
    - DELETE: 查询待删除数据
    ↓
序列化为 JSON 存入 snapshot_data 字段
    ↓
AuditController 读取并传递到前端
    ↓
JavaScript 解析并渲染业务视图
    - 渲染主字段
    - 渲染子表数据
    - 应用红色差异高亮
```

---

## 🎨 前端交互特性

### 视图切换
- **默认**：业务友好视图（卡片式）
- **切换**：技术详情视图（JSON）
- **按钮**：一键切换，状态记忆

### 响应式设计
- 桌面：双列布局
- 移动：单列自适应

### 性能优化
- 快照数据预生成（无需前端计算）
- 按需加载技术详情

---

## 📝 扩展新业务类型

### 步骤 1：创建 SnapshotBuilder

```java
@Component
public class NewBusinessSnapshotBuilder implements SnapshotBuilder {
    
    @Override
    public boolean supports(String businessType) {
        return "NEW_BUSINESS".equals(businessType);
    }
    
    @Override
    public SnapshotData buildSnapshot(String operationType, Object[] args) {
        // 实现业务特定的快照生成逻辑
    }
}
```

### 步骤 2：自动注册

Spring 会自动扫描并注入到 `AuditAspect` 的 `snapshotBuilders` 列表。

### 步骤 3：前端自动适配

前端渲染器会自动解析 `SnapshotData` 结构，无需修改前端代码。

---

## ✅ 测试检查清单

- [ ] INSERT 操作：用户新增展示正常
- [ ] INSERT 操作：订单新增展示正常（含明细）
- [ ] UPDATE 操作：用户修改对比正常，差异红色高亮
- [ ] UPDATE 操作：订单修改对比正常（含明细对比）
- [ ] DELETE 操作：用户删除展示正常
- [ ] DELETE 操作：订单删除展示正常
- [ ] 技术视图切换：按钮功能正常
- [ ] 响应式布局：移动端显示正常

---

## 🐛 常见问题

### Q1: UPDATE 时旧数据为空？
**A**: 确保数据库中存在原始记录，`UserSnapshotBuilder` 会从数据库查询。

### Q2: 红色高亮不显示？
**A**: 检查 `style.css` 是否正确加载，浏览器控制台是否有 CSS 错误。

### Q3: 订单明细对比不显示？
**A**: 确认 `OrderSnapshotBuilder` 正确设置了 `SubSection.oldRows`。

### Q4: 前端显示 "Failed to load business data"？
**A**: 检查浏览器控制台 JavaScript 错误，确认 `snapshotJson` 格式正确。

---

## 🎯 优势总结

✅ **业务友好**：非技术人员也能看懂  
✅ **差异高亮**：UPDATE 变更一目了然（红色醒目）  
✅ **代码复用**：字段映射集中管理  
✅ **易于扩展**：新增业务类型只需实现 SnapshotBuilder  
✅ **性能优越**：后端预生成，前端轻量渲染  
✅ **双视图**：业务视图 + 技术视图自由切换  

---

## 📞 技术支持

如有问题，请检查：
1. 浏览器控制台错误日志
2. 后端 Spring Boot 日志
3. 数据库 `snapshot_data` 字段内容

**祝您使用愉快！** 🎉
