<template>
  <div class="page-container">
    <NavBar title="学生认证" :showBack="true" />
    <div class="page-content">
      <div v-if="user?.verifyStatus === 2" class="success-card">
        <el-icon :size="64" color="#4CAF50"><CircleCheck /></el-icon>
        <p style="font-size:18px;font-weight:600;margin:12px 0;">认证已通过</p>
        <p style="color:#999;">学校: {{ user.schoolName }}</p>
        <p style="color:#999;">学号: {{ user.studentId }}</p>
      </div>

      <div v-else-if="user?.verifyStatus === 1" class="pending-card">
        <el-icon :size="64" color="#FF9800"><Clock /></el-icon>
        <p style="font-size:18px;font-weight:600;margin:12px 0;">认证审核中</p>
        <p style="color:#999;">请耐心等待管理员审核</p>
      </div>

      <div v-else class="form-card card">
        <div class="card-title">学生身份认证</div>
        <p class="card-desc">认证后可以发布商品，获得更多信任</p>
        <el-form :model="form" label-width="80px" size="large">
          <el-form-item label="学校">
            <el-input v-model="form.schoolName" placeholder="请输入学校全称" />
          </el-form-item>
          <el-form-item label="学号">
            <el-input v-model="form.studentId" placeholder="请输入学号" />
          </el-form-item>
          <el-form-item label="学生证">
            <el-upload
              :auto-upload="false"
              :on-change="handleFileChange"
              :limit="1"
              list-type="picture"
              accept="image/*"
            >
              <el-button type="primary" plain>选择学生证照片</el-button>
              <template #tip>
                <div style="font-size:12px;color:#999;margin-top:4px;">请上传清晰的学生证照片（封面或内页）</div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="submitVerify" :disabled="!canSubmit">
          提交认证
        </el-button>
      </div>

      <div v-if="user?.verifyStatus === 3" class="reject-card">
        <p style="color:#F44336;font-weight:600;">认证被拒绝</p>
        <p style="color:#999;margin-top:4px;" v-if="user.verifyReason">原因: {{ user.verifyReason }}</p>
        <el-button type="primary" style="margin-top:12px;" @click="user.verifyStatus = 0">重新认证</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, Clock } from '@element-plus/icons-vue'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import NavBar from '@/components/NavBar.vue'

const userStore = useUserStore()
const { user } = storeToRefs(userStore)

const loading = ref(false)
const studentCardFile = ref(null)

const form = reactive({
  schoolName: '',
  studentId: ''
})

const canSubmit = computed(() => form.schoolName && form.studentId && studentCardFile.value)

function handleFileChange(file) {
  studentCardFile.value = file.raw
}

async function submitVerify() {
  if (!canSubmit.value) {
    ElMessage.warning('请填写完整信息并上传学生证')
    return
  }
  loading.value = true
  try {
    const formData = new FormData()
    formData.append('schoolName', form.schoolName)
    formData.append('studentId', form.studentId)
    formData.append('file', studentCardFile.value)
    await authApi.verifyStudent(formData)
    ElMessage.success('认证申请已提交，请等待审核')
    userStore.fetchUser()
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  userStore.fetchUser()
})
</script>

<style scoped>
.card, .success-card, .pending-card, .reject-card {
  background: white;
  border-radius: var(--radius);
  padding: 24px 16px;
  box-shadow: var(--shadow-card);
  text-align: center;
}
.card-title { font-size: 18px; font-weight: 600; margin-bottom: 8px; text-align: left; }
.card-desc { color: var(--color-text-muted); margin-bottom: 20px; text-align: left; font-size: 13px; }
.submit-btn { width: 100%; margin-top: 16px; border-radius: 20px; }
</style>
