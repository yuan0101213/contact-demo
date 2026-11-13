<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { contactApi } from './api'

const contacts = ref([])
const keyword = ref('')
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const page = ref(1)
const pageSize = 6
const form = reactive({ name: '', email: '', phone: '', note: '' })
const rows = computed(() => contacts.value.slice((page.value - 1) * pageSize, page.value * pageSize))
const rules = {
  name: [{ required: true, whitespace: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入 11 位中国大陆手机号', trigger: 'blur' }],
}
const messageOf = (e) => e.response?.data?.message || '请求失败，请确认 Java 后端已启动后重试'

async function load() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await contactApi.list(keyword.value)
    contacts.value = data
    page.value = Math.max(1, Math.min(page.value, Math.ceil(data.length / pageSize)))
  } catch (e) { error.value = messageOf(e) }
  finally { loading.value = false }
}
function search() { page.value = 1; load() }
function openEditor(contact) {
  editingId.value = contact?.id ?? null
  Object.assign(form, { name: contact?.name || '', email: contact?.email || '', phone: contact?.phone || '', note: contact?.note || '' })
  dialogVisible.value = true
}
async function save() {
  if (saving.value || !(await formRef.value.validate().catch(() => false))) return
  saving.value = true
  try {
    const payload = Object.fromEntries(Object.entries(form).map(([key, value]) => [key, value.trim()]))
    if (editingId.value !== null) await contactApi.update(editingId.value, payload)
    else await contactApi.create(payload)
    ElMessage.success(editingId.value !== null ? '联系人已更新' : '联系人已添加')
    dialogVisible.value = false
    await load()
  } catch (e) { ElMessage.error(messageOf(e)) }
  finally { saving.value = false }
}
async function remove(contact) {
  try { await ElMessageBox.confirm(`确定删除「${contact.name}」吗？此操作无法撤销。`, '删除联系人', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }) }
  catch { return }
  try { await contactApi.remove(contact.id); ElMessage.success('联系人已删除'); await load() }
  catch (e) { ElMessage.error(messageOf(e)) }
}
onMounted(load)
</script>

<template>
  <div class="app-shell">
    <header class="topbar"><a href="/" class="brand"><span class="brand-icon">C</span> Contact Book<span class="brand-divider">/</span><span class="workspace">我的工作空间</span></a><span class="profile">我</span></header>
    <main>
      <div class="breadcrumb">工作空间 <span>/</span> 联系人管理</div>
      <section class="page-heading">
        <div><div class="eyebrow">YOUR PEOPLE, IN ONE PLACE</div><h1>联系人管理<span class="heading-dot">.</span></h1><p>让每一次联系，都井井有条。</p></div>
        <el-button type="primary" size="large" @click="openEditor()"><span class="plus">＋</span> 新增联系人</el-button>
      </section>
      <section class="overview"><div class="overview-icon">▤</div><div><span class="overview-label">{{ keyword.trim() ? '搜索结果' : '全部联系人' }}</span><div class="overview-count">{{ contacts.length }} <small>位联系人</small></div></div><div class="overview-note">把重要的人，留在触手可及的地方。</div></section>
      <section class="table-card">
        <div class="table-toolbar"><div><h2>联系人列表 <span>{{ contacts.length }}</span></h2><p>查看和管理你的联系信息</p></div><div class="search-area"><el-input v-model="keyword" placeholder="搜索姓名、邮箱或手机号" clearable aria-label="搜索联系人" @keyup.enter="search" @clear="search"/><el-button :loading="loading" @click="search">搜索</el-button></div></div>
        <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="error-alert" />
        <el-table v-loading="loading" :data="rows" empty-text="暂无联系人，点击右上角添加第一位吧" class="contact-table">
          <el-table-column label="姓名" min-width="180"><template #default="{ row }"><div class="person"><span class="avatar" :class="`tone-${row.id % 4}`">{{ row.name.slice(0, 1) }}</span><div><strong>{{ row.name }}</strong><small>联系人 #{{ String(row.id).padStart(3, '0') }}</small></div></div></template></el-table-column>
          <el-table-column prop="email" label="邮箱地址" min-width="220" />
          <el-table-column prop="phone" label="手机号码" min-width="160" />
          <el-table-column label="备注" min-width="160" show-overflow-tooltip><template #default="{ row }"><span class="note-tag" v-if="row.note">{{ row.note }}</span><span v-else class="muted">—</span></template></el-table-column>
          <el-table-column label="操作" width="120" align="right"><template #default="{ row }"><el-button link type="primary" @click="openEditor(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
        </el-table>
        <div class="table-footer"><span>共 {{ contacts.length }} 位联系人</span><el-pagination v-model:current-page="page" :page-size="pageSize" :total="contacts.length" layout="prev, pager, next" background /></div>
      </section>
      <footer class="page-footer"><span>CONTACT BOOK</span><span>简单记录 · 轻松联系</span></footer>
    </main>
    <el-dialog v-model="dialogVisible" :title="editingId !== null ? '编辑联系人' : '新增联系人'" width="480px" destroy-on-close :close-on-click-modal="false" :close-on-press-escape="!saving" :show-close="!saving" @opened="formRef?.clearValidate()">
      <p class="dialog-description">填写联系信息，方便下次快速找到对方。</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="saving" @submit.prevent="save">
        <el-form-item label="姓名" prop="name"><el-input v-model="form.name" placeholder="例如：张三" maxlength="30" /></el-form-item>
        <el-form-item label="邮箱地址" prop="email"><el-input v-model="form.email" placeholder="name@example.com" maxlength="100" /></el-form-item>
        <el-form-item label="手机号码" prop="phone"><el-input v-model="form.phone" placeholder="11 位中国大陆手机号" maxlength="11" /></el-form-item>
        <el-form-item label="备注" prop="note"><el-input v-model="form.note" type="textarea" :rows="3" placeholder="例如：同事、朋友，或其他需要记住的信息" maxlength="100" show-word-limit /></el-form-item>
      </el-form>
      <template #footer><el-button :disabled="saving" @click="dialogVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存联系人</el-button></template>
    </el-dialog>
  </div>
</template>
