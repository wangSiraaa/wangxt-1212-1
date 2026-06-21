<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">环保检查管理</h2>
      <n-tooltip v-if="pendingCheckCount > 0">
        <template #trigger>
          <n-tag type="warning" size="small">
            待检查: {{ pendingCheckCount }} 项
          </n-tag>
        </template>
        有 {{ pendingCheckCount }} 项检查待处理
      </n-tooltip>
    </div>

    <div class="table-toolbar">
      <div class="table-toolbar-left">
        <n-input
          v-model:value="searchForm.checkNo"
          placeholder="搜索检查单号"
          clearable
          style="width: 200px;"
          @keyup.enter="handleSearch"
        >
          <template #prefix><n-icon><SearchOutlined /></n-icon></template>
        </n-input>
        <n-select
          v-model:value="searchForm.checkResult"
          placeholder="检查结果"
          clearable
          style="width: 140px;"
          :options="resultOptions"
        />
        <n-select
          v-model:value="searchForm.checkStatus"
          placeholder="检查状态"
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
          新建检查
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
        label-width="140px"
      >
        <n-form-item label="检查编号" path="checkNo">
          <n-input v-model:value="formData.checkNo" placeholder="请输入检查编号" />
        </n-form-item>
        <n-form-item label="回收池编号" path="poolNo">
          <n-input v-model:value="formData.poolNo" placeholder="请输入回收池编号" />
        </n-form-item>
        <n-form-item label="关联回收记录" path="recoveryId">
          <n-select
            v-model:value="formData.recoveryId"
            placeholder="请选择关联回收记录"
            :options="wasteOptions"
            filterable
            clearable
          />
        </n-form-item>
        <n-form-item label="标准浓度(%)" path="standardConcentration">
          <n-input-number
            v-model:value="formData.standardConcentration"
            placeholder="请输入标准浓度"
            :min="0"
            :max="100"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="实测浓度(%)" path="concentration">
          <n-input-number
            v-model:value="formData.concentration"
            placeholder="请输入实测浓度"
            :min="0"
            :max="100"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="pH值" path="phValue">
          <n-input-number
            v-model:value="formData.phValue"
            placeholder="请输入pH值"
            :min="0"
            :max="14"
            :step="0.1"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="COD(mg/L)" path="codValue">
          <n-input-number
            v-model:value="formData.codValue"
            placeholder="请输入COD值"
            :min="0"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="BOD(mg/L)" path="bodValue">
          <n-input-number
            v-model:value="formData.bodValue"
            placeholder="请输入BOD值"
            :min="0"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="检查结果" path="checkResult">
          <n-select
            v-model:value="formData.checkResult"
            placeholder="请选择检查结果"
            :options="resultOptions"
          />
        </n-form-item>
        <n-form-item label="检查状态" path="checkStatus">
          <n-select
            v-model:value="formData.checkStatus"
            placeholder="请选择检查状态"
            :options="statusOptions"
          />
        </n-form-item>
        <n-form-item label="检查员" path="inspectorName">
          <n-input v-model:value="formData.inspectorName" placeholder="请输入检查员姓名" />
        </n-form-item>
        <n-form-item label="需要复检" path="recheckRequired">
          <n-switch v-model:value="formData.recheckRequired" />
        </n-form-item>
        <n-form-item label="备注" path="remark">
          <n-input v-model:value="formData.remark" type="textarea" placeholder="请输入备注" />
        </n-form-item>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, h } from 'vue'
import { useMessage, useDialog, NTag } from 'naive-ui'
import { SearchOutlined, PlusOutlined } from '@vicons/antd'
import {
  getEnvCheckPage,
  addEnvCheck,
  updateEnvCheck,
  deleteEnvCheck,
  closeCheck
} from '@/api/env'
import { getWasteList } from '@/api/waste'
import dayjs from 'dayjs'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const tableData = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const modalTitle = ref('新建检查')
const formRef = ref(null)
const wasteOptions = ref([])

const searchForm = reactive({
  checkNo: '',
  checkResult: null,
  checkStatus: null,
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

const resultOptions = [
  { label: '待检测', value: 'PENDING' },
  { label: '合格', value: 'QUALIFIED' },
  { label: '不合格', value: 'UNQUALIFIED' }
]

const statusOptions = [
  { label: '开放', value: 'OPEN' },
  { label: '已关闭', value: 'CLOSED' }
]

const formData = reactive({
  id: null,
  checkNo: '',
  poolNo: '',
  recoveryId: null,
  standardConcentration: 30,
  concentration: 0,
  phValue: 7,
  codValue: 0,
  bodValue: 0,
  checkResult: 'PENDING',
  checkStatus: 'OPEN',
  inspectorName: '',
  recheckRequired: false,
  remark: ''
})

const formRules = {
  checkNo: { required: true, message: '请输入检查编号', trigger: 'blur' },
  poolNo: { required: true, message: '请输入回收池编号', trigger: 'blur' },
  concentration: { required: true, message: '请输入实测浓度', trigger: 'blur' }
}

const pendingCheckCount = computed(() => {
  return tableData.value.filter(item => item.checkStatus === 'OPEN').length
})

const columns = [
  { title: '检查编号', key: 'checkNo', width: 140 },
  { title: '回收池编号', key: 'poolNo', width: 120 },
  {
    title: '浓度(%)',
    key: 'concentration',
    width: 100,
    render: (row) => {
      if (!row.concentration) return '--'
      const isOver = row.standardConcentration && row.concentration > row.standardConcentration
      return h(
        'span',
        { style: { color: isOver ? '#ef4444' : '#1f2937', fontWeight: isOver ? '600' : 'normal' } },
        row.concentration + (isOver ? ' (超标)' : '')
      )
    }
  },
  { title: 'pH值', key: 'phValue', width: 80 },
  { title: 'COD', key: 'codValue', width: 90 },
  { title: 'BOD', key: 'bodValue', width: 90 },
  {
    title: '检查结果',
    key: 'checkResult',
    width: 100,
    render: (row) => {
      const typeMap = { PENDING: 'default', QUALIFIED: 'success', UNQUALIFIED: 'error' }
      const textMap = { PENDING: '待检测', QUALIFIED: '合格', UNQUALIFIED: '不合格' }
      return h(NTag, { type: typeMap[row.checkResult] || 'default', size: 'small' }, () => textMap[row.checkResult] || '未知')
    }
  },
  {
    title: '检查状态',
    key: 'checkStatus',
    width: 90,
    render: (row) => {
      const typeMap = { OPEN: 'warning', CLOSED: 'success' }
      const textMap = { OPEN: '开放', CLOSED: '已关闭' }
      return h(NTag, { type: typeMap[row.checkStatus] || 'default', size: 'small' }, () => textMap[row.checkStatus] || '未知')
    }
  },
  { title: '检查员', key: 'inspectorName', width: 100 },
  {
    title: '复检',
    key: 'recheckRequired',
    width: 80,
    render: (row) => row.recheckRequired ? h(NTag, { type: 'warning', size: 'small' }, () => '需复检') : '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: (row) => {
      const btns = []
      if (row.checkStatus === 'OPEN') {
        btns.push(h('n-button', { size: 'small', type: 'success', onClick: () => handleClose(row) }, () => '关闭检查'))
      }
      btns.push(h('n-button', { size: 'small', type: 'primary', onClick: () => handleEdit(row) }, () => '编辑'))
      btns.push(h('n-button', { size: 'small', type: 'error', onClick: () => handleDelete(row) }, () => '删除'))
      return h('n-space', { size: 'small' }, () => btns)
    }
  }
]

function fetchData() {
  loading.value = true
  getEnvCheckPage({
    pageNum: pagination.page,
    pageSize: pagination.pageSize,
    checkNo: searchForm.checkNo || undefined,
    checkResult: searchForm.checkResult || undefined,
    checkStatus: searchForm.checkStatus || undefined,
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

function loadWasteOptions() {
  getWasteList().then(data => {
    wasteOptions.value = data.map(w => ({ label: w.recoveryNo, value: w.id }))
  }).catch(() => {})
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() {
  searchForm.checkNo = ''
  searchForm.checkResult = null
  searchForm.checkStatus = null
  searchForm.poolNo = ''
  pagination.page = 1
  fetchData()
}
function handlePageChange(page) { pagination.page = page; fetchData() }

function handleAdd() {
  isEdit.value = false
  modalTitle.value = '新建检查'
  resetForm()
  loadWasteOptions()
  showModal.value = true
}

function handleEdit(row) {
  isEdit.value = true
  modalTitle.value = '编辑检查'
  loadWasteOptions()
  Object.assign(formData, row)
  showModal.value = true
}

function handleDelete(row) {
  dialog.warning({
    title: '删除确认',
    content: `确定要删除环保检查 ${row.checkNo} 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      deleteEnvCheck(row.id).then(() => {
        message.success('删除成功')
        fetchData()
      }).catch(err => message.error(err.message || '删除失败'))
    }
  })
}

function handleClose(row) {
  if (row.checkResult !== 'QUALIFIED') {
    dialog.warning({
      title: '关闭失败',
      content: `环保检查 ${row.checkNo} 结果为${row.checkResult === 'PENDING' ? '待检测' : '不合格'}，不能关闭。请确保各项指标达标。`,
      positiveText: '知道了',
      negativeText: null,
      type: 'warning'
    })
    return
  }

  dialog.success({
    title: '关闭确认',
    content: `确定要关闭环保检查 ${row.checkNo} 吗？关闭后将无法修改。`,
    positiveText: '确定关闭',
    negativeText: '取消',
    onPositiveClick: () => {
      closeCheck(row.id).then(() => {
        message.success('环保检查已关闭')
        fetchData()
      }).catch(err => message.error(err.message || '关闭失败'))
    }
  })
}

function handleSubmit() {
  formRef.value?.validate((errors) => {
    if (!errors) {
      const promise = isEdit.value ? updateEnvCheck(formData) : addEnvCheck(formData)
      promise.then(() => {
        message.success(isEdit.value ? '更新成功' : '创建成功')
        showModal.value = false
        fetchData()
      }).catch(err => message.error(err.message || '操作失败'))
    }
  })
}

function resetForm() {
  Object.assign(formData, {
    id: null,
    checkNo: '',
    poolNo: '',
    recoveryId: null,
    standardConcentration: 30,
    concentration: 0,
    phValue: 7,
    codValue: 0,
    bodValue: 0,
    checkResult: 'PENDING',
    checkStatus: 'OPEN',
    inspectorName: '',
    recheckRequired: false,
    remark: ''
  })
}

onMounted(() => { fetchData() })
</script>
