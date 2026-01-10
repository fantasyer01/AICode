<template>
  <div class="isolation-demo">
    <el-page-header @back="$router.back()" title="Back">
      <template #content>
        <span class="page-title">Transaction Isolation Levels</span>
      </template>
    </el-page-header>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>Select Isolation Level to Demonstrate</span>
      </template>

      <el-row :gutter="20">
        <el-col :span="6" v-for="level in isolationLevels" :key="level.value">
          <el-button 
            type="primary" 
            plain 
            @click="runDemo(level.value)" 
            :loading="loading && currentDemo === level.value"
            style="width: 100%; margin-bottom: 10px;"
          >
            {{ level.label }}
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

      <el-alert 
        title="Isolation Level Guide" 
        type="info" 
        :closable="false"
        style="margin-top: 20px;"
      >
        <ul style="margin: 0; padding-left: 20px;">
          <li><strong>READ_UNCOMMITTED</strong>: Allows dirty reads, non-repeatable reads, and phantom reads</li>
          <li><strong>READ_COMMITTED</strong>: Prevents dirty reads, allows non-repeatable reads and phantom reads</li>
          <li><strong>REPEATABLE_READ</strong>: Prevents dirty reads and non-repeatable reads, allows phantom reads</li>
          <li><strong>SERIALIZABLE</strong>: Prevents all read phenomena (highest isolation, lowest concurrency)</li>
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

      <!-- Special display for comparison results -->
      <div v-if="demoResult.scenario === 'ISOLATION_COMPARISON'">
        <el-table :data="formatComparisonData()" border stripe style="margin-top: 20px;">
          <el-table-column prop="level" label="Isolation Level" width="200" />
          <el-table-column prop="dirtyRead" label="Dirty Read" width="150">
            <template #default="scope">
              <el-tag :type="scope.row.dirtyRead === 'Prevented' ? 'success' : 'danger'">
                {{ scope.row.dirtyRead }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="nonRepeatableRead" label="Non-Repeatable Read" width="200">
            <template #default="scope">
              <el-tag :type="scope.row.nonRepeatableRead === 'Prevented' ? 'success' : 'danger'">
                {{ scope.row.nonRepeatableRead }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="phantomRead" label="Phantom Read" width="220">
            <template #default="scope">
              <el-tag :type="scope.row.phantomRead.includes('Prevented') ? 'success' : 'danger'">
                {{ scope.row.phantomRead }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="performance" label="Performance">
            <template #default="scope">
              <el-tag :type="getPerformanceType(scope.row.performance)">
                {{ scope.row.performance }}
              </el-tag>
            </template>
          </el-table-column>
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

const isolationLevels = [
  { value: 'read-uncommitted', label: 'READ_UNCOMMITTED' },
  { value: 'read-committed', label: 'READ_COMMITTED' },
  { value: 'repeatable-read', label: 'REPEATABLE_READ' },
  { value: 'serializable', label: 'SERIALIZABLE' }
]

const loading = ref(false)
const currentDemo = ref('')
const demoResult = ref(null)
const activeTab = ref('steps')

const runDemo = async (level) => {
  loading.value = true
  currentDemo.value = level
  try {
    const result = await springDemoAPI.demonstrateIsolation(level)
    demoResult.value = result
    ElMessage.success('Demo executed successfully')
  } catch (error) {
    ElMessage.error('Failed to execute demo: ' + (error.message || 'Unknown error'))
    console.error('Demo error:', error)
  } finally {
    loading.value = false
  }
}

const compareAll = async () => {
  loading.value = true
  currentDemo.value = 'compare'
  try {
    const result = await springDemoAPI.compareIsolationLevels()
    demoResult.value = result
    ElMessage.success('Comparison completed successfully')
  } catch (error) {
    ElMessage.error('Failed to compare isolation levels: ' + (error.message || 'Unknown error'))
    console.error('Compare error:', error)
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

const formatComparisonData = () => {
  if (!demoResult.value || !demoResult.value.results) return []
  
  const order = ['READ_UNCOMMITTED', 'READ_COMMITTED', 'REPEATABLE_READ', 'SERIALIZABLE']
  return order.map(key => demoResult.value.results[key])
}

const getPerformanceType = (performance) => {
  const typeMap = {
    'Highest': 'success',
    'High': 'success',
    'Medium': 'warning',
    'Lowest': 'danger'
  }
  return typeMap[performance] || 'info'
}
</script>

<style scoped lang="scss">
.isolation-demo {
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
