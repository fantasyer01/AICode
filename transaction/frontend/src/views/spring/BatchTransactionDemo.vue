<template>
  <div class="batch-demo">
    <el-page-header @back="$router.back()" title="Back">
      <template #content>
        <span class="page-title">Spring Batch Transaction Management</span>
      </template>
    </el-page-header>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>Batch Transaction Demonstrations</span>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-button 
            type="success" 
            plain 
            @click="runDemo('batch-success')" 
            :loading="loading && currentDemo === 'batch-success'"
            style="width: 100%; margin-bottom: 10px;"
          >
            <el-icon><SuccessFilled /></el-icon>
            <span style="margin-left: 8px;">Batch Success (All Commit)</span>
          </el-button>
        </el-col>
        <el-col :span="12">
          <el-button 
            type="danger" 
            plain 
            @click="runDemo('batch-rollback')" 
            :loading="loading && currentDemo === 'batch-rollback'"
            style="width: 100%; margin-bottom: 10px;"
          >
            <el-icon><CircleCloseFilled /></el-icon>
            <span style="margin-left: 8px;">Batch Rollback (All Revert)</span>
          </el-button>
        </el-col>
      </el-row>

      <el-alert 
        title="Spring Batch Chunk-Oriented Processing" 
        type="info" 
        :closable="false"
        style="margin-top: 20px;"
      >
        <ul style="margin: 0; padding-left: 20px;">
          <li><strong>ItemReader</strong>: Reads data from CSV file one item at a time</li>
          <li><strong>ItemProcessor</strong>: Validates and transforms each item (business logic)</li>
          <li><strong>ItemWriter</strong>: Writes chunk of items to database in a transaction</li>
          <li><strong>Chunk Size = 3</strong>: Items are processed in groups of 3 within one transaction</li>
          <li><strong>Transaction Boundary</strong>: Each chunk commits/rolls back independently</li>
        </ul>
      </el-alert>
    </el-card>

    <el-card v-if="demoResult" style="margin-top: 20px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>Demo Result: {{ demoResult.scenario }}</span>
          <el-tag :type="demoResult.success ? 'success' : 'danger'" size="large">
            {{ demoResult.success ? 'SUCCESS' : 'ROLLED BACK' }}
          </el-tag>
        </div>
      </template>

      <el-alert 
        :title="demoResult.explanation" 
        :type="demoResult.success ? 'success' : 'error'" 
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-row :gutter="20" style="margin-bottom: 20px;" v-if="demoResult.results">
        <el-col :span="4">
          <el-statistic title="Total Records" :value="demoResult.results.totalRecords || 0">
            <template #prefix>
              <el-icon><DocumentCopy /></el-icon>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="4">
          <el-statistic title="Chunk Size" :value="demoResult.results.chunkSize || 0">
            <template #prefix>
              <el-icon><Grid /></el-icon>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="4">
          <el-statistic 
            title="Chunks Processed" 
            :value="demoResult.results.chunksProcessed || demoResult.results.failedChunk || 0"
          >
            <template #prefix>
              <el-icon><Finished /></el-icon>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="4">
          <el-statistic 
            title="Committed" 
            :value="demoResult.results.recordsCommitted !== undefined ? demoResult.results.recordsCommitted : demoResult.results.persistedRecords || 0"
            :value-style="{ color: demoResult.success ? '#67C23A' : '#F56C6C' }"
          >
            <template #prefix>
              <el-icon><CircleCheck /></el-icon>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="4">
          <el-statistic title="Job Status" :value="demoResult.results.jobStatus || 'FAILED'">
            <template #prefix>
              <el-icon><Operation /></el-icon>
            </template>
          </el-statistic>
        </el-col>
        <el-col :span="4">
          <el-statistic title="Duration (ms)" :value="demoResult.results.duration || 0">
            <template #prefix>
              <el-icon><Timer /></el-icon>
            </template>
          </el-statistic>
        </el-col>
      </el-row>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="Execution Steps" name="steps">
          <el-timeline>
            <el-timeline-item 
              v-for="(step, index) in demoResult.steps" 
              :key="index"
              :timestamp="step.duration ? `${step.duration}ms` : ''"
              :type="getStepType(step.status)"
              :icon="getStepIcon(step.status)"
            >
              <h4>Step {{ step.stepNumber }}: {{ step.description }}</h4>
              <p v-if="step.details" :class="{'error-text': step.status === 'ERROR'}">
                {{ step.details }}
              </p>
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>

        <el-tab-pane label="Transaction Logs" name="logs">
          <el-card shadow="never" class="log-console">
            <div 
              v-for="(log, index) in demoResult.logs" 
              :key="index" 
              class="log-entry"
              :class="{'error-log': log.includes('ERROR'), 'rollback-log': log.includes('ROLLBACK')}"
            >
              <el-icon>
                <Document v-if="!log.includes('ERROR') && !log.includes('ROLLBACK')" />
                <WarningFilled v-else />
              </el-icon>
              <span>{{ log }}</span>
            </div>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="Code Example" name="code">
          <el-card shadow="never">
            <pre class="code-block"><code>{{ demoResult.codeSnippet }}</code></pre>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="Batch Flow" name="flow">
          <el-card shadow="never">
            <div class="flow-diagram">
              <el-steps :active="demoResult.success ? 5 : 3" finish-status="success" process-status="error">
                <el-step title="Read File" description="Parse CSV/Excel data" />
                <el-step title="Validate" description="Check data integrity" />
                <el-step title="Begin Transaction" description="Start database transaction" />
                <el-step 
                  :title="demoResult.success ? 'Process Batch' : 'Process Failed'" 
                  :description="demoResult.success ? 'Insert all records' : 'Validation error detected'" 
                />
                <el-step 
                  :title="demoResult.success ? 'Commit' : 'Rollback'" 
                  :description="demoResult.success ? 'All changes persisted' : 'All changes reverted'" 
                />
              </el-steps>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>Spring Batch Transaction Mechanism</span>
      </template>

      <el-alert 
        title="How Spring Batch Manages Transactions (Without @Transactional)" 
        type="warning" 
        :closable="false"
        style="margin-bottom: 20px;"
      >
        <p style="margin: 10px 0;"><strong>Key Difference from @Transactional:</strong></p>
        <ul style="margin: 0; padding-left: 20px;">
          <li>Spring Batch does NOT use <code>@Transactional</code> annotation</li>
          <li>Transactions are managed at the <strong>CHUNK level</strong>, not method level</li>
          <li>AOP is used, but wraps the entire chunk processing cycle</li>
        </ul>
      </el-alert>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never" style="background: #f0f9ff; border-left: 4px solid #409EFF;">
            <template #header>
              <strong>Transaction Boundary: Chunk-Oriented</strong>
            </template>
            <div style="font-family: monospace; font-size: 13px; line-height: 1.8;">
              <div style="color: #409EFF;">■ BEGIN TRANSACTION</div>
              <div style="margin-left: 20px;">├─ Read Item 1 (ItemReader)</div>
              <div style="margin-left: 20px;">├─ Process Item 1 (ItemProcessor)</div>
              <div style="margin-left: 20px;">├─ Read Item 2</div>
              <div style="margin-left: 20px;">├─ Process Item 2</div>
              <div style="margin-left: 20px;">├─ Read Item 3</div>
              <div style="margin-left: 20px;">├─ Process Item 3</div>
              <div style="margin-left: 20px; color: #67C23A; font-weight: bold;">└─ Write Chunk [1,2,3] (ItemWriter)</div>
              <div style="color: #67C23A; font-weight: bold;">■ COMMIT (or ROLLBACK on error)</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" style="background: #fff7e6; border-left: 4px solid #E6A23C;">
            <template #header>
              <strong>AOP Implementation Details</strong>
            </template>
            <ul style="margin: 0; padding-left: 20px; line-height: 1.8;">
              <li><strong>ChunkOrientedTasklet</strong>: Core tasklet that executes chunks</li>
              <li><strong>TransactionInterceptor</strong>: AOP proxy wrapping chunk execution</li>
              <li><strong>RepeatTemplate</strong>: Loops through items within transaction</li>
              <li><strong>PlatformTransactionManager</strong>: Same as @Transactional uses</li>
              <li style="color: #E6A23C; font-weight: bold;">Configured via: <code>.chunk(size, txManager)</code></li>
            </ul>
          </el-card>
        </el-col>
      </el-row>

      <el-divider />

      <el-row :gutter="20">
        <el-col :span="24">
          <el-card shadow="never" style="background: #fef0f0; border-left: 4px solid #F56C6C;">
            <template #header>
              <strong>Rollback Behavior</strong>
            </template>
            <el-row :gutter="20">
              <el-col :span="8">
                <div style="text-align: center; padding: 10px;">
                  <el-icon :size="40" color="#F56C6C"><WarningFilled /></el-icon>
                  <p style="font-weight: bold; margin-top: 10px;">ItemProcessor Exception</p>
                  <p style="color: #666; font-size: 13px;">Current chunk rolls back<br/>Previous chunks remain committed</p>
                </div>
              </el-col>
              <el-col :span="8">
                <div style="text-align: center; padding: 10px;">
                  <el-icon :size="40" color="#F56C6C"><WarningFilled /></el-icon>
                  <p style="font-weight: bold; margin-top: 10px;">ItemWriter Exception</p>
                  <p style="color: #666; font-size: 13px;">Current chunk rolls back<br/>Previous chunks remain committed</p>
                </div>
              </el-col>
              <el-col :span="8">
                <div style="text-align: center; padding: 10px;">
                  <el-icon :size="40" color="#67C23A"><SuccessFilled /></el-icon>
                  <p style="font-weight: bold; margin-top: 10px;">No Exception</p>
                  <p style="color: #666; font-size: 13px;">Chunk commits successfully<br/>Proceeds to next chunk</p>
                </div>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>Batch Transaction Best Practices</span>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="never" class="practice-card success-card">
            <template #header>
              <el-icon color="#67C23A"><CircleCheckFilled /></el-icon>
              <span style="margin-left: 8px;">Spring Batch Advantages</span>
            </template>
            <ul>
              <li>Built-in chunk-oriented processing pattern</li>
              <li>Automatic transaction management per chunk</li>
              <li>Configurable chunk size for performance tuning</li>
              <li>Item-level retry and skip capabilities</li>
              <li>Job restart and recovery features</li>
              <li>Comprehensive monitoring and metrics</li>
            </ul>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="practice-card error-card">
            <template #header>
              <el-icon color="#F56C6C"><CircleCloseFilled /></el-icon>
              <span style="margin-left: 8px;">Common Pitfalls</span>
            </template>
            <ul>
              <li>Chunk size too large causes long transactions and locks</li>
              <li>Chunk size too small reduces performance (overhead)</li>
              <li>Not handling ItemProcessor exceptions properly</li>
              <li>Forgetting to configure PlatformTransactionManager</li>
              <li>Not considering job restart/recovery strategies</li>
              <li>Missing proper error logging in ItemReader/Processor/Writer</li>
            </ul>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>Performance Comparison</span>
      </template>

      <el-table :data="comparisonData" border style="width: 100%">
        <el-table-column prop="approach" label="Approach" width="200" />
        <el-table-column prop="performance" label="Performance" />
        <el-table-column prop="atomicity" label="Atomicity" />
        <el-table-column prop="useCase" label="Best For" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { springDemoAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { 
  Document, 
  WarningFilled, 
  SuccessFilled, 
  CircleCloseFilled,
  CircleCheckFilled,
  DocumentCopy,
  Check,
  CircleCheck,
  Timer,
  Grid,
  Finished,
  Operation
} from '@element-plus/icons-vue'

const loading = ref(false)
const currentDemo = ref('')
const demoResult = ref(null)
const activeTab = ref('steps')

const comparisonData = [
  {
    approach: 'Spring Batch (Chunk=3)',
    performance: 'High - Chunk commits',
    atomicity: 'Per-chunk atomicity',
    useCase: 'Large file processing with chunk control'
  },
  {
    approach: 'Spring Batch (Chunk=1)',
    performance: 'Medium - Frequent commits',
    atomicity: 'Per-record atomicity',
    useCase: 'Fine-grained control, skip bad records'
  },
  {
    approach: 'Single @Transactional',
    performance: 'Medium - One commit',
    atomicity: 'All-or-nothing',
    useCase: 'Small batches, simple logic'
  },
  {
    approach: 'No Framework',
    performance: 'Lowest - Manual control',
    atomicity: 'Manual management',
    useCase: 'Custom complex workflows'
  }
]

const runDemo = async (type) => {
  loading.value = true
  currentDemo.value = type
  demoResult.value = null
  
  try {
    let result
    switch (type) {
      case 'batch-success':
        result = await springDemoAPI.demonstrateBatchSuccess()
        break
      case 'batch-rollback':
        result = await springDemoAPI.demonstrateBatchRollback()
        break
      default:
        throw new Error('Unknown demo type: ' + type)
    }
    demoResult.value = result
    
    if (result.success) {
      ElMessage.success('Batch transaction completed successfully')
    } else {
      ElMessage.warning('Batch transaction rolled back')
    }
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

const getStepIcon = (status) => {
  return status === 'ERROR' ? WarningFilled : null
}
</script>

<style scoped lang="scss">
.batch-demo {
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
      align-items: flex-start;
      gap: 8px;
      margin: 8px 0;
      font-size: 13px;
      line-height: 1.6;

      &.error-log {
        color: #f56c6c;
      }

      &.rollback-log {
        color: #e6a23c;
        font-weight: 600;
      }
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

  .error-text {
    color: #f56c6c;
    font-weight: 600;
  }

  .flow-diagram {
    padding: 20px;
  }

  .practice-card {
    height: 100%;
    
    ul {
      margin: 0;
      padding-left: 20px;
      
      li {
        margin: 8px 0;
        line-height: 1.6;
      }
    }

    &.success-card {
      border-left: 4px solid #67C23A;
    }

    &.error-card {
      border-left: 4px solid #F56C6C;
    }
  }

  :deep(.el-statistic) {
    .el-statistic__head {
      font-size: 14px;
      color: #909399;
    }
    
    .el-statistic__content {
      font-size: 24px;
      font-weight: 600;
    }
  }
}
</style>
