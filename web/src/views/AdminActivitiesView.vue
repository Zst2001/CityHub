<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminActivities, updateAdminActivity, getAdminTickets, updateAdminTicket } from '../api/admin'

const activities = ref([]); const keyword = ref(''); const loading = ref(false)
const edit = ref(null); const tickets = ref([]); const ticketEdit = ref(null); const ticketActivityId = ref(null)
async function load() { loading.value = true; try { const result = await getAdminActivities({ keyword: keyword.value || undefined, current: 1 }); activities.value = result.data || [] } finally { loading.value = false } }
async function saveActivity() { await updateAdminActivity(edit.value); ElMessage.success('活动已保存'); edit.value = null; await load() }
async function openTickets(activity) { ticketActivityId.value = activity.id; tickets.value = await getAdminTickets(activity.id); if (!tickets.value.length) ElMessage.info('暂无票券') }
async function saveTicket() { const activityId = ticketActivityId.value; await updateAdminTicket(ticketEdit.value.id, ticketEdit.value); ElMessage.success('票券与库存已同步'); ticketEdit.value = null; await openTickets({ id: activityId }) }
onMounted(load)
</script>
<template>
  <main class="admin-page">
    <div class="admin-head"><div><p class="eyebrow">CityHub Admin</p><h1>活动管理</h1></div><el-input v-model="keyword" placeholder="搜索活动" clearable @keyup.enter="load" /><el-button type="primary" @click="load">搜索</el-button></div>
    <el-table v-loading="loading" :data="activities" stripe><el-table-column prop="title" label="活动名称" min-width="220" /><el-table-column prop="area" label="区域" width="130" /><el-table-column prop="avgPrice" label="参考价" width="110" /><el-table-column label="操作" width="190"><template #default="scope"><el-button link type="primary" @click="edit = { ...scope.row }">编辑</el-button><el-button link @click="openTickets(scope.row)">票券/库存</el-button></template></el-table-column></el-table>
    <el-dialog v-model="edit" title="编辑活动" width="min(92vw, 560px)"><el-form v-if="edit" label-position="top"><el-form-item label="活动名称"><el-input v-model="edit.title" /></el-form-item><el-form-item label="区域"><el-input v-model="edit.area" /></el-form-item><el-form-item label="地址"><el-input v-model="edit.address" /></el-form-item><el-form-item label="开放时间"><el-input v-model="edit.openHours" /></el-form-item><el-form-item label="参考价格"><el-input-number v-model="edit.avgPrice" :min="0" /></el-form-item></el-form><template #footer><el-button @click="edit=null">取消</el-button><el-button type="primary" @click="saveActivity">保存修改</el-button></template></el-dialog>
    <el-dialog :model-value="tickets.length > 0" title="票券与库存" width="min(92vw, 620px)" @close="tickets=[]"><el-table :data="tickets"><el-table-column prop="title" label="票券" /><el-table-column prop="stock" label="库存" /><el-table-column label="操作"><template #default="scope"><el-button link type="primary" @click="ticketEdit={...scope.row}">编辑</el-button></template></el-table-column></el-table></el-dialog>
    <el-dialog v-model="ticketEdit" title="编辑票券" width="min(92vw, 460px)"><el-form v-if="ticketEdit" label-position="top"><el-form-item label="票券名称"><el-input v-model="ticketEdit.title" /></el-form-item><el-form-item label="库存"><el-input-number v-model="ticketEdit.stock" :min="0" /></el-form-item><el-form-item label="开始时间"><el-date-picker v-model="ticketEdit.beginTime" type="datetime" /></el-form-item><el-form-item label="结束时间"><el-date-picker v-model="ticketEdit.endTime" type="datetime" /></el-form-item></el-form><template #footer><el-button @click="ticketEdit=null">取消</el-button><el-button type="primary" @click="saveTicket">保存</el-button></template></el-dialog>
  </main>
</template>
<style scoped>.admin-page{max-width:1180px;margin:0 auto;padding:48px 24px}.admin-head{display:flex;align-items:end;gap:12px;margin-bottom:24px}.admin-head h1{margin:4px 0 0;font-family:Georgia,serif}.admin-head .el-input{max-width:300px;margin-left:auto}@media(max-width:767px){.admin-page{padding:28px 16px}.admin-head{align-items:stretch;flex-wrap:wrap}.admin-head .el-input{order:3;max-width:none;width:100%;margin-left:0}}</style>
