<template>
  <div>
    <h2 style="margin-bottom: 20px;">物料库存管理</h2>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="物料库存" name="inventory">
        <el-table :data="materials" border stripe>
          <el-table-column prop="materialCode" label="物料编码" width="120" />
          <el-table-column prop="materialName" label="物料名称" width="150" />
          <el-table-column prop="quantity" label="总数量" width="100" />
          <el-table-column prop="reservedQuantity" label="已锁定" width="100" />
          <el-table-column prop="availableQuantity" label="可用数量" width="100">
            <template #default="{ row }">
              <el-tag :type="row.availableQuantity < 100 ? 'danger' : 'success'">
                {{ row.availableQuantity }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="缺料提醒" name="shortage">
        <el-table :data="materialShortages" border stripe empty-text="暂无缺料记录">
          <el-table-column prop="workOrderId" label="工单ID" width="150" show-overflow-tooltip />
          <el-table-column prop="materialCode" label="物料编码" width="120" />
          <el-table-column prop="materialName" label="物料名称" width="150" />
          <el-table-column prop="requiredQuantity" label="需求数量" width="100" />
          <el-table-column prop="availableQuantity" label="可用数量" width="100" />
          <el-table-column prop="shortageQuantity" label="缺料数量" width="100">
            <template #default="{ row }">
              <el-tag type="danger">{{ row.shortageQuantity }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="采购建议" name="purchase">
        <el-table :data="purchaseSuggestions" border stripe empty-text="暂无采购建议">
          <el-table-column prop="materialCode" label="物料编码" width="120" />
          <el-table-column prop="materialName" label="物料名称" width="150" />
          <el-table-column prop="requiredQuantity" label="建议采购数量" width="150">
            <template #default="{ row }">
              <el-tag type="warning">{{ row.requiredQuantity }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="suggestedSupplier" label="建议供应商" width="150" />
          <el-table-column prop="suggestedDeliveryDate" label="建议交付日期" width="180">
            <template #default="{ row }">
              {{ formatDate(row.suggestedDeliveryDate) }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Materials',
  data() {
    return {
      activeTab: 'inventory',
      materials: [],
      materialShortages: [],
      purchaseSuggestions: []
    }
  },
  mounted() {
    this.loadMaterials()
    this.loadMaterialShortages()
    this.loadPurchaseSuggestions()
  },
  methods: {
    loadMaterials() {
      axios.get('http://localhost:8001/api/work-orders/materials')
        .then(response => {
          this.materials = response.data
        })
    },
    loadMaterialShortages() {
      axios.get('http://localhost:8001/api/work-orders/material-shortages')
        .then(response => {
          this.materialShortages = response.data
        })
    },
    loadPurchaseSuggestions() {
      axios.get('http://localhost:8001/api/work-orders/purchase-suggestions')
        .then(response => {
          this.purchaseSuggestions = response.data
        })
    },
    formatDate(dateStr) {
      if (!dateStr) return '-'
      return new Date(dateStr).toLocaleString()
    }
  }
}
</script>
