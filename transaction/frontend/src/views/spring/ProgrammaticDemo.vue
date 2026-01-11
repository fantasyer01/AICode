<template>
  <div class="programmatic-demo">
    <el-page-header @back="$router.back()" title="Back">
      <template #content>
        <span class="page-title">Programmatic Transaction Management</span>
      </template>
    </el-page-header>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>Transaction Management Approaches</span>
      </template>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-button 
            type="primary" 
            plain 
            @click="runDemo('declarative')" 
            :loading="loading && currentDemo === 'declarative'"
            style="width: 100%; margin-bottom: 10px;"
          >
            Declarative (@Transactional)
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button 
            type="success" 
            plain 
            @click="runDemo('transaction-template')" 
            :loading="loading && currentDemo === 'transaction-template'"
            style="width: 100%; margin-bottom: 10px;"
          >
            TransactionTemplate
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button 
            type="warning" 
            plain 
            @click="runDemo('platform-tx-manager')" 
            :loading="loading && currentDemo === 'platform-tx-manager'"
            style="width: 100%; margin-bottom: 10px;"
          >
            PlatformTransactionManager
          </el-button>
        </el-col>
      </el-row>

      <el-alert 
        title="Transaction Management Methods" 
        type="info" 
        :closable="false"
        style="margin-top: 20px;"
      >
        <ul style="margin: 0; padding-left: 20px;">
          <li><strong>Declarative (@Transactional)</strong>: Simplest approach using annotations, suitable for most scenarios</li>
          <li><strong>TransactionTemplate</strong>: Programmatic control with template pattern, provides more flexibility</li>
          <li><strong>PlatformTransactionManager</strong>: Lowest level API, maximum control over transaction lifecycle</li>
        </ul>
      </el-alert>
    </el-card>

    <el-card v-if="demoResult" style="margin-top: 20px;">
      <template #header>
        <span>Demo Result: {{ demoResult.scenario }}</span>
      </template>

      <el-alert 
        :title="demoResult.explanation" 
        :type="demoResult.success ? 'success' : 'error'" 
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-tabs v-model="activeTab">
        <el-tab-pane label="Execution Steps" name="steps">
          <el-timeline>
            <el-timeline-item 
              v-for="(step, index) in demoResult.steps" 
              :key="index"
              :timestamp="step.duration ? `${step.duration}ms` : ''"
              :type="getStepType(step.status)"
            >
              <h4>Step {{ step.stepNumber }}: {{ step.description }}</h4>
              <p v-if="step.details">{{ step.details }}</p>
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>

        <el-tab-pane label="Transaction Logs" name="logs">
          <el-card shadow="never" class="log-console">
            <div v-for="(log, index) in demoResult.logs" :key="index" class="log-entry">
              <el-icon><Document /></el-icon>
              <span>{{ log }}</span>
            </div>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="Code Example" name="code">
          <el-card shadow="never">
            <pre class="code-block"><code>{{ demoResult.codeSnippet }}</code></pre>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="Database State" name="state" v-if="demoResult.databaseState">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card>
                <template #header>Before Transaction</template>
                <pre>{{ JSON.stringify(demoResult.databaseState.before, null, 2) }}</pre>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card>
                <template #header>After Transaction</template>
                <pre>{{ JSON.stringify(demoResult.databaseState.after, null, 2) }}</pre>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>Comparison & Best Practices</span>
      </template>

      <el-table :data="comparisonData" border style="width: 100%">
        <el-table-column prop="aspect" label="Aspect" width="180" />
        <el-table-column prop="declarative" label="@Transactional" />
        <el-table-column prop="template" label="TransactionTemplate" />
        <el-table-column prop="manager" label="PlatformTransactionManager" />
      </el-table>

      <el-alert 
        title="When to Use Each Approach" 
        type="success" 
        :closable="false"
        style="margin-top: 20px;"
      >
        <ul style="margin: 0; padding-left: 20px;">
          <li><strong>Use @Transactional</strong>: For 90% of cases, simple and clean</li>
          <li><strong>Use TransactionTemplate</strong>: When you need programmatic control with callback structure</li>
          <li><strong>Use PlatformTransactionManager</strong>: For complex transaction logic requiring fine-grained control</li>
        </ul>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { springDemoAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'

const loading = ref(false)
const currentDemo = ref('')
const demoResult = ref(null)
const activeTab = ref('steps')

const comparisonData = [
  {
    aspect: 'Ease of Use',
    declarative: 'Very Easy - just add annotation',
    template: 'Moderate - callback structure',
    manager: 'Complex - manual lifecycle'
  },
  {
    aspect: 'Flexibility',
    declarative: 'Limited - annotation-based',
    template: 'Good - programmatic control',
    manager: 'Maximum - full control'
  },
  {
    aspect: 'Code Readability',
    declarative: 'Excellent - clean code',
    template: 'Good - callback pattern',
    manager: 'Fair - verbose code'
  },
  {
    aspect: 'Use Case',
    declarative: 'Standard CRUD operations',
    template: 'Dynamic transaction logic',
    manager: 'Complex multi-step workflows'
  },
  {
    aspect: 'Error Handling',
    declarative: 'Automatic rollback',
    template: 'Callback-based handling',
    manager: 'Manual rollback control'
  }
]

const runDemo = async (type) => {
  loading.value = true
  currentDemo.value = type
  try {
    let result
    switch (type) {
      case 'declarative':
        result = await springDemoAPI.demonstrateDeclarative()
        break
      case 'transaction-template':
        result = await springDemoAPI.demonstrateTransactionTemplate()
        break
      case 'platform-tx-manager':
        result = await springDemoAPI.demonstratePlatformTxManager()
        break
      default:
        throw new Error('Unknown demo type: ' + type)
    }
    demoResult.value = result
    ElMessage.success('Demo executed successfully')
  } catch (error) {
    ElMessage.error('Failed to execute demo: ' + (error.message || 'Unknown error'))
    console.error('Demo error:', error)
  } finally {
    loading.value = false
  }
}

const getStepType = (status) => {
  const typeMap = {
    'STARTED': 'primary',
    'SUCCESS': 'success',
    'IN_PROGRESS': 'warning',
    'ERROR': 'danger'
  }
  return typeMap[status] || 'info'
}
</script>

<style scoped lang="scss">
.programmatic-demo {
  .page-title {
    font-size: 18px;
    font-weight: 600;
  }

  .log-console {
    background: #1e1e1e;
    color: #d4d4d4;
    font-family: 'Courier New', monospace;
    padding: 16px;
    max-height: 400px;
    overflow-y: auto;

    .log-entry {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 8px 0;
      font-size: 13px;
    }
  }

  .code-block {
    background: #f5f7fa;
    padding: 16px;
    border-radius: 4px;
    overflow-x: auto;
    font-family: 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.5;
    margin: 0;
  }
}
</style>
