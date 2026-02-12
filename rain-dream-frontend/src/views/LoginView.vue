<template>
  <div class="auth page-shell">
    <el-card class="auth-card card-panel">
      <h2>{{ isRegister ? '注册' : '登录' }} RainDream</h2>
      <el-form :model="form" label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item v-if="isRegister" label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">{{ isRegister ? '注册并登录' : '登录' }}</el-button>
        <el-button text @click="isRegister = !isRegister">{{ isRegister ? '已有账号，去登录' : '没有账号，去注册' }}</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const loading = ref(false)
const isRegister = ref(false)
const form = reactive({ username: '', password: '', phone: '' })

const submit = async () => {
  if (!form.username || !form.password) return ElMessage.warning('请填写用户名和密码')
  loading.value = true
  try {
    if (isRegister.value) {
      await auth.register(form)
      ElMessage.success('注册成功，请登录')
      isRegister.value = false
      return
    }
    await auth.login(form)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth { display: grid; place-items: center; padding: 16px; }
.auth-card { width: 420px; max-width: 100%; padding: 12px; }
</style>
