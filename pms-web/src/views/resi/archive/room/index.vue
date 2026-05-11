<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable size="small" style="width: 180px" @change="handleProjectChange">
          <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="所属楼栋" prop="buildingId">
        <el-select v-model="queryParams.buildingId" placeholder="请选择楼栋" clearable size="small" style="width: 180px">
          <el-option v-for="item in buildingOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="房号" prop="roomNo">
        <el-input v-model="queryParams.roomNo" placeholder="请输入房号" clearable size="small" style="width: 150px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="state">
        <el-select v-model="queryParams.state" placeholder="状态" clearable size="small" style="width: 120px">
          <el-option label="正常" value="NORMAL" />
          <el-option label="空置" value="VACANT" />
          <el-option label="装修中" value="DECORATING" />
          <el-option label="已过户" value="TRANSFERRED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['resi:room:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['resi:room:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['resi:room:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="roomList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="所属项目" align="center" prop="projectName" width="120" />
      <el-table-column label="所属楼栋" align="center" prop="buildingName" width="120" />
      <el-table-column label="单元" align="center" prop="unitNo" width="80" />
      <el-table-column label="房号" align="center" prop="roomNo" width="80" />
      <el-table-column label="房间简称" align="center" prop="roomAlias" width="120" />
      <el-table-column label="建筑面积" align="center" prop="buildingArea" width="90" />
      <el-table-column label="状态" align="center" prop="state" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.state === 'NORMAL'" type="success">正常</el-tag>
          <el-tag v-else-if="scope.row.state === 'VACANT'" type="info">空置</el-tag>
          <el-tag v-else-if="scope.row.state === 'DECORATING'" type="warning">装修中</el-tag>
          <el-tag v-else-if="scope.row.state === 'TRANSFERRED'" type="danger">已过户</el-tag>
          <el-tag v-else>{{ scope.row.state }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['resi:room:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['resi:room:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%" @change="handleFormProjectChange">
            <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" placeholder="请选择楼栋" style="width: 100%">
            <el-option v-for="item in formBuildingOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="单元号" prop="unitNo">
          <el-input v-model="form.unitNo" placeholder="请输入单元号，如1" maxlength="20" />
        </el-form-item>
        <el-form-item label="楼层" prop="floorNo">
          <el-input-number v-model="form.floorNo" :min="1" :max="100" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="房号" prop="roomNo">
          <el-input v-model="form.roomNo" placeholder="请输入房号，如101" maxlength="50" />
        </el-form-item>
        <el-form-item label="房间简称" prop="roomAlias">
          <el-input v-model="form.roomAlias" placeholder="留空则自动生成：{楼栋名}{单元号}{房号}" maxlength="100" />
        </el-form-item>
        <el-form-item label="建筑面积" prop="buildingArea">
          <el-input-number v-model="form.buildingArea" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="套内面积" prop="innerArea">
          <el-input-number v-model="form.innerArea" :min="0" :precision="2" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="房间类型" prop="roomType">
          <el-select v-model="form.roomType" placeholder="请选择" style="width: 100%">
            <el-option label="住宅" :value="1" />
            <el-option label="商铺" :value="2" />
            <el-option label="车库" :value="3" />
            <el-option label="储藏室" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="state">
          <el-select v-model="form.state" placeholder="请选择" style="width: 100%">
            <el-option label="正常" value="NORMAL" />
            <el-option label="空置" value="VACANT" />
            <el-option label="装修中" value="DECORATING" />
            <el-option label="已过户" value="TRANSFERRED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" maxlength="500" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRoom, getRoom, addRoom, updateRoom, delRoom, listProject, listBuilding } from "@/api/resi/archive";

export default {
  name: "ResiRoom",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      roomList: [],
      projectOptions: [],
      buildingOptions: [],
      formBuildingOptions: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: undefined,
        buildingId: undefined,
        roomNo: undefined,
        state: undefined
      },
      form: {},
      rules: {
        projectId: [{ required: true, message: "所属项目不能为空", trigger: "change" }],
        buildingId: [{ required: true, message: "所属楼栋不能为空", trigger: "change" }],
        roomNo: [{ required: true, message: "房号不能为空", trigger: "blur" }],
        state: [{ required: true, message: "状态不能为空", trigger: "change" }]
      }
    };
  },
  created() {
    this.getList();
    this.getProjectOptions();
    this.getBuildingOptions();
  },
  methods: {
    getList() {
      this.loading = true;
      listRoom(this.queryParams).then(response => {
        this.roomList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getProjectOptions() {
      listProject({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.projectOptions = response.rows;
      });
    },
    getBuildingOptions(projectId) {
      const params = { pageNum: 1, pageSize: 1000 };
      if (projectId) params.projectId = projectId;
      listBuilding(params).then(response => {
        if (projectId) {
          this.buildingOptions = response.rows;
        } else {
          this.buildingOptions = response.rows;
        }
      });
    },
    handleProjectChange(val) {
      this.queryParams.buildingId = undefined;
      this.getBuildingOptions(val);
    },
    handleFormProjectChange(val) {
      this.form.buildingId = undefined;
      this.formBuildingOptions = [];
      if (!val) return;
      listBuilding({ pageNum: 1, pageSize: 1000, projectId: val }).then(response => {
        this.formBuildingOptions = response.rows;
      });
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        id: undefined,
        projectId: undefined,
        buildingId: undefined,
        unitNo: undefined,
        floorNo: 1,
        roomNo: undefined,
        roomAlias: undefined,
        buildingArea: undefined,
        innerArea: undefined,
        roomType: 1,
        state: "NORMAL",
        remark: undefined
      };
      this.formBuildingOptions = [];
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.buildingOptions = [];
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加房间";
    },
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getRoom(id).then(response => {
        this.form = response.data;
        if (this.form.projectId) {
          listBuilding({ pageNum: 1, pageSize: 1000, projectId: this.form.projectId }).then(res => {
            this.formBuildingOptions = res.rows;
          });
        }
        this.open = true;
        this.title = "修改房间";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id !== undefined) {
            updateRoom(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addRoom(this.form).then(() => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除房间编号为"' + ids + '"的数据项？').then(() => {
        return delRoom(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>
