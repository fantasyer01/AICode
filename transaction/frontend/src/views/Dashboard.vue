<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="info-card">
          <template #header>
            <span><el-icon><Connection /></el-icon> Database Connection</span>
          </template>
          <div class="card-content">
            <p><strong>Active Profile:</strong> {{ databaseInfo.activeProfile || 'N/A' }}</p>
            <p><strong>Database Type:</strong> {{ databaseInfo.databaseType || 'N/A' }}</p>
            <p><strong>Status:</strong> 
              <el-tag :type="databaseInfo.connected ? 'success' : 'danger'">
                {{ databaseInfo.connected ? 'Connected' : 'Disconnected' }}
              </el-tag>
            </p>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="info-card">
          <template #header>
            <span><el-icon><DocumentCopy /></el-icon> Demo Categories</span>
          </template>
          <div class="card-content">
            <p>Spring Transactions: <strong>20+ scenarios</strong></p>
            <p>Distributed Transactions: <strong>10+ patterns</strong></p>
            <p>Database Internals: <strong>15+ visualizations</strong></p>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="info-card">
          <template #header>
            <span><el-icon><Tools /></el-icon> Quick Actions</span>
          </template>
          <div class="card-content">
            <el-button type="primary" @click="resetData" :loading="resetting">
              Reset Demo Data
            </el-button>
            <el-button type="info" @click="viewLogs">
              View Logs
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span><el-icon><Guide /></el-icon> Getting Started</span>
          </template>
          <div class="getting-started">
            <el-steps :active="0" finish-status="success">
              <el-step title="Select Database" description="Choose MySQL or Oracle from header" />
              <el-step title="Choose Demo Category" description="Navigate using sidebar menu" />
              <el-step title="Run Demonstration" description="Execute and observe transaction behavior" />
              <el-step title="Review Results" description="Analyze code, logs, and visualizations" />
            </el-steps>
            
            <div class="demo-categories" style="margin-top: 30px;">
              <h3>Available Demo Categories:</h3>
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-card shadow="hover" class="category-card" @click="$router.push('/spring/propagation')">
                    <h4><el-icon><Connection /></el-icon> Spring Transactions</h4>
                    <p>Propagation behaviors, isolation levels, rollback rules, and common pitfalls</p>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card shadow="hover" class="category-card" @click="$router.push('/distributed/seata')">
                    <h4><el-icon><Share /></el-icon> Distributed Transactions</h4>
                    <p>Seata AT/TCC/SAGA modes, ShardingSphere, and transaction patterns</p>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card shadow="hover" class="category-card" @click="$router.push('/internals/mvcc')">
                    <h4><el-icon><Monitor /></el-icon> Database Internals</h4>
                    <p>MVCC, Redo/Undo logs, WAL protocol, lock mechanisms</p>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { databaseAPI, utilityAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { Connection, DocumentCopy, Tools, Guide, Share, Monitor } from '@element-plus/icons-vue'

const databaseInfo = ref({
  activeProfile: 'mysql',
  databaseType: 'MySQL',
  connected: true
})

const resetting = ref(false)

const loadDatabaseInfo = async () => {
  try {
    const info = await databaseAPI.getActive()
    databaseInfo.value = info
  } catch (error) {
    console.error('Failed to load database info:', error)
  }
}

const resetData = async () => {
  resetting.value = true
  try {
    await utilityAPI.resetData()
    ElMessage.success('Demo data reset successfully')
  } catch (error) {
    ElMessage.error('Failed to reset data')
  } finally {
    resetting.value = false
  }
}

const viewLogs = async () => {
  ElMessage.info('Viewing transaction logs...')
  // Implement log viewing functionality
}

onMounted(() => {
  loadDatabaseInfo()
})
</script>

<style scoped lang="scss">
.dashboard {
  .info-card {
    height: 180px;
    
    .card-content {
      p {
        margin: 10px 0;
        font-size: 14px;
      }
    }
  }
  
  .getting-started {
    padding: 20px 0;
  }
  
  .demo-categories {
    .category-card {
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      }
      
      h4 {
        margin: 0 0 10px 0;
        color: #409EFF;
        display: flex;
        align-items: center;
        gap: 8px;
      }
      
      p {
        margin: 0;
        color: #606266;
        font-size: 13px;
      }
    }
  }
}
</style>
