<template>
  <el-container style="height: 100vh">
    <el-header style="background: #409EFF; color: white; font-size: 20px; font-weight: bold; display: flex; align-items: center; padding: 0 20px;">
      生产工单排产系统
    </el-header>
    <el-container>
      <el-aside width="200px" style="background: #f5f5f5; border-right: 1px solid #e6e6e6;">
        <el-menu
          :default-active="$route.path"
          router
          style="border-right: none"
        >
          <el-menu-item index="/">
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/sales-orders">
            <span>销售订单</span>
          </el-menu-item>
          <el-menu-item index="/work-orders">
            <span>生产工单</span>
          </el-menu-item>
          <el-menu-item index="/materials">
            <span>物料库存</span>
          </el-menu-item>
          <el-menu-item index="/quality-inspections">
            <span>质检管理</span>
          </el-menu-item>
          <el-menu-item index="/statistics">
            <span>统计报表</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main style="background: #fff; padding: 20px;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
}
</style>
