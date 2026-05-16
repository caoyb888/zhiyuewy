<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable size="small" style="width: 180px">
          <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="应收ID" prop="receivableId">
        <el-input v-model="queryParams.receivableId" placeholder="请输入应收记录ID" clearable size="small" style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="调账类型" prop="adjustType">
        <el-select v-model="queryParams.adjustType" placeholder="调账类型" clearable size="small" style="width: 140px">
          <el-option label="金额调整" value="AMOUNT" />
          <el-option label="账期调整" value="PERIOD" />
          <el-option label="状态调整" value="STATUS" />
          <el-option label="减免滞纳金" value="OVERDUE_WAIVE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="adjustLogList">
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="应收记录ID" align="center" prop="receivableId" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="调账类型" align="center" prop="adjustType" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.adjustType === 'AMOUNT'" type="primary">金额调整</el-tag>
          <el-tag v-else-if="scope.row.adjustType === 'PERIOD'" type="warning">账期调整</el-tag>
          <el-tag v-else-if="scope.row.adjustType === 'STATUS'" type="info">状态调整</el-tag>
          <el-tag v-else-if="scope.row.adjustType === 'OVERDUE_WAIVE'" type="success">减免滞纳金</el-tag>
          <el-tag v-else>{{ scope.row.adjustType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="调整前" align="left" prop="beforeValue" width="200" :show-overflow-tooltip="true" />
      <el-table-column label="调整后" align="left" prop="afterValue" width="200" :show-overflow-tooltip="true" />
      <el-table-column label="调整原因" align="left" prop="reason" width="200" :show-overflow-tooltip="true" />
      <el-table-column label="操作人" align="center" prop="creatorUserId" width="100" />
      <el-table-column label="操作时间" align="center" prop="creatorTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.creatorTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listAdjustLog } from "@/api/resi/finance";
import { listProject } from "@/api/resi/archive";

export default {
  name: "ResiAdjustLog",
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      adjustLogList: [],
      projectOptions: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: undefined,
        receivableId: undefined,
        adjustType: undefined
      }
    };
  },
  created() {
    this.getList();
    this.getProjectOptions();
  },
  methods: {
    getList() {
      this.loading = true;
      listAdjustLog(this.queryParams).then(response => {
        this.adjustLogList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getProjectOptions() {
      listProject({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.projectOptions = response.rows;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    }
  }
};
</script>
