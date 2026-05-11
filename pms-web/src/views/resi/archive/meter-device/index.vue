<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable size="small" style="width: 180px">
          <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="仪表编号" prop="meterCode">
        <el-input v-model="queryParams.meterCode" placeholder="请输入仪表编号" clearable size="small" style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="仪表类型" prop="meterType">
        <el-select v-model="queryParams.meterType" placeholder="请选择" clearable size="small" style="width: 120px">
          <el-option label="水表" :value="1" />
          <el-option label="电表" :value="2" />
          <el-option label="燃气表" :value="3" />
          <el-option label="暖气表" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否公摊" prop="isPublic">
        <el-select v-model="queryParams.isPublic" placeholder="请选择" clearable size="small" style="width: 120px">
          <el-option label="分户表" :value="0" />
          <el-option label="公摊总表" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="公摊组" prop="publicGroup">
        <el-input v-model="queryParams.publicGroup" placeholder="公摊组编号" clearable size="small" style="width: 150px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['resi:meterDevice:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['resi:meterDevice:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['resi:meterDevice:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="deviceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="仪表编号" align="center" prop="meterCode" width="120" />
      <el-table-column label="仪表类型" align="center" prop="meterType" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.meterType === 1" type="primary">水表</el-tag>
          <el-tag v-else-if="scope.row.meterType === 2" type="warning">电表</el-tag>
          <el-tag v-else-if="scope.row.meterType === 3" type="success">燃气表</el-tag>
          <el-tag v-else-if="scope.row.meterType === 4" type="danger">暖气表</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="所属房间" align="center" prop="roomName" width="130">
        <template slot-scope="scope">
          <span>{{ scope.row.roomName || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="初始读数" align="center" prop="initReading" width="100" />
      <el-table-column label="倍率" align="center" prop="multiplier" width="80" />
      <el-table-column label="是否公摊" align="center" prop="isPublic" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isPublic === 1" type="danger">公摊总表</el-tag>
          <el-tag v-else type="success">分户表</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="公摊组" align="center" prop="publicGroup" width="120">
        <template slot-scope="scope">
          <span>{{ scope.row.publicGroup || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="安装日期" align="center" prop="installDate" width="120">
        <template slot-scope="scope">
          <span>{{ scope.row.installDate ? parseTime(scope.row.installDate, '{y}-{m}-{d}') : '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['resi:meterDevice:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['resi:meterDevice:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%" @change="handleProjectChange">
            <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="仪表编号" prop="meterCode">
          <el-input v-model="form.meterCode" placeholder="请输入仪表编号" maxlength="50" />
        </el-form-item>
        <el-form-item label="仪表类型" prop="meterType">
          <el-select v-model="form.meterType" placeholder="请选择" style="width: 100%">
            <el-option label="水表" :value="1" />
            <el-option label="电表" :value="2" />
            <el-option label="燃气表" :value="3" />
            <el-option label="暖气表" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否公摊" prop="isPublic">
          <el-radio-group v-model="form.isPublic" @change="handleIsPublicChange">
            <el-radio :label="0">分户表</el-radio>
            <el-radio :label="1">公摊总表</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="所属房间" prop="roomId">
          <el-select v-model="form.roomId" placeholder="请选择房间（公摊表可不选）" style="width: 100%" clearable filterable :disabled="form.isPublic === 1">
            <el-option v-for="item in roomOptions" :key="item.id" :label="item.roomAlias || item.roomNo" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="初始读数" prop="initReading">
          <el-input-number v-model="form.initReading" :min="0" :precision="4" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="倍率" prop="multiplier">
          <el-input-number v-model="form.multiplier" :min="0.0001" :precision="4" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="安装日期" prop="installDate">
          <el-date-picker v-model="form.installDate" type="date" placeholder="选择日期" style="width: 100%" value-format="yyyy-MM-dd" />
        </el-form-item>
        <el-form-item label="公摊组" prop="publicGroup">
          <el-input v-model="form.publicGroup" placeholder="请输入公摊组编号" maxlength="50" />
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
import { listMeterDevice, getMeterDevice, addMeterDevice, updateMeterDevice, delMeterDevice, listProject, listRoom } from "@/api/resi/archive";

export default {
  name: "ResiMeterDevice",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      deviceList: [],
      projectOptions: [],
      roomOptions: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: undefined,
        meterCode: undefined,
        meterType: undefined,
        isPublic: undefined,
        publicGroup: undefined
      },
      form: {},
      rules: {
        projectId: [{ required: true, message: "所属项目不能为空", trigger: "change" }],
        meterCode: [{ required: true, message: "仪表编号不能为空", trigger: "blur" }],
        meterType: [{ required: true, message: "仪表类型不能为空", trigger: "change" }],
        roomId: [{ required: true, message: "分户表必须选择所属房间", trigger: "change", validator: (rule, value, callback) => {
          if (this.form.isPublic === 0 && !value) {
            callback(new Error("分户表必须选择所属房间"));
          } else {
            callback();
          }
        }}]
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
      listMeterDevice(this.queryParams).then(response => {
        this.deviceList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getProjectOptions() {
      listProject({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.projectOptions = response.rows;
      });
    },
    loadRoomOptions(projectId) {
      if (!projectId) {
        this.roomOptions = [];
        return;
      }
      listRoom({ projectId, pageNum: 1, pageSize: 1000 }).then(response => {
        this.roomOptions = response.rows || [];
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
        roomId: undefined,
        meterCode: undefined,
        meterType: 1,
        initReading: 0,
        multiplier: 1,
        isPublic: 0,
        installDate: undefined,
        publicGroup: undefined
      };
      this.roomOptions = [];
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleProjectChange() {
      this.form.roomId = undefined;
      this.loadRoomOptions(this.form.projectId);
    },
    handleIsPublicChange(val) {
      if (val === 1) {
        this.form.roomId = undefined;
      }
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加仪表";
    },
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getMeterDevice(id).then(response => {
        this.form = response.data;
        this.loadRoomOptions(this.form.projectId);
        this.open = true;
        this.title = "修改仪表";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id !== undefined) {
            updateMeterDevice(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addMeterDevice(this.form).then(() => {
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
      this.$modal.confirm('是否确认删除仪表编号为"' + ids + '"的数据项？').then(() => {
        return delMeterDevice(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>
