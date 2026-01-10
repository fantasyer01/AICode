<template>
  <div class="mvcc-demo">
    <el-page-header @back="$router.back()" title="Back">
      <template #content>
        <span class="page-title">MVCC Visualization</span>
      </template>
    </el-page-header>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>Multi-Version Concurrency Control Demonstration</span>
      </template>

      <el-button 
        type="primary" 
        @click="runDemo()" 
        :loading="loading"
        style="width: 100%; margin-bottom: 10px;"
      >
        Demonstrate MVCC
      </el-button>

      <el-alert 
        title="MVCC Overview" 
        type="info" 
        :closable="false"
        style="margin-top: 20px;"
      >
        <ul style="margin: 0; padding-left: 20px;">
          <li><strong>MVCC</strong>: Multi-Version Concurrency Control allows multiple transactions to see different versions of the same data</li>
          <li><strong>Read View</strong>: Snapshot of active transactions at the time a query starts</li>
          <li><strong>Version Chain</strong>: Linked list of row versions with transaction IDs and rollback pointers</li>
          <li><strong>Benefits</strong>: Readers don't block writers, writers don't block readers</li>
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

        <el-tab-pane label="Version Chain" name="versions" v-if="demoResult.versionChain">
          <el-card>
            <template #header>MVCC Version Chain</template>
            <div v-for="(version, index) in demoResult.versionChain" :key="index" style="margin-bottom: 20px;">
              <el-divider v-if="index > 0" />
              <el-descriptions :column="2" border>
                <el-descriptions-item label="Version">{{ version.version }}</el-descriptions-item>
                <el-descriptions-item label="Transaction ID">{{ version.transactionId }}</el-descriptions-item>
                <el-descriptions-item label="Data" :span="2">
                  <pre style="margin: 0;">{{ JSON.stringify(version.data, null, 2) }}</pre>
                </el-descriptions-item>
                <el-descriptions-item label="Rollback Pointer" :span="2">{{ version.rollbackPointer || 'NULL' }}</el-descriptions-item>
              </el-descriptions>
            </div>
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

const loading = ref(false)
const demoResult = ref(null)
const activeTab = ref('steps')

const runDemo = async () => {
  loading.value = true
  try {
    const result = await internalsDemoAPI.demonstrateMVCC()
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
.mvcc-demo {
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
