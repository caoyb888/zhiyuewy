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
            <div v-if="prePayAmount > 0" class="summary-item">
              <div class="label">预收款冲抵</div>
              <div class="value text-warning">{{ prePayAmount | formatMoney }}</div>
            </div>
            <div class="summary-item">
              <div class="label">{{ prePayAmount > 0 ? '冲抵后应付' : '实收金额' }}</div>
              <div class="value text-danger">{{ totalPay | formatMoney }}</div>
            </div>
          </div>
          <el-divider />
          <el-form label-width="80px" size="small">
            <el-form-item v-if="prePayAccounts.length > 0" label="预收款余额">
              <div style="font-size:12px;color:#606266;">
                <div v-for="acc in prePayAccounts" :key="acc.id">
                  {{ acc.feeName || '通用预收款' }}：{{ acc.balance | formatMoney }}
                  <el-tag v-if="acc.feeId" size="mini" type="warning">专款专用</el-tag>
                </div>
              </div>
            </el-form-item>
            <el-form-item v-if="prePayAccounts.length > 0">
              <el-checkbox v-model="usePrePay" @change="calcPreview">
                使用预收款冲抵（余额合计 {{ totalPrePayBalance | formatMoney }}）
              </el-checkbox>
            </el-form-item>
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

    <!-- 收款成功弹窗（含打印） -->
    <el-dialog
      title="收款成功"
      :visible.sync="receiptVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="receiptData.payLog" class="receipt-preview">
        <div class="receipt-header">
          <h3>{{ receiptData.config.title || '收款凭证' }}</h3>
          <p v-if="receiptData.config.collectOrg" class="org-name">{{ receiptData.config.collectOrg }}</p>
        </div>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="收据号">{{ receiptData.payLog.payNo }}</el-descriptions-item>
          <el-descriptions-item label="收款时间">{{ receiptData.payLog.payTime }}</el-descriptions-item>
          <el-descriptions-item label="房间">{{ receiptData.payLog.resourceName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="业主">{{ receiptData.payLog.customerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收款人">{{ receiptData.operator.nickName || receiptData.operator.userName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">
            {{ { CASH: '现金', WECHAT: '微信支付', TRANSFER: '银行转账', OTHER: '其他' }[receiptData.payLog.payMethod] || receiptData.payLog.payMethod }}
          </el-descriptions-item>
        </el-descriptions>

        <el-table :data="receiptData.feeItems" size="small" style="margin-top: 12px" border>
          <el-table-column label="费用名称" prop="feeName" min-width="120" />
          <el-table-column label="账单月" prop="billPeriod" width="90" align="center" />
          <el-table-column label="单价" align="right" width="90">
            <template slot-scope="scope">{{ scope.row.price | formatMoney }}</template>
          </el-table-column>
          <el-table-column label="数量" prop="num" align="center" width="70" />
          <el-table-column label="金额" align="right" width="100">
            <template slot-scope="scope">{{ scope.row.receivable | formatMoney }}</template>
          </el-table-column>
        </el-table>

        <div class="receipt-summary">
          <div class="summary-line">
            <span>费用合计</span>
            <span>{{ receiptData.payLog.totalAmount | formatMoney }}</span>
          </div>
          <div class="summary-line">
            <span>折扣减免</span>
            <span class="text-warning">{{ receiptData.payLog.discountAmount | formatMoney }}</span>
          </div>
          <div class="summary-line">
            <span>滞纳金</span>
            <span>{{ receiptData.payLog.overdueAmount | formatMoney }}</span>
          </div>
          <div class="summary-line" v-if="receiptData.payLog.prePayAmount > 0">
            <span>预收款冲抵</span>
            <span>{{ receiptData.payLog.prePayAmount | formatMoney }}</span>
          </div>
          <div class="summary-line total">
            <span>实收金额</span>
            <span class="text-danger">{{ receiptData.payLog.payAmount | formatMoney }}</span>
          </div>
          <div class="summary-line" v-if="receiptData.payLog.changeAmount > 0">
            <span>找零</span>
            <span>{{ receiptData.payLog.changeAmount | formatMoney }}</span>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="closeReceiptDialog">关 闭</el-button>
        <el-button type="primary" icon="el-icon-printer" @click="handlePrintReceipt">打 印</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { formatMoney, payStateLabel, payStateTagType } from '@/utils/resi'
import { searchRoom, getRoomReceivables, getRoomSummary, calcCollect, collectPayment, getReceiptPrintData, getRoomPreAccounts } from '@/api/resi/cashier'

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
      currentProjectId: null,
      totalReceivable: 0,
      totalDiscount: 0,
      totalPay: 0,
      prePayAmount: 0,
      actualPay: 0,
      usePrePay: false,
      prePayAccounts: [],
      totalPrePayBalance: 0,
      payForm: {
        payMethod: 'CASH',
        payAmount: 0,
        note: ''
      },
      calcResult: null,
      receiptVisible: false,
      receiptData: {
        config: {},
        payLog: {},
        feeItems: [],
        operator: {}
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
          roomId: room.id,
          projectId: room.projectId
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
        this.currentProjectId = data.projectId || null
        this.loadReceivables(data.roomId)
        this.loadPreAccounts(data.projectId, data.roomId)
      }
    },
    loadPreAccounts(projectId, roomId) {
      this.prePayAccounts = []
      this.totalPrePayBalance = 0
      if (!projectId || !roomId) return
      getRoomPreAccounts(projectId, 'ROOM', roomId).then(res => {
        this.prePayAccounts = res.data || []
        this.totalPrePayBalance = this.prePayAccounts.reduce((sum, a) => sum + Number(a.balance || 0), 0)
      }).catch(() => {})
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
      this.calcPreview()
    },
    async calcPreview() {
      if (this.selectedIds.length === 0) {
        this.totalReceivable = 0
        this.totalDiscount = 0
        this.totalPay = 0
        this.prePayAmount = 0
        this.actualPay = 0
        this.payForm.payAmount = 0
        this.calcResult = null
        return
      }
      try {
        const calcReq = {
          projectId: this.currentProjectId,
          receivableIds: this.selectedIds,
          usePrePay: this.usePrePay,
          resourceType: 'ROOM',
          resourceId: this.currentRoomId
        }
        const calcRes = await calcCollect(calcReq)
        this.calcResult = calcRes.data
        this.totalReceivable = Number(this.calcResult.receivableAmount || 0) + Number(this.calcResult.prePayAmount || 0)
        this.totalDiscount = Number(this.calcResult.discountAmount || 0)
        this.prePayAmount = Number(this.calcResult.prePayAmount || 0)
        this.actualPay = Number(this.calcResult.actualPayAmount || this.calcResult.payAmount || 0)
        this.totalPay = this.actualPay
        this.payForm.payAmount = Number(this.totalPay.toFixed(2))
      } catch (e) {
        // 本地快速计算作为降级
        let totalReceivable = 0
        let totalDiscount = 0
        this.receivableList.filter(r => this.selectedIds.includes(r.id)).forEach(item => {
          totalReceivable += Number(item.receivable || 0)
          totalDiscount += Number(item.discountAmount || 0)
        })
        this.totalReceivable = totalReceivable
        this.totalDiscount = totalDiscount
        this.totalPay = totalReceivable - totalDiscount
        this.payForm.payAmount = Number(this.totalPay.toFixed(2))
        this.prePayAmount = 0
        this.actualPay = this.totalPay
      }
    },
    resetSummary() {
      this.totalReceivable = 0
      this.totalDiscount = 0
      this.totalPay = 0
      this.prePayAmount = 0
      this.actualPay = 0
      this.totalPrePayBalance = 0
      this.payForm.payAmount = 0
      this.prePayAccounts = []
      this.usePrePay = false
    },
    async handleCollect() {
      if (this.selectedIds.length === 0) {
        this.$message.warning('请至少选择一条待缴费用')
        return
      }
      if (!this.currentRoomId) {
        this.$message.warning('请先选择房间')
        return
      }
      if (!this.currentProjectId) {
        this.$message.warning('房间信息不完整，请重新搜索选择')
        return
      }
      if (!this.payForm.payMethod) {
        this.$message.warning('请选择支付方式')
        return
      }
      if (this.actualPay > 0 && (!this.payForm.payAmount || this.payForm.payAmount < 0)) {
        this.$message.warning('实收金额不能为负数')
        return
      }

      this.loading = true
      try {
        // 1. 先调用 calc 预览接口做金额校验
        const calcReq = {
          projectId: this.currentProjectId,
          receivableIds: this.selectedIds,
          usePrePay: this.usePrePay,
          resourceType: 'ROOM',
          resourceId: this.currentRoomId
        }
        const calcRes = await calcCollect(calcReq)
        this.calcResult = calcRes.data
        const expectedPay = Number(this.calcResult.actualPayAmount || this.calcResult.payAmount || 0)
        const actualPay = Number(this.payForm.payAmount)
        if (Math.abs(expectedPay - actualPay) > 0.02) {
          this.$message.warning('实收金额与应收金额不符，应收：' + expectedPay.toFixed(2) + ' 元')
          return
        }

        // 2. 确认收款
        const collectReq = {
          projectId: this.currentProjectId,
          resourceType: 'ROOM',
          resourceId: this.currentRoomId,
          receivableIds: this.selectedIds,
          payMethod: this.payForm.payMethod,
          payAmount: this.payForm.payAmount,
          note: this.payForm.note || '',
          usePrePay: this.usePrePay
        }
        const collectRes = await collectPayment(collectReq)
        const result = collectRes.data

        this.$message.success('收款成功！收据号：' + result.payNo)
        this.$notify({
          title: '收款成功',
          message: '收据号：' + result.payNo + '，实收金额：' + result.payAmount + ' 元',
          type: 'success',
          duration: 5000
        })

        // 3. 刷新费用列表和预收款余额
        this.loadReceivables(this.currentRoomId)
        this.loadPreAccounts(this.currentProjectId, this.currentRoomId)

        // 4. 打开收款成功弹窗（含打印按钮）
        this.openReceiptDialog(result.payLogId)
      } catch (error) {
        console.error('收款失败', error)
        this.$message.error(error.response?.data?.msg || '收款失败，请重试')
      } finally {
        this.loading = false
      }
    },
    openReceiptDialog(payLogId) {
      getReceiptPrintData(payLogId).then(res => {
        this.receiptData = res.data || {}
        this.receiptVisible = true
      }).catch(() => {
        this.$message.warning('打印数据加载失败')
      })
    },
    closeReceiptDialog() {
      this.receiptVisible = false
    },
    handlePrintReceipt() {
      const printWindow = window.open('', '_blank')
      if (!printWindow) {
        this.$message.warning('请允许浏览器弹窗以进行打印')
        return
      }
      const d = this.receiptData
      const cfg = d.config || {}
      const log = d.payLog || {}
      const items = d.feeItems || []
      const op = d.operator || {}

      const methodMap = { CASH: '现金', WECHAT: '微信支付', TRANSFER: '银行转账', OTHER: '其他' }
      const payMethodName = methodMap[log.payMethod] || log.payMethod || '-'

      let feeRows = ''
      items.forEach(item => {
        feeRows += `<tr>
          <td>${item.feeName || '-'}</td>
          <td align="center">${item.billPeriod || '-'}</td>
          <td align="right">${this.formatMoney(item.price)}</td>
          <td align="center">${item.num || '-'}</td>
          <td align="right">${this.formatMoney(item.total)}</td>
          <td align="right">${this.formatMoney(item.receivable)}</td>
        </tr>`
      })

      const html = `
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>收款单</title>
<style>
  body { font-family: "Microsoft YaHei", sans-serif; margin: 20px; color: #333; }
  .receipt { max-width: 720px; margin: 0 auto; border: 1px solid #ccc; padding: 24px; }
  .header { text-align: center; margin-bottom: 20px; }
  .header h2 { margin: 0; font-size: 22px; }
  .header .org { font-size: 14px; color: #666; margin-top: 4px; }
  .info-row { display: flex; justify-content: space-between; margin: 8px 0; font-size: 13px; }
  table { width: 100%; border-collapse: collapse; margin-top: 12px; font-size: 13px; }
  th, td { border: 1px solid #ccc; padding: 6px 8px; }
  th { background: #f5f5f5; }
  .summary { margin-top: 16px; font-size: 13px; }
  .summary-row { display: flex; justify-content: space-between; margin: 4px 0; }
  .footer { margin-top: 24px; font-size: 12px; color: #666; display: flex; justify-content: space-between; }
  @media print { body { margin: 0; } .no-print { display: none; } }
</style>
</head>
<body>
<div class="receipt">
  <div class="header">
    <h2>${cfg.title || '收款凭证'}</h2>
    <div class="org">${cfg.collectOrg || ''}</div>
  </div>
  <div class="info-row">
    <span><b>收据号：</b>${log.payNo || '-'}</span>
    <span><b>收款时间：</b>${log.payTime || '-'}</span>
  </div>
  <div class="info-row">
    <span><b>房间：</b>${log.resourceName || '-'}</span>
    <span><b>业主：</b>${log.customerName || '-'}</span>
  </div>
  <div class="info-row">
    <span><b>收款人：</b>${op.nickName || op.userName || '-'}</span>
    <span><b>支付方式：</b>${payMethodName}</span>
  </div>
  <table>
    <thead>
      <tr>
        <th>费用名称</th>
        <th width="90">账单月</th>
        <th width="90">单价</th>
        <th width="60">数量</th>
        <th width="100">金额</th>
        <th width="100">应收</th>
      </tr>
    </thead>
    <tbody>${feeRows}</tbody>
  </table>
  <div class="summary">
    <div class="summary-row"><span>费用合计</span><span>${this.formatMoney(log.totalAmount)}</span></div>
    <div class="summary-row"><span>折扣减免</span><span>${this.formatMoney(log.discountAmount)}</span></div>
    <div class="summary-row"><span>滞纳金</span><span>${this.formatMoney(log.overdueAmount)}</span></div>
    <div class="summary-row"><span>预收款冲抵</span><span>${this.formatMoney(log.prePayAmount)}</span></div>
    <div class="summary-row" style="font-size:15px;font-weight:bold;"><span>实收金额</span><span>${this.formatMoney(log.payAmount)}</span></div>
    ${log.changeAmount && log.changeAmount > 0 ? '<div class="summary-row"><span>找零</span><span>' + this.formatMoney(log.changeAmount) + '</span></div>' : ''}
  </div>
  ${cfg.remark ? '<div style="margin-top:12px;font-size:12px;color:#666;">备注：' + cfg.remark + '</div>' : ''}
  ${log.note ? '<div style="margin-top:8px;font-size:12px;color:#666;">收据备注：' + log.note + '</div>' : ''}
  <div class="footer">
    <span>本收据由肇新智慧物业系统自动生成</span>
    <span>打印时间：${new Date().toLocaleString()}</span>
  </div>
  <div class="no-print" style="text-align:center;margin-top:20px;">
    <button onclick="window.print()" style="padding:8px 24px;font-size:14px;cursor:pointer;">打印</button>
    <button onclick="window.close()" style="padding:8px 24px;font-size:14px;cursor:pointer;margin-left:12px;">关闭</button>
  </div>
</div>
</body>
</html>`
      printWindow.document.write(html)
      printWindow.document.close()
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

/* 收款单预览弹窗样式 */
.receipt-preview {
  padding: 0 8px;
}
.receipt-header {
  text-align: center;
  margin-bottom: 16px;
}
.receipt-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
.receipt-header .org-name {
  margin: 4px 0 0;
  font-size: 13px;
  color: #606266;
}
.receipt-summary {
  margin-top: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 4px;
}
.receipt-summary .summary-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  font-size: 13px;
  color: #606266;
}
.receipt-summary .summary-line.total {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #dcdfe6;
  font-size: 15px;
  font-weight: bold;
  color: #303133;
}
.receipt-summary .text-warning {
  color: #e6a23c;
}
.receipt-summary .text-danger {
  color: #f56c6c;
}
</style>
