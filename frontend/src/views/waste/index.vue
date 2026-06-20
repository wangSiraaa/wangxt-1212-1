<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">废液回收管理</h2>
    </div>

    <div class="table-toolbar">
      <div class="table-toolbar-left">
        <n-input
          v-model:value="searchForm.recoveryNo"
          placeholder="搜索回收单号"
          clearable
          style="width: 200px;"
          @keyup.enter="handleSearch"
        >
          <template #prefix><n-icon><SearchOutlined /></n-icon></template>
        </n-input>
        <n-select
          v-model:value="searchForm.recoveryStatus"
          placeholder="回收状态"
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
          新增回收
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
      style="width: 600px;"
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
        <n-form-item label="回收池编号" path="poolNo">
          <n-input v-model:value="formData.poolNo" placeholder="请输入回收池编号" />
        </n-form-item>
        <n-form-item label="回收量(L)" path="recoveryVolume">
          <n-input-number
            v-model:value="formData.recoveryVolume"
            placeholder="请输入回收量"
            :min="0"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="预估浓度(%)" path="estimatedConcentration">
          <n-input-number
            v-model:value="formData.estimatedConcentration"
            placeholder="请输入预估浓度"
            :min="0"
            :max="100"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="回收来源" path="sourceType">
          <n-select
            v-model:value="formData.sourceType"
            placeholder="请选择回收来源"
            :options="sourceOptions"
          />
        </n-form-item>
        <n-form-item label="关联航班" path="flightId">
          <n-select
            v-model:value="formData.flightId"
            placeholder="请选择关联航班(可选)"
            :options="flightOptions"
            filterable
            clearable
          />
        </n-form-item>
        <n-form-item label="回收人员" path="operatorName">
          <n-input v-model:value="formData.operatorName" placeholder="请输入回收人员姓名" />
        </n-form-item>
        <n-form-item label="回收时间" path="recoveryTime">
          <n-date-picker
            v-model:value="formData.recoveryTime"
            type="datetime"
            placeholder="请选择回收时间"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="回收状态" path="recoveryStatus">
          <n-select
            v-model:value="formData.recoveryStatus"
            placeholder="请选择回收状态"
            :options="statusOptions"
          />
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
import { useMessage, useDialog, NTag } from 'naive-ui'
import { SearchOutlined, PlusOutlined } from '@vicons/antd'
import {
  getWastePage,
  addWaste,
  updateWaste,
  deleteWaste
} from '@/api/waste'
import { getFlightList } from '@/api/flight'
import dayjs from 'dayjs'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const tableData = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const modalTitle = ref('新增回收')
const formRef = ref(null)

const searchForm = reactive({
  recoveryNo: '',
  recoveryStatus: null,
  poolNo: ''
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

const statusOptions = [
  { label: '待检测', value: 'PENDING' },
  { label: '检测中', value: 'TESTING' },
  { label: '检测完成', value: 'COMPLETED' },
  { label: '已处理', value: 'DISPOSED' }
]

const sourceOptions = [
  { label: '除冰喷洒', value: 'SPRAY' },
  { label: '车辆清洗', value: 'CLEAN' },
  { label: '其他', value: 'OTHER' }
]

const flightOptions = ref([])

const formData = reactive({
  id: null,
  poolNo: '',
  recoveryVolume: 0,
  estimatedConcentration: 30,
  sourceType: 'SPRAY',
  flightId: null,
  operatorName: '',
  recoveryTime: null,
  recoveryStatus: 'PENDING',
  remark: ''
})

const formRules = {
  poolNo: { required: true, message: '请输入回收池编号', trigger: 'blur' },
  recoveryVolume: { required: true, message: '请输入回收量', trigger: 'blur' }
}

const columns = [
  { title: '回收单号', key: 'recoveryNo', width: 140 },
  { title: '回收池编号', key: 'poolNo', width: 120 },
  { title: '回收量(L)', key: 'recoveryVolume', width: 110 },
  { title: '预估浓度(%)', key: 'estimatedConcentration', width: 120 },
  {
    title: '回收来源',
    key: 'sourceType',
    width: 100,
    render: (row) => {
      const map = { SPRAY: '除冰喷洒', CLEAN: '车辆清洗', OTHER: '其他' }
      return map[row.sourceType] || row.sourceType
    }
  },
  { title: '航班号', key: 'flightNo', width: 100 },
  { title: '回收人员', key: 'operatorName', width: 100 },
  {
    title: '回收状态',
    key: 'recoveryStatus',
    width: 100,
    render: (row) => {
      const typeMap = { PENDING: 'warning', TESTING: 'info', COMPLETED: 'success', DISPOSED: 'default' }
      const textMap = { PENDING: '待检测', TESTING: '检测中', COMPLETED: '检测完成', DISPOSED: '已处理' }
      return h(NTag, { type: typeMap[row.recoveryStatus] || 'default', size: 'small' }, () => textMap[row.recoveryStatus] || '未知')
    }
  },
  {
    title: '回收时间',
    key: 'recoveryTime',
    width: 160,
    render: (row) => row.recoveryTime ? dayjs(row.recoveryTime).format('YYYY-MM-DD HH:mm') : '--'
  },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render: (row) => {
      return h('n-space', { size: 'small' }, () => [
        h('n-button', { size: 'small', type: 'primary', onClick: () => handleEdit(row) }, () => '编辑'),
        h('n-button', { size: 'small', type: 'error', onClick: () => handleDelete(row) }, () => '删除')
      ])
    }
  }
]

function fetchData() {
  loading.value = true
  getWastePage({
    pageNum: pagination.page,
    pageSize: pagination.pageSize,
    recoveryNo: searchForm.recoveryNo || undefined,
    recoveryStatus: searchForm.recoveryStatus || undefined,
    poolNo: searchForm.poolNo || undefined
  }).then(data => {
    tableData.value = data.records
    pagination.itemCount = data.total
  }).catch(err => {
    message.error(err.message || '获取数据失败')
  }).finally(() => {
    loading.value = false
  })
}

function loadFlightOptions() {
  getFlightList().then(data => {
    flightOptions.value = data.map(f => ({ label: f.flightNo, value: f.id }))
  }).catch(() => {})
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() {
  searchForm.recoveryNo = ''
  searchForm.recoveryStatus = null
  searchForm.poolNo = ''
  pagination.page = 1
  fetchData()
}
function handlePageChange(page) { pagination.page = page; fetchData() }

function handleAdd() {
  isEdit.value = false
  modalTitle.value = '新增回收'
  resetForm()
  loadFlightOptions()
  showModal.value = true
}

function handleEdit(row) {
  isEdit.value = true
  modalTitle.value = '编辑回收'
  loadFlightOptions()
  Object.assign(formData, row)
  if (row.recoveryTime) formData.recoveryTime = dayjs(row.recoveryTime).valueOf()
  showModal.value = true
}

function handleDelete(row) {
  dialog.warning({
    title: '删除确认',
    content: `确定要删除回收记录 ${row.recoveryNo} 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      deleteWaste(row.id).then(() => {
        message.success('删除成功')
        fetchData()
      }).catch(err => message.error(err.message || '删除失败'))
    }
  })
}

function handleSubmit() {
  formRef.value?.validate((errors) => {
    if (!errors) {
      const submitData = { ...formData }
      if (submitData.recoveryTime) submitData.recoveryTime = dayjs(submitData.recoveryTime).toISOString()

      const promise = isEdit.value ? updateWaste(submitData) : addWaste(submitData)
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
    id: null,
    poolNo: '',
    recoveryVolume: 0,
    estimatedConcentration: 30,
    sourceType: 'SPRAY',
    flightId: null,
    operatorName: '',
    recoveryTime: null,
    recoveryStatus: 'PENDING',
    remark: ''
  })
}

onMounted(() => { fetchData() })
</script>
