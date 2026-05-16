<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable style="width: 180px">
          <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="楼栋" prop="buildingId">
        <el-select v-model="queryParams.buildingId" placeholder="请选择楼栋" clearable style="width: 150px">
          <el-option v-for="item in buildingList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="资源名称" prop="resourceName">
        <el-input v-model="queryParams.resourceName" placeholder="请输入资源名称" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="客户姓名" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户姓名" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="费用" prop="feeId">
        <el-select v-model="queryParams.feeId" placeholder="请选择费用" clearable style="width: 150px">
          <el-option v-for="item in feeList" :key="item.id" :label="item.feeName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="账单月份" prop="billPeriod">
        <el-date-picker v-model="queryParams.billPeriod" type="month" placeholder="选择月份" value-format="yyyy-MM" style="width: 140px" />
      </el-form-item>
      <el-form-item label="费用类型" prop="feeType">
        <el-select v-model="queryParams.feeType" placeholder="全部" clearable style="width: 120px">
          <el-option label="周期费" value="PERIOD" />
          <el-option label="临时费" value="TEMP" />
          <el-option label="押金" value="DEPOSIT" />
          <el-option label="预收款" value="PRE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['resi:report:arrearsDetail:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="tableData" border show-summary :summary-method="getSummaries">
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="资源名称" prop="resourceName" min-width="120" align="center" />
      <el-table-column label="客户姓名" prop="customerName" min-width="100" align="center" />
      <el-table-column label="费用名称" prop="feeName" min-width="120" align="center" />
      <el-table-column label="费用类型" prop="feeType" align="center" width="90">
        <template slot-scope="scope">{{ feeTypeLabel(scope.row.feeType) }}</template>
      </el-table-column>
      <el-table-column label="账单月份" prop="billPeriod" align="center" width="100" />
      <el-table-column label="数量" prop="num" align="right" width="100" />
      <el-table-column label="单价" prop="price" align="right" width="100">
        <template slot-scope="scope">{{ scope.row.price | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="费用金额" prop="total" align="right" width="110">
        <template slot-scope="scope">{{ scope.row.total | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="滞纳金" prop="overdueFee" align="right" width="110">
        <template slot-scope="scope">{{ scope.row.overdueFee | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="折扣减免" prop="discountAmount" align="right" width="110">
        <template slot-scope="scope">{{ scope.row.discountAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="应收合计" prop="receivable" align="right" width="120">
        <template slot-scope="scope">
          <span style="color: #f56c6c; font-weight: bold">{{ scope.row.receivable | formatMoney }}</span>
        </template>
      </el-table-column>
      <el-table-column label="已收金额" prop="paidAmount" align="right" width="110">
        <template slot-scope="scope">{{ scope.row.paidAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="欠费金额" prop="arrearsAmount" align="right" width="120">
        <template slot-scope="scope">
          <span style="color: #f56c6c; font-weight: bold">{{ scope.row.arrearsAmount | formatMoney }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="150" :show-overflow-tooltip="true" />
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
import { formatMoney, feeTypeLabel } from '@/utils/resi'
import { arrearsDetail, exportReport } from '@/api/resi/report'
import { listProject } from '@/api/resi/archive'
import { listBuilding } from '@/api/resi/archive'
import { listFeeDefinition } from '@/api/resi/feeconfig'

export default {
  name: 'ResiArrearsDetail',
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
      buildingList: [],
      feeList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: null,
        buildingId: null,
        resourceName: null,
        customerName: null,
        feeId: null,
        billPeriod: null,
        feeType: null
      }
    }
  },
  created() {
    this.loadProjects()
    this.loadFees()
    this.getList()
  },
  methods: {
    formatMoney,
    feeTypeLabel,
    loadProjects() {
      listProject({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.projectList = res.rows || []
      }).catch(() => {})
    },
    loadBuildings() {
      if (!this.queryParams.projectId) {
        this.buildingList = []
        return
      }
      listBuilding({ projectId: this.queryParams.projectId, pageNum: 1, pageSize: 1000 }).then(res => {
        this.buildingList = res.rows || []
      }).catch(() => {})
    },
    loadFees() {
      listFeeDefinition({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.feeList = res.rows || []
      }).catch(() => {})
    },
    getList() {
      this.loading = true
      arrearsDetail(this.queryParams).then(response => {
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
        buildingId: null,
        resourceName: null,
        customerName: null,
        feeId: null,
        billPeriod: null,
        feeType: null
      }
      this.buildingList = []
      this.getList()
    },
    handleExport() {
      exportReport('/resi/report/arrears-detail', this.queryParams, '欠费明细报表')
    },
    getSummaries(param) {
      const { columns, data } = param
      const sums = []
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '合计'
          return
        }
        if ([1, 2, 3, 4, 5, 14].includes(index)) {
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
