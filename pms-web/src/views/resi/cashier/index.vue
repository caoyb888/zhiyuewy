<template>
  <div class="app-container resi-cashier">
    <!-- 收银台三区布局 -->
    <el-row :gutter="12" class="cashier-layout">
      <!-- 左侧：搜索树 -->
      <el-col :span="5" class="cashier-sidebar">
        <el-card shadow="never">
          <div slot="header">
            <span>房间搜索</span>
          </div>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索房间/业主"
            size="small"
            prefix-icon="el-icon-search"
            clearable
            @keyup.enter.native="handleSearch"
          />
          <el-tree
            v-loading="treeLoading"
            :data="roomTree"
            :props="treeProps"
            node-key="id"
            highlight-current
            @node-click="handleNodeClick"
            class="room-tree"
          />
        </el-card>
      </el-col>

      <!-- 中间：费用列表 -->
      <el-col :span="12" class="cashier-main">
        <el-card shadow="never">
          <div slot="header">
            <span>待缴费用</span>
            <el-button
              style="float: right; padding: 3px 0"
              type="text"
              icon="el-icon-refresh"
              :disabled="!currentRoomId"
              @click="loadReceivables(currentRoomId)"
            >刷新</el-button>
          </div>
          <el-table
            v-loading="loading"
            :data="receivableList"
            @selection-change="handleSelectionChange"
            height="calc(100vh - 280px)"
          >
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="费用名称" prop="feeName" min-width="120" />
            <el-table-column label="账单月" prop="billPeriod" width="90" align="center" />
            <el-table-column label="应收金额" prop="receivable" align="right" width="120">
              <template slot-scope="scope">
                {{ scope.row.receivable | formatMoney }}
              </template>
            </el-table-column>
            <el-table-column label="状态" prop="payState" align="center" width="90">
              <template slot-scope="scope">
                <el-tag :type="payStateTagType(scope.row.payState)">
                  {{ payStateLabel(scope.row.payState) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：收款操作区 -->
      <el-col :span="7" class="cashier-action">
        <el-card shadow="never">
          <div slot="header">
            <span>收款结算</span>
          </div>
          <div class="summary-cards">
            <div class="summary-item">
              <div class="label">应收合计</div>
              <div class="value">{{ totalReceivable | formatMoney }}</div>
            </div>
            <div class="summary-item">
              <div class="label">折扣减免</div>
              <div class="value text-warning">{{ totalDiscount | formatMoney }}</div>
            </div>
            <div class="summary-item">
              <div class="label">实收金额</div>
              <div class="value text-danger">{{ totalPay | formatMoney }}</div>
            </div>
          </div>
          <el-divider />
          <el-form label-width="80px" size="small">
            <el-form-item label="支付方式">
              <el-select v-model="payForm.payMethod" placeholder="请选择" style="width: 100%">
                <el-option label="现金" value="CASH" />
                <el-option label="微信支付" value="WECHAT" />
                <el-option label="银行转账" value="TRANSFER" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
            <el-form-item label="实收金额">
              <el-input-number v-model="payForm.payAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-form>
          <el-button
            type="primary"
            size="medium"
            style="width: 100%; margin-top: 12px"
            :disabled="selectedIds.length === 0 || loading"
            :loading="loading"
            @click="handleCollect"
          >确认收款</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { formatMoney, payStateLabel, payStateTagType } from '@/utils/resi'
import { searchRoom, getRoomReceivables, getRoomSummary } from '@/api/resi/cashier'

export default {
  name: 'ResiCashier',
  filters: {
    formatMoney
  },
  data() {
    return {
      loading: false,
      treeLoading: false,
      searchKeyword: '',
      roomTree: [],
      treeProps: {
        label: 'label',
        children: 'children',
        isLeaf: 'isLeaf'
      },
      receivableList: [],
      selectedIds: [],
      currentRoomId: null,
      totalReceivable: 0,
      totalDiscount: 0,
      totalPay: 0,
      payForm: {
        payMethod: 'CASH',
        payAmount: 0
      }
    }
  },
  created() {
    // 初始状态：树为空，等待用户搜索
  },
  methods: {
    payStateLabel,
    payStateTagType,
    // 搜索房间
    handleSearch() {
      const keyword = this.searchKeyword.trim()
      if (!keyword) {
        this.roomTree = []
        return
      }
      this.treeLoading = true
      searchRoom(keyword).then(response => {
        const rooms = response.data || []
        this.roomTree = this.buildRoomTree(rooms)
        this.treeLoading = false
      }).catch(() => {
        this.treeLoading = false
      })
    },
    // 将房间列表按项目+楼栋分组，组装为树形结构
    buildRoomTree(rooms) {
      const projectMap = {}
      rooms.forEach(room => {
        const projectKey = room.projectId || 0
        const buildingKey = room.buildingId || 0
        if (!projectMap[projectKey]) {
          projectMap[projectKey] = {
            id: 'project_' + projectKey,
            label: room.projectName || '未知项目',
            type: 'project',
            children: {}
          }
        }
        const projectNode = projectMap[projectKey]
        if (!projectNode.children[buildingKey]) {
          projectNode.children[buildingKey] = {
            id: 'building_' + buildingKey,
            label: room.buildingName || '未知楼栋',
            type: 'building',
            children: []
          }
        }
        const buildingNode = projectNode.children[buildingKey]
        buildingNode.children.push({
          id: room.id,
          label: (room.roomAlias || room.roomNo || room.id) + (room.customerName ? ' (' + room.customerName + ')' : ''),
          type: 'room',
          isLeaf: true,
          roomId: room.id
        })
      })
      // 转换为数组结构
      return Object.values(projectMap).map(project => {
        project.children = Object.values(project.children)
        return project
      })
    },
    handleNodeClick(data) {
      if (data.type === 'room') {
        this.currentRoomId = data.roomId
        this.loadReceivables(data.roomId)
      }
    },
    loadReceivables(roomId) {
      this.loading = true
      this.receivableList = []
      this.selectedIds = []
      this.resetSummary()
      getRoomReceivables(roomId).then(response => {
        this.receivableList = response.data || []
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
      // S4-03 阶段本地计算，S4-04 接入 calc 接口
      let totalReceivable = 0
      let totalDiscount = 0
      selection.forEach(item => {
        totalReceivable += Number(item.receivable || 0)
        totalDiscount += Number(item.discountAmount || 0)
      })
      this.totalReceivable = totalReceivable
      this.totalDiscount = totalDiscount
      this.totalPay = totalReceivable - totalDiscount
      this.payForm.payAmount = Number(this.totalPay.toFixed(2))
    },
    resetSummary() {
      this.totalReceivable = 0
      this.totalDiscount = 0
      this.totalPay = 0
      this.payForm.payAmount = 0
    },
    async handleCollect() {
      this.loading = true
      try {
        // S4-04 实现收款核心接口
        this.$message.warning('收款功能将在 S4-04 阶段实现')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.resi-cashier {
  padding: 0;
}
.cashier-layout {
  height: calc(100vh - 84px);
}
.cashier-sidebar,
.cashier-main,
.cashier-action {
  height: 100%;
}
.room-tree {
  margin-top: 12px;
}
.summary-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
}
.summary-item .label {
  color: #606266;
  font-size: 13px;
}
.summary-item .value {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
.summary-item .text-warning {
  color: #e6a23c;
}
.summary-item .text-danger {
  color: #f56c6c;
}
</style>
