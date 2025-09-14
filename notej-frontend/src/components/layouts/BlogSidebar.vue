<template>
  <v-navigation-drawer
    v-model="blog.drawerOpen"
    app
    class="sidebar"
    temporary
    transition="slide-x-transition"
  >
    <v-btn
      :aria-label="blog.drawerOpen ? '접기' : '펼치기'"
      class="toggle-btn"
      icon
      @click="blog.toggleDrawer"
    >
      <v-icon>{{ blog.drawerOpen ? 'mdi-chevron-left' : 'mdi-menu' }}</v-icon>
    </v-btn>

    <div v-if="blog.drawerOpen" class="profile">
      <v-avatar class="mb-4 mt-4" size="96">
        <v-img cover :src="blog.user.profilePicture || defaultProfilePic" />
      </v-avatar>
      <div class="username">{{ blog.user.name }}</div>
      <div class="bio">{{ blog.user.bio || '소개글이 없습니다.' }}</div>
    </div>

    <v-list v-if="blog.drawerOpen" density="compact" nav>
      <div class="position-relative text-center">
        <v-list-subheader class="d-inline-block py-0">카테고리</v-list-subheader>
      </div>

      <v-list-item class="px-2 py-1">
        <v-btn
          block
          class="mb-2"
          color="success"
          size="small"
          variant="tonal"
          @click="openAddCategoryDialog(null)"
        >
          <v-icon left size="small">mdi-plus</v-icon>
          루트 카테고리 추가
        </v-btn>
      </v-list-item>

      <!-- 드래그 가능한 카테고리 목록 -->
      <div class="pa-2">
        <button class="mb-2 pa-1 text-caption" @click="start()">드래그 활성화</button>

        <!-- 루트 카테고리 드래그 영역 -->
        <div ref="rootEl" class="category-list">
          <v-list-group
            v-for="category in rootCategories"
            :key="category.categoryId"
            :value="category.categoryId"
          >
            <template #activator="{ props }">
              <v-list-item
                v-bind="props"
                class="cursor-move"
                :title="category.name"
              >
                <template #append>
                  <v-btn
                    icon
                    size="x-small"
                    variant="text"
                    @click.stop="openAddCategoryDialog(category.categoryId)"
                  >
                    <v-icon size="small">mdi-plus-circle-outline</v-icon>
                  </v-btn>
                </template>
              </v-list-item>
            </template>

            <!-- 하위 카테고리 드래그 영역 -->
            <div :ref="el => { if(el) sublistRefs[category.categoryId] = el }" class="subcategory-list pl-4">
              <div
                v-for="child in category.children"
                :key="child.categoryId"
                class="cursor-move mb-2"
              >
                <v-list-item
                  :title="child.name"
                  @click="blog.selectCategory(child)"
                />
              </div>
            </div>
          </v-list-group>
        </div>
      </div>
    </v-list>
  </v-navigation-drawer>

  <!-- 카테고리 추가 다이얼로그 -->
  <v-dialog v-model="showAddDialog" max-width="500px">
    <v-card>
      <v-card-title>
        {{ parentId ? '하위 카테고리 추가' : '최상위 카테고리 추가' }}
      </v-card-title>
      <v-card-text>
        <v-form ref="form" @submit.prevent="addCategory">
          <v-text-field
            v-model="newCategory.name"
            label="카테고리명"
            required
            :rules="[v => !!v || '카테고리명을 입력해주세요']"
          />
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn color="secondary" variant="text" @click="showAddDialog = false">취소</v-btn>
        <v-btn color="primary" :loading="loading" @click="addCategory">추가</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-btn
    v-if="!blog.drawerOpen"
    aria-label="사이드바 열기"
    class="open-btn"
    icon
    @click="blog.toggleDrawer"
  >
    <v-icon>mdi-menu</v-icon>
  </v-btn>

  <v-snackbar v-model="snackbar" :color="snackbarColor" timeout="3000">
    {{ snackbarText }}
    <template #actions>
      <v-btn variant="text" @click="snackbar = false">닫기</v-btn>
    </template>
  </v-snackbar>
</template>
<script setup>
  import { nextTick, onMounted, ref } from 'vue'
  import { useBlogStore } from '@/stores/blog'
  import { useRoute } from 'vue-router'
  import { useDraggable } from 'vue-draggable-plus'
  import defaultProfilePic from '@/assets/defaults/profile.png'

  const blog = useBlogStore()
  const route = useRoute()

  // 카테고리 목록 - 깊은 복사로 참조 문제 방지
  const rootCategories = ref([...blog.categoryTree])

  // DOM 요소 참조
  const rootEl = ref(null)
  const sublistRefs = ref({}) // 하위 카테고리 ref 저장용 객체

  // 컴포넌트 마운트 시 초기화
  onMounted(async () => {
    // 스토어에서 최신 카테고리 가져오기
    await blog.fetchBlogCategories(route.params.blogUrlName)
    // 로컬 상태에 복사
    rootCategories.value = JSON.parse(JSON.stringify(blog.categoryTree))
    // 드래그 활성화
    start()
    // 하위 카테고리 드래그 초기화는 DOM이 업데이트된 후에
    nextTick(() => {
      initSubCategoryDraggable()
    })
  })

  // 루트 카테고리용 드래그앤드롭
  const { start } = useDraggable(rootEl, rootCategories, {
    animation: 150,
    ghostClass: 'ghost',
    onEnd (event) {
      handleRootCategoryOrderChange(event)
    },
  })

  // 하위 카테고리 드래그앤드롭 초기화
  function initSubCategoryDraggable () {
    rootCategories.value.forEach(category => {
      if (category.children && category.children.length) {
        const sublistRef = sublistRefs.value[category.categoryId]
        if (sublistRef) {
          const { start: startSub } = useDraggable(sublistRef, category.children, {
            animation: 150,
            ghostClass: 'ghost',
            onEnd (event) {
              handleSubCategoryOrderChange(event, category.categoryId)
            },
          })
          startSub() // 하위 카테고리 드래그 즉시 활성화
        }
      }
    })
  }

  // 최상위 카테고리 순서 변경 이벤트 처리
  async function handleRootCategoryOrderChange (event) {
    console.log('--- Draggable Event Info ---');
    console.log('Original Index:', event.oldIndex);
    console.log('New Index:', event.newIndex);
    console.log('--------------------------');

    console.log('--- rootCategories.value (After Drag) ---');
    console.log(rootCategories.value);
    console.log('-----------------------------------------');

    try {
      const updatedOrder = rootCategories.value.map((cat, index) => ({
        categoryId: cat.categoryId,
        seq: index + 1,
        parentId: cat.parentId,
      }));

      console.log('--- Data to be sent to Backend (with new seq) ---');
      console.log(updatedOrder);
      console.log('--------------------------------------------------');

      await blog.updateCategoriesOrder(updatedOrder, route.params.blogUrlName);
      showSnackbar('카테고리 순서가 업데이트되었습니다', 'success');
    } catch (error) {
      console.error('카테고리 순서 업데이트 실패:', error);
      showSnackbar('카테고리 순서 변경에 실패했습니다', 'error');
    }
  }

  // 하위 카테고리 순서 변경 이벤트 처리
  async function handleSubCategoryOrderChange (event, parentId) {
    console.log('하위 카테고리 순서 변경:', event);

    const parentCategory = rootCategories.value.find(cat => cat.categoryId === parentId)
    if (!parentCategory || !parentCategory.children) return

    try {
      const updatedOrder = parentCategory.children.map((child, index) => ({
        categoryId: child.categoryId,
        seq: index + 1,
        parentId: child.parentId,
      }))

      await blog.updateCategoriesOrder(updatedOrder, route.params.blogUrlName)
      showSnackbar('하위 카테고리 순서가 업데이트되었습니다', 'success')
    } catch (error) {
      console.error('하위 카테고리 순서 업데이트 실패:', error)
      showSnackbar('하위 카테고리 순서 변경에 실패했습니다', 'error')
    }
  }

  // 카테고리 추가 관련 상태
  const showAddDialog = ref(false)
  const parentId = ref(null)
  const newCategory = ref({
    name: '',
  })
  const form = ref(null)
  const loading = ref(false)

  // 스낵바 관련
  const snackbar = ref(false)
  const snackbarText = ref('')
  const snackbarColor = ref('success')

  // 카테고리 추가 다이얼로그 열기
  function openAddCategoryDialog (categoryId) {
    parentId.value = categoryId
    newCategory.value = {
      name: '',
      seq: 1,
    }
    showAddDialog.value = true
  }

  // 카테고리 추가 함수
  async function addCategory () {
    const { valid } = await form.value.validate()
    if (!valid) return

    loading.value = true

    try {
      await blog.addCategory({
        name: newCategory.value.name,
        seq: Number(newCategory.value.seq) || 1,
        parentId: parentId.value,
      }, route.params.blogUrlName)

      showSnackbar('카테고리가 추가되었습니다', 'success')
      showAddDialog.value = false

      // 카테고리 추가 후 목록 다시 불러오기
      await blog.fetchBlogCategories(route.params.blogUrlName)
      rootCategories.value = JSON.parse(JSON.stringify(blog.categoryTree))

      // 하위 카테고리 드래그 초기화
      nextTick(() => {
        initSubCategoryDraggable()
      })

    } catch (error) {
      console.error('카테고리 추가 실패:', error)
      showSnackbar('카테고리 추가에 실패했습니다', 'error')
    } finally {
      loading.value = false
    }
  }

  // 스낵바 표시 함수
  function showSnackbar (text, color = 'success') {
    snackbarText.value = text
    snackbarColor.value = color
    snackbar.value = true
  }
</script>

<style scoped>
.sidebar {
  background-color: var(--v-theme-surface);
  width: 280px;
}

.profile {
  text-align: center;
  margin-bottom: 1rem;
}

.username {
  font-weight: 600;
  font-size: 1.2rem;
}

.bio {
  color: var(--v-theme-secondary);
  font-size: 0.9rem;
}

.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}

.cursor-move {
  cursor: move;
}

.toggle-btn {
  position: absolute;
  top: 10px;
  right: -48px;
  background-color: var(--v-theme-primary);
  color: white;
  border-radius: 4px;
  box-shadow: var(--v-shadow-2);
  z-index: 5;
}

.open-btn {
  position: fixed;
  top: 10px;
  left: 10px;
  background-color: var(--v-theme-primary);
  color: white;
  border-radius: 4px;
  z-index: 10;
}
</style>
