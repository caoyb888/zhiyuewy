<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable size="small" style="width: 180px">
          <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="抄表期间" prop="period">
        <el-date-picker
          v-model="queryParams.period"
          type="month"
          placeholder="请选择期间"
          size="small"
          style="width: 150px"
          value-format="yyyy-MM"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small" style="width: 120px">
          <el-option label="已录入" value="INPUT" />
          <el-option label="已入账" value="BILLED" />
          <el-option label="已复核" value="VERIFIED" />
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
          v-hasPermi="['resi:meter:add']"
        >录入抄表</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['resi:meter:edit']"
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
          v-hasPermi="['resi:meter:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-s-order"
          size="mini"
          :disabled="single"
          @click="handleBill"
          v-hasPermi="['resi:meter:bill']"
        >单户入账</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-s-claim"
          size="mini"
          :disabled="multiple"
          @click="handleBatchBill"
          v-hasPermi="['resi:meter:bill']"
        >批量入账</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="readingList" @selection-change="handleSelectionChange" :row-class-name="tableRowClassName">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="" align="center" width="40">
        <template slot-scope="scope">
          <i v-if="isWarningRow(scope.row)" class="el-icon-warning" style="color: #e6a23c; font-size: 16px;" title="读数回退预警" />
        </template>
      </el-table-column>
      <el-table-column label="仪表编号" align="center" prop="meterCode" width="120">
        <template slot-scope="scope">
          <span>{{ scope.row.meterCode }}</span>
          <el-tag v-if="scope.row.isPublic === 1" type="danger" size="mini" style="margin-left: 4px">总</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="抄表期间" align="center" prop="period" width="100" />
      <el-table-column label="上次读数" align="right" prop="lastReading" width="100">
        <template slot-scope="scope">
          <span>{{ scope.row.lastReading !== null ? scope.row.lastReading : '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="本次读数" align="right" prop="currReading" width="100" />
      <el-table-column label="原始用量" align="right" prop="rawUsage" width="100">
        <template slot-scope="scope">
          <span style="font-weight: bold; color: #409eff">{{ scope.row.rawUsage }}</span>
        </template>
      </el-table-column>
      <el-table-column label="损耗量" align="right" prop="lossAmount" width="90">
        <template slot-scope="scope">
          <span>{{ scope.row.lossAmount > 0 ? scope.row.lossAmount : '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="公摊量" align="right" prop="shareAmount" width="90">
        <template slot-scope="scope">
          <span>{{ scope.row.shareAmount > 0 ? scope.row.shareAmount : '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="计费用量" align="right" prop="billedUsage" width="100">
        <template slot-scope="scope">
          <span style="font-weight: bold; color: #e6a23c">{{ scope.row.billedUsage }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)" size="mini">
            {{ statusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="抄表日期" align="center" prop="currDate" width="110">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.currDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="creatorTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.creatorTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="180">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['resi:meter:edit']"
            :disabled="scope.row.status !== 'INPUT'"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['resi:meter:remove']"
            :disabled="scope.row.status !== 'INPUT'"
          >删除</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-s-order"
            @click="handleBillRow(scope.row)"
            v-hasPermi="['resi:meter:bill']"
            :disabled="scope.row.status !== 'INPUT'"
            style="color: #e6a23c"
          >入账</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-info"
            @click="handleShareDetail(scope.row)"
            v-hasPermi="['resi:meter:share']"
            :disabled="!scope.row.publicGroup"
          >公摊</el-button>
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

    <!-- 添加或修改抄表记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="650px" append-to-body :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="所属项目" prop="projectId">
              <el-select v-model="form.projectId" placeholder="请选择项目" style="width: 100%" @change="handleProjectChange" :disabled="form.id !== undefined">
                <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仪表" prop="meterId">
              <el-select v-model="form.meterId" placeholder="请选择仪表" style="width: 100%" @change="handleMeterChange" :disabled="form.id !== undefined">
                <el-option v-for="item in meterOptions" :key="item.id" :label="item.meterCode" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="抄表期间" prop="period">
              <el-date-picker
                v-model="form.period"
                type="month"
                placeholder="请选择期间"
                style="width: 100%"
                value-format="yyyy-MM"
                :disabled="form.id !== undefined"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="抄表日期" prop="currDate">
              <el-date-picker
                v-model="form.currDate"
                type="date"
                placeholder="请选择日期"
                style="width: 100%"
                value-format="yyyy-MM-dd"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="上次读数" prop="lastReading">
              <el-input-number v-model="form.lastReading" :precision="4" :controls="false" placeholder="自动带入" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="本次读数" prop="currReading">
              <el-input-number v-model="form.currReading" :precision="4" :min="0" :controls="false" placeholder="请输入本次读数" style="width: 100%" @change="handleReadingChange" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="损耗比率" prop="lossRate">
              <el-input-number v-model="form.lossRate" :precision="4" :min="0" :max="1" :controls="false" placeholder="如0.03" style="width: 100%" @change="handleReadingChange" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公摊量" prop="shareAmount">
              <el-input-number v-model="form.shareAmount" :precision="4" :min="0" :controls="false" placeholder="公摊后填写" style="width: 100%" @change="handleReadingChange" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 用量计算预览 -->
        <el-divider content-position="left">用量计算</el-divider>
        <el-row>
          <el-col :span="8">
            <el-form-item label="原始用量">
              <el-input v-model="calcResult.rawUsage" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="损耗量">
              <el-input v-model="calcResult.lossAmount" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="计费用量">
              <el-input v-model="calcResult.billedUsage" readonly style="font-weight: bold; color: #e6a23c" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 公摊详情抽屉 -->
    <el-drawer
      :title="'公摊详情 - ' + shareDetailTitle"
      :visible.sync="shareDrawerVisible"
      direction="rtl"
      size="700px"
      :close-on-click-modal="false"
    >
      <div v-loading="shareLoading" style="padding: 0 20px 20px;">
        <!-- 公摊组汇总卡片 -->
        <el-card v-if="shareResult" shadow="never" style="margin-bottom: 15px;">
          <div slot="header">
            <span>公摊组：{{ shareResult.publicGroup }}</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="share-stat-item">
                <div class="share-stat-label">总表用量</div>
                <div class="share-stat-value">{{ shareResult.totalUsage }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="share-stat-item">
                <div class="share-stat-label">分户合计</div>
                <div class="share-stat-value">{{ shareResult.subTotalUsage }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="share-stat-item">
                <div class="share-stat-label">公摊总量</div>
                <div class="share-stat-value" style="color: #e6a23c">{{ shareResult.shareTotal }}</div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 10px;">
            <el-col :span="8">
              <div class="share-stat-item">
                <div class="share-stat-label">组内总面积</div>
                <div class="share-stat-value">{{ shareResult.totalArea }} ㎡</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="share-stat-item">
                <div class="share-stat-label">分户数量</div>
                <div class="share-stat-value">{{ shareResult.roomCount }} 户</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 公摊明细表格 -->
        <el-table v-if="shareResult && shareResult.details" :data="shareResult.details" size="small" border>
          <el-table-column label="房间名称" align="center" prop="roomName" min-width="120" />
          <el-table-column label="建筑面积" align="right" prop="buildingArea" width="100">
            <template slot-scope="scope">
              {{ scope.row.buildingArea }} ㎡
            </template>
          </el-table-column>
          <el-table-column label="面积占比" align="right" prop="areaRatio" width="100">
            <template slot-scope="scope">
              {{ (scope.row.areaRatio * 100).toFixed(2) }}%
            </template>
          </el-table-column>
          <el-table-column label="原始用量" align="right" prop="rawUsage" width="100" />
          <el-table-column label="公摊分摊量" align="right" prop="shareAmount" width="110">
            <template slot-scope="scope">
              <span style="color: #e6a23c; font-weight: bold">{{ scope.row.shareAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="计费用量" align="right" prop="billedUsage" width="100">
            <template slot-scope="scope">
              <span style="font-weight: bold">{{ scope.row.billedUsage }}</span>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!shareResult || !shareResult.details || shareResult.details.length === 0" description="暂无公摊明细数据" />
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { listMeterReading, getMeterReading, addMeterReading, updateMeterReading, delMeterReading, billMeterReading, batchBillMeterReading, sharePreview } from "@/api/resi/meter";
import { listProject } from "@/api/resi/archive";
import { listMeterDevice } from "@/api/resi/archive";

export default {
  name: "ResiMeterReading",
  data() {
    return {
      loading: false,
      showSearch: true,
      single: true,
      multiple: true,
      ids: [],
      total: 0,
      readingList: [],
      projectOptions: [],
      meterOptions: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: undefined,
        period: undefined,
        status: undefined
      },
      form: {},
      calcResult: {
        rawUsage: "—",
        lossAmount: "—",
        billedUsage: "—"
      },
      shareDrawerVisible: false,
      shareLoading: false,
      shareDetailTitle: "",
      shareResult: null,
      rules: {
        projectId: [
          { required: true, message: "所属项目不能为空", trigger: "change" }
        ],
        meterId: [
          { required: true, message: "仪表不能为空", trigger: "change" }
        ],
        period: [
          { required: true, message: "抄表期间不能为空", trigger: "change" }
        ],
        currReading: [
          { required: true, message: "本次读数不能为空", trigger: "blur" }
        ],
        currDate: [
          { required: true, message: "抄表日期不能为空", trigger: "change" }
        ]
      }
    };
  },
  created() {
    this.getList();
    this.getProjectOptions();
  },
  methods: {
    /** 查询抄表记录列表 */
    getList() {
      this.loading = true;
      listMeterReading(this.queryParams).then(response => {
        this.readingList = response.rows;
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

    /** 状态标签类型 */
    statusTagType(status) {
      const map = { INPUT: "info", BILLED: "success", VERIFIED: "primary" };
      return map[status] || "info";
    },

    /** 状态中文 */
    statusLabel(status) {
      const map = { INPUT: "已录入", BILLED: "已入账", VERIFIED: "已复核" };
      return map[status] || status;
    },

    /** 判断是否为预警行 */
    isWarningRow(row) {
      if (row.currReading != null && row.lastReading != null) {
        const curr = parseFloat(row.currReading);
        const last = parseFloat(row.lastReading);
        if (curr < last) return true;
      }
      if (row.rawUsage != null && parseFloat(row.rawUsage) < 0) return true;
      return false;
    },

    /** 表格行样式 */
    tableRowClassName({ row }) {
      if (this.isWarningRow(row)) {
        return 'warning-row';
      }
      return '';
    },

    /** 打开公摊详情抽屉 */
    handleShareDetail(row) {
      if (!row.publicGroup) {
        this.$modal.msgWarning("该仪表未配置公摊组");
        return;
      }
      this.shareDetailTitle = row.meterCode + ' (' + row.period + ')';
      this.shareDrawerVisible = true;
      this.shareLoading = true;
      this.shareResult = null;
      sharePreview({
        projectId: row.projectId,
        period: row.period,
        publicGroup: row.publicGroup
      }).then(response => {
        this.shareLoading = false;
        const list = response.data;
        if (list && list.length > 0) {
          this.shareResult = list[0];
        } else {
          this.shareResult = { publicGroup: row.publicGroup, details: [] };
        }
      }).catch(() => {
        this.shareLoading = false;
      });
    },

    /** 项目变更时加载仪表列表 */
    handleProjectChange(projectId) {
      this.meterOptions = [];
      this.form.meterId = undefined;
      if (projectId) {
        listMeterDevice({ projectId: projectId, pageNum: 1, pageSize: 1000 }).then(response => {
          this.meterOptions = response.rows;
        });
      }
    },

    /** 仪表变更时自动计算（仅新增时） */
    handleMeterChange(meterId) {
      if (!meterId || this.form.id) return;
      // 前端不自动带入上期读数，由后端处理
    },

    /** 读数变更时前端预览计算 */
    handleReadingChange() {
      const last = parseFloat(this.form.lastReading) || 0;
      const curr = parseFloat(this.form.currReading) || 0;
      const lossRate = parseFloat(this.form.lossRate) || 0;
      const share = parseFloat(this.form.shareAmount) || 0;

      if (curr > 0) {
        const raw = (curr - last).toFixed(4);
        const loss = (raw * lossRate).toFixed(4);
        const billed = (raw - loss + share).toFixed(4);
        this.calcResult = {
          rawUsage: raw,
          lossAmount: loss,
          billedUsage: billed
        };
      } else {
        this.calcResult = { rawUsage: "—", lossAmount: "—", billedUsage: "—" };
      }
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
        meterId: undefined,
        roomId: undefined,
        period: undefined,
        lastReading: undefined,
        lastDate: undefined,
        currReading: undefined,
        currDate: undefined,
        rawUsage: undefined,
        lossRate: 0,
        lossAmount: 0,
        shareAmount: 0,
        billedUsage: undefined,
        status: "INPUT",
        remark: undefined
      };
      this.calcResult = { rawUsage: "—", lossAmount: "—", billedUsage: "—" };
      this.meterOptions = [];
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
      this.queryParams.period = undefined;
      this.queryParams.status = undefined;
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
      this.title = "录入抄表";
    },

    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids;
      getMeterReading(id).then(response => {
        this.form = response.data;
        // 加载该项目下的仪表列表
        if (this.form.projectId) {
          listMeterDevice({ projectId: this.form.projectId, pageNum: 1, pageSize: 1000 }).then(response => {
            this.meterOptions = response.rows;
          });
        }
        this.handleReadingChange();
        this.open = true;
        this.title = "修改抄表记录";
      });
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id !== undefined) {
            updateMeterReading(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addMeterReading(this.form).then(response => {
              this.$modal.msgSuccess("录入成功");
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
      this.$modal.confirm('是否确认删除抄表记录为"' + ids + '"的数据项？').then(function() {
        return delMeterReading(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },

    /** 单户入账（工具栏） */
    handleBill() {
      const id = this.ids[0];
      const row = this.readingList.find(item => item.id === id);
      this.handleBillRow(row);
    },

    /** 单户入账（行内） */
    handleBillRow(row) {
      if (row.status !== 'INPUT') {
        this.$modal.msgWarning("仅状态为【已录入】的记录可入账");
        return;
      }
      this.$modal.confirm('确认对【' + row.meterCode + '】' + row.period + '的抄表记录进行入账？').then(() => {
        return billMeterReading(row.id);
      }).then(response => {
        if (response.data && response.data.success) {
          this.$modal.msgSuccess("入账成功");
          this.getList();
        } else {
          this.$modal.msgError(response.data ? response.data.message : "入账失败");
        }
      }).catch(() => {});
    },

    /** 批量入账 */
    handleBatchBill() {
      if (!this.queryParams.projectId) {
        this.$modal.msgWarning("请先选择项目");
        return;
      }
      if (!this.queryParams.period) {
        this.$modal.msgWarning("请先选择抄表期间");
        return;
      }
      const selectedIds = this.ids;
      const hint = selectedIds.length > 0
        ? '确认对选中的 ' + selectedIds.length + ' 条记录进行批量入账？'
        : '确认对当前筛选条件下所有【已录入】的抄表记录进行批量入账？';
      this.$modal.confirm(hint).then(() => {
        this.loading = true;
        return batchBillMeterReading({
          projectId: this.queryParams.projectId,
          period: this.queryParams.period,
          ids: selectedIds.length > 0 ? selectedIds : undefined
        });
      }).then(response => {
        this.loading = false;
        const data = response.data;
        this.$modal.msgSuccess('批量入账完成：成功 ' + data.success + ' 条，跳过 ' + data.skip + ' 条，失败 ' + data.fail + ' 条');
        this.getList();
      }).catch(() => {
        this.loading = false;
      });
    }
  }
};
</script>

<style>
.el-table .warning-row {
  background: #fdf6ec !important;
}
.el-table .warning-row:hover > td {
  background: #fbf0d8 !important;
}
.share-stat-item {
  text-align: center;
  padding: 10px 0;
  border-radius: 4px;
  background: #f5f7fa;
}
.share-stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}
.share-stat-value {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
</style>
