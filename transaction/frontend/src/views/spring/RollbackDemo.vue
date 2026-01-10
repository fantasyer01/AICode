<template>
  <div class="rollback-demo">
    <el-page-header @back="$router.back()" title="Back">
      <template #content>
        <span class="page-title">Transaction Rollback Rules & Pitfalls</span>
      </template>
    </el-page-header>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>Rollback Rules Demonstrations</span>
      </template>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-button 
            type="primary" 
            plain 
            @click="runDemo('default-success')" 
            :loading="loading && currentDemo === 'default-success'"
            style="width: 100%; margin-bottom: 10px;"
          >
            Default (No Exception)
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button 
            type="danger" 
            plain 
            @click="runDemo('default-fail')" 
            :loading="loading && currentDemo === 'default-fail'"
            style="width: 100%; margin-bottom: 10px;"
          >
            Default (With Exception)
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button 
            type="warning" 
            plain 
            @click="runDemo('checked-exception')" 
            :loading="loading && currentDemo === 'checked-exception'"
            style="width: 100%; margin-bottom: 10px;"
          >
            Checked Exception
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button 
            type="success" 
            plain 
            @click="runDemo('rollback-for')" 
            :loading="loading && currentDemo === 'rollback-for'"
            style="width: 100%; margin-bottom: 10px;"
          >
            rollbackFor
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button 
            type="info" 
            plain 
            @click="runDemo('no-rollback-for-success')" 
            :loading="loading && currentDemo === 'no-rollback-for-success'"
            style="width: 100%; margin-bottom: 10px;"
          >
            noRollbackFor (Success)
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button 
            type="warning" 
            plain 
            @click="runDemo('no-rollback-for-fail')" 
            :loading="loading && currentDemo === 'no-rollback-for-fail'"
            style="width: 100%; margin-bottom: 10px;"
          >
            noRollbackFor (With Exception)
          </el-button>
        </el-col>
      </el-row>

      <el-alert 
        title="Rollback Rules Guide" 
        type="info" 
        :closable="false"
        style="margin-top: 20px;"
      >
        <ul style="margin: 0; padding-left: 20px;">
          <li><strong>Default behavior</strong>: Rolls back only for RuntimeException and Error</li>
          <li><strong>Checked Exception</strong>: Does not trigger rollback by default</li>
          <li><strong>rollbackFor</strong>: Explicitly specify exceptions that trigger rollback</li>
          <li><strong>noRollbackFor</strong>: Explicitly specify exceptions that do NOT trigger rollback</li>
        </ul>
      </el-alert>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>Common Transaction Pitfalls</span>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-button 
            type="danger" 
            plain 
            @click="runDemo('self-invocation')" 
            :loading="loading && currentDemo === 'self-invocation'"
            style="width: 100%; margin-bottom: 10px;"
          >
            Self-Invocation Pitfall
          </el-button>
        </el-col>
        <el-col :span="12">
          <el-button 
            type="danger" 
            plain 
            @click="runDemo('transaction-boundary')" 
            :loading="loading && currentDemo === 'transaction-boundary'"
            style="width: 100%; margin-bottom: 10px;"
          >
            Transaction Boundary Pitfall
          </el-button>
        </el-col>
      </el-row>

      <el-alert 
        title="Common Pitfalls" 
        type="warning" 
        :closable="false"
        style="margin-top: 20px;"
      >
        <ul style="margin: 0; padding-left: 20px;">
          <li><strong>Self-invocation</strong>: Calling @Transactional method from within the same class bypasses proxy</li>
          <li><strong>Transaction Boundary</strong>: Exception caught and not re-thrown prevents rollback</li>
          <li><strong>Wrong Exception Type</strong>: Checked exceptions don't trigger rollback by default</li>
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

const runDemo = async (type) => {
  loading.value = true
  currentDemo.value = type
  try {
    let result
    switch (type) {
      case 'default-success':
        result = await springDemoAPI.demonstrateRollbackDefault(false)
        break
      case 'default-fail':
        result = await springDemoAPI.demonstrateRollbackDefault(true)
        break
      case 'checked-exception':
        result = await springDemoAPI.demonstrateRollbackChecked()
        break
      case 'rollback-for':
        result = await springDemoAPI.demonstrateRollbackFor()
        break
      case 'no-rollback-for-success':
        result = await springDemoAPI.demonstrateNoRollbackFor(false)
        break
      case 'no-rollback-for-fail':
        result = await springDemoAPI.demonstrateNoRollbackFor(true)
        break
      case 'self-invocation':
        result = await springDemoAPI.demonstrateSelfInvocation()
        break
      case 'transaction-boundary':
        result = await springDemoAPI.demonstrateTransactionBoundary()
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
.rollback-demo {
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
