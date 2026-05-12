<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable style="width: 180px">
          <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="分组维度" prop="groupBy">
        <el-select v-model="queryParams.groupBy" placeholder="请选择分组" style="width: 150px">
          <el-option label="按支付方式" value="payMethod" />
          <el-option label="按费用名称" value="feeName" />
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
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['resi:report:transactionSummary:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="tableData" border show-summary :summary-method="getSummaries">
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="分组项" prop="groupKey" min-width="160" align="center" />
      <el-table-column label="交易笔数" prop="transactionCount" align="center" width="100" />
      <el-table-column label="应收合计" prop="totalAmount" align="right" width="120">
        <template slot-scope="scope">{{ scope.row.totalAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="折扣减免" prop="discountAmount" align="right" width="120">
        <template slot-scope="scope">{{ scope.row.discountAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="滞纳金" prop="overdueAmount" align="right" width="120">
        <template slot-scope="scope">{{ scope.row.overdueAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="预收款冲抵" prop="prePayAmount" align="right" width="120">
        <template slot-scope="scope">{{ scope.row.prePayAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="实收金额" prop="payAmount" align="right" width="120">
        <template slot-scope="scope">{{ scope.row.payAmount | formatMoney }}</template>
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
  </div>
</template>

<script>
import { formatMoney } from '@/utils/resi'
import { transactionSummary, exportReport } from '@/api/resi/report'
import { listProject } from '@/api/resi/archive'

export default {
  name: 'ResiTransactionSummary',
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
        groupBy: 'payMethod',
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
      transactionSummary(query).then(response => {
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
        groupBy: 'payMethod',
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
      exportReport('/resi/report/transaction-summary', query, '交易汇总报表')
    },
    getSummaries(param) {
      const { columns, data } = param
      const sums = []
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '合计'
          return
        }
        if (index === 1) {
          sums[index] = ''
          return
        }
        const values = data.map(item => Number(item[column.property] || 0))
        if (!values.every(value => isNaN(value))) {
          const sum = values.reduce((prev, curr) => {
            const value = Number(curr)
            if (!isNaN(value)) {
              return prev + curr
            }
            return prev
          }, 0)
          sums[index] = formatMoney(sum)
        } else {
          sums[index] = ''
        }
      })
      return sums
    }
  }
}
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
</style>
