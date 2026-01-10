import request from './request'

export const databaseAPI = {
  // Get available database profiles
  getProfiles() {
    return request.get('/database/profiles')
  },
  
  // Get active database info
  getActive() {
    return request.get('/database/active')
  },
  
  // Switch database
  switchDatabase(profile) {
    return request.post('/database/switch', { profile })
  },
  
  // Check database status
  getStatus() {
    return request.get('/database/status')
  }
}

export const springDemoAPI = {
  // Propagation demos
  demonstratePropagation(type) {
    return request.post(`/demo/spring/propagation/${type}`)
  },
  
  comparePropagations() {
    return request.get('/demo/spring/propagation/compare')
  },
  
  // Isolation demos
  demonstrateIsolation(level) {
    return request.post(`/demo/spring/isolation/${level}`)
  },
  
  compareIsolationLevels() {
    return request.get('/demo/spring/isolation/compare')
  },
  
  // Rollback demos
  demonstrateRollbackDefault(throwException = false) {
    return request.post(`/demo/spring/rollback/default?throwException=${throwException}`)
  },
  
  demonstrateRollbackChecked() {
    return request.post('/demo/spring/rollback/checked-exception')
  },
  
  demonstrateRollbackFor() {
    return request.post('/demo/spring/rollback/rollback-for')
  },
  
  demonstrateNoRollbackFor(throwException = false) {
    return request.post(`/demo/spring/rollback/no-rollback-for?throwException=${throwException}`)
  },
  
  demonstrateSelfInvocation() {
    return request.get('/demo/spring/rollback/pitfalls/self-invocation')
  },
  
  demonstrateTransactionBoundary() {
    return request.get('/demo/spring/rollback/pitfalls/transaction-boundary')
  },
  
  // Programmatic demos
  demonstrateDeclarative() {
    return request.post('/demo/spring/programmatic/declarative')
  },
  
  demonstrateTransactionTemplate() {
    return request.post('/demo/spring/programmatic/transaction-template')
  },
  
  demonstratePlatformTxManager() {
    return request.post('/demo/spring/programmatic/platform-transaction-manager')
  },
  
  // Read-only demo
  demonstrateReadOnly() {
    return request.post('/demo/spring/readonly')
  },
  
  // Timeout demo
  demonstrateTimeout() {
    return request.post('/demo/spring/timeout')
  }
}

export const distributedDemoAPI = {
  // Seata demos
  demonstrateSeata(mode) {
    return request.post(`/demo/distributed/seata/${mode}`)
  },
  
  // ShardingSphere demos
  demonstrateSharding(type) {
    return request.post(`/demo/distributed/sharding/${type}`)
  }
}

export const internalsDemoAPI = {
  // Redo log demo
  demonstrateRedoLog() {
    return request.post('/demo/internals/redolog')
  },
  
  // Undo log demo
  demonstrateUndoLog() {
    return request.post('/demo/internals/undolog')
  },
  
  // MVCC demo
  demonstrateMVCC() {
    return request.post('/demo/internals/mvcc')
  },
  
  // WAL demo
  demonstrateWAL() {
    return request.post('/demo/internals/wal')
  },
  
  // Locks demo
  demonstrateLocks() {
    return request.post('/demo/internals/locks')
  }
}

export const utilityAPI = {
  // Reset demo data
  resetData() {
    return request.post('/demo/reset')
  },
  
  // Get logs
  getLogs() {
    return request.get('/demo/logs')
  },
  
  // Get code snippet
  getCodeSnippet(scenario) {
    return request.get(`/demo/code/${scenario}`)
  }
}
