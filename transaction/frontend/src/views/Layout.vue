<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <div class="header-left">
        <h2><el-icon><TrendCharts /></el-icon> Transaction Training System</h2>
      </div>
      <div class="header-right">
        <el-select v-model="activeDatabase" @change="switchDatabase" placeholder="Select Database" style="width: 200px;">
          <el-option label="MySQL" value="mysql" />
          <el-option label="Oracle" value="oracle" />
        </el-select>
        <el-tag :type="databaseConnected ? 'success' : 'danger'" style="margin-left: 16px;">
          {{ databaseConnected ? 'Connected' : 'Disconnected' }}
        </el-tag>
      </div>
    </el-header>

    <el-container class="main-container">
      <el-aside width="250px" class="layout-sidebar">
        <el-menu
          :default-active="activeMenu"
          :router="true"
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>Dashboard</span>
          </el-menu-item>

          <el-sub-menu index="spring">
            <template #title>
              <el-icon><Connection /></el-icon>
              <span>Spring Transactions</span>
            </template>
            <el-menu-item index="/spring/propagation">Propagation Behaviors</el-menu-item>
            <el-menu-item index="/spring/isolation">Isolation Levels</el-menu-item>
            <el-menu-item index="/spring/rollback">Rollback Rules</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="distributed">
            <template #title>
              <el-icon><Share /></el-icon>
              <span>Distributed Transactions</span>
            </template>
            <el-menu-item index="/distributed/seata">Seata Framework</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="internals">
            <template #title>
              <el-icon><Monitor /></el-icon>
              <span>Database Internals</span>
            </template>
            <el-menu-item index="/internals/mvcc">MVCC Visualization</el-menu-item>
            <el-menu-item index="/internals/logs">Redo/Undo Logs</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { databaseAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { TrendCharts, HomeFilled, Connection, Share, Monitor } from '@element-plus/icons-vue'

const route = useRoute()
const activeDatabase = ref('mysql')
const databaseConnected = ref(true)

const activeMenu = computed(() => route.path)

const switchDatabase = async (profile) => {
  try {
    await databaseAPI.switchDatabase(profile)
    ElMessage.success(`Switched to ${profile} database`)
    checkDatabaseStatus()
  } catch (error) {
    ElMessage.error('Failed to switch database')
  }
}

const checkDatabaseStatus = async () => {
  try {
    const status = await databaseAPI.getStatus()
    databaseConnected.value = status.connected
  } catch (error) {
    databaseConnected.value = false
  }
}

onMounted(() => {
  checkDatabaseStatus()
})
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  
  .layout-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #409EFF;
    color: white;
    padding: 0 20px;
    
    .header-left {
      h2 {
        margin: 0;
        display: flex;
        align-items: center;
        gap: 10px;
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
    }
  }
  
  .main-container {
    height: calc(100vh - 60px);
    
    .layout-sidebar {
      background: #f5f7fa;
      border-right: 1px solid #e4e7ed;
      overflow-y: auto;
      
      .sidebar-menu {
        border-right: none;
      }
    }
    
    .layout-main {
      background: #ffffff;
      padding: 20px;
      overflow-y: auto;
    }
  }
}
</style>
