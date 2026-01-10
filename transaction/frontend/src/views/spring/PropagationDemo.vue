<template>
  <div class="propagation-demo">
    <el-page-header @back="$router.back()" title="Back">
      <template #content>
        <span class="page-title">Transaction Propagation Behaviors</span>
      </template>
    </el-page-header>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>Select Propagation Type to Demonstrate</span>
      </template>

      <el-row :gutter="20">
        <el-col :span="6" v-for="type in propagationTypes" :key="type.value">
          <el-button 
            type="primary" 
            plain 
            @click="runDemo(type.value)" 
            :loading="loading && currentDemo === type.value"
            style="width: 100%; margin-bottom: 10px;"
          >
            {{ type.label }}
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button 
            type="success" 
            plain 
            @click="compareAll()" 
            :loading="loading && currentDemo === 'compare'"
            style="width: 100%; margin-bottom: 10px;"
          >
            COMPARE ALL
          </el-button>
        </el-col>
      </el-row>
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

      <!-- Special display for comparison results -->
      <div v-if="demoResult.scenario === 'PROPAGATION_COMPARISON'">
        <el-table :data="formatComparisonData()" border stripe style="margin-top: 20px;">
          <el-table-column prop="propagation" label="Propagation" width="150" fixed />
          <el-table-column prop="transactionExists" label="If Transaction Exists" width="200" />
          <el-table-column prop="noTransaction" label="If No Transaction" width="200" />
          <el-table-column prop="newTransaction" label="Creates New Transaction?" width="180" />
          <el-table-column prop="useCase" label="Typical Use Case" min-width="250" />
        </el-table>
      </div>

      <!-- Normal demo display -->
      <el-tabs v-else v-model="activeTab">
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

const propagationTypes = [
  { value: 'required', label: 'REQUIRED' },
  { value: 'requires-new', label: 'REQUIRES_NEW' },
  { value: 'nested', label: 'NESTED' },
  { value: 'supports', label: 'SUPPORTS' },
  { value: 'not-supported', label: 'NOT_SUPPORTED' },
  { value: 'mandatory', label: 'MANDATORY' },
  { value: 'never', label: 'NEVER' }
]

const loading = ref(false)
const currentDemo = ref('')
const demoResult = ref(null)
const activeTab = ref('steps')

const runDemo = async (type) => {
  loading.value = true
  currentDemo.value = type
  try {
    const result = await springDemoAPI.demonstratePropagation(type)
    demoResult.value = result
    ElMessage.success('Demo executed successfully')
  } catch (error) {
    ElMessage.error('Failed to execute demo')
  } finally {
    loading.value = false
  }
}

const compareAll = async () => {
  loading.value = true
  currentDemo.value = 'compare'
  try {
    const result = await springDemoAPI.comparePropagations()
    demoResult.value = result
    ElMessage.success('Comparison completed successfully')
  } catch (error) {
    ElMessage.error('Failed to compare propagations: ' + (error.message || 'Unknown error'))
    console.error('Compare error:', error)
  } finally {
    loading.value = false
  }
}

const formatComparisonData = () => {
  if (!demoResult.value || !demoResult.value.results) return []
  
  const order = ['REQUIRED', 'REQUIRES_NEW', 'NESTED', 'SUPPORTS', 'NOT_SUPPORTED', 'MANDATORY', 'NEVER']
  return order.map(key => demoResult.value.results[key])
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
.propagation-demo {
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
