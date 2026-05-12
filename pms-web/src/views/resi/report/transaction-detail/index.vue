<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable style="width: 180px">
          <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="资源名称" prop="resourceName">
        <el-input v-model="queryParams.resourceName" placeholder="请输入资源名称" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="客户姓名" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户姓名" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="费用名称" prop="feeName">
        <el-input v-model="queryParams.feeName" placeholder="请输入费用名称" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="支付方式" prop="payMethod">
        <el-select v-model="queryParams.payMethod" placeholder="全部" clearable style="width: 120px">
          <el-option label="现金" value="CASH" />
          <el-option label="微信支付" value="WECHAT" />
          <el-option label="银行转账" value="TRANSFER" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作类型" prop="payType">
        <el-select v-model="queryParams.payType" placeholder="全部" clearable style="width: 120px">
          <el-option label="收款" value="COLLECT" />
          <el-option label="退款" value="REFUND" />
          <el-option label="冲红" value="WRITEOFF" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围" prop="dateRange">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="width: 240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['resi:report:transactionDetail:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="收据号" prop="payNo" min-width="160" align="center" />
      <el-table-column label="资源名称" prop="resourceName" min-width="120" align="center" />
      <el-table-column label="客户姓名" prop="customerName" min-width="100" align="center" />
      <el-table-column label="费用名称" prop="feeName" min-width="120" align="center" />
      <el-table-column label="账单月份" prop="billPeriod" align="center" width="100" />
      <el-table-column label="操作类型" prop="payType" align="center" width="90">
        <template slot-scope="scope">
          <el-tag :type="payTypeTagType(scope.row.payType)">{{ payTypeLabel(scope.row.payType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" prop="payMethod" align="center" width="100" />
      <el-table-column label="应收合计" prop="totalAmount" align="right" width="110">
        <template slot-scope="scope">{{ scope.row.totalAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="折扣减免" prop="discountAmount" align="right" width="110">
        <template slot-scope="scope">{{ scope.row.discountAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="滞纳金" prop="overdueAmount" align="right" width="110">
        <template slot-scope="scope">{{ scope.row.overdueAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="预收款冲抵" prop="prePayAmount" align="right" width="110">
        <template slot-scope="scope">{{ scope.row.prePayAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="实收金额" prop="payAmount" align="right" width="110">
        <template slot-scope="scope">{{ scope.row.payAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="收款时间" prop="payTime" align="center" width="160" />
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { formatMoney } from '@/utils/resi'
import { transactionDetail, exportReport } from '@/api/resi/report'
import { listProject } from '@/api/resi/archive'

export default {
  name: 'ResiTransactionDetail',
  filters: {
    formatMoney
  },
  data() {
    return {
      loading: false,
      showSearch: true,
      tableData: [],
      total: 0,
      projectList: [],
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: null,
        resourceName: null,
        customerName: null,
        feeName: null,
        payMethod: null,
        payType: null,
        beginDate: null,
        endDate: null
      }
    }
  },
  created() {
    this.loadProjects()
    this.getList()
  },
  methods: {
    formatMoney,
    loadProjects() {
      listProject({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.projectList = res.rows || []
      }).catch(() => {})
    },
    getList() {
      this.loading = true
      const query = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        query.beginDate = this.dateRange[0]
        query.endDate = this.dateRange[1]
      } else {
        query.beginDate = null
        query.endDate = null
      }
      transactionDetail(query).then(response => {
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
      this.dateRange = []
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        projectId: null,
        resourceName: null,
        customerName: null,
        feeName: null,
        payMethod: null,
        payType: null,
        beginDate: null,
        endDate: null
      }
      this.getList()
    },
    handleExport() {
      const query = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        query.beginDate = this.dateRange[0]
        query.endDate = this.dateRange[1]
      }
      exportReport('/resi/report/transaction-detail', query, '交易明细报表')
    },
    payTypeLabel(payType) {
      const map = { COLLECT: '收款', REFUND: '退款', WRITEOFF: '冲红' }
      return map[payType] || payType
    },
    payTypeTagType(payType) {
      const map = { COLLECT: 'success', REFUND: 'warning', WRITEOFF: 'danger' }
      return map[payType] || ''
    }
  }
}
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
</style>
