<template>
  <div>
    <h2 style="margin-bottom: 20px;">统计报表</h2>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" style="text-align: center;">
          <div style="font-size: 40px; color: #409EFF; font-weight: bold;">{{ stats.totalWorkOrders || 0 }}</div>
          <div style="margin-top: 10px; color: #666;">总工单数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="text-align: center;">
          <div style="font-size: 40px; color: #67C23A; font-weight: bold;">{{ stats.completedWorkOrders || 0 }}</div>
          <div style="margin-top: 10px; color: #666;">已完成工单</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="text-align: center;">
          <div style="font-size: 40px; color: #E6A23C; font-weight: bold;">{{ stats.completionRate || '0.00' }}%</div>
          <div style="margin-top: 10px; color: #666;">完成率</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="text-align: center;">
          <div style="font-size: 40px; color: #F56C6C; font-weight: bold;">{{ stats.materialShortageCount || 0 }}</div>
          <div style="margin-top: 10px; color: #666;">缺料影响次数</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-bottom: 20px;">
      <template #header>
        <span style="font-weight: bold;">设备利用率</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8" v-for="(rate, name) in equipmentUtilization" :key="name">
          <el-card shadow="never" style="text-align: center;">
            <div style="font-size: 24px; color: #67C23A; font-weight: bold;">{{ rate }}%</div>
            <div style="margin-top: 10px; color: #666;">{{ name }}</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card>
      <template #header>
        <span style="font-weight: bold;">工单状态分布</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="4" v-for="item in statusDistribution" :key="item.status">
          <el-card shadow="never" style="text-align: center;">
            <div style="font-size: 24px; font-weight: bold;" :style="{ color: item.color }">{{ item.count }}</div>
            <div style="margin-top: 10px; color: #666;">{{ item.label }}</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Statistics',
  data() {
    return {
      stats: {},
      equipmentUtilization: {}
    }
  },
  mounted() {
    this.loadStats()
  },
  computed: {
    statusDistribution() {
      const statusMap = {}
      const statusLabels = {
        'PENDING': { label: '待处理', color: '#909399' },
        'READY': { label: '准备就绪', color: '#E6A23C' },
        'SCHEDULED': { label: '已排产', color: '#409EFF' },
        'IN_PRODUCTION': { label: '生产中', color: '#F56C6C' },
        'PAUSED': { label: '已暂停', color: '#909399' },
        'COMPLETED': { label: '已完成', color: '#67C23A' },
        'QUALITY_INSPECTION': { label: '待质检', color: '#E6A23C' },
        'QUALITY_PASSED': { label: '质检通过', color: '#67C23A' },
        'QUALITY_FAILED': { label: '质检失败', color: '#F56C6C' },
        'REWORK': { label: '返工中', color: '#E6A23C' }
      }

      if (this.workOrders) {
        this.workOrders.forEach(wo => {
          statusMap[wo.status] = (statusMap[wo.status] || 0) + 1
        })
      }

      return Object.entries(statusLabels).map(([status, info]) => ({
        status,
        label: info.label,
        color: info.color,
        count: statusMap[status] || 0
      })).filter(item => item.count > 0)
    },
    workOrders() {
      return []
    }
  },
  methods: {
    loadStats() {
      axios.get('http://localhost:8001/api/statistics')
        .then(response => {
          this.stats = response.data
          this.equipmentUtilization = response.data.equipmentUtilization || {}
        })
    }
  }
}
</script>
