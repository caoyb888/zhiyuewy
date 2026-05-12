<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable style="width: 180px">
          <el-option
            v-for="item in projectList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="收据号" prop="payNo">
        <el-input v-model="queryParams.payNo" placeholder="请输入收据号" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="操作类型" prop="payType">
        <el-select v-model="queryParams.payType" placeholder="全部" clearable style="width: 120px">
          <el-option label="收款" value="COLLECT" />
          <el-option label="退款" value="REFUND" />
          <el-option label="冲红" value="WRITEOFF" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付方式" prop="payMethod">
        <el-select v-model="queryParams.payMethod" placeholder="全部" clearable style="width: 120px">
          <el-option label="现金" value="CASH" />
          <el-option label="微信支付" value="WECHAT" />
          <el-option label="银行转账" value="TRANSFER" />
          <el-option label="其他" value="OTHER" />
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
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="tableData" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="收据号" prop="payNo" min-width="160" align="center" />
      <el-table-column label="房间/资源" prop="resourceName" min-width="120" align="center" />
      <el-table-column label="客户姓名" prop="customerName" min-width="100" align="center" />
      <el-table-column label="操作类型" prop="payType" align="center" width="90">
        <template slot-scope="scope">
          <el-tag :type="payTypeTagType(scope.row.payType)">
            {{ payTypeLabel(scope.row.payType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" prop="payMethod" align="center" width="90">
        <template slot-scope="scope">
          {{ payMethodLabel(scope.row.payMethod) }}
        </template>
      </el-table-column>
      <el-table-column label="应收合计" prop="totalAmount" align="right" width="100">
        <template slot-scope="scope">{{ scope.row.totalAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="实收金额" prop="payAmount" align="right" width="100">
        <template slot-scope="scope">{{ scope.row.payAmount | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="复核状态" prop="isVerified" align="center" width="90">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isVerified === 1 ? 'success' : 'info'">
            {{ scope.row.isVerified === 1 ? '已复核' : '未复核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="收款时间" prop="creatorTime" align="center" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.creatorTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.payType === 'COLLECT' && scope.row.isVerified !== 1"
            size="mini"
            type="text"
            icon="el-icon-refresh-left"
            @click="handleRefund(scope.row)"
            v-hasPermi="['resi:cashier:refund']"
          >退款</el-button>
          <el-button
            v-if="scope.row.payType === 'COLLECT' && scope.row.isVerified !== 1"
            size="mini"
            type="text"
            icon="el-icon-document-delete"
            @click="handleWriteOff(scope.row)"
            v-hasPermi="['resi:cashier:writeoff']"
          >冲红</el-button>
          <el-button
            v-if="scope.row.isVerified !== 1"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleVerify(scope.row)"
            v-hasPermi="['resi:finance:paylog:verify']"
          >复核</el-button>
          <span v-if="scope.row.isVerified === 1" style="color: #999; font-size: 12px;">已复核</span>
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

    <!-- 退款弹窗 -->
    <el-dialog title="退款操作" :visible.sync="refundVisible" width="500px" :close-on-click-modal="false">
      <el-form ref="refundForm" :model="refundForm" :rules="refundRules" label-width="100px" size="small">
        <el-form-item label="原收据号">
          <span>{{ refundForm.payNo }}</span>
        </el-form-item>
        <el-form-item label="原实收金额">
          <span>{{ refundForm.originalAmount | formatMoney }}</span>
        </el-form-item>
        <el-form-item label="退款金额" prop="refundAmount">
          <el-input-number v-model="refundForm.refundAmount" :min="0.01" :max="refundForm.originalAmount" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="退款方式" prop="refundMethod">
          <el-select v-model="refundForm.refundMethod" placeholder="请选择" style="width: 100%">
            <el-option label="现金" value="CASH" />
            <el-option label="微信支付" value="WECHAT" />
            <el-option label="银行转账" value="TRANSFER" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="note">
          <el-input v-model="refundForm.note" type="textarea" rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="refundVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitRefund">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 冲红弹窗 -->
    <el-dialog title="冲红操作" :visible.sync="writeOffVisible" width="500px" :close-on-click-modal="false">
      <el-alert
        title="冲红后将撤销该笔收款，关联的应收费用将恢复为未收状态，且不可恢复。"
        type="warning"
        :closable="false"
        style="margin-bottom: 16px"
      />
      <el-form ref="writeOffForm" :model="writeOffForm" label-width="100px" size="small">
        <el-form-item label="原收据号">
          <span>{{ writeOffForm.payNo }}</span>
        </el-form-item>
        <el-form-item label="原实收金额">
          <span>{{ writeOffForm.originalAmount | formatMoney }}</span>
        </el-form-item>
        <el-form-item label="备注" prop="note">
          <el-input v-model="writeOffForm.note" type="textarea" rows="2" placeholder="请输入冲红原因" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="writeOffVisible = false">取 消</el-button>
        <el-button type="danger" :loading="submitLoading" @click="submitWriteOff">确 定冲红</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { formatMoney, payMethodLabel } from '@/utils/resi'
import { listPayLog, verifyPayLog } from '@/api/resi/finance'
import { refundPayment, writeOffPayment } from '@/api/resi/cashier'
import { listProject } from '@/api/resi/archive'

export default {
  name: 'ResiPayLog',
  filters: {
    formatMoney
  },
  data() {
    return {
      loading: false,
      submitLoading: false,
      showSearch: true,
      tableData: [],
      total: 0,
      projectList: [],
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        projectId: null,
        payNo: null,
        payType: null,
        payMethod: null,
        beginTime: null,
        endTime: null
      },
      refundVisible: false,
      refundForm: {
        payLogId: null,
        payNo: '',
        originalAmount: 0,
        refundAmount: 0,
        refundMethod: 'CASH',
        note: ''
      },
      refundRules: {
        refundAmount: [
          { required: true, message: '退款金额不能为空', trigger: 'blur' }
        ],
        refundMethod: [
          { required: true, message: '退款方式不能为空', trigger: 'change' }
        ]
      },
      writeOffVisible: false,
      writeOffForm: {
        payLogId: null,
        payNo: '',
        originalAmount: 0,
        note: ''
      }
    }
  },
  created() {
    this.loadProjects()
    this.getList()
  },
  methods: {
    formatMoney,
    payMethodLabel,
    parseTime(time) {
      if (!time) return '-'
      const d = new Date(time)
      const yyyy = d.getFullYear()
      const mm = String(d.getMonth() + 1).padStart(2, '0')
      const dd = String(d.getDate()).padStart(2, '0')
      const hh = String(d.getHours()).padStart(2, '0')
      const mi = String(d.getMinutes()).padStart(2, '0')
      const ss = String(d.getSeconds()).padStart(2, '0')
      return `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}`
    },
    payTypeLabel(payType) {
      const map = { COLLECT: '收款', REFUND: '退款', WRITEOFF: '冲红' }
      return map[payType] || payType
    },
    payTypeTagType(payType) {
      const map = { COLLECT: 'success', REFUND: 'warning', WRITEOFF: 'danger' }
      return map[payType] || ''
    },
    loadProjects() {
      listProject().then(res => {
        this.projectList = res.rows || []
      }).catch(() => {})
    },
    getList() {
      this.loading = true
      const query = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        query.beginTime = this.dateRange[0]
        query.endTime = this.dateRange[1]
      } else {
        query.beginTime = null
        query.endTime = null
      }
      listPayLog(query).then(response => {
        this.tableData = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        projectId: null,
        payNo: null,
        payType: null,
        payMethod: null,
        beginTime: null,
        endTime: null
      }
      this.getList()
    },
    handleSelectionChange(selection) {
      // 暂不需要批量操作
    },
    handleRefund(row) {
      this.refundForm = {
        payLogId: row.id,
        payNo: row.payNo,
        originalAmount: Number(row.payAmount || 0),
        refundAmount: Number(row.payAmount || 0),
        refundMethod: row.payMethod || 'CASH',
        note: ''
      }
      this.refundVisible = true
      this.$nextTick(() => {
        this.$refs.refundForm && this.$refs.refundForm.clearValidate()
      })
    },
    submitRefund() {
      this.$refs.refundForm.validate(valid => {
        if (!valid) return
        this.submitLoading = true
        refundPayment({
          payLogId: this.refundForm.payLogId,
          refundAmount: this.refundForm.refundAmount,
          refundMethod: this.refundForm.refundMethod,
          note: this.refundForm.note
        }).then(() => {
          this.$message.success('退款成功')
          this.refundVisible = false
          this.getList()
        }).catch(err => {
          this.$message.error(err.response?.data?.msg || '退款失败')
        }).finally(() => {
          this.submitLoading = false
        })
      })
    },
    handleWriteOff(row) {
      this.writeOffForm = {
        payLogId: row.id,
        payNo: row.payNo,
        originalAmount: Number(row.payAmount || 0),
        note: ''
      }
      this.writeOffVisible = true
    },
    submitWriteOff() {
      this.submitLoading = true
      writeOffPayment({
        payLogId: this.writeOffForm.payLogId,
        note: this.writeOffForm.note
      }).then(() => {
        this.$message.success('冲红成功')
        this.writeOffVisible = false
        this.getList()
      }).catch(err => {
        this.$message.error(err.response?.data?.msg || '冲红失败')
      }).finally(() => {
        this.submitLoading = false
      })
    },
    handleVerify(row) {
      this.$confirm('确认复核该收款单？复核后不可进行退款/冲红操作。', '提示', {
        confirmButtonText: '确认复核',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        verifyPayLog(row.id).then(() => {
          this.$message.success('复核成功')
          this.getList()
        }).catch(err => {
          this.$message.error(err.response?.data?.msg || '复核失败')
        })
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
</style>
