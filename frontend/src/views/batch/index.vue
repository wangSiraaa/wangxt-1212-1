<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">除冰液批次管理</h2>
      <n-tag size="small" type="info" v-if="envTemp !== null">
        环境温度: {{ envTemp }}°C
      </n-tag>
    </div>

    <div class="table-toolbar">
      <div class="table-toolbar-left">
        <n-input
          v-model:value="searchForm.batchNo"
          placeholder="搜索批次号"
          clearable
          style="width: 200px;"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <n-icon><SearchOutlined /></n-icon>
          </template>
        </n-input>
        <n-select
          v-model:value="searchForm.fluidType"
          placeholder="液体类型"
          clearable
          style="width: 140px;"
          :options="fluidTypeOptions"
        />
        <n-select
          v-model:value="searchForm.batchStatus"
          placeholder="批次状态"
          clearable
          style="width: 140px;"
          :options="statusOptions"
        />
        <n-button type="primary" @click="handleSearch">
          <template #icon><n-icon><SearchOutlined /></n-icon></template>
          查询
        </n-button>
        <n-button @click="handleReset">重置</n-button>
      </div>
      <div class="table-toolbar-right">
        <n-button type="primary" @click="handleAdd">
          <template #icon><n-icon><PlusOutlined /></n-icon></template>
          新增批次
        </n-button>
      </div>
    </div>

    <n-data-table
      :columns="columns"
      :data="tableData"
      :loading="loading"
      :pagination="pagination"
      :bordered="false"
      striped
      @update:page="handlePageChange"
    />

    <n-modal
      v-model:show="showModal"
      preset="dialog"
      :title="modalTitle"
      :mask-closable="false"
      positive-text="确定"
      negative-text="取消"
      @positive-click="handleSubmit"
      @negative-click="showModal = false"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="130px"
      >
        <n-form-item label="批次号" path="batchNo">
          <n-input v-model:value="formData.batchNo" placeholder="请输入批次号" />
        </n-form-item>
        <n-form-item label="液体类型" path="fluidType">
          <n-select
            v-model:value="formData.fluidType"
            placeholder="请选择液体类型"
            :options="fluidTypeOptions"
          />
        </n-form-item>
        <n-form-item label="液体浓度" path="concentration">
          <n-input-number
            v-model:value="formData.concentration"
            placeholder="请输入浓度(%)"
            :min="0"
            :max="100"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="总体积(L)" path="totalVolume">
          <n-input-number
            v-model:value="formData.totalVolume"
            placeholder="请输入总体积"
            :min="0"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="最低有效温度" path="minEffectiveTemp">
          <n-input-number
            v-model:value="formData.minEffectiveTemp"
            placeholder="请输入最低有效温度(°C)"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="最高有效温度" path="maxEffectiveTemp">
          <n-input-number
            v-model:value="formData.maxEffectiveTemp"
            placeholder="请输入最高有效温度(°C)"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="批次状态" path="batchStatus">
          <n-select
            v-model:value="formData.batchStatus"
            placeholder="请选择批次状态"
            :options="statusOptions"
          />
        </n-form-item>
        <n-form-item label="供应商" path="supplier">
          <n-input v-model:value="formData.supplier" placeholder="请输入供应商" />
        </n-form-item>
        <n-form-item label="备注" path="remark">
          <n-input v-model:value="formData.remark" type="textarea" placeholder="请输入备注" />
        </n-form-item>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, useDialog, NTag, NIcon } from 'naive-ui'
import { SearchOutlined, PlusOutlined, WarningFilled, CheckCircleFilled } from '@vicons/antd'
import {
  getBatchPage,
  addBatch,
  updateBatch,
  deleteBatch,
  checkBatchAvailability
} from '@/api/batch'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const tableData = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const modalTitle = ref('新增批次')
const formRef = ref(null)
const envTemp = ref(-5)

const searchForm = reactive({
  batchNo: '',
  fluidType: null,
  batchStatus: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page) => { pagination.page = page; fetchData() },
  onUpdatePageSize: (pageSize) => {
    pagination.pageSize = pageSize
    pagination.page = 1
    fetchData()
  }
})

const fluidTypeOptions = [
  { label: 'I型除冰液', value: 'TYPE1' },
  { label: 'II型除冰液', value: 'TYPE2' },
  { label: 'III型除冰液', value: 'TYPE3' },
  { label: 'IV型除冰液', value: 'TYPE4' }
]

const statusOptions = [
  { label: '可用', value: 'AVAILABLE' },
  { label: '使用中', value: 'IN_USE' },
  { label: '已用完', value: 'USED_UP' },
  { label: '已过期', value: 'EXPIRED' }
]

const formData = reactive({
  id: null,
  batchNo: '',
  fluidType: 'TYPE1',
  concentration: 50,
  totalVolume: 5000,
  remainingVolume: 5000,
  minEffectiveTemp: -20,
  maxEffectiveTemp: 10,
  batchStatus: 'AVAILABLE',
  supplier: '',
  remark: ''
})

const formRules = {
  batchNo: { required: true, message: '请输入批次号', trigger: 'blur' },
  totalVolume: { required: true, message: '请输入总体积', trigger: 'blur' }
}

const columns = [
  { title: '批次号', key: 'batchNo', width: 140 },
  {
    title: '液体类型',
    key: 'fluidType',
    width: 110,
    render: (row) => {
      const map = { TYPE1: 'I型', TYPE2: 'II型', TYPE3: 'III型', TYPE4: 'IV型' }
      return map[row.fluidType] || row.fluidType
    }
  },
  { title: '浓度(%)', key: 'concentration', width: 90 },
  { title: '总体积(L)', key: 'totalVolume', width: 100 },
  { title: '剩余量(L)', key: 'remainingVolume', width: 100 },
  {
    title: '温度区间(°C)',
    key: 'tempRange',
    width: 130,
    render: (row) => `${row.minEffectiveTemp} ~ ${row.maxEffectiveTemp}`
  },
  {
    title: '可用性',
    key: 'availability',
    width: 100,
    render: (row) => {
      const valid = row.batchStatus === 'AVAILABLE' || row.batchStatus === 'IN_USE'
      const tempValid = envTemp.value >= row.minEffectiveTemp && envTemp.value <= row.maxEffectiveTemp
      if (valid && tempValid) {
        return h(NTag, { type: 'success', size: 'small' }, () => '可用')
      }
      return h(NTag, { type: 'error', size: 'small' }, () => '不可用')
    }
  },
  {
    title: '状态',
    key: 'batchStatus',
    width: 90,
    render: (row) => {
      const typeMap = { AVAILABLE: 'success', IN_USE: 'info', USED_UP: 'warning', EXPIRED: 'error' }
      const textMap = { AVAILABLE: '可用', IN_USE: '使用中', USED_UP: '已用完', EXPIRED: '已过期' }
      return h(NTag, { type: typeMap[row.batchStatus] || 'default', size: 'small' }, () => textMap[row.batchStatus] || '未知')
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) => {
      return h('n-space', { size: 'small' }, () => [
        h('n-button', { size: 'small', type: 'info', onClick: () => handleCheckAvailability(row) }, () => '检查可用性'),
        h('n-button', { size: 'small', type: 'primary', onClick: () => handleEdit(row) }, () => '编辑'),
        h('n-button', { size: 'small', type: 'error', onClick: () => handleDelete(row) }, () => '删除')
      ])
    }
  }
]

function fetchData() {
  loading.value = true
  getBatchPage({
    pageNum: pagination.page,
    pageSize: pagination.pageSize,
    batchNo: searchForm.batchNo || undefined,
    fluidType: searchForm.fluidType || undefined,
    batchStatus: searchForm.batchStatus || undefined
  }).then(data => {
    tableData.value = data.records
    pagination.itemCount = data.total
  }).catch(err => {
    message.error(err.message || '获取数据失败')
  }).finally(() => {
    loading.value = false
  })
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() {
  searchForm.batchNo = ''
  searchForm.fluidType = null
  searchForm.batchStatus = null
  pagination.page = 1
  fetchData()
}
function handlePageChange(page) { pagination.page = page; fetchData() }

function handleAdd() {
  isEdit.value = false
  modalTitle.value = '新增批次'
  resetForm()
  showModal.value = true
}

function handleEdit(row) {
  isEdit.value = true
  modalTitle.value = '编辑批次'
  Object.assign(formData, row)
  showModal.value = true
}

function handleDelete(row) {
  dialog.warning({
    title: '删除确认',
    content: `确定要删除批次 ${row.batchNo} 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      deleteBatch(row.id).then(() => {
        message.success('删除成功')
        fetchData()
      }).catch(err => message.error(err.message || '删除失败'))
    }
  })
}

function handleCheckAvailability(row) {
  checkBatchAvailability(row.id, envTemp.value).then(data => {
    if (data.available) {
      message.success(`批次 ${row.batchNo} 当前可用`)
    } else {
      message.warning(data.reason || '批次不可用')
    }
  }).catch(err => {
    message.error(err.message || '检查失败')
  })
}

function handleSubmit() {
  formRef.value?.validate((errors) => {
    if (!errors) {
      const submitData = { ...formData }
      if (!isEdit.value) {
        submitData.remainingVolume = submitData.totalVolume
      }
      const promise = isEdit.value ? updateBatch(submitData) : addBatch(submitData)
      promise.then(() => {
        message.success(isEdit.value ? '更新成功' : '新增成功')
        showModal.value = false
        fetchData()
      }).catch(err => message.error(err.message || '操作失败'))
    }
  })
}

function resetForm() {
  Object.assign(formData, {
    id: null, batchNo: '', fluidType: 'TYPE1', concentration: 50,
    totalVolume: 5000, remainingVolume: 5000, minEffectiveTemp: -20,
    maxEffectiveTemp: 10, batchStatus: 'AVAILABLE', supplier: '', remark: ''
  })
}

onMounted(() => { fetchData() })
</script>
