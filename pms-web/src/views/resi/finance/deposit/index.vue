<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable style="width: 180px">
          <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="资源类型" prop="resourceType">
        <el-select v-model="queryParams.resourceType" placeholder="请选择" clearable style="width: 120px">
          <el-option label="房间" value="ROOM" />
        </el-select>
      </el-form-item>
      <el-form-item label="资源ID" prop="resourceId">
        <el-input v-model.number="queryParams.resourceId" placeholder="资源ID" clearable style="width: 120px" />
      </el-form-item>
      <el-form-item label="状态" prop="state">
        <el-select v-model="queryParams.state" placeholder="全部" clearable style="width: 120px">
          <el-option label="已收取" value="COLLECTED" />
          <el-option label="已退还" value="REFUNDED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="资源名称" prop="resourceName" align="center" min-width="120" />
      <el-table-column label="费用名称" prop="feeName" align="center" min-width="120" />
      <el-table-column label="缴纳人" prop="customerName" align="center" min-width="100" />
      <el-table-column label="押金金额" prop="amount" align="right" width="120">
        <template slot-scope="scope">{{ scope.row.amount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="已退金额" prop="refundAmount" align="right" width="120">
        <template slot-scope="scope">{{ scope.row.refundAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="剩余金额" align="right" width="120">
        <template slot-scope="scope">{{ (scope.row.amount - (scope.row.refundAmount || 0)) | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="state" align="center" width="90">
        <template slot-scope="scope">
          <el-tag :type="scope.row.state === 'REFUNDED' ? 'info' : 'success'">
            {{ scope.row.state === 'REFUNDED' ? '已退还' : '已收取' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="收款方式" prop="payMethod" align="center" width="100">
        <template slot-scope="scope">{{ methodMap[scope.row.payMethod] || scope.row.payMethod || '-' }}</template>
      </el-table-column>
      <el-table-column label="收款单号" prop="payNo" align="center" min-width="160" />
      <el-table-column label="收取时间" prop="creatorTime" align="center" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.creatorTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="120">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-minus"
            v-hasPermi="['resi:finance:deposit:refund']"
            :disabled="scope.row.state === 'REFUNDED' || (scope.row.amount - (scope.row.refundAmount || 0)) <= 0"
            @click="handleRefund(scope.row)"
          >退还</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 退还弹窗 -->
    <el-dialog title="押金退还" :visible.sync="refundVisible" width="500px" :close-on-click-modal="false">
      <el-form ref="refundForm" :model="refundForm" :rules="refundRules" label-width="100px" size="small">
        <el-form-item label="费用名称">
          <span>{{ currentDeposit.feeName }}</span>
        </el-form-item>
        <el-form-item label="押金金额">
          <span>{{ currentDeposit.amount | formatMoney }}</span>
        </el-form-item>
        <el-form-item label="已退金额">
          <span>{{ (currentDeposit.refundAmount || 0) | formatMoney }}</span>
        </el-form-item>
        <el-form-item label="剩余可退">
          <span>{{ remainAmount | formatMoney }}</span>
        </el-form-item>
        <el-form-item label="退还金额" prop="refundAmount">
          <el-input-number v-model="refundForm.refundAmount" :min="0.01" :max="remainAmount" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="退款方式" prop="refundMethod">
          <el-select v-model="refundForm.refundMethod" placeholder="请选择" style="width: 100%">
            <el-option label="现金" value="CASH" />
            <el-option label="微信支付" value="WECHAT" />
            <el-option label="银行转账" value="TRANSFER" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="refundForm.remark" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="refundVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitRefund">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { formatMoney } from '@/utils/resi'
import { listDeposit, refundDeposit } from '@/api/resi/finance'
import { listProject } from '@/api/resi/archive'

export default {
  name: 'ResiDeposit',
  filters: { formatMoney },
  data() {
    return {
      loading: false,
      submitLoading: false,
      showSearch: true,
      tableData: [],
      total: 0,
      projectList: [],
      methodMap: { CASH: '现金', WECHAT: '微信支付', TRANSFER: '银行转账', OTHER: '其他' },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: null,
        resourceType: null,
        resourceId: null,
        state: null
      },
      refundVisible: false,
      currentDeposit: { amount: 0, refundAmount: 0 },
      refundForm: {
        refundAmount: 0,
        refundMethod: 'CASH',
        remark: ''
      },
      refundRules: {
        refundAmount: [{ required: true, message: '退还金额不能为空', trigger: 'blur' }],
        refundMethod: [{ required: true, message: '退款方式不能为空', trigger: 'change' }]
      }
    }
  },
  computed: {
    remainAmount() {
      const amount = Number(this.currentDeposit.amount || 0)
      const refund = Number(this.currentDeposit.refundAmount || 0)
      return Math.max(0, amount - refund)
    }
  },
  created() {
    this.loadProjects()
    this.getList()
  },
  methods: {
    formatMoney,
    parseTime(time) {
      if (!time) return '-'
      const d = new Date(time)
      return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}:${String(d.getSeconds()).padStart(2,'0')}`
    },
    loadProjects() {
      listProject().then(res => {
        this.projectList = res.rows || []
      }).catch(() => {})
    },
    getList() {
      this.loading = true
      listDeposit(this.queryParams).then(response => {
        this.tableData = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        projectId: null,
        resourceType: null,
        resourceId: null,
        state: null
      }
      this.getList()
    },
    handleRefund(row) {
      this.currentDeposit = row
      this.refundForm = {
        refundAmount: Math.max(0.01, Number((row.amount - (row.refundAmount || 0)).toFixed(2))),
        refundMethod: 'CASH',
        remark: ''
      }
      this.refundVisible = true
      this.$nextTick(() => {
        this.$refs.refundForm && this.$refs.refundForm.clearValidate()
      })
    },
    submitRefund() {
      this.$refs.refundForm.validate(valid => {
        if (!valid) return
        if (this.refundForm.refundAmount > this.remainAmount) {
          this.$message.warning('退还金额不能超出剩余押金金额')
          return
        }
        this.submitLoading = true
        refundDeposit(this.currentDeposit.id, {
          refundAmount: this.refundForm.refundAmount,
          refundMethod: this.refundForm.refundMethod,
          remark: this.refundForm.remark
        }).then(() => {
          this.$message.success('退还成功')
          this.refundVisible = false
          this.getList()
        }).catch(err => {
          this.$message.error(err.response?.data?.msg || '退还失败')
        }).finally(() => {
          this.submitLoading = false
        })
      })
    }
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
</style>
