<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable size="small" style="width: 180px">
          <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="客户姓名" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户姓名" clearable size="small" style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable size="small" style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="客户类型" prop="customerType">
        <el-select v-model="queryParams.customerType" placeholder="请选择类型" clearable size="small" style="width: 120px">
          <el-option label="业主" :value="1" />
          <el-option label="租户" :value="2" />
          <el-option label="临时" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['resi:customer:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['resi:customer:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['resi:customer:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="customerList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="客户姓名" align="center" prop="customerName" width="120" />
      <el-table-column label="手机号" align="center" width="130">
        <template slot-scope="scope">
          <span>{{ maskPhone(scope.row.phone) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="身份证号" align="center" width="180">
        <template slot-scope="scope">
          <span>{{ maskIdCard(scope.row.idCard) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="性别" align="center" prop="gender" width="80">
        <template slot-scope="scope">
          <span>{{ scope.row.gender === 1 ? '男' : scope.row.gender === 2 ? '女' : '未知' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="类型" align="center" prop="customerType" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.customerType === 1" type="success">业主</el-tag>
          <el-tag v-else-if="scope.row.customerType === 2" type="warning">租户</el-tag>
          <el-tag v-else type="info">临时</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-s-cooperation" @click="handleAsset(scope.row)">资产</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['resi:customer:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['resi:customer:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 客户新增/修改弹窗 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%">
            <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户姓名" prop="customerName">
          <el-input v-model="form.customerName" placeholder="请输入客户姓名" maxlength="100" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="20" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号（将加密存储）" maxlength="18" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
            <el-radio :label="0">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="客户类型" prop="customerType">
          <el-select v-model="form.customerType" placeholder="请选择" style="width: 100%">
            <el-option label="业主" :value="1" />
            <el-option label="租户" :value="2" />
            <el-option label="临时" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 客户资产绑定弹窗 -->
    <el-dialog :title="'客户资产绑定 - ' + currentCustomer.customerName" :visible.sync="assetOpen" width="700px" append-to-body>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleBindAdd" v-hasPermi="['resi:customer:add']">新增绑定</el-button>
        </el-col>
      </el-row>
      <el-table v-loading="assetLoading" :data="assetList" size="small">
        <el-table-column label="资产名称" align="center" prop="assetName" width="150" />
        <el-table-column label="资产类型" align="center" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.assetType === 1" type="primary">房间</el-tag>
            <el-tag v-else-if="scope.row.assetType === 2" type="warning">车位</el-tag>
            <el-tag v-else type="info">储藏室</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="绑定日期" align="center" prop="bindDate" width="120">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.bindDate, '{y}-{m}-{d}') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="解绑日期" align="center" prop="unbindDate" width="120">
          <template slot-scope="scope">
            <span>{{ scope.row.unbindDate ? parseTime(scope.row.unbindDate, '{y}-{m}-{d}') : '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="80">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.isCurrent === 1" type="success">有效</el-tag>
            <el-tag v-else type="info">已解绑</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100">
          <template slot-scope="scope">
            <el-button v-if="scope.row.isCurrent === 1" size="mini" type="text" icon="el-icon-circle-close" @click="handleUnbind(scope.row)" v-hasPermi="['resi:customer:edit']">解绑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 新增资产绑定弹窗 -->
    <el-dialog title="新增资产绑定" :visible.sync="bindOpen" width="450px" append-to-body>
      <el-form ref="bindForm" :model="bindForm" :rules="bindRules" label-width="100px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="bindForm.projectId" placeholder="请选择项目" style="width: 100%" @change="handleProjectChange">
            <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="资产类型" prop="assetType">
          <el-select v-model="bindForm.assetType" placeholder="请选择资产类型" style="width: 100%" @change="handleAssetTypeChange">
            <el-option label="房间" :value="1" />
            <el-option label="车位" :value="2" />
            <el-option label="储藏室" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择资产" prop="assetId">
          <el-select v-model="bindForm.assetId" placeholder="请选择资产" style="width: 100%" filterable :disabled="!bindForm.projectId || !bindForm.assetType">
            <el-option v-for="item in assetOptions" :key="item.id" :label="item.name || item.roomAlias || item.spaceCode || item.id" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定日期" prop="bindDate">
          <el-date-picker v-model="bindForm.bindDate" type="date" placeholder="选择日期" style="width: 100%" value-format="yyyy-MM-dd" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitBind">确 定</el-button>
        <el-button @click="cancelBind">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCustomer, getCustomer, addCustomer, updateCustomer, delCustomer, listProject, getCustomerAssets, bindCustomerAsset, unbindCustomerAsset, listRoom } from "@/api/resi/archive";
import { maskPhone, maskIdCard } from "@/utils/resi";

export default {
  name: "ResiCustomer",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      customerList: [],
      projectOptions: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: undefined,
        customerName: undefined,
        phone: undefined,
        customerType: undefined
      },
      form: {},
      rules: {
        projectId: [{ required: true, message: "所属项目不能为空", trigger: "change" }],
        customerName: [{ required: true, message: "客户姓名不能为空", trigger: "blur" }],
        phone: [{ required: true, message: "手机号不能为空", trigger: "blur" }],
        customerType: [{ required: true, message: "客户类型不能为空", trigger: "change" }]
      },
      // 资产绑定相关
      assetOpen: false,
      assetLoading: false,
      assetList: [],
      currentCustomer: {},
      // 新增绑定相关
      bindOpen: false,
      bindForm: {},
      assetOptions: [],
      bindRules: {
        projectId: [{ required: true, message: "所属项目不能为空", trigger: "change" }],
        assetType: [{ required: true, message: "资产类型不能为空", trigger: "change" }],
        assetId: [{ required: true, message: "请选择资产", trigger: "change" }],
        bindDate: [{ required: true, message: "绑定日期不能为空", trigger: "change" }]
      }
    };
  },
  created() {
    this.getList();
    this.getProjectOptions();
  },
  methods: {
    maskPhone,
    maskIdCard,
    getList() {
      this.loading = true;
      listCustomer(this.queryParams).then(response => {
        this.customerList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getProjectOptions() {
      listProject({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.projectOptions = response.rows;
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
        customerName: undefined,
        phone: undefined,
        idCard: undefined,
        gender: 0,
        customerType: 1
      };
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
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加客户";
    },
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getCustomer(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改客户";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id !== undefined) {
            updateCustomer(this.form).then(() => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addCustomer(this.form).then(() => {
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
      this.$modal.confirm('是否确认删除客户编号为"' + ids + '"的数据项？').then(() => {
        return delCustomer(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    // ==================== 资产绑定 ====================
    handleAsset(row) {
      this.currentCustomer = row;
      this.assetOpen = true;
      this.loadAssets(row.id);
    },
    loadAssets(customerId) {
      this.assetLoading = true;
      getCustomerAssets(customerId).then(response => {
        this.assetList = response.data || [];
        this.assetLoading = false;
      });
    },
    handleBindAdd() {
      this.bindForm = {
        customerId: this.currentCustomer.id,
        projectId: undefined,
        assetType: 1,
        assetId: undefined,
        bindDate: this.parseTime(new Date(), "{y}-{m}-{d}")
      };
      this.assetOptions = [];
      this.bindOpen = true;
    },
    handleProjectChange() {
      this.bindForm.assetId = undefined;
      this.loadAssetOptions();
    },
    handleAssetTypeChange() {
      this.bindForm.assetId = undefined;
      this.loadAssetOptions();
    },
    loadAssetOptions() {
      const { projectId, assetType } = this.bindForm;
      if (!projectId || !assetType) {
        this.assetOptions = [];
        return;
      }
      if (assetType === 1) {
        listRoom({ projectId, pageNum: 1, pageSize: 1000 }).then(response => {
          this.assetOptions = (response.rows || []).map(item => ({
            id: item.id,
            name: item.roomAlias || item.roomNo
          }));
        });
      } else {
        // 车位/储藏室暂无可选接口，预留扩展
        this.assetOptions = [];
      }
    },
    submitBind() {
      this.$refs["bindForm"].validate(valid => {
        if (valid) {
          bindCustomerAsset(this.bindForm).then(() => {
            this.$modal.msgSuccess("绑定成功");
            this.bindOpen = false;
            this.loadAssets(this.currentCustomer.id);
          });
        }
      });
    },
    cancelBind() {
      this.bindOpen = false;
    },
    handleUnbind(row) {
      this.$modal.confirm('是否确认解绑资产"' + row.assetName + '"？').then(() => {
        return unbindCustomerAsset(row.id);
      }).then(() => {
        this.loadAssets(this.currentCustomer.id);
        this.$modal.msgSuccess("解绑成功");
      }).catch(() => {});
    }
  }
};
</script>
