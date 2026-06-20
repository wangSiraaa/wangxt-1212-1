<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">喷洒记录</h2>
    </div>

    <div class="table-toolbar">
      <div class="table-toolbar-left">
        <n-input
          v-model:value="searchForm.sprayNo"
          placeholder="搜索喷洒单号"
          clearable
          style="width: 200px;"
          @keyup.enter="handleSearch"
        >
          <template #prefix><n-icon><SearchOutlined /></n-icon></template>
        </n-input>
        <n-select
          v-model:value="searchForm.sprayStatus"
          placeholder="喷洒状态"
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
          新增喷洒
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
        label-width="120px"
      >
        <n-form-item label="关联调度" path="dispatchId">
          <n-select
            v-model:value="formData.dispatchId"
            placeholder="请选择调度单"
            :options="dispatchOptions"
            filterable
          />
        </n-form-item>
        <n-form-item label="驾驶员" path="driverName">
          <n-input v-model:value="formData.driverName" placeholder="请输入驾驶员姓名" />
        </n-form-item>
        <n-form-item label="实际喷洒量(L)" path="sprayVolume">
          <n-input-number
            v-model:value="formData.sprayVolume"
            placeholder="请输入实际喷洒量"
            :min="0"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="开始时间" path="startTime">
          <n-date-picker
            v-model:value="formData.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="结束时间" path="endTime">
          <n-date-picker
            v-model:value="formData.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="喷洒状态" path="sprayStatus">
          <n-select
            v-model:value="formData.sprayStatus"
            placeholder="请选择喷洒状态"
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
  getSprayPage,
  addSpray,
  updateSpray,
  deleteSpray,
  completeSpray
} from '@/api/spray'
import { getDispatchList } from '@/api/dispatch'
import dayjs from 'dayjs'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const tableData = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const modalTitle = ref('新增喷洒')
const formRef = ref(null)

const searchForm = reactive({
  sprayNo: '',
  sprayStatus: null,
  dispatchId: null
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
  { label: '待喷洒', value: 'PENDING' },
  { label: '喷洒中', value: 'SPRAYING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

const dispatchOptions = ref([])

const formData = reactive({
  id: null,
  dispatchId: null,
  driverName: '',
  sprayVolume: 0,
  startTime: null,
  endTime: null,
  sprayStatus: 'PENDING',
  remark: ''
})

const formRules = {
  dispatchId: { required: true, message: '请选择调度单', trigger: 'change' }
}

const columns = [
  { title: '喷洒单号', key: 'sprayNo', width: 140 },
  { title: '航班号', key: 'flightNo', width: 100 },
  { title: '车辆编号', key: 'vehicleNo', width: 110 },
  { title: '批次号', key: 'batchNo', width: 130 },
  { title: '驾驶员', key: 'driverName', width: 100 },
  { title: '喷洒量(L)', key: 'sprayVolume', width: 110 },
  {
    title: '喷洒状态',
    key: 'sprayStatus',
    width: 100,
    render: (row) => {
      const typeMap = { PENDING: 'warning', SPRAYING: 'info', COMPLETED: 'success', CANCELLED: 'default' }
      const textMap = { PENDING: '待喷洒', SPRAYING: '喷洒中', COMPLETED: '已完成', CANCELLED: '已取消' }
      return h(NTag, { type: typeMap[row.sprayStatus] || 'default', size: 'small' }, () => textMap[row.sprayStatus] || '未知')
    }
  },
  {
    title: '开始时间',
    key: 'startTime',
    width: 160,
    render: (row) => row.startTime ? dayjs(row.startTime).format('YYYY-MM-DD HH:mm') : '--'
  },
  {
    title: '结束时间',
    key: 'endTime',
    width: 160,
    render: (row) => row.endTime ? dayjs(row.endTime).format('YYYY-MM-DD HH:mm') : '--'
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) => {
      const btns = []
      if (row.sprayStatus === 'PENDING') {
        btns.push(h('n-button', { size: 'small', type: 'primary', onClick: () => handleComplete(row) }, () => '完成喷洒'))
      }
      btns.push(h('n-button', { size: 'small', type: 'primary', onClick: () => handleEdit(row) }, () => '编辑'))
      btns.push(h('n-button', { size: 'small', type: 'error', onClick: () => handleDelete(row) }, () => '删除'))
      return h('n-space', { size: 'small' }, () => btns)
    }
  }
]

function fetchData() {
  loading.value = true
  getSprayPage({
    pageNum: pagination.page,
    pageSize: pagination.pageSize,
    sprayNo: searchForm.sprayNo || undefined,
    sprayStatus: searchForm.sprayStatus || undefined,
    dispatchId: searchForm.dispatchId || undefined
  }).then(data => {
    tableData.value = data.records
    pagination.itemCount = data.total
  }).catch(err => {
    message.error(err.message || '获取数据失败')
  }).finally(() => {
    loading.value = false
  })
}

function loadDispatchOptions() {
  getDispatchList().then(data => {
    dispatchOptions.value = data.map(d => ({ label: d.dispatchNo, value: d.id }))
  }).catch(() => {})
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() {
  searchForm.sprayNo = ''
  searchForm.sprayStatus = null
  searchForm.dispatchId = null
  pagination.page = 1
  fetchData()
}
function handlePageChange(page) { pagination.page = page; fetchData() }

function handleAdd() {
  isEdit.value = false
  modalTitle.value = '新增喷洒'
  resetForm()
  loadDispatchOptions()
  showModal.value = true
}

function handleEdit(row) {
  isEdit.value = true
  modalTitle.value = '编辑喷洒'
  loadDispatchOptions()
  Object.assign(formData, row)
  if (row.startTime) formData.startTime = dayjs(row.startTime).valueOf()
  if (row.endTime) formData.endTime = dayjs(row.endTime).valueOf()
  showModal.value = true
}

function handleDelete(row) {
  dialog.warning({
    title: '删除确认',
    content: `确定要删除喷洒记录 ${row.sprayNo} 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      deleteSpray(row.id).then(() => {
        message.success('删除成功')
        fetchData()
      }).catch(err => message.error(err.message || '删除失败'))
    }
  })
}

function handleComplete(row) {
  dialog.success({
    title: '完成确认',
    content: `确定要完成喷洒 ${row.sprayNo} 吗？系统将自动扣减批次液体量并更新状态。`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      completeSpray(row.id).then(() => {
        message.success('喷洒已完成，液体已扣减')
        fetchData()
      }).catch(err => message.error(err.message || '操作失败'))
    }
  })
}

function handleSubmit() {
  formRef.value?.validate((errors) => {
    if (!errors) {
      const submitData = { ...formData }
      if (submitData.startTime) submitData.startTime = dayjs(submitData.startTime).toISOString()
      if (submitData.endTime) submitData.endTime = dayjs(submitData.endTime).toISOString()

      const promise = isEdit.value ? updateSpray(submitData) : addSpray(submitData)
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
    dispatchId: null,
    driverName: '',
    sprayVolume: 0,
    startTime: null,
    endTime: null,
    sprayStatus: 'PENDING',
    remark: ''
  })
}

onMounted(() => { fetchData() })
</script>
