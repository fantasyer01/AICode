<template>
  <div class="seata-demo">
    <el-page-header @back="$router.back()" title="Back">
      <template #content>
        <span class="page-title">Seata Distributed Transaction</span>
      </template>
    </el-page-header>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>Select Seata Transaction Mode</span>
      </template>

      <el-row :gutter="20">
        <el-col :span="6" v-for="mode in seataModes" :key="mode.value">
          <el-button 
            type="primary" 
            plain 
            @click="runDemo(mode.value)" 
            :loading="loading && currentDemo === mode.value"
            style="width: 100%; margin-bottom: 10px;"
          >
            {{ mode.label }}
          </el-button>
        </el-col>
      </el-row>

      <el-alert 
        title="Seata Transaction Modes" 
        type="info" 
        :closable="false"
        style="margin-top: 20px;"
      >
        <ul style="margin: 0; padding-left: 20px;">
          <li><strong>AT Mode</strong>: Automatic transaction mode with UNDO_LOG for rollback</li>
          <li><strong>TCC Mode</strong>: Try-Confirm-Cancel pattern, requires manual implementation</li>
          <li><strong>SAGA Mode</strong>: Long transaction solution, event-driven compensation</li>
          <li><strong>XA Mode</strong>: Two-phase commit protocol, strong consistency guarantee</li>
        </ul>
      </el-alert>

      <el-alert 
        title="Note: Seata Server Required" 
        type="warning" 
        :closable="false"
        style="margin-top: 10px;"
      >
        These demos require Seata Server to be running. If not available, demos will show simulation data.
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
import { distributedDemoAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'

const seataModes = [
  { value: 'at', label: 'AT Mode' },
  { value: 'tcc', label: 'TCC Mode' },
  { value: 'saga', label: 'SAGA Mode' },
  { value: 'xa', label: 'XA Mode' }
]

const loading = ref(false)
const currentDemo = ref('')
const demoResult = ref(null)
const activeTab = ref('steps')

const runDemo = async (mode) => {
  loading.value = true
  currentDemo.value = mode
  try {
    const result = await distributedDemoAPI.demonstrateSeata(mode)
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
.seata-demo {
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
