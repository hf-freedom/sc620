<template>
  <div>
    <h2 style="margin-bottom: 20px;">质检管理</h2>

    <el-table :data="inspections" border stripe>
      <el-table-column prop="workOrderId" label="工单ID" width="150" show-overflow-tooltip />
      <el-table-column prop="inspectedQuantity" label="抽检数量" width="120" />
      <el-table-column prop="passedQuantity" label="合格数量" width="120" />
      <el-table-column prop="failedQuantity" label="不合格数量" width="120" />
      <el-table-column prop="inspectionResult" label="质检结果" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.inspectionResult" :type="row.inspectionResult === 'PASSED' ? 'success' : 'danger'">
            {{ row.inspectionResult === 'PASSED' ? '通过' : '不通过' }}
          </el-tag>
          <el-tag v-else type="warning">待检验</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="inspector" label="检验员" width="120" />
      <el-table-column prop="failureReason" label="不合格原因" width="200" show-overflow-tooltip />
      <el-table-column prop="inspectionTime" label="检验时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.inspectionTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button v-if="!row.inspectionResult" size="small" type="primary" @click="showInspectDialog(row)">
            执行质检
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="inspectDialogVisible" title="执行质检" width="500px">
      <el-form :model="inspectForm" label-width="100px">
        <el-form-item label="合格数量">
          <el-input-number v-model="inspectForm.passedQuantity" :min="0" :max="currentInspection?.inspectedQuantity || 100" />
        </el-form-item>
        <el-form-item label="不合格数量">
          <el-input-number v-model="inspectForm.failedQuantity" :min="0" :max="currentInspection?.inspectedQuantity || 100" />
        </el-form-item>
        <el-form-item label="检验员">
          <el-input v-model="inspectForm.inspector" placeholder="请输入检验员姓名" />
        </el-form-item>
        <el-form-item v-if="inspectForm.failedQuantity > 0" label="不合格原因">
          <el-input v-model="inspectForm.failureReason" type="textarea" :rows="3" placeholder="请输入不合格原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inspectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="processInspection">提交质检结果</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'QualityInspections',
  data() {
    return {
      inspections: [],
      inspectDialogVisible: false,
      currentInspection: null,
      inspectForm: {
        passedQuantity: 0,
        failedQuantity: 0,
        inspector: '',
        failureReason: ''
      }
    }
  },
  mounted() {
    this.loadInspections()
  },
  methods: {
    loadInspections() {
      axios.get('http://localhost:8001/api/work-orders/quality-inspections')
        .then(response => {
          this.inspections = response.data
        })
    },
    showInspectDialog(row) {
      this.currentInspection = row
      this.inspectForm = {
        passedQuantity: row.inspectedQuantity,
        failedQuantity: 0,
        inspector: '',
        failureReason: ''
      }
      this.inspectDialogVisible = true
    },
    processInspection() {
      if (this.inspectForm.passedQuantity + this.inspectForm.failedQuantity !== this.currentInspection.inspectedQuantity) {
        this.$message.error('合格数量 + 不合格数量 必须等于抽检数量')
        return
      }

      axios.post(`http://localhost:8001/api/work-orders/quality-inspections/${this.currentInspection.id}/process`, this.inspectForm)
        .then(() => {
          this.inspectDialogVisible = false
          this.loadInspections()
          if (this.inspectForm.failedQuantity > 0) {
            this.$message.warning('质检不通过，已自动生成返工工单')
          } else {
            this.$message.success('质检通过')
          }
        })
        .catch(() => {
          this.$message.error('质检处理失败')
        })
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
      return new Date(dateStr).toLocaleString()
    }
  }
}
</script>
