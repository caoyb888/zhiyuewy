<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable style="width: 180px">
          <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id" />
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
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['resi:room:transferQuery:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="项目名称" prop="projectName" min-width="140" align="center" />
      <el-table-column label="房间" prop="roomName" min-width="140" align="center" />
      <el-table-column label="原业主" prop="oldCustomerName" align="center" width="120" />
      <el-table-column label="新业主" prop="newCustomerName" align="center" width="120" />
      <el-table-column label="过户日期" prop="transferDate" align="center" width="120" />
      <el-table-column label="备注" prop="transferRemark" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作员" prop="operator" align="center" width="100" />
      <el-table-column label="记录时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { queryTransferList } from '@/api/resi/archive'
import { listProject } from '@/api/resi/archive'

export default {
  name: 'ResiTransferQuery',
  data() {
    return {
      loading: false,
      showSearch: true,
      tableData: [],
      projectList: [],
      dateRange: [],
      queryParams: {
        projectId: null,
        startDate: null,
        endDate: null
      }
    }
  },
  created() {
    this.loadProjects()
    this.getList()
  },
  methods: {
    loadProjects() {
      listProject({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.projectList = res.rows || []
      }).catch(() => {})
    },
    getList() {
      this.loading = true
      const params = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        params.startDate = this.dateRange[0]
        params.endDate = this.dateRange[1]
      } else {
        params.startDate = null
        params.endDate = null
      }
      queryTransferList(params).then(response => {
        this.tableData = response.data || []
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.queryParams = {
        projectId: null,
        startDate: null,
        endDate: null
      }
      this.getList()
    },
    handleExport() {
      this.$modal.msg("导出功能待实现")
    }
  }
}
</script>
