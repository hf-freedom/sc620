<template>
  <div>
    <h2 style="margin-bottom: 20px;">销售订单管理</h2>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="12">
        <el-button type="primary" @click="showCreateDialog" size="large" icon="Plus">
          新建销售订单
        </el-button>
        <el-button type="info" @click="loadSalesOrders" style="margin-left: 10px;" icon="Refresh">
          刷新列表
        </el-button>
      </el-col>
      <el-col :span="12" style="text-align: right;">
        <el-alert
          title="操作提示"
          type="info"
          :closable="false"
          show-icon
          style="display: inline-block;">
          <template #default>
            新建订单后状态为"草稿"，点击"确认订单"可自动拆分生产工单
          </template>
        </el-alert>
      </el-col>
    </el-row>

    <el-table :data="salesOrders" border stripe style="width: 100%;" empty-text="暂无销售订单，请点击上方按钮创建">
      <el-table-column prop="orderNo" label="订单编号" width="140" />
      <el-table-column prop="customerName" label="客户名称" width="150" />
      <el-table-column prop="productName" label="产品名称" width="150" />
      <el-table-column prop="quantity" label="数量" width="100" />
      <el-table-column prop="deliveryDate" label="交付日期" width="180">
        <template #default="{ row }">
          {{ formatDate(row.deliveryDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button-group>
            <el-button v-if="row.status === 'DRAFT'" size="small" type="success" @click="confirmOrder(row.id)" icon="Check">
              确认订单
            </el-button>
            <el-button size="small" type="primary" @click="viewWorkOrders(row)" icon="Document">
              查看工单
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="createDialogVisible" title="新建销售订单" width="550px">
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="orderForm.customerName" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="orderForm.productName" placeholder="请输入产品名称" clearable />
        </el-form-item>
        <el-form-item label="生产数量" prop="quantity">
          <el-input-number v-model="orderForm.quantity" :min="1" :max="10000" controls-position="right" />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            提示：系统将自动按每100个单位拆分为一个生产工单
          </div>
        </el-form-item>
        <el-form-item label="交付日期" prop="deliveryDate">
          <el-date-picker v-model="orderForm.deliveryDate" type="datetime" placeholder="选择日期时间" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrderForm" :loading="submitLoading">创建订单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="workOrdersDialogVisible" title="关联生产工单" width="800px">
      <el-table :data="relatedWorkOrders" border stripe>
        <el-table-column prop="workOrderNo" label="工单号" width="150" />
        <el-table-column prop="productName" label="产品名称" width="150" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="status" label="状态" width="150">
          <template #default="{ row }">
            <el-tag :type="getWorkOrderStatusType(row.status)">
              {{ getWorkOrderStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="workOrdersDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'SalesOrders',
  data() {
    return {
      salesOrders: [],
      createDialogVisible: false,
      workOrdersDialogVisible: false,
      relatedWorkOrders: [],
      submitLoading: false,
      orderForm: {
        customerName: '',
        productName: '',
        quantity: 100,
        deliveryDate: new Date()
      }
    }
  },
  mounted() {
    this.loadSalesOrders()
  },
  methods: {
    loadSalesOrders() {
      axios.get('http://localhost:8001/api/sales-orders')
        .then(response => {
          this.salesOrders = response.data
        })
    },
    showCreateDialog() {
      this.orderForm = {
        customerName: '',
        productName: '',
        quantity: 100,
        deliveryDate: new Date()
      }
      this.createDialogVisible = true
    },
    submitOrderForm() {
      if (!this.orderForm.customerName || !this.orderForm.productName) {
        this.$message.warning('请填写完整的订单信息')
        return
      }
      this.submitLoading = true
      axios.post('http://localhost:8001/api/sales-orders', this.orderForm)
        .then(() => {
          this.createDialogVisible = false
          this.loadSalesOrders()
          this.$message.success('订单创建成功，请点击"确认订单"进行工单拆分')
        })
        .catch(() => {
          this.$message.error('订单创建失败')
        })
        .finally(() => {
          this.submitLoading = false
        })
    },
    confirmOrder(id) {
      axios.post(`http://localhost:8001/api/sales-orders/${id}/confirm`)
        .then(() => {
          this.loadSalesOrders()
          this.$message.success('订单确认成功，已自动生成生产工单')
        })
        .catch(() => {
          this.$message.error('订单确认失败')
        })
    },
    viewWorkOrders(row) {
      axios.get('http://localhost:8001/api/work-orders')
        .then(response => {
          this.relatedWorkOrders = response.data.filter(wo => wo.salesOrderId === row.id)
          this.workOrdersDialogVisible = true
        })
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
      return new Date(dateStr).toLocaleString()
    },
    getStatusType(status) {
      const map = {
        'DRAFT': 'info',
        'CONFIRMED': 'warning',
        'PRODUCING': 'primary',
        'COMPLETED': 'success',
        'CANCELLED': 'danger'
      }
      return map[status] || 'info'
    },
    getStatusText(status) {
      const map = {
        'DRAFT': '草稿',
        'CONFIRMED': '已确认',
        'PRODUCING': '生产中',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消'
      }
      return map[status] || status
    },
    getWorkOrderStatusType(status) {
      const map = {
        'PENDING': 'info',
        'READY': 'warning',
        'SCHEDULED': 'primary',
        'IN_PRODUCTION': 'danger',
        'PAUSED': 'info',
        'COMPLETED': 'success',
        'QUALITY_INSPECTION': 'warning',
        'QUALITY_PASSED': 'success',
        'QUALITY_FAILED': 'danger',
        'REWORK': 'warning',
        'CANCELLED': 'info'
      }
      return map[status] || 'info'
    },
    getWorkOrderStatusText(status) {
      const map = {
        'PENDING': '待处理',
        'READY': '准备就绪',
        'SCHEDULED': '已排产',
        'IN_PRODUCTION': '生产中',
        'PAUSED': '已暂停',
        'COMPLETED': '已完成',
        'QUALITY_INSPECTION': '待质检',
        'QUALITY_PASSED': '质检通过',
        'QUALITY_FAILED': '质检失败',
        'REWORK': '返工中',
        'CANCELLED': '已取消'
      }
      return map[status] || status
    }
  }
}
</script>
