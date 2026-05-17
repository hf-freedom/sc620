<template>
  <div>
    <h2 style="margin-bottom: 20px;">欢迎使用生产工单排产系统</h2>
    <el-row :gutter="20">
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
          <div style="font-size: 40px; color: #F56C6C; font-weight: bold;">{{ stats.delayedOrders || 0 }}</div>
          <div style="margin-top: 10px; color: #666;">延期工单</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span style="font-weight: bold;">快速操作</span>
          </template>
          <el-button type="primary" @click="$router.push('/sales-orders')" style="width: 100%; margin-bottom: 10px;">
            创建销售订单
          </el-button>
          <el-button type="success" @click="$router.push('/work-orders')" style="width: 100%; margin-bottom: 10px;">
            管理生产工单
          </el-button>
          <el-button type="warning" @click="$router.push('/materials')" style="width: 100%; margin-bottom: 10px;">
            查看物料库存
          </el-button>
          <el-button type="info" @click="$router.push('/statistics')" style="width: 100%;">
            查看统计报表
          </el-button>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span style="font-weight: bold;">系统说明</span>
          </template>
          <ul style="line-height: 2.5; color: #666;">
            <li>销售订单确认后自动拆分为多个生产工单</li>
            <li>根据物料库存判断每个工单是否可以开工</li>
            <li>物料不足时自动生成缺料提醒和采购建议</li>
            <li>生产线排产时校验设备产能和班次容量</li>
            <li>工单开始生产后锁定所需物料和设备资源</li>
            <li>工单暂停时记录暂停原因并释放部分资源</li>
            <li>生产完成后自动生成质检任务等待检验</li>
            <li>质检不合格时生成返工工单并关联原工单</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Home',
  data() {
    return {
      stats: {}
    }
  },
  mounted() {
    this.loadStats()
  },
  methods: {
    loadStats() {
      axios.get('http://localhost:8001/api/statistics')
        .then(response => {
          this.stats = response.data
        })
        .catch(() => {
          console.log('统计数据加载失败')
        })
    }
  }
}
</script>
