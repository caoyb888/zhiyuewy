<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable size="small" style="width: 200px">
          <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="票据类型" prop="ticketType">
        <el-select v-model="queryParams.ticketType" placeholder="请选择票据类型" clearable size="small" style="width: 150px">
          <el-option label="收款单" :value="1" />
          <el-option label="缴费通知单" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="票据标题" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入票据标题"
          clearable
          size="small"
          style="width: 200px"
          @keyup.enter.native="handleQuery"
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
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['resi:ticketConfig:add']"
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
          v-hasPermi="['resi:ticketConfig:edit']"
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
          v-hasPermi="['resi:ticketConfig:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="ticketList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="票据标题" align="center" prop="title" :show-overflow-tooltip="true" width="180" />
      <el-table-column label="票据类型" align="center" prop="ticketType" width="110">
        <template slot-scope="scope">
          <el-tag :type="scope.row.ticketType === 1 ? 'success' : 'warning'" size="mini">
            {{ scope.row.ticketType === 1 ? '收款单' : '缴费通知单' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="收款单位" align="center" prop="collectOrg" :show-overflow-tooltip="true" width="180" />
      <el-table-column label="纸张规格" align="center" prop="paperSize" width="90">
        <template slot-scope="scope">
          <el-tag size="mini">{{ paperSizeLabel(scope.row.paperSize) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="creatorTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.creatorTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handlePreview(scope.row)"
            v-hasPermi="['resi:ticketConfig:query']"
          >预览</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['resi:ticketConfig:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['resi:ticketConfig:remove']"
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

    <!-- 添加或修改票据配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="850px" append-to-body :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="所属项目" prop="projectId">
              <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%" :disabled="form.id !== undefined">
                <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="票据类型" prop="ticketType">
              <el-select v-model="form.ticketType" placeholder="请选择票据类型" style="width: 100%">
                <el-option label="收款单" :value="1" />
                <el-option label="缴费通知单" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="票据标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入票据标题" maxlength="200" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收款单位" prop="collectOrg">
              <el-input v-model="form.collectOrg" placeholder="请输入收款单位全称" maxlength="200" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="纸张规格" prop="paperSize">
              <el-select v-model="form.paperSize" placeholder="请选择纸张规格" style="width: 100%">
                <el-option label="A4" value="A4" />
                <el-option label="A5" value="A5" />
                <el-option label="连续纸" value="ROLL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效状态" prop="enabledMark">
              <el-switch v-model="form.enabledMark" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="公司Logo" prop="logoUrl">
              <image-upload v-model="form.logoUrl" :limit="1" :file-size="2" :file-type="['png', 'jpg', 'jpeg']" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公章图片" prop="sealUrl">
              <image-upload v-model="form.sealUrl" :limit="1" :file-size="2" :file-type="['png', 'jpg', 'jpeg']" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="固定备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入收据固定备注文字" maxlength="500" show-word-limit />
        </el-form-item>

        <!-- 字段配置区域 -->
        <el-divider content-position="left">
          <span style="font-weight: bold;">打印字段配置</span>
          <el-button type="text" size="mini" icon="el-icon-refresh" @click="resetDefaultFields" style="margin-left: 10px;">恢复默认</el-button>
        </el-divider>
        <div class="field-config-tip">拖动字段可调整打印顺序，勾选"显示"控制是否在票据上展示</div>
        <el-table :data="fieldList" row-key="field" size="small" border class="field-config-table">
          <el-table-column label="排序" width="60" align="center">
            <template slot-scope="scope">
              <i class="el-icon-rank drag-handle" title="拖动排序" />
            </template>
          </el-table-column>
          <el-table-column label="字段标识" prop="field" width="120" />
          <el-table-column label="显示名称" width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.label" size="mini" maxlength="20" />
            </template>
          </el-table-column>
          <el-table-column label="是否显示" width="80" align="center">
            <template slot-scope="scope">
              <el-switch v-model="scope.row.show" :active-value="true" :inactive-value="false" />
            </template>
          </el-table-column>
          <el-table-column label="顺序" width="60" align="center">
            <template slot-scope="scope">
              <span>{{ scope.$index + 1 }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 票据预览对话框 -->
    <el-dialog title="票据预览" :visible.sync="previewOpen" width="600px" append-to-body :close-on-click-modal="false">
      <div v-if="previewData" class="ticket-preview" :class="'paper-' + previewData.config.paperSize">
        <div class="ticket-header">
          <img v-if="previewData.config.logoUrl" :src="baseUrl + previewData.config.logoUrl" class="ticket-logo" />
          <div class="ticket-title">{{ previewData.config.title || '票据标题' }}</div>
          <div class="ticket-org">{{ previewData.config.collectOrg || '' }}</div>
        </div>
        <div class="ticket-no">No. {{ previewData.payNo }}</div>
        <table class="ticket-table">
          <tbody>
            <tr v-for="(f, idx) in visiblePreviewFields" :key="idx">
              <td class="ticket-label">{{ f.label }}</td>
              <td class="ticket-value">{{ previewData[f.field] || '' }}</td>
            </tr>
          </tbody>
        </table>
        <div v-if="previewData.config.remark" class="ticket-remark">
          备注：{{ previewData.config.remark }}
        </div>
        <div class="ticket-footer">
          <div class="ticket-date">打印日期：{{ parseTime(new Date()) }}</div>
          <img v-if="previewData.config.sealUrl" :src="baseUrl + previewData.config.sealUrl" class="ticket-seal" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTicketConfig, getTicketConfig, addTicketConfig, updateTicketConfig, delTicketConfig, getTicketDefaultFields } from "@/api/resi/feeconfig";
import { listProject } from "@/api/resi/archive";
import ImageUpload from "@/components/ImageUpload";

export default {
  name: "ResiTicketConfig",
  components: { ImageUpload },
  data() {
    return {
      loading: false,
      showSearch: true,
      single: true,
      multiple: true,
      ids: [],
      total: 0,
      ticketList: [],
      projectOptions: [],
      title: "",
      open: false,
      previewOpen: false,
      previewData: null,
      baseUrl: process.env.VUE_APP_BASE_API,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: undefined,
        ticketType: undefined,
        title: undefined
      },
      form: {},
      fieldList: [],
      rules: {
        projectId: [
          { required: true, message: "所属项目不能为空", trigger: "change" }
        ],
        ticketType: [
          { required: true, message: "票据类型不能为空", trigger: "change" }
        ],
        title: [
          { max: 200, message: "长度不能超过200个字符", trigger: "blur" }
        ],
        collectOrg: [
          { max: 200, message: "长度不能超过200个字符", trigger: "blur" }
        ],
        paperSize: [
          { max: 20, message: "长度不能超过20个字符", trigger: "blur" }
        ],
        remark: [
          { max: 500, message: "长度不能超过500个字符", trigger: "blur" }
        ]
      }
    };
  },
  computed: {
    visiblePreviewFields() {
      if (!this.previewData || !this.previewData.config || !this.previewData.config.fieldConfig) {
        return [];
      }
      try {
        const fields = JSON.parse(this.previewData.config.fieldConfig);
        return fields.filter(f => f.show).sort((a, b) => a.sort - b.sort);
      } catch (e) {
        return [];
      }
    }
  },
  created() {
    this.getList();
    this.getProjectOptions();
  },
  methods: {
    /** 查询票据配置列表 */
    getList() {
      this.loading = true;
      listTicketConfig(this.queryParams).then(response => {
        this.ticketList = response.rows;
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

    /** 纸张规格中文 */
    paperSizeLabel(size) {
      const map = { A4: 'A4', A5: 'A5', ROLL: '连续纸' };
      return map[size] || size;
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
        ticketType: 1,
        title: undefined,
        collectOrg: undefined,
        paperSize: 'A4',
        logoUrl: undefined,
        sealUrl: undefined,
        remark: undefined,
        fieldConfig: undefined,
        enabledMark: 1
      };
      this.fieldList = [];
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
      this.queryParams.ticketType = undefined;
      this.queryParams.title = undefined;
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
      this.loadDefaultFields();
      this.open = true;
      this.title = "添加票据配置";
      this.$nextTick(() => {
        this.initDragSort();
      });
    },

    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getTicketConfig(id).then(response => {
        this.form = response.data;
        this.loadFieldConfig(this.form.fieldConfig);
        this.open = true;
        this.title = "修改票据配置";
        this.$nextTick(() => {
          this.initDragSort();
        });
      });
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除票据配置为"' + ids + '"的数据项？').then(function() {
        return delTicketConfig(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },

    /** 预览按钮操作 */
    handlePreview(row) {
      this.previewOpen = true;
      this.previewData = null;
      import("@/api/resi/feeconfig").then(module => {
        module.getTicketConfig(row.id).then(response => {
          this.previewData = response.data;
        });
      });
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 构建fieldConfig JSON
          const fieldConfig = this.fieldList.map((item, index) => ({
            field: item.field,
            label: item.label,
            show: item.show,
            sort: index + 1
          }));
          this.form.fieldConfig = JSON.stringify(fieldConfig);

          if (this.form.id !== undefined) {
            updateTicketConfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addTicketConfig(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },

    /** 加载默认字段配置 */
    loadDefaultFields() {
      getTicketDefaultFields().then(response => {
        this.fieldList = response.data.map(item => ({
          field: item.field,
          label: item.label,
          show: item.show,
          sort: item.sort
        }));
      });
    },

    /** 解析已有fieldConfig */
    loadFieldConfig(fieldConfigStr) {
      if (fieldConfigStr) {
        try {
          const parsed = JSON.parse(fieldConfigStr);
          this.fieldList = parsed.map(item => ({
            field: item.field,
            label: item.label,
            show: item.show,
            sort: item.sort
          }));
        } catch (e) {
          this.loadDefaultFields();
        }
      } else {
        this.loadDefaultFields();
      }
    },

    /** 恢复默认字段 */
    resetDefaultFields() {
      this.$modal.confirm("确定要恢复默认字段配置吗？当前自定义内容将丢失。").then(() => {
        this.loadDefaultFields();
      }).catch(() => {});
    },

    /** 初始化表格行拖拽排序 */
    initDragSort() {
      const table = this.$el.querySelector(".field-config-table .el-table__body-wrapper tbody");
      if (!table) return;

      // 清理旧监听器
      const oldRows = table.querySelectorAll("tr");
      oldRows.forEach(row => {
        row.draggable = true;
        row.ondragstart = null;
        row.ondragover = null;
        row.ondrop = null;
      });

      let dragRow = null;
      const rows = table.querySelectorAll("tr");
      rows.forEach(row => {
        row.draggable = true;
        row.ondragstart = (e) => {
          dragRow = row;
          row.style.opacity = "0.5";
        };
        row.ondragend = (e) => {
          row.style.opacity = "1";
          dragRow = null;
        };
        row.ondragover = (e) => {
          e.preventDefault();
        };
        row.ondrop = (e) => {
          e.preventDefault();
          if (dragRow && dragRow !== row) {
            const fromIndex = Array.from(table.children).indexOf(dragRow);
            const toIndex = Array.from(table.children).indexOf(row);
            const item = this.fieldList.splice(fromIndex, 1)[0];
            this.fieldList.splice(toIndex, 0, item);
          }
        };
      });
    }
  }
};
</script>

<style scoped>
.field-config-tip {
  color: #909399;
  font-size: 12px;
  margin-bottom: 8px;
}
.drag-handle {
  cursor: move;
  color: #909399;
}
.drag-handle:hover {
  color: #409eff;
}

/* 票据预览样式 */
.ticket-preview {
  border: 1px solid #dcdfe6;
  padding: 30px;
  background: #fff;
  margin: 0 auto;
}
.ticket-preview.paper-A4 {
  width: 210mm;
  min-height: 297mm;
}
.ticket-preview.paper-A5 {
  width: 148mm;
  min-height: 210mm;
}
.ticket-preview.paper-ROLL {
  width: 80mm;
  min-height: 150mm;
}
.ticket-header {
  text-align: center;
  margin-bottom: 20px;
  border-bottom: 2px solid #409eff;
  padding-bottom: 15px;
}
.ticket-logo {
  max-height: 50px;
  margin-bottom: 8px;
}
.ticket-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}
.ticket-org {
  font-size: 13px;
  color: #606266;
  margin-top: 5px;
}
.ticket-no {
  text-align: right;
  font-size: 12px;
  color: #909399;
  margin-bottom: 15px;
}
.ticket-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.ticket-table td {
  border: 1px solid #ebeef5;
  padding: 10px 12px;
}
.ticket-table .ticket-label {
  width: 120px;
  background: #f5f7fa;
  color: #606266;
  font-weight: 500;
}
.ticket-table .ticket-value {
  color: #303133;
}
.ticket-remark {
  margin-top: 15px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
}
.ticket-footer {
  margin-top: 30px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}
.ticket-date {
  font-size: 12px;
  color: #909399;
}
.ticket-seal {
  max-height: 80px;
  opacity: 0.8;
}
</style>
