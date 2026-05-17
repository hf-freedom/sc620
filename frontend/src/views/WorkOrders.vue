<template>
  <div>
    <h2 style="margin-bottom: 20px;">生产工单管理</h2>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-statistic title="总工单" :value="workOrders.length" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="待排产" :value="pendingCount" value-style="color: #E6A23C" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="生产中" :value="productionCount" value-style="color: #F56C6C" />
      </el-col>
      <el-col :span="6">
        <el-button type="primary" @click="loadData" icon="Refresh" style="margin-top: 15px;">
          刷新数据
        </el-button>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" style="margin-bottom: 20px;">
      <el-tab-pane label="工单列表" name="orders">
        <el-table :data="workOrders" border stripe style="width: 100%;" empty-text="暂无生产工单">
          <el-table-column prop="workOrderNo" label="工单号" width="140" />
          <el-table-column prop="productName" label="产品名称" width="150" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="设备信息" width="180">
            <template #default="{ row }">
              <span v-if="row.equipmentId">{{ getEquipmentName(row.equipmentId) }}</span>
              <span v-else style="color: #909399;">未分配</span>
            </template>
          </el-table-column>
          <el-table-column label="设备产能" width="120">
            <template #default="{ row }">
              <span v-if="row.equipmentId">{{ getEquipmentCapacity(row.equipmentId) }} 件/小时</span>
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>
          <el-table-column label="班次信息" width="120">
            <template #default="{ row }">
              <span v-if="row.shiftId">{{ getShiftName(row.shiftId) }}</span>
              <span v-else style="color: #909399;">未分配</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getWorkOrderStatusType(row.status)" size="small">
                {{ getWorkOrderStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="scheduledStartTime" label="计划开始时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.scheduledStartTime) }}
            </template>
          </el-table-column>
          <el-table-column label="资源锁定状态" width="200">
            <template #default="{ row }">
              <div v-if="row.status === 'IN_PRODUCTION'">
                <el-tag type="danger" size="small" style="margin-right: 5px;">
                  设备已锁定
                </el-tag>
                <el-tag type="warning" size="small">
                  物料已锁定
                </el-tag>
              </div>
              <span v-else style="color: #909399; font-size: 12px;">
                -
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="380" fixed="right">
            <template #default="{ row }">
              <el-button-group>
                <el-button v-if="row.status === 'READY'" size="small" type="primary" @click="showScheduleDialog(row)" icon="Calendar">
                  排产
                </el-button>
                <el-button v-if="row.status === 'SCHEDULED'" size="small" type="success" @click="startProduction(row.id)" icon="VideoPlay">
                  开始生产
                </el-button>
                <el-button v-if="row.status === 'IN_PRODUCTION'" size="small" type="warning" @click="showPauseDialog(row)" icon="VideoPause">
                  暂停
                </el-button>
                <el-button v-if="row.status === 'IN_PRODUCTION'" size="small" type="success" @click="completeWorkOrder(row.id)" icon="Check">
                  完成生产
                </el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="设备产能" name="equipment">
        <el-table :data="equipments" border stripe>
          <el-table-column prop="equipmentNo" label="设备编号" width="120" />
          <el-table-column prop="equipmentName" label="设备名称" width="150" />
          <el-table-column prop="capacityPerHour" label="每小时产能" width="120">
            <template #default="{ row }">
              <el-tag type="success">{{ row.capacityPerHour }} 件</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="设备状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.status === 'AVAILABLE' ? 'success' : 'danger'">
                {{ row.status === 'AVAILABLE' ? '可用' : '使用中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="可用班次" width="200">
            <template #default="{ row }">
              <el-tag v-for="(available, shiftId) in row.shiftAvailability" :key="shiftId"
                :type="available ? 'success' : 'info'" size="small" style="margin-right: 5px;">
                {{ getShiftName(shiftId) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="班次容量" name="shift">
        <el-table :data="shifts" border stripe>
          <el-table-column prop="shiftName" label="班次名称" width="120" />
          <el-table-column prop="startTime" label="开始时间" width="150">
            <template #default="{ row }">
              {{ formatTime(row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="endTime" label="结束时间" width="150">
            <template #default="{ row }">
              {{ formatTime(row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="capacity" label="班次容量" width="120">
            <template #default="{ row }">
              <el-tag type="warning">{{ row.capacity }} 件</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="工作时长" width="120">
            <template #default="{ row }">
              {{ calculateShiftHours(row) }} 小时
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="scheduleDialogVisible" title="生产线排产" width="600px">
      <el-alert
        title="排产校验规则"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;">
        <template #default>
          <ul style="margin: 0; padding-left: 20px;">
            <li>设备状态必须为"可用"</li>
            <li>工单数量不能超过班次容量</li>
            <li>所需工时不能超过班次工作时长</li>
          </ul>
        </template>
      </el-alert>

      <el-descriptions :column="2" border style="margin-bottom: 20px;">
        <el-descriptions-item label="工单号">{{ currentWorkOrder?.workOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentWorkOrder?.productName }}</el-descriptions-item>
        <el-descriptions-item label="工单数量" content-style="color: #F56C6C; font-weight: bold;">{{ currentWorkOrder?.quantity }} 件</el-descriptions-item>
        <el-descriptions-item label="预计工时" content-style="color: #E6A23C; font-weight: bold;">{{ estimatedHours }} 小时</el-descriptions-item>
      </el-descriptions>

      <el-form :model="scheduleForm" label-width="100px">
        <el-form-item label="选择设备" required>
          <el-select v-model="scheduleForm.equipmentId" placeholder="请选择设备" style="width: 100%;" @change="onEquipmentChange">
            <el-option
              v-for="eq in availableEquipments"
              :key="eq.id"
              :label="`${eq.equipmentName} (产能: ${eq.capacityPerHour}件/小时)`"
              :value="eq.id"
              :disabled="eq.status !== 'AVAILABLE'">
              <span style="float: left">{{ eq.equipmentName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px;">
                产能: {{ eq.capacityPerHour }}件/小时 | {{ eq.status === 'AVAILABLE' ? '可用' : '使用中' }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择班次" required>
          <el-select v-model="scheduleForm.shiftId" placeholder="请选择班次" style="width: 100%;" @change="onShiftChange">
            <el-option
              v-for="shift in shifts"
              :key="shift.id"
              :label="`${shift.shiftName} (容量: ${shift.capacity}件)`"
              :value="shift.id">
              <span style="float: left">{{ shift.shiftName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px;">
                容量: {{ shift.capacity }}件 | 时长: {{ calculateShiftHours(shift) }}小时
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker v-model="scheduleForm.startTime" type="datetime" placeholder="选择日期时间" style="width: 100%;" />
        </el-form-item>
      </el-form>

      <div v-if="validationMessages.length > 0" style="margin-top: 20px;">
        <el-alert
          v-for="(msg, index) in validationMessages"
          :key="index"
          :title="msg.text"
          :type="msg.type"
          :closable="false"
          show-icon
          style="margin-bottom: 10px;" />
      </div>

      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="scheduleWorkOrder" :disabled="!canSchedule" :loading="scheduleLoading">
          确认排产
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="startProductionDialogVisible" title="开始生产确认" width="650px">
      <el-alert
        title="生产资源确认"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;">
        开始生产后，系统将自动锁定以下资源，请确认后继续
      </el-alert>

      <el-descriptions :column="2" border style="margin-bottom: 20px;">
        <el-descriptions-item label="工单号">{{ currentStartOrder?.workOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentStartOrder?.productName }}</el-descriptions-item>
        <el-descriptions-item label="生产数量">{{ currentStartOrder?.quantity }} 件</el-descriptions-item>
        <el-descriptions-item label="计划开始时间">{{ formatDate(currentStartOrder?.scheduledStartTime) }}</el-descriptions-item>
      </el-descriptions>

      <h4 style="margin-bottom: 15px; color: #409EFF;">
        <i class="el-icon-setting" style="margin-right: 5px;"></i>即将锁定的设备资源
      </h4>
      <el-table :data="equipmentLockInfo" border stripe style="margin-bottom: 20px;">
        <el-table-column prop="equipmentNo" label="设备编号" width="120" />
        <el-table-column prop="equipmentName" label="设备名称" width="150" />
        <el-table-column prop="capacity" label="产能(件/小时)" width="130" />
        <el-table-column prop="lockStatus" label="锁定状态">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.lockStatus }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <h4 style="margin-bottom: 15px; color: #67C23A;">
        <i class="el-icon-box" style="margin-right: 5px;"></i>即将锁定的物料资源
      </h4>
      <el-table :data="materialLockInfo" border stripe>
        <el-table-column prop="materialCode" label="物料编码" width="120" />
        <el-table-column prop="materialName" label="物料名称" width="150" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="requiredQty" label="需求数量" width="100" />
        <el-table-column prop="availableQty" label="可用库存" width="100" />
        <el-table-column prop="lockStatus" label="锁定状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.canLock ? 'success' : 'danger'">
              {{ row.canLock ? '可锁定' : '库存不足' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="startProductionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStartProduction" :loading="startProductionLoading">
          确认开始生产
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resourceLockResultVisible" title="资源锁定结果" width="650px">
      <el-result
        icon="success"
        title="生产资源锁定成功"
        sub-title="以下资源已成功锁定，生产已开始">
        <template #extra>
          <el-button type="primary" @click="resourceLockResultVisible = false">
            确定
          </el-button>
        </template>
      </el-result>

      <h4 style="margin: 20px 0 15px; color: #409EFF;">设备锁定结果</h4>
      <el-table :data="equipmentLockResult" border stripe style="margin-bottom: 20px;">
        <el-table-column prop="equipmentNo" label="设备编号" width="120" />
        <el-table-column prop="equipmentName" label="设备名称" width="150" />
        <el-table-column prop="status" label="当前状态" width="120">
          <template #default="{ row }">
            <el-tag type="danger">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lockTime" label="锁定时间" />
      </el-table>

      <h4 style="margin: 20px 0 15px; color: #67C23A;">物料锁定结果</h4>
      <el-table :data="materialLockResult" border stripe>
        <el-table-column prop="materialCode" label="物料编码" width="120" />
        <el-table-column prop="materialName" label="物料名称" width="150" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="lockedQty" label="已锁定数量" width="120">
          <template #default="{ row }">
            <el-tag type="success">{{ row.lockedQty }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remainingQty" label="剩余可用" width="120">
          <template #default="{ row }">
            <el-tag :type="row.remainingQty > 0 ? 'info' : 'danger'">{{ row.remainingQty }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lockTime" label="锁定时间" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="pauseDialogVisible" title="暂停工单" width="400px">
      <el-form :model="pauseForm" label-width="100px">
        <el-form-item label="暂停原因">
          <el-input v-model="pauseForm.reason" type="textarea" :rows="3" placeholder="请输入暂停原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pauseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="pauseWorkOrder">确认暂停</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'WorkOrders',
  data() {
    return {
      activeTab: 'orders',
      workOrders: [],
      equipments: [],
      shifts: [],
      scheduleDialogVisible: false,
      pauseDialogVisible: false,
      startProductionDialogVisible: false,
      resourceLockResultVisible: false,
      scheduleLoading: false,
      startProductionLoading: false,
      currentWorkOrderId: null,
      currentWorkOrder: null,
      currentStartOrder: null,
      scheduleForm: {
        equipmentId: '',
        shiftId: '',
        startTime: new Date()
      },
      pauseForm: {
        reason: ''
      },
      equipmentLockInfo: [],
      materialLockInfo: [],
      equipmentLockResult: [],
      materialLockResult: [],
      materialList: []
    }
  },
  computed: {
    pendingCount() {
      return this.workOrders.filter(o => o.status === 'READY').length
    },
    productionCount() {
      return this.workOrders.filter(o => o.status === 'IN_PRODUCTION').length
    },
    materials() {
      return this.materialList || []
    },
    availableEquipments() {
      return this.equipments
    },
    selectedEquipment() {
      return this.equipments.find(e => e.id === this.scheduleForm.equipmentId)
    },
    selectedShift() {
      return this.shifts.find(s => s.id === this.scheduleForm.shiftId)
    },
    estimatedHours() {
      if (!this.currentWorkOrder || !this.selectedEquipment) {
        return 0
      }
      return Math.ceil(this.currentWorkOrder.quantity / this.selectedEquipment.capacityPerHour)
    },
    validationMessages() {
      const messages = []
      if (!this.currentWorkOrder || !this.selectedEquipment || !this.selectedShift) {
        return messages
      }

      if (this.selectedEquipment.status !== 'AVAILABLE') {
        messages.push({ type: 'error', text: `设备【${this.selectedEquipment.equipmentName}】当前不可用，请选择其他设备` })
      }

      if (this.currentWorkOrder.quantity > this.selectedShift.capacity) {
        messages.push({ type: 'error', text: `工单数量(${this.currentWorkOrder.quantity}件)超过班次容量(${this.selectedShift.capacity}件)` })
      }

      const shiftHours = this.calculateShiftHours(this.selectedShift)
      if (this.estimatedHours > shiftHours) {
        messages.push({ type: 'error', text: `预计工时(${this.estimatedHours}小时)超过班次工作时长(${shiftHours}小时)` })
      }

      if (this.selectedEquipment.status === 'AVAILABLE' &&
          this.currentWorkOrder.quantity <= this.selectedShift.capacity &&
          this.estimatedHours <= shiftHours) {
        messages.push({ type: 'success', text: '产能校验通过，可以排产' })
      }

      return messages
    },
    canSchedule() {
      if (!this.scheduleForm.equipmentId || !this.scheduleForm.shiftId || !this.currentWorkOrder) {
        return false
      }
      return this.validationMessages.every(m => m.type !== 'error')
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loadWorkOrders()
      this.loadEquipments()
      this.loadShifts()
      this.loadMaterials()
    },
    loadMaterials() {
      axios.get('http://localhost:8001/api/work-orders/materials')
        .then(response => {
          this.materialList = response.data
        })
    },
    loadWorkOrders() {
      axios.get('http://localhost:8001/api/work-orders')
        .then(response => {
          this.workOrders = response.data
        })
    },
    loadEquipments() {
      axios.get('http://localhost:8001/api/work-orders/equipments')
        .then(response => {
          this.equipments = response.data
        })
    },
    loadShifts() {
      axios.get('http://localhost:8001/api/work-orders/shifts')
        .then(response => {
          this.shifts = response.data
        })
    },
    getEquipmentName(id) {
      const eq = this.equipments.find(e => e.id === id)
      return eq ? eq.equipmentName : id
    },
    getEquipmentCapacity(id) {
      const eq = this.equipments.find(e => e.id === id)
      return eq ? eq.capacityPerHour : 0
    },
    getShiftName(id) {
      const shift = this.shifts.find(s => s.id === id)
      return shift ? shift.shiftName : id
    },
    calculateShiftHours(shift) {
      if (!shift || !shift.startTime || !shift.endTime) return 0
      const start = new Date(`2000-01-01T${shift.startTime}`)
      const end = new Date(`2000-01-01T${shift.endTime}`)
      if (end < start) end.setDate(end.getDate() + 1)
      return Math.round((end - start) / (1000 * 60 * 60))
    },
    formatTime(timeStr) {
      if (!timeStr) return '-'
      return timeStr.substring(0, 5)
    },
    onEquipmentChange() {
    },
    onShiftChange() {
    },
    showScheduleDialog(row) {
      this.currentWorkOrderId = row.id
      this.currentWorkOrder = row
      this.scheduleForm = {
        equipmentId: '',
        shiftId: '',
        startTime: new Date()
      }
      this.scheduleDialogVisible = true
    },
    scheduleWorkOrder() {
      this.scheduleLoading = true
      const data = {
        ...this.scheduleForm,
        startTime: this.scheduleForm.startTime.toISOString()
      }
      axios.post(`http://localhost:8001/api/work-orders/${this.currentWorkOrderId}/schedule`, data)
        .then(() => {
          this.scheduleDialogVisible = false
          this.loadData()
          this.$message.success('排产成功，设备和班次已分配')
        })
        .catch(() => {
          this.$message.error('排产失败')
        })
        .finally(() => {
          this.scheduleLoading = false
        })
    },
    startProduction(id) {
      const order = this.workOrders.find(o => o.id === id)
      if (!order) return

      this.currentStartOrder = order
      this.prepareLockInfo(order)
      this.startProductionDialogVisible = true
    },
    prepareLockInfo(order) {
      const equipment = this.equipments.find(e => e.id === order.equipmentId)
      if (equipment) {
        this.equipmentLockInfo = [{
          equipmentNo: equipment.equipmentNo,
          equipmentName: equipment.equipmentName,
          capacity: equipment.capacityPerHour,
          lockStatus: '即将锁定'
        }]
      }

      const materialInfo = []
      const requiredMaterials = order.requiredMaterials || {}

      Object.entries(requiredMaterials).forEach(([materialCode, qtyPerUnit]) => {
        const material = this.materials.find(m => m.materialCode === materialCode)
        const requiredQty = qtyPerUnit * order.quantity
        const availableQty = material ? material.availableQuantity : 0

        materialInfo.push({
          materialCode,
          materialName: material ? material.materialName : '未知物料',
          unit: material ? material.unit : '-',
          requiredQty,
          availableQty,
          canLock: availableQty >= requiredQty
        })
      })

      this.materialLockInfo = materialInfo
    },
    confirmStartProduction() {
      if (!this.currentStartOrder) return

      this.startProductionLoading = true
      axios.post(`http://localhost:8001/api/work-orders/${this.currentStartOrder.id}/start`)
        .then(() => {
          this.startProductionDialogVisible = false
          this.prepareLockResult()
          this.resourceLockResultVisible = true
          this.loadData()
        })
        .catch(() => {
          this.$message.error('开始生产失败')
        })
        .finally(() => {
          this.startProductionLoading = false
        })
    },
    prepareLockResult() {
      const order = this.currentStartOrder
      const equipment = this.equipments.find(e => e.id === order.equipmentId)
      const now = new Date().toLocaleString()

      if (equipment) {
        this.equipmentLockResult = [{
          equipmentNo: equipment.equipmentNo,
          equipmentName: equipment.equipmentName,
          status: '使用中',
          lockTime: now
        }]
      }

      const materialResult = []
      const requiredMaterials = order.requiredMaterials || {}

      Object.entries(requiredMaterials).forEach(([materialCode, qtyPerUnit]) => {
        const material = this.materials.find(m => m.materialCode === materialCode)
        const lockedQty = qtyPerUnit * order.quantity
        const remainingQty = material ? (material.availableQuantity - lockedQty) : 0

        materialResult.push({
          materialCode,
          materialName: material ? material.materialName : '未知物料',
          unit: material ? material.unit : '-',
          lockedQty,
          remainingQty: Math.max(0, remainingQty),
          lockTime: now
        })
      })

      this.materialLockResult = materialResult
    },
    showPauseDialog(row) {
      this.currentWorkOrderId = row.id
      this.pauseForm.reason = ''
      this.pauseDialogVisible = true
    },
    pauseWorkOrder() {
      axios.post(`http://localhost:8001/api/work-orders/${this.currentWorkOrderId}/pause`, this.pauseForm)
        .then(() => {
          this.pauseDialogVisible = false
          this.loadData()
          this.$message.success('工单已暂停，已释放部分资源')
        })
        .catch(() => {
          this.$message.error('暂停失败')
        })
    },
    completeWorkOrder(id) {
      axios.post(`http://localhost:8001/api/work-orders/${id}/complete`)
        .then(() => {
          this.loadData()
          this.$message.success('生产完成，已生成质检任务')
        })
        .catch(() => {
          this.$message.error('完成生产失败')
        })
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
      return new Date(dateStr).toLocaleString()
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
