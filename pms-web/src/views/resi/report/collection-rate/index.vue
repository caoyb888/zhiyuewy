<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable style="width: 180px">
          <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="费用" prop="feeId">
        <el-select v-model="queryParams.feeId" placeholder="请选择费用" clearable style="width: 180px">
          <el-option v-for="item in feeList" :key="item.id" :label="item.feeName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="年份" prop="year">
        <el-date-picker v-model="queryParams.year" type="year" placeholder="选择年份" value-format="yyyy" style="width: 120px" />
      </el-form-item>
      <el-form-item label="月份范围" prop="periodRange">
        <el-date-picker
          v-model="periodRange"
          type="monthrange"
          range-separator="至"
          start-placeholder="开始月份"
          end-placeholder="结束月份"
          value-format="yyyy-MM"
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
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['resi:report:collectionRate:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="tableData" border show-summary :summary-method="getSummaries">
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="账单月份" prop="billPeriod" align="center" width="120" />
      <el-table-column label="费用名称" prop="feeName" align="center" min-width="120" />
      <el-table-column label="应收笔数" prop="totalCount" align="center" width="100" />
      <el-table-column label="应收金额" prop="receivableAmount" align="right" width="120">
        <template slot-scope="scope">{{ scope.row.receivableAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="实收金额" prop="paidAmount" align="right" width="120">
        <template slot-scope="scope">{{ scope.row.paidAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="收费率" prop="collectionRate" align="center" width="100">
        <template slot-scope="scope">
          <el-progress :percentage="Number(scope.row.collectionRate || 0)" :color="rateColor" />
        </template>
      </el-table-column>
      <el-table-column label="未收金额" prop="unpaidAmount" align="right" width="120">
        <template slot-scope="scope">{{ scope.row.unpaidAmount | formatMoney }}</template>
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
import { collectionRate, exportReport } from '@/api/resi/report'
import { listProject } from '@/api/resi/archive'
import { listFeeDefinition } from '@/api/resi/feeconfig'

export default {
  name: 'ResiCollectionRate',
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
      feeList: [],
      periodRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: null,
        feeId: null,
        year: null,
        beginPeriod: null,
        endPeriod: null
      },
      rateColor: [
        { color: '#f56c6c', percentage: 50 },
        { color: '#e6a23c', percentage: 80 },
        { color: '#67c23a', percentage: 100 }
      ]
    }
  },
  created() {
    this.loadProjects()
    this.loadFees()
    this.getList()
  },
  methods: {
    formatMoney,
    loadProjects() {
      listProject({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.projectList = res.rows || []
      }).catch(() => {})
    },
    loadFees() {
      listFeeDefinition({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.feeList = res.rows || []
      }).catch(() => {})
    },
    getList() {
      this.loading = true
      const query = { ...this.queryParams }
      if (this.periodRange && this.periodRange.length === 2) {
        query.beginPeriod = this.periodRange[0]
        query.endPeriod = this.periodRange[1]
      } else {
        query.beginPeriod = null
        query.endPeriod = null
      }
      collectionRate(query).then(response => {
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
      this.periodRange = []
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        projectId: null,
        feeId: null,
        year: null,
        beginPeriod: null,
        endPeriod: null
      }
      this.getList()
    },
    handleExport() {
      const query = { ...this.queryParams }
      if (this.periodRange && this.periodRange.length === 2) {
        query.beginPeriod = this.periodRange[0]
        query.endPeriod = this.periodRange[1]
      }
      exportReport('/resi/report/collection-rate', query, '收费率报表')
    },
    getSummaries(param) {
      const { columns, data } = param
      const sums = []
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '合计'
          return
        }
        if (index === 1 || index === 2 || index === 6) {
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
