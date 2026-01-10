<template>
  <div class="logs-demo">
    <el-page-header @back="$router.back()" title="Back">
      <template #content>
        <span class="page-title">Database Log Mechanisms</span>
      </template>
    </el-page-header>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>Select Log Mechanism to Demonstrate</span>
      </template>

      <el-row :gutter="20">
        <el-col :span="6" v-for="mechanism in logMechanisms" :key="mechanism.value">
          <el-button 
            :type="mechanism.type" 
            plain 
            @click="runDemo(mechanism.value)" 
            :loading="loading && currentDemo === mechanism.value"
            style="width: 100%; margin-bottom: 10px;"
          >
            {{ mechanism.label }}
          </el-button>
        </el-col>
      </el-row>

      <el-alert 
        title="Database Log Mechanisms" 
        type="info" 
        :closable="false"
        style="margin-top: 20px;"
      >
        <ul style="margin: 0; padding-left: 20px;">
          <li><strong>Redo Log</strong>: Ensures durability by recording changes before they are written to disk (WAL principle)</li>
          <li><strong>Undo Log</strong>: Supports transaction rollback and MVCC by storing old versions of modified data</li>
          <li><strong>WAL (Write-Ahead Logging)</strong>: Protocol ensuring logs are written before data pages</li>
          <li><strong>Locks</strong>: Mechanisms to control concurrent access (row locks, table locks, gap locks)</li>
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

        <el-tab-pane label="Log Entries" name="entries" v-if="demoResult.logEntries">
          <el-card>
            <template #header>Log Entries</template>
            <el-table :data="demoResult.logEntries" border stripe>
              <el-table-column prop="lsn" label="LSN" width="100" />
              <el-table-column prop="type" label="Type" width="120" />
              <el-table-column prop="transactionId" label="Transaction ID" width="150" />
              <el-table-column prop="operation" label="Operation" width="120" />
              <el-table-column prop="data" label="Data">
                <template #default="scope">
                  <pre style="margin: 0;">{{ JSON.stringify(scope.row.data, null, 2) }}</pre>
                </template>
              </el-table-column>
            </el-table>
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
import { internalsDemoAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'

const logMechanisms = [
  { value: 'redolog', label: 'Redo Log', type: 'primary' },
  { value: 'undolog', label: 'Undo Log', type: 'success' },
  { value: 'wal', label: 'WAL', type: 'warning' },
  { value: 'locks', label: 'Locks', type: 'danger' }
]

const loading = ref(false)
const currentDemo = ref('')
const demoResult = ref(null)
const activeTab = ref('steps')

const runDemo = async (mechanism) => {
  loading.value = true
  currentDemo.value = mechanism
  try {
    let result
    switch (mechanism) {
      case 'redolog':
        result = await internalsDemoAPI.demonstrateRedoLog()
        break
      case 'undolog':
        result = await internalsDemoAPI.demonstrateUndoLog()
        break
      case 'wal':
        result = await internalsDemoAPI.demonstrateWAL()
        break
      case 'locks':
        result = await internalsDemoAPI.demonstrateLocks()
        break
      default:
        throw new Error('Unknown mechanism: ' + mechanism)
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
.logs-demo {
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
