<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">车辆管理</h2>
    </div>

    <div class="table-toolbar">
      <div class="table-toolbar-left">
        <n-input
          v-model:value="searchForm.vehicleNo"
          placeholder="搜索车辆编号"
          clearable
          style="width: 200px;"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <n-icon><SearchOutlined /></n-icon>
          </template>
        </n-input>
        <n-select
          v-model:value="searchForm.vehicleStatus"
          placeholder="车辆状态"
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
          新增车辆
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
        label-width="100px"
      >
        <n-form-item label="车辆编号" path="vehicleNo">
          <n-input v-model:value="formData.vehicleNo" placeholder="请输入车辆编号" />
        </n-form-item>
        <n-form-item label="车辆名称" path="vehicleName">
          <n-input v-model:value="formData.vehicleName" placeholder="请输入车辆名称" />
        </n-form-item>
        <n-form-item label="车辆类型" path="vehicleType">
          <n-select
            v-model:value="formData.vehicleType"
            placeholder="请选择车辆类型"
            :options="typeOptions"
          />
        </n-form-item>
        <n-form-item label="驾驶员" path="driverName">
          <n-input v-model:value="formData.driverName" placeholder="请输入驾驶员姓名" />
        </n-form-item>
        <n-form-item label="联系电话" path="driverPhone">
          <n-input v-model:value="formData.driverPhone" placeholder="请输入联系电话" />
        </n-form-item>
        <n-form-item label="当前液体量" path="currentFluidVolume">
          <n-input-number
            v-model:value="formData.currentFluidVolume"
            placeholder="请输入液体量(升)"
            :min="0"
            style="width: 100%;"
          />
        </n-form-item>
        <n-form-item label="车辆状态" path="vehicleStatus">
          <n-select
            v-model:value="formData.vehicleStatus"
            placeholder="请选择车辆状态"
            :options="statusOptions"
          />
        </n-form-item>
        <n-form-item label="当前位置" path="currentStand">
          <n-input v-model:value="formData.currentStand" placeholder="请输入当前位置" />
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
  getVehiclePage,
  addVehicle,
  updateVehicle,
  deleteVehicle
} from '@/api/vehicle'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const tableData = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const modalTitle = ref('新增车辆')
const formRef = ref(null)

const searchForm = reactive({
  vehicleNo: '',
  vehicleStatus: null,
  vehicleType: null
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
  { label: '空闲', value: 'IDLE' },
  { label: '已调度', value: 'DISPATCHED' },
  { label: '作业中', value: 'SPRAYING' },
  { label: '维护中', value: 'MAINTENANCE' }
]

const typeOptions = [
  { label: '除冰车', value: 'TYPE1' },
  { label: '防冰车', value: 'TYPE2' }
]

const formData = reactive({
  id: null,
  vehicleNo: '',
  vehicleName: '',
  vehicleType: 'TYPE1',
  driverName: '',
  driverPhone: '',
  currentFluidVolume: 0,
  vehicleStatus: 'IDLE',
  currentStand: '',
  remark: ''
})

const formRules = {
  vehicleNo: { required: true, message: '请输入车辆编号', trigger: 'blur' }
}

const columns = [
  { title: '车辆编号', key: 'vehicleNo', width: 120 },
  { title: '车辆名称', key: 'vehicleName', width: 120 },
  {
    title: '车辆类型',
    key: 'vehicleType',
    width: 100,
    render: (row) => row.vehicleType === 'TYPE1' ? '除冰车' : '防冰车'
  },
  { title: '驾驶员', key: 'driverName', width: 100 },
  { title: '联系电话', key: 'driverPhone', width: 130 },
  {
    title: '当前液体量(L)',
    key: 'currentFluidVolume',
    width: 120,
    render: (row) => row.currentFluidVolume || 0
  },
  {
    title: '车辆状态',
    key: 'vehicleStatus',
    width: 100,
    render: (row) => {
      const typeMap = { IDLE: 'success', DISPATCHED: 'warning', SPRAYING: 'info', MAINTENANCE: 'error' }
      const textMap = { IDLE: '空闲', DISPATCHED: '已调度', SPRAYING: '作业中', MAINTENANCE: '维护中' }
      return h(NTag, { type: typeMap[row.vehicleStatus] || 'default', size: 'small' }, () => textMap[row.vehicleStatus] || '未知')
    }
  },
  { title: '当前位置', key: 'currentStand', width: 120 },
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
  getVehiclePage({
    pageNum: pagination.page,
    pageSize: pagination.pageSize,
    vehicleNo: searchForm.vehicleNo || undefined,
    vehicleStatus: searchForm.vehicleStatus || undefined,
    vehicleType: searchForm.vehicleType || undefined
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
  searchForm.vehicleNo = ''
  searchForm.vehicleStatus = null
  searchForm.vehicleType = null
  pagination.page = 1
  fetchData()
}
function handlePageChange(page) { pagination.page = page; fetchData() }

function handleAdd() {
  isEdit.value = false
  modalTitle.value = '新增车辆'
  resetForm()
  showModal.value = true
}

function handleEdit(row) {
  isEdit.value = true
  modalTitle.value = '编辑车辆'
  Object.assign(formData, row)
  showModal.value = true
}

function handleDelete(row) {
  dialog.warning({
    title: '删除确认',
    content: `确定要删除车辆 ${row.vehicleNo} 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      deleteVehicle(row.id).then(() => {
        message.success('删除成功')
        fetchData()
      }).catch(err => message.error(err.message || '删除失败'))
    }
  })
}

function handleSubmit() {
  formRef.value?.validate((errors) => {
    if (!errors) {
      const promise = isEdit.value ? updateVehicle(formData) : addVehicle(formData)
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
    id: null, vehicleNo: '', vehicleName: '', vehicleType: 'TYPE1',
    driverName: '', driverPhone: '', currentFluidVolume: 0,
    vehicleStatus: 'IDLE', currentStand: '', remark: ''
  })
}

onMounted(() => { fetchData() })
</script>
