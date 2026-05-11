<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable style="width: 200px">
          <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="费用名称" prop="feeId">
        <el-select v-model="queryParams.feeId" placeholder="请选择费用" clearable style="width: 200px">
          <el-option v-for="item in feeOptions" :key="item.id" :label="item.feeName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="资源名称" prop="resourceName">
        <el-input v-model="queryParams.resourceName" placeholder="请输入资源名称" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['resi:feeAllocation:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-s-operation" size="mini" @click="handleBatchOpen" v-hasPermi="['resi:feeAllocation:add']">批量分配</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['resi:feeAllocation:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="allocationList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="费用名称" align="center" prop="feeName" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="费用编码" align="center" prop="feeCode" width="100" />
      <el-table-column label="资源类型" align="center" prop="resourceType" width="90">
        <template slot-scope="scope">
          <el-tag size="mini">{{ resourceTypeLabel(scope.row.resourceType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="资源名称" align="center" prop="resourceName" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="个性化单价" align="right" prop="customPrice" width="120">
        <template slot-scope="scope">
          <span>{{ scope.row.customPrice !== null ? scope.row.customPrice : '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="生效日期" align="center" prop="startDate" width="110">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="截止日期" align="center" prop="endDate" width="110">
        <template slot-scope="scope">
          <span>{{ scope.row.endDate ? parseTime(scope.row.endDate, '{y}-{m}-{d}') : '长期有效' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="creatorTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.creatorTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['resi:feeAllocation:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['resi:feeAllocation:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%" :disabled="form.id !== undefined">
            <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用定义" prop="feeId">
          <el-select v-model="form.feeId" placeholder="请选择费用" style="width: 100%" :disabled="form.id !== undefined">
            <el-option v-for="item in feeOptions" :key="item.id" :label="item.feeName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="form.resourceType" placeholder="请选择资源类型" style="width: 100%" :disabled="form.id !== undefined">
            <el-option label="房间" value="ROOM" />
            <el-option label="车位" value="PARKING" />
            <el-option label="储藏室" value="STORAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源" prop="resourceId">
          <el-select v-model="form.resourceId" filterable placeholder="请选择资源" style="width: 100%" :disabled="form.id !== undefined">
            <el-option v-for="item in roomOptions" :key="item.id" :label="item.roomAlias || item.roomNo" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="个性化单价" prop="customPrice">
          <el-input-number v-model="form.customPrice" :precision="4" :min="0" :controls="false" placeholder="为空则使用费用定义默认价" style="width: 100%" />
        </el-form-item>
        <el-form-item label="生效日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" placeholder="选择生效日期" value-format="yyyy-MM-dd" style="width: 100%" />
        </el-form-item>
        <el-form-item label="截止日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" placeholder="选择截止日期（留空长期有效）" value-format="yyyy-MM-dd" style="width: 100%" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 批量分配向导对话框 -->
    <el-dialog title="批量费用分配" :visible.sync="batchOpen" width="650px" append-to-body :close-on-click-modal="false">
      <el-steps :active="batchStep" finish-status="success" simple style="margin-bottom: 20px">
        <el-step title="选择参数" />
        <el-step title="预览数量" />
        <el-step title="执行结果" />
      </el-steps>

      <!-- 步骤1：选择参数 -->
      <div v-if="batchStep === 0">
        <el-form ref="batchForm" :model="batchParams" :rules="batchRules" label-width="110px">
          <el-form-item label="所属项目" prop="projectId">
            <el-select v-model="batchParams.projectId" placeholder="请选择项目" style="width: 100%" @change="handleBatchProjectChange">
              <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="费用定义" prop="feeId">
            <el-select v-model="batchParams.feeId" placeholder="请选择费用" style="width: 100%">
              <el-option v-for="item in batchFeeOptions" :key="item.id" :label="item.feeName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="批量方式" prop="batchType">
            <el-radio-group v-model="batchParams.batchType" @change="handleBatchTypeChange">
              <el-radio label="BUILDING">按楼栋</el-radio>
              <el-radio label="UNIT">按单元</el-radio>
              <el-radio label="PROJECT">全项目</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="楼栋" prop="buildingId" v-if="batchParams.batchType === 'BUILDING' || batchParams.batchType === 'UNIT'">
            <el-select v-model="batchParams.buildingId" placeholder="请选择楼栋" style="width: 100%" @change="handleBatchBuildingChange">
              <el-option v-for="item in buildingOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="单元号" prop="unitNo" v-if="batchParams.batchType === 'UNIT'">
            <el-select v-model="batchParams.unitNo" placeholder="请选择单元" style="width: 100%">
              <el-option v-for="item in unitOptions" :key="item" :label="item + '单元'" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="生效日期" prop="startDate">
            <el-date-picker v-model="batchParams.startDate" type="date" placeholder="选择生效日期" value-format="yyyy-MM-dd" style="width: 100%" />
          </el-form-item>
          <el-form-item label="截止日期">
            <el-date-picker v-model="batchParams.endDate" type="date" placeholder="选择截止日期（留空长期有效）" value-format="yyyy-MM-dd" style="width: 100%" />
          </el-form-item>
          <el-form-item label="个性化单价">
            <el-input-number v-model="batchParams.customPrice" :precision="4" :min="0" :controls="false" placeholder="为空则使用费用定义默认价" style="width: 100%" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤2：预览数量 -->
      <div v-if="batchStep === 1" style="text-align: center; padding: 20px 0">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="preview-stat">
              <div class="preview-number">{{ previewResult.total }}</div>
              <div class="preview-label">目标资源总数</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="preview-stat">
              <div class="preview-number" style="color: #E6A23C">{{ previewResult.skip }}</div>
              <div class="preview-label">已存在（将跳过）</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="preview-stat">
              <div class="preview-number" style="color: #67C23A">{{ previewResult.newCount }}</div>
              <div class="preview-label">待分配数量</div>
            </div>
          </el-col>
        </el-row>
        <el-alert v-if="previewResult.newCount === 0" title="无可分配资源（全部已存在或目标为空）" type="warning" :closable="false" show-icon style="margin-top: 20px" />
      </div>

      <!-- 步骤3：执行结果 -->
      <div v-if="batchStep === 2" style="text-align: center; padding: 20px 0">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="preview-stat">
              <div class="preview-number">{{ batchResult.total }}</div>
              <div class="preview-label">目标资源总数</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="preview-stat">
              <div class="preview-number" style="color: #67C23A">{{ batchResult.success }}</div>
              <div class="preview-label">分配成功</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="preview-stat">
              <div class="preview-number" style="color: #E6A23C">{{ batchResult.skip }}</div>
              <div class="preview-label">跳过（已存在）</div>
            </div>
          </el-col>
        </el-row>
        <el-alert v-if="batchResult.success > 0" title="批量分配执行成功" type="success" :closable="false" show-icon style="margin-top: 20px" />
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button v-if="batchStep > 0 && batchStep < 2" @click="batchStep--">上一步</el-button>
        <el-button v-if="batchStep === 0" type="primary" @click="handleBatchPreview">下一步：预览数量</el-button>
        <el-button v-if="batchStep === 1 && previewResult.newCount > 0" type="primary" @click="handleBatchConfirm">确认执行</el-button>
        <el-button v-if="batchStep === 1 && previewResult.newCount === 0" type="primary" disabled>无可分配资源</el-button>
        <el-button v-if="batchStep === 2" type="primary" @click="batchOpen = false; getList()">完成</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listFeeAllocation,
  getFeeAllocation,
  addFeeAllocation,
  updateFeeAllocation,
  delFeeAllocation,
  batchAllocate,
  previewBatchAllocate
} from "@/api/resi/feeconfig";
import { listProject } from "@/api/resi/archive";
import { listBuilding } from "@/api/resi/archive";
import { listRoom } from "@/api/resi/archive";
import { selectFeeDefinition } from "@/api/resi/feeconfig";

export default {
  name: "ResiFeeAllocation",
  data() {
    return {
      loading: false,
      showSearch: true,
      single: true,
      multiple: true,
      ids: [],
      total: 0,
      allocationList: [],
      projectOptions: [],
      feeOptions: [],
      roomOptions: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: undefined,
        feeId: undefined,
        resourceName: undefined
      },
      form: {},
      rules: {
        projectId: [{ required: true, message: "所属项目不能为空", trigger: "change" }],
        feeId: [{ required: true, message: "费用定义不能为空", trigger: "change" }],
        resourceType: [{ required: true, message: "资源类型不能为空", trigger: "change" }],
        resourceId: [{ required: true, message: "资源不能为空", trigger: "change" }],
        startDate: [{ required: true, message: "生效日期不能为空", trigger: "change" }]
      },
      // 批量分配向导
      batchOpen: false,
      batchStep: 0,
      batchParams: {
        projectId: undefined,
        feeId: undefined,
        batchType: "BUILDING",
        buildingId: undefined,
        unitNo: undefined,
        startDate: undefined,
        endDate: undefined,
        customPrice: undefined
      },
      batchRules: {
        projectId: [{ required: true, message: "所属项目不能为空", trigger: "change" }],
        feeId: [{ required: true, message: "费用定义不能为空", trigger: "change" }],
        batchType: [{ required: true, message: "批量方式不能为空", trigger: "change" }],
        buildingId: [{ required: true, message: "楼栋不能为空", trigger: "change" }],
        unitNo: [{ required: true, message: "单元号不能为空", trigger: "change" }],
        startDate: [{ required: true, message: "生效日期不能为空", trigger: "change" }]
      },
      batchFeeOptions: [],
      buildingOptions: [],
      unitOptions: [],
      previewResult: { total: 0, existing: 0, newCount: 0, skip: 0 },
      batchResult: { total: 0, success: 0, skip: 0 }
    };
  },
  created() {
    this.getList();
    this.getProjectOptions();
    this.getFeeOptions();
  },
  methods: {
    /** 查询费用分配列表 */
    getList() {
      this.loading = true;
      listFeeAllocation(this.queryParams).then(response => {
        this.allocationList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },

    /** 获取项目下拉选项 */
    getProjectOptions() {
      listProject({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.projectOptions = response.rows;
      });
    },

    /** 获取费用定义选项 */
    getFeeOptions(projectId) {
      const pid = projectId || this.queryParams.projectId;
      if (!pid) {
        this.feeOptions = [];
        return;
      }
      selectFeeDefinition(pid).then(response => {
        this.feeOptions = response.data || [];
      });
    },

    /** 获取房间选项 */
    getRoomOptions(projectId) {
      if (!projectId) {
        this.roomOptions = [];
        return;
      }
      listRoom({ projectId: projectId, pageNum: 1, pageSize: 1000 }).then(response => {
        this.roomOptions = response.rows || [];
      });
    },

    /** 资源类型中文 */
    resourceTypeLabel(type) {
      const map = { ROOM: "房间", PARKING: "车位", STORAGE: "储藏室" };
      return map[type] || type;
    },

    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },

    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.queryParams.projectId = undefined;
      this.queryParams.feeId = undefined;
      this.queryParams.resourceName = undefined;
      this.handleQuery();
    },

    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },

    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },

    // 表单重置
    reset() {
      this.form = {
        id: undefined,
        projectId: undefined,
        feeId: undefined,
        resourceType: "ROOM",
        resourceId: undefined,
        resourceName: undefined,
        customPrice: undefined,
        customFormula: undefined,
        startDate: undefined,
        endDate: undefined
      };
      this.resetForm("form");
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增费用分配";
    },

    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getFeeAllocation(id).then(response => {
        this.form = response.data;
        this.getRoomOptions(this.form.projectId);
        this.open = true;
        this.title = "修改费用分配";
      });
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 回填 resourceName
          if (this.form.resourceType === "ROOM") {
            const room = this.roomOptions.find(r => r.id === this.form.resourceId);
            if (room) {
              this.form.resourceName = room.roomAlias || room.roomNo;
            }
          }
          if (this.form.id !== undefined) {
            updateFeeAllocation(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addFeeAllocation(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除选中的费用分配数据？').then(function() {
        return delFeeAllocation(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },

    // ==================== 批量分配向导 ====================

    /** 打开批量分配向导 */
    handleBatchOpen() {
      this.batchOpen = true;
      this.batchStep = 0;
      this.batchParams = {
        projectId: undefined,
        feeId: undefined,
        batchType: "BUILDING",
        buildingId: undefined,
        unitNo: undefined,
        startDate: this.parseTime(new Date(), "{y}-{m}-{d}"),
        endDate: undefined,
        customPrice: undefined
      };
      this.previewResult = { total: 0, existing: 0, newCount: 0, skip: 0 };
      this.batchResult = { total: 0, success: 0, skip: 0 };
      this.batchFeeOptions = [];
      this.buildingOptions = [];
      this.unitOptions = [];
    },

    /** 批量向导：项目切换 */
    handleBatchProjectChange(val) {
      this.batchParams.feeId = undefined;
      this.batchParams.buildingId = undefined;
      this.batchParams.unitNo = undefined;
      this.batchFeeOptions = [];
      this.buildingOptions = [];
      this.unitOptions = [];
      if (val) {
        selectFeeDefinition(val).then(response => {
          this.batchFeeOptions = response.data || [];
        });
        listBuilding({ projectId: val, pageNum: 1, pageSize: 1000 }).then(response => {
          this.buildingOptions = response.rows || [];
        });
      }
    },

    /** 批量向导：方式切换 */
    handleBatchTypeChange(val) {
      this.batchParams.buildingId = undefined;
      this.batchParams.unitNo = undefined;
      this.unitOptions = [];
    },

    /** 批量向导：楼栋切换 */
    handleBatchBuildingChange(val) {
      this.batchParams.unitNo = undefined;
      this.unitOptions = [];
      if (val && this.batchParams.batchType === "UNIT") {
        // 查询该楼栋下所有单元号
        listRoom({ buildingId: val, pageNum: 1, pageSize: 1000 }).then(response => {
          const rooms = response.rows || [];
          const unitSet = new Set();
          rooms.forEach(r => {
            if (r.unitNo) {
              unitSet.add(r.unitNo);
            } else {
              unitSet.add("无单元");
            }
          });
          this.unitOptions = Array.from(unitSet).sort();
        });
      }
    },

    /** 批量向导：预览数量 */
    handleBatchPreview() {
      this.$refs["batchForm"].validate(valid => {
        if (valid) {
          const params = {
            projectId: this.batchParams.projectId,
            feeId: this.batchParams.feeId,
            batchType: this.batchParams.batchType,
            buildingId: this.batchParams.buildingId,
            unitNo: this.batchParams.unitNo === "无单元" ? "" : this.batchParams.unitNo,
            startDate: this.batchParams.startDate
          };
          previewBatchAllocate(params).then(response => {
            if (response.code === 200) {
              this.previewResult = response.data;
              this.batchStep = 1;
            } else {
              this.$modal.msgError(response.msg || "预览失败");
            }
          }).catch(error => {
            this.$modal.msgError(error.message || "预览失败");
          });
        }
      });
    },

    /** 批量向导：确认执行 */
    handleBatchConfirm() {
      const data = {
        projectId: this.batchParams.projectId,
        feeId: this.batchParams.feeId,
        batchType: this.batchParams.batchType,
        buildingId: this.batchParams.buildingId,
        unitNo: this.batchParams.unitNo === "无单元" ? "" : this.batchParams.unitNo,
        startDate: this.batchParams.startDate,
        endDate: this.batchParams.endDate,
        customPrice: this.batchParams.customPrice
      };
      batchAllocate(data).then(response => {
        if (response.code === 200) {
          this.batchResult = response.data;
          this.batchStep = 2;
        } else {
          this.$modal.msgError(response.msg || "批量分配失败");
        }
      }).catch(error => {
        this.$modal.msgError(error.message || "批量分配失败");
      });
    }
  },
  watch: {
    "queryParams.projectId": {
      handler(val) {
        this.getFeeOptions(val);
      },
      immediate: false
    },
    "form.projectId": {
      handler(val) {
        if (val) {
          this.getFeeOptions(val);
          this.getRoomOptions(val);
        }
      },
      immediate: false
    }
  }
};
</script>

<style scoped>
.preview-stat {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px 10px;
}
.preview-number {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  line-height: 1.2;
}
.preview-label {
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}
</style>
