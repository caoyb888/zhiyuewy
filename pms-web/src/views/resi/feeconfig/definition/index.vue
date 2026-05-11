<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable size="small" style="width: 200px">
          <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="费用名称" prop="feeName">
        <el-input
          v-model="queryParams.feeName"
          placeholder="请输入费用名称"
          clearable
          size="small"
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="费用类型" prop="feeType">
        <el-select v-model="queryParams.feeType" placeholder="请选择费用类型" clearable size="small" style="width: 150px">
          <el-option label="周期费" value="PERIOD" />
          <el-option label="临时费" value="TEMP" />
          <el-option label="押金" value="DEPOSIT" />
          <el-option label="预收款" value="PRE" />
        </el-select>
      </el-form-item>
      <el-form-item label="计费方式" prop="calcType">
        <el-select v-model="queryParams.calcType" placeholder="请选择计费方式" clearable size="small" style="width: 150px">
          <el-option label="固定金额" value="FIXED" />
          <el-option label="按面积" value="AREA" />
          <el-option label="按用量" value="USAGE" />
          <el-option label="自定义公式" value="FORMULA" />
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
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['resi:definition:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['resi:definition:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['resi:definition:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="definitionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="费用名称" align="center" prop="feeName" :show-overflow-tooltip="true" width="140" />
      <el-table-column label="费用编码" align="center" prop="feeCode" width="100" />
      <el-table-column label="费用类型" align="center" prop="feeType" width="90">
        <template slot-scope="scope">
          <el-tag :type="feeTypeTagType(scope.row.feeType)" size="mini">
            {{ feeTypeLabel(scope.row.feeType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="计费方式" align="center" prop="calcType" width="100">
        <template slot-scope="scope">
          <el-tag :type="calcTypeTagType(scope.row.calcType)" size="mini">
            {{ calcTypeLabel(scope.row.calcType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="单价" align="right" prop="unitPrice" width="100">
        <template slot-scope="scope">
          <span>{{ scope.row.unitPrice !== null ? scope.row.unitPrice : '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="周期" align="center" width="100">
        <template slot-scope="scope">
          <span v-if="scope.row.feeType === 'PERIOD'">
            {{ scope.row.cycleValue }}{{ cycleUnitLabel(scope.row.cycleUnit) }}
          </span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="滞纳金" align="center" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.overdueEnable === 1 ? 'danger' : 'info'" size="mini">
            {{ scope.row.overdueEnable === 1 ? '启用' : '未启用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="专款专冲" align="center" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.earmarkEnable === 1 ? 'warning' : 'info'" size="mini">
            {{ scope.row.earmarkEnable === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序码" align="center" prop="sortCode" width="70" />
      <el-table-column label="创建时间" align="center" prop="creatorTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.creatorTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['resi:definition:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['resi:definition:remove']"
          >删除</el-button>
        </template>
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

    <!-- 添加或修改费用定义对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="750px" append-to-body :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="所属项目" prop="projectId">
              <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%" :disabled="form.id !== undefined">
                <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="费用编码" prop="feeCode">
              <el-input v-model="form.feeCode" placeholder="请输入费用编码" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="费用名称" prop="feeName">
              <el-input v-model="form.feeName" placeholder="请输入费用名称" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="费用类型" prop="feeType">
              <el-select v-model="form.feeType" placeholder="请选择费用类型" style="width: 100%" @change="handleFeeTypeChange">
                <el-option label="周期费" value="PERIOD" />
                <el-option label="临时费" value="TEMP" />
                <el-option label="押金" value="DEPOSIT" />
                <el-option label="预收款" value="PRE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="计费方式" prop="calcType">
              <el-select v-model="form.calcType" placeholder="请选择计费方式" style="width: 100%" @change="handleCalcTypeChange">
                <el-option label="固定金额" value="FIXED" />
                <el-option label="按面积" value="AREA" />
                <el-option label="按用量" value="USAGE" />
                <el-option label="自定义公式" value="FORMULA" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单价(元)" prop="unitPrice">
              <el-input-number v-model="form.unitPrice" :precision="4" :min="0" :controls="false" placeholder="请输入单价" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 周期配置（仅周期费显示） -->
        <el-row v-if="form.feeType === 'PERIOD'">
          <el-col :span="12">
            <el-form-item label="计费周期单位" prop="cycleUnit">
              <el-select v-model="form.cycleUnit" placeholder="请选择周期单位" style="width: 100%">
                <el-option label="月" value="MONTH" />
                <el-option label="季" value="QUARTER" />
                <el-option label="年" value="YEAR" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="周期数" prop="cycleValue">
              <el-input-number v-model="form.cycleValue" :min="1" :max="12" :controls="true" placeholder="如每2月收一次" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 公式编辑区（仅FORMULA显示） -->
        <el-form-item v-if="form.calcType === 'FORMULA'" label="计费公式" prop="formula">
          <el-input v-model="form.formula" type="textarea" :rows="3" placeholder="请输入计费公式，如：if(数量&lt;=230){return 单价*数量;}elsif(数量&lt;=400){return 单价*数量;}else{return 单价*数量;}" />
        </el-form-item>

        <!-- 公式预览（仅FORMULA显示） -->
        <div v-if="form.calcType === 'FORMULA'" class="formula-preview-box">
          <el-divider content-position="left">公式预览</el-divider>
          <el-row :gutter="10">
            <el-col :span="8">
              <el-form-item label="测试单价" label-width="80px">
                <el-input-number v-model="previewParams.price" :precision="4" :min="0" :controls="false" placeholder="单价" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="测试数量" label-width="80px">
                <el-input-number v-model="previewParams.num" :precision="4" :min="0" :controls="false" placeholder="数量" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-button type="primary" size="small" icon="el-icon-video-play" @click="handlePreviewFormula" :loading="previewLoading">测试公式</el-button>
            </el-col>
          </el-row>
          <el-row v-if="previewResult !== null">
            <el-col :span="24">
              <el-alert
                :title="previewError ? '计算失败：' + previewResult : '计算结果：' + previewResult + ' 元'"
                :type="previewError ? 'error' : 'success'"
                :closable="false"
                show-icon
              />
            </el-col>
          </el-row>
        </div>

        <el-divider content-position="left">滞纳金配置</el-divider>
        <el-row>
          <el-col :span="8">
            <el-form-item label="启用滞纳金" prop="overdueEnable">
              <el-switch v-model="form.overdueEnable" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="逾期天数" prop="overdueDays">
              <el-input-number v-model="form.overdueDays" :min="0" :max="999" :controls="true" :disabled="form.overdueEnable !== 1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="计算类型" prop="overdueType">
              <el-select v-model="form.overdueType" placeholder="请选择" style="width: 100%" :disabled="form.overdueEnable !== 1">
                <el-option label="按天" value="DAY" />
                <el-option label="按月" value="MONTH" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="滞纳金利率" prop="overdueRate">
              <el-input-number v-model="form.overdueRate" :precision="6" :min="0" :controls="false" placeholder="如日万分之五=0.000500" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="滞纳金上限" prop="overdueMax">
              <el-input-number v-model="form.overdueMax" :precision="2" :min="0" :controls="false" placeholder="NULL表示不限" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">其他配置</el-divider>
        <el-row>
          <el-col :span="8">
            <el-form-item label="取整方式" prop="roundType">
              <el-select v-model="form.roundType" placeholder="请选择" style="width: 100%">
                <el-option label="四舍五入" value="ROUND" />
                <el-option label="向上取整" value="CEIL" />
                <el-option label="截尾" value="FLOOR" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="专款专冲" prop="earmarkEnable">
              <el-switch v-model="form.earmarkEnable" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序码" prop="sortCode">
              <el-input-number v-model="form.sortCode" :min="0" :max="9999" :controls="true" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="发票项目名称" prop="invoiceTitle">
              <el-input v-model="form.invoiceTitle" placeholder="请输入发票项目名称" maxlength="200" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="税率(%)" prop="taxRate">
              <el-input-number v-model="form.taxRate" :precision="2" :min="0" :max="100" :controls="false" placeholder="如 3.00" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listFeeDefinition, getFeeDefinition, addFeeDefinition, updateFeeDefinition, delFeeDefinition, previewFormula } from "@/api/resi/feeconfig";
import { listProject } from "@/api/resi/archive";
import { feeTypeLabel, calcTypeLabel, feeTypeTagType, calcTypeTagType } from "@/utils/resi";

export default {
  name: "ResiFeeDefinition",
  data() {
    return {
      loading: false,
      showSearch: true,
      single: true,
      multiple: true,
      ids: [],
      total: 0,
      definitionList: [],
      projectOptions: [],
      title: "",
      open: false,
      previewLoading: false,
      previewResult: null,
      previewError: false,
      previewParams: {
        price: 1,
        num: 1
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: undefined,
        feeName: undefined,
        feeType: undefined,
        calcType: undefined
      },
      form: {},
      rules: {
        projectId: [
          { required: true, message: "所属项目不能为空", trigger: "change" }
        ],
        feeCode: [
          { required: true, message: "费用编码不能为空", trigger: "blur" },
          { min: 1, max: 50, message: "长度在 1 到 50 个字符", trigger: "blur" }
        ],
        feeName: [
          { required: true, message: "费用名称不能为空", trigger: "blur" },
          { min: 1, max: 100, message: "长度在 1 到 100 个字符", trigger: "blur" }
        ],
        feeType: [
          { required: true, message: "费用类型不能为空", trigger: "change" }
        ],
        calcType: [
          { required: true, message: "计费方式不能为空", trigger: "change" }
        ],
        cycleUnit: [
          { required: true, message: "计费周期单位不能为空", trigger: "change" }
        ],
        formula: [
          { required: true, message: "计费公式不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
    this.getProjectOptions();
  },
  methods: {
    feeTypeLabel,
    calcTypeLabel,
    feeTypeTagType,
    calcTypeTagType,

    /** 查询费用定义列表 */
    getList() {
      this.loading = true;
      listFeeDefinition(this.queryParams).then(response => {
        this.definitionList = response.rows;
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

    /** 费用类型变更 */
    handleFeeTypeChange(val) {
      if (val !== 'PERIOD') {
        this.form.cycleUnit = undefined;
        this.form.cycleValue = 1;
      }
      // 动态更新校验规则
      this.$nextTick(() => {
        this.$refs.form.clearValidate();
      });
    },

    /** 计费方式变更 */
    handleCalcTypeChange(val) {
      if (val !== 'FORMULA') {
        this.form.formula = undefined;
      }
      this.previewResult = null;
      this.previewError = false;
      this.$nextTick(() => {
        this.$refs.form.clearValidate();
      });
    },

    /** 周期单位中文 */
    cycleUnitLabel(unit) {
      const map = { MONTH: '个月', QUARTER: '个季度', YEAR: '年' };
      return map[unit] || unit;
    },

    /** 公式预览 */
    handlePreviewFormula() {
      if (!this.form.formula) {
        this.$modal.msgError("请先输入计费公式");
        return;
      }
      this.previewLoading = true;
      this.previewResult = null;
      this.previewError = false;
      previewFormula({
        formula: this.form.formula,
        price: String(this.previewParams.price),
        num: String(this.previewParams.num)
      }).then(response => {
        this.previewLoading = false;
        if (response.code === 200) {
          this.previewResult = response.data.result;
          this.previewError = false;
        } else {
          this.previewResult = response.msg;
          this.previewError = true;
        }
      }).catch(error => {
        this.previewLoading = false;
        this.previewResult = error.message || "公式计算失败";
        this.previewError = true;
      });
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
        feeCode: undefined,
        feeName: undefined,
        feeType: undefined,
        calcType: undefined,
        unitPrice: undefined,
        formula: undefined,
        cycleUnit: undefined,
        cycleValue: 1,
        overdueEnable: 0,
        overdueDays: 0,
        overdueType: undefined,
        overdueRate: undefined,
        overdueMax: undefined,
        roundType: 'ROUND',
        earmarkEnable: 0,
        sortCode: 0,
        invoiceTitle: undefined,
        taxRate: undefined
      };
      this.previewResult = null;
      this.previewError = false;
      this.previewParams = { price: 1, num: 1 };
      this.resetForm("form");
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
      this.queryParams.feeName = undefined;
      this.queryParams.feeType = undefined;
      this.queryParams.calcType = undefined;
      this.handleQuery();
    },

    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加费用定义";
    },

    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getFeeDefinition(id).then(response => {
        this.form = response.data;
        // 确保数值类型字段正确
        if (this.form.cycleValue === null || this.form.cycleValue === undefined) {
          this.form.cycleValue = 1;
        }
        if (this.form.overdueEnable === null || this.form.overdueEnable === undefined) {
          this.form.overdueEnable = 0;
        }
        if (this.form.overdueDays === null || this.form.overdueDays === undefined) {
          this.form.overdueDays = 0;
        }
        if (this.form.roundType === null || this.form.roundType === undefined) {
          this.form.roundType = 'ROUND';
        }
        if (this.form.earmarkEnable === null || this.form.earmarkEnable === undefined) {
          this.form.earmarkEnable = 0;
        }
        if (this.form.sortCode === null || this.form.sortCode === undefined) {
          this.form.sortCode = 0;
        }
        this.open = true;
        this.title = "修改费用定义";
      });
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id !== undefined) {
            updateFeeDefinition(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addFeeDefinition(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除费用定义为"' + ids + '"的数据项？').then(function() {
        return delFeeDefinition(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.formula-preview-box {
  background-color: #f5f7fa;
  padding: 10px 15px;
  border-radius: 4px;
  margin-bottom: 15px;
}
</style>
