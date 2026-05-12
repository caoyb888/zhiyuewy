<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="所属项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable size="small" style="width: 180px">
          <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="账单月份" prop="billPeriod">
        <el-date-picker v-model="queryParams.billPeriod" type="month" placeholder="选择月份" value-format="yyyy-MM" size="small" style="width: 150px" />
      </el-form-item>
      <el-form-item label="费用类型" prop="feeType">
        <el-select v-model="queryParams.feeType" placeholder="费用类型" clearable size="small" style="width: 120px">
          <el-option label="周期费" value="PERIOD" />
          <el-option label="临时费" value="TEMP" />
          <el-option label="押金" value="DEPOSIT" />
          <el-option label="预收款" value="PRE" />
        </el-select>
      </el-form-item>
      <el-form-item label="缴费状态" prop="payState">
        <el-select v-model="queryParams.payState" placeholder="缴费状态" clearable size="small" style="width: 120px">
          <el-option label="未收" value="0" />
          <el-option label="部分收" value="1" />
          <el-option label="已收" value="2" />
          <el-option label="减免" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="资源名称" prop="resourceName">
        <el-input v-model="queryParams.resourceName" placeholder="请输入资源名称" clearable size="small" style="width: 150px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-s-operation" size="mini" @click="handleGenerateOpen" v-hasPermi="['resi:receivable:generate']">批量生成</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleTempOpen" v-hasPermi="['resi:receivable:add']">临时费录入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['resi:receivable:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="receivableList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="资源名称" align="center" prop="resourceName" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="客户姓名" align="center" prop="customerName" width="100" />
      <el-table-column label="费用名称" align="center" prop="feeName" width="120" />
      <el-table-column label="账单月份" align="center" prop="billPeriod" width="100" />
      <el-table-column label="计费周期" align="center" width="180">
        <template slot-scope="scope">
          <span v-if="scope.row.beginDate && scope.row.endDate">
            {{ parseTime(scope.row.beginDate, '{y}-{m}-{d}') }} ~ {{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}
          </span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="数量" align="right" prop="num" width="100" />
      <el-table-column label="单价" align="right" prop="price" width="100" />
      <el-table-column label="金额" align="right" prop="total" width="120">
        <template slot-scope="scope">
          <span>{{ scope.row.total | formatMoney }}</span>
        </template>
      </el-table-column>
      <el-table-column label="滞纳金" align="right" prop="overdueFee" width="100">
        <template slot-scope="scope">
          <span>{{ scope.row.overdueFee | formatMoney }}</span>
        </template>
      </el-table-column>
      <el-table-column label="应收合计" align="right" prop="receivable" width="120">
        <template slot-scope="scope">
          <span style="color: #f56c6c; font-weight: bold">{{ scope.row.receivable | formatMoney }}</span>
        </template>
      </el-table-column>
      <el-table-column label="缴费状态" align="center" prop="payState" width="90">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.payState === '0'" type="danger">未收</el-tag>
          <el-tag v-else-if="scope.row.payState === '1'" type="warning">部分收</el-tag>
          <el-tag v-else-if="scope.row.payState === '2'" type="success">已收</el-tag>
          <el-tag v-else-if="scope.row.payState === '3'" type="info">减免</el-tag>
          <el-tag v-else>{{ scope.row.payState }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="批次号" align="center" prop="genBatch" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="creatorTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.creatorTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.payState === '0'" size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['resi:receivable:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 批量生成向导 -->
    <el-dialog title="批量生成应收" :visible.sync="generateOpen" width="550px" append-to-body :close-on-click-modal="false">
      <el-steps :active="generateStep" finish-status="success" simple style="margin-bottom: 20px">
        <el-step title="选择月份" />
        <el-step title="执行结果" />
      </el-steps>

      <!-- 步骤1：选择参数 -->
      <div v-if="generateStep === 0">
        <el-form ref="generateForm" :model="generateParams" :rules="generateRules" label-width="100px">
          <el-form-item label="所属项目" prop="projectId">
            <el-select v-model="generateParams.projectId" placeholder="请选择项目" style="width: 100%">
              <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="账单月份" prop="billPeriod">
            <el-date-picker v-model="generateParams.billPeriod" type="month" placeholder="选择账单月份" value-format="yyyy-MM" style="width: 100%" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤2：执行结果 -->
      <div v-if="generateStep === 1" style="text-align: center; padding: 20px 0">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="preview-stat">
              <div class="preview-number">{{ generateResult.total }}</div>
              <div class="preview-label">目标总数</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="preview-stat">
              <div class="preview-number" style="color: #67C23A">{{ generateResult.success }}</div>
              <div class="preview-label">成功生成</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="preview-stat">
              <div class="preview-number" style="color: #E6A23C">{{ generateResult.skip }}</div>
              <div class="preview-label">跳过（已存在）</div>
            </div>
          </el-col>
        </el-row>
        <el-alert v-if="generateResult.skip > 0" title="该月份已存在生成记录，本次未产生新数据" type="warning" :closable="false" show-icon style="margin-top: 20px" />
        <el-alert v-else-if="generateResult.success > 0" title="批量生成应收成功" type="success" :closable="false" show-icon style="margin-top: 20px" />
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button v-if="generateStep === 0" type="primary" :loading="generateLoading" @click="submitGenerate">执 行</el-button>
        <el-button v-if="generateStep === 1" type="primary" @click="generateOpen = false; generateStep = 0; getList()">完 成</el-button>
        <el-button @click="generateOpen = false; generateStep = 0">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 临时费录入 -->
    <el-dialog title="临时费录入" :visible.sync="tempOpen" width="600px" append-to-body :close-on-click-modal="false">
      <el-form ref="tempForm" :model="tempParams" :rules="tempRules" label-width="100px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="tempParams.projectId" placeholder="请选择项目" style="width: 100%" @change="onTempProjectChange">
            <el-option v-for="item in projectOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="tempParams.resourceType" placeholder="请选择资源类型" style="width: 100%" @change="onTempResourceTypeChange">
            <el-option label="房间" value="ROOM" />
            <el-option label="车位" value="PARKING" />
            <el-option label="储藏室" value="STORAGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源" prop="resourceId">
          <el-select v-model="tempParams.resourceId" filterable placeholder="请选择资源" style="width: 100%" :disabled="!tempParams.projectId || tempResourceOptions.length === 0">
            <el-option v-for="item in tempResourceOptions" :key="item.id" :label="item.roomAlias || item.roomNo || item.spaceCode || item.name || item.id" :value="item.id" />
          </el-select>
          <span v-if="tempParams.projectId && tempResourceOptions.length === 0 && tempParams.resourceType !== 'ROOM'" style="color: #909399; font-size: 12px">该资源类型暂无数据</span>
        </el-form-item>
        <el-form-item label="费用定义" prop="feeId">
          <el-select v-model="tempParams.feeId" filterable placeholder="请选择费用" style="width: 100%" @change="onTempFeeChange" :disabled="!tempParams.projectId">
            <el-option v-for="item in tempFeeOptions" :key="item.id" :label="item.feeName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量" prop="num">
          <el-input-number v-model="tempParams.num" :precision="4" :min="0" :controls="false" placeholder="请输入数量" style="width: 100%" />
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number v-model="tempParams.price" :precision="4" :min="0" :controls="false" placeholder="请输入单价" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="tempParams.remark" type="textarea" :rows="2" placeholder="请输入备注" maxlength="500" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="tempLoading" @click="submitTemp">确 定</el-button>
        <el-button @click="tempOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="应收详情" :visible.sync="detailOpen" width="600px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="资源名称">{{ detailData.resourceName }}</el-descriptions-item>
        <el-descriptions-item label="客户姓名">{{ detailData.customerName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="费用名称">{{ detailData.feeName }}</el-descriptions-item>
        <el-descriptions-item label="账单月份">{{ detailData.billPeriod || '—' }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ detailData.num }}</el-descriptions-item>
        <el-descriptions-item label="单价">{{ detailData.price | formatMoney }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ detailData.total | formatMoney }}</el-descriptions-item>
        <el-descriptions-item label="滞纳金">{{ detailData.overdueFee | formatMoney }}</el-descriptions-item>
        <el-descriptions-item label="折扣减免">{{ detailData.discountAmount | formatMoney }}</el-descriptions-item>
        <el-descriptions-item label="应收合计">{{ detailData.receivable | formatMoney }}</el-descriptions-item>
        <el-descriptions-item label="已收金额">{{ detailData.paidAmount | formatMoney }}</el-descriptions-item>
        <el-descriptions-item label="缴费状态">
          <el-tag v-if="detailData.payState === '0'" type="danger">未收</el-tag>
          <el-tag v-else-if="detailData.payState === '1'" type="warning">部分收</el-tag>
          <el-tag v-else-if="detailData.payState === '2'" type="success">已收</el-tag>
          <el-tag v-else-if="detailData.payState === '3'" type="info">减免</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="批次号" :span="2">{{ detailData.genBatch || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { listReceivable, delReceivable, generateReceivable, createTempReceivable } from "@/api/resi/receivable";
import { formatMoney } from "@/utils/resi";
import { listProject } from "@/api/resi/archive";
import { listRoom } from "@/api/resi/archive";
import { listFeeDefinition } from "@/api/resi/feeconfig";
import { listParkingSpace } from "@/api/resi/archive";

export default {
  name: "ResiReceivable",
  filters: {
    formatMoney
  },
  data() {
    return {
      loading: false,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      receivableList: [],
      projectOptions: [],
      roomOptions: [],
      feeOptions: [],
      // 批量生成
      generateOpen: false,
      generateStep: 0,
      generateLoading: false,
      generateParams: {
        projectId: undefined,
        billPeriod: undefined
      },
      generateRules: {
        projectId: [{ required: true, message: "请选择项目", trigger: "change" }],
        billPeriod: [{ required: true, message: "请选择账单月份", trigger: "change" }]
      },
      generateResult: {
        total: 0,
        success: 0,
        skip: 0
      },
      // 临时费
      tempOpen: false,
      tempLoading: false,
      tempParams: {
        projectId: undefined,
        resourceType: "ROOM",
        resourceId: undefined,
        feeId: undefined,
        num: 1,
        price: undefined,
        remark: undefined
      },
      tempRules: {
        projectId: [{ required: true, message: "请选择项目", trigger: "change" }],
        resourceType: [{ required: true, message: "请选择资源类型", trigger: "change" }],
        resourceId: [{ required: true, message: "请选择资源", trigger: "change" }],
        feeId: [{ required: true, message: "请选择费用", trigger: "change" }],
        num: [{ required: true, message: "数量不能为空", trigger: "blur" }],
        price: [{ required: true, message: "单价不能为空", trigger: "blur" }]
      },
      tempResourceOptions: [],
      tempFeeOptions: [],
      // 详情
      detailOpen: false,
      detailData: {},
      // 查询
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: undefined,
        billPeriod: undefined,
        feeType: undefined,
        payState: undefined,
        resourceName: undefined
      }
    };
  },
  created() {
    this.getList();
    this.getProjectOptions();
    this.getRoomOptions();
    this.getFeeOptions();
  },
  methods: {
    getList() {
      this.loading = true;
      listReceivable(this.queryParams).then(response => {
        this.receivableList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getProjectOptions() {
      listProject({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.projectOptions = response.rows;
      });
    },
    getRoomOptions() {
      listRoom({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.roomOptions = response.rows;
      });
    },
    getFeeOptions() {
      listFeeDefinition({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.feeOptions = response.rows;
      });
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
    // 批量生成
    handleGenerateOpen() {
      this.generateStep = 0;
      this.generateParams = { projectId: undefined, billPeriod: undefined };
      this.generateResult = { total: 0, success: 0, skip: 0 };
      this.generateOpen = true;
    },
    submitGenerate() {
      this.$refs["generateForm"].validate(valid => {
        if (valid) {
          this.generateLoading = true;
          generateReceivable(this.generateParams).then(response => {
            this.generateResult = response.data;
            this.generateStep = 1;
            this.generateLoading = false;
          }).catch(() => {
            this.generateLoading = false;
          });
        }
      });
    },
    // 临时费
    handleTempOpen() {
      this.tempParams = {
        projectId: undefined,
        resourceType: "ROOM",
        resourceId: undefined,
        feeId: undefined,
        num: 1,
        price: undefined,
        remark: undefined
      };
      this.tempResourceOptions = [];
      this.tempFeeOptions = [];
      this.tempOpen = true;
    },
    onTempProjectChange(projectId) {
      this.tempParams.resourceId = undefined;
      this.tempParams.feeId = undefined;
      this.tempParams.price = undefined;
      this.tempResourceOptions = [];
      this.tempFeeOptions = [];
      if (!projectId) {
        return;
      }
      // 加载该项目的资源
      this.onTempResourceTypeChange(this.tempParams.resourceType);
      // 加载该项目的费用定义
      listFeeDefinition({ projectId: projectId, pageNum: 1, pageSize: 1000 }).then(response => {
        this.tempFeeOptions = response.rows || [];
      });
    },
    onTempResourceTypeChange(resourceType) {
      this.tempParams.resourceId = undefined;
      this.tempResourceOptions = [];
      if (!this.tempParams.projectId) {
        return;
      }
      if (resourceType === "ROOM") {
        listRoom({ projectId: this.tempParams.projectId, pageNum: 1, pageSize: 1000 }).then(response => {
          this.tempResourceOptions = response.rows || [];
        });
      } else if (resourceType === "PARKING") {
        listParkingSpace({ projectId: this.tempParams.projectId, pageNum: 1, pageSize: 1000 }).then(response => {
          this.tempResourceOptions = response.rows || [];
        }).catch(() => {
          this.tempResourceOptions = [];
        });
      } else if (resourceType === "STORAGE") {
        // 储藏室按房间类型筛选（room_type=4）
        listRoom({ projectId: this.tempParams.projectId, roomType: 4, pageNum: 1, pageSize: 1000 }).then(response => {
          this.tempResourceOptions = response.rows || [];
        });
      }
    },
    onTempFeeChange(feeId) {
      const fee = this.tempFeeOptions.find(item => item.id === feeId);
      if (fee && fee.unitPrice != null) {
        this.tempParams.price = fee.unitPrice;
      }
    },
    submitTemp() {
      this.$refs["tempForm"].validate(valid => {
        if (valid) {
          this.tempLoading = true;
          createTempReceivable(this.tempParams).then(() => {
            this.$modal.msgSuccess("录入成功");
            this.tempOpen = false;
            this.getList();
          }).finally(() => {
            this.tempLoading = false;
          });
        }
      });
    },
    // 删除
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除选中的应收记录？').then(() => {
        return delReceivable(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    // 详情
    handleDetail(row) {
      this.detailData = row;
      this.detailOpen = true;
    }
  }
};
</script>

<style scoped>
.preview-stat {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px 0;
}
.preview-number {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}
.preview-label {
  font-size: 14px;
  color: #606266;
}
</style>
