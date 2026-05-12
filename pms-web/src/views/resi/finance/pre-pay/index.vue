<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="项目" prop="projectId">
        <el-select v-model="queryParams.projectId" placeholder="请选择项目" clearable style="width: 180px">
          <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="资源类型" prop="resourceType">
        <el-select v-model="queryParams.resourceType" placeholder="请选择" clearable style="width: 120px">
          <el-option label="房间" value="ROOM" />
        </el-select>
      </el-form-item>
      <el-form-item label="资源ID" prop="resourceId">
        <el-input v-model.number="queryParams.resourceId" placeholder="资源ID" clearable style="width: 120px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['resi:finance:prepay:add']">收取预收款</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column label="序号" type="index" width="50" align="center" />
      <el-table-column label="资源类型" prop="resourceType" align="center" width="90">
        <template slot-scope="scope">{{ scope.row.resourceType === 'ROOM' ? '房间' : scope.row.resourceType }}</template>
      </el-table-column>
      <el-table-column label="资源ID" prop="resourceId" align="center" width="90" />
      <el-table-column label="费用类型" align="center" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.feeId ? 'warning' : 'success'">
            {{ scope.row.feeId ? '专款' : '通用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="余额" prop="balance" align="right" width="120">
        <template slot-scope="scope">{{ scope.row.balance | formatMoney }}</template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" align="center" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleLogs(scope.row)">流水</el-button>
          <el-button size="mini" type="text" icon="el-icon-minus" @click="handleOffset(scope.row)" v-hasPermi="['resi:finance:prepay:offset']">冲抵</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 收取预收款弹窗 -->
    <el-dialog title="收取预收款" :visible.sync="addVisible" width="500px" :close-on-click-modal="false">
      <el-form ref="addForm" :model="addForm" :rules="addRules" label-width="100px" size="small">
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="addForm.projectId" style="width: 100%" />
        </el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="addForm.resourceType" placeholder="请选择" style="width: 100%">
            <el-option label="房间" value="ROOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源ID" prop="resourceId">
          <el-input-number v-model="addForm.resourceId" style="width: 100%" />
        </el-form-item>
        <el-form-item label="存入金额" prop="amount">
          <el-input-number v-model="addForm.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支付方式" prop="payMethod">
          <el-select v-model="addForm.payMethod" placeholder="请选择" style="width: 100%">
            <el-option label="现金" value="CASH" />
            <el-option label="微信支付" value="WECHAT" />
            <el-option label="银行转账" value="TRANSFER" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="addForm.remark" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitAdd">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 流水弹窗 -->
    <el-dialog title="预收款流水" :visible.sync="logsVisible" width="700px">
      <el-table :data="logData" size="small" border>
        <el-table-column label="操作类型" prop="opType" align="center" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.opType === 'IN' ? 'success' : (scope.row.opType === 'OUT' ? 'warning' : 'danger')">
              {{ { IN: '存入', OUT: '冲抵', REFUND: '退还' }[scope.row.opType] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" prop="amount" align="right" width="100">
          <template slot-scope="scope">{{ scope.row.amount | formatMoney }}</template>
        </el-table-column>
        <el-table-column label="余额" prop="balanceAfter" align="right" width="100">
          <template slot-scope="scope">{{ scope.row.balanceAfter | formatMoney }}</template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="120" />
        <el-table-column label="时间" prop="creatorTime" align="center" width="160">
          <template slot-scope="scope">{{ parseTime(scope.row.creatorTime) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 冲抵弹窗 -->
    <el-dialog title="冲抵预收款" :visible.sync="offsetVisible" width="500px" :close-on-click-modal="false">
      <el-form ref="offsetForm" :model="offsetForm" label-width="100px" size="small">
        <el-form-item label="当前余额">
          <span>{{ currentAccount.balance | formatMoney }}</span>
        </el-form-item>
        <el-form-item label="冲抵金额" prop="offsetAmount">
          <el-input-number v-model="offsetForm.offsetAmount" :min="0.01" :max="currentAccount.balance" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="offsetForm.remark" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="offsetVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitOffset">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { formatMoney } from '@/utils/resi'
import { listPreAccount, listPrePay, addPrePay, batchOffsetPrePay } from '@/api/resi/finance'
import { listProject } from '@/api/resi/archive'

export default {
  name: 'ResiPrePay',
  filters: { formatMoney },
  data() {
    return {
      loading: false,
      submitLoading: false,
      showSearch: true,
      tableData: [],
      projectList: [],
      queryParams: {
        projectId: null,
        resourceType: 'ROOM',
        resourceId: null
      },
      addVisible: false,
      addForm: {
        projectId: null,
        resourceType: 'ROOM',
        resourceId: null,
        amount: 100,
        payMethod: 'CASH',
        remark: ''
      },
      addRules: {
        projectId: [{ required: true, message: '项目ID不能为空', trigger: 'blur' }],
        resourceType: [{ required: true, message: '资源类型不能为空', trigger: 'change' }],
        resourceId: [{ required: true, message: '资源ID不能为空', trigger: 'blur' }],
        amount: [{ required: true, message: '金额不能为空', trigger: 'blur' }],
        payMethod: [{ required: true, message: '支付方式不能为空', trigger: 'change' }]
      },
      logsVisible: false,
      logData: [],
      offsetVisible: false,
      currentAccount: { balance: 0 },
      offsetForm: {
        accountId: null,
        offsetAmount: 0,
        remark: ''
      }
    }
  },
  created() {
    this.loadProjects()
  },
  methods: {
    formatMoney,
    parseTime(time) {
      if (!time) return '-'
      const d = new Date(time)
      return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}:${String(d.getSeconds()).padStart(2,'0')}`
    },
    loadProjects() {
      listProject().then(res => {
        this.projectList = res.rows || []
      }).catch(() => {})
    },
    getList() {
      this.loading = true
      listPreAccount(this.queryParams).then(response => {
        this.tableData = response.data || []
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.getList()
    },
    resetQuery() {
      this.queryParams = {
        projectId: null,
        resourceType: 'ROOM',
        resourceId: null
      }
      this.getList()
    },
    handleAdd() {
      this.addForm = {
        projectId: this.queryParams.projectId,
        resourceType: this.queryParams.resourceType || 'ROOM',
        resourceId: this.queryParams.resourceId,
        amount: 100,
        payMethod: 'CASH',
        remark: ''
      }
      this.addVisible = true
      this.$nextTick(() => {
        this.$refs.addForm && this.$refs.addForm.clearValidate()
      })
    },
    submitAdd() {
      this.$refs.addForm.validate(valid => {
        if (!valid) return
        this.submitLoading = true
        addPrePay(this.addForm).then(() => {
          this.$message.success('收取成功')
          this.addVisible = false
          this.getList()
        }).catch(err => {
          this.$message.error(err.response?.data?.msg || '收取失败')
        }).finally(() => {
          this.submitLoading = false
        })
      })
    },
    handleLogs(row) {
      this.logData = []
      listPrePay({ accountId: row.id }).then(res => {
        this.logData = res.rows || []
        this.logsVisible = true
      }).catch(() => {
        this.$message.warning('流水加载失败')
      })
    },
    handleOffset(row) {
      this.currentAccount = row
      this.offsetForm = {
        accountId: row.id,
        offsetAmount: Number(row.balance || 0),
        remark: ''
      }
      this.offsetVisible = true
    },
    submitOffset() {
      if (!this.offsetForm.offsetAmount || this.offsetForm.offsetAmount <= 0) {
        this.$message.warning('冲抵金额必须大于0')
        return
      }
      this.submitLoading = true
      batchOffsetPrePay({
        items: [{
          accountId: this.offsetForm.accountId,
          offsetAmount: this.offsetForm.offsetAmount,
          remark: this.offsetForm.remark
        }]
      }).then(() => {
        this.$message.success('冲抵成功')
        this.offsetVisible = false
        this.getList()
      }).catch(err => {
        this.$message.error(err.response?.data?.msg || '冲抵失败')
      }).finally(() => {
        this.submitLoading = false
      })
    }
  }
}
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
</style>
