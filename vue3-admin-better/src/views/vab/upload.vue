<template>
  <div class="upload-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>轮播图管理</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover">
            <div class="upload-demo">
              <h3>点击上传</h3>
              <el-upload
                class="upload-demo"
                drag
                :auto-upload="false"
                multiple
                :on-change="handleFileChange"
                :on-remove="handleRemove"
                :file-list="fileList"
                accept="image/jpeg,image/png"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">
                  将文件拖到此处，或<em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    轮播图上传：支持jpg/png格式，建议尺寸1920x600px，不超过5MB
                  </div>
                </template>
              </el-upload>
              
              <!-- 轮播图信息输入 -->
              <div style="margin-top: 20px;">
                <el-form :model="bannerForm" label-width="80px" size="small">
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="标题">
                        <el-input 
                          v-model="bannerTitle" 
                          placeholder="请输入轮播图标题"
                          maxlength="20"
                          show-word-limit
                        />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="描述">
                        <el-input 
                          v-model="bannerDescription" 
                          placeholder="请输入轮播图描述"
                          maxlength="30"
                          show-word-limit
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </el-form>
                
                <el-button 
                  type="primary" 
                  @click="submitUpload"
                  :loading="uploadLoading"
                  :disabled="fileList.length === 0"
                >
                  <el-icon><UploadFilled /></el-icon>
                  确认上传轮播图
                </el-button>
                <span style="margin-left: 10px; color: #666; font-size: 12px;">
                  已选择 {{ fileList.length }} 个文件
                </span>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card shadow="hover">
            <div class="upload-demo">
              <h3>轮播图预览</h3>
              <el-upload
                :auto-upload="false"
                list-type="picture-card"
                :on-preview="handlePictureCardPreview"
                :on-change="handleImageChange"
                :on-remove="handleImageRemove"
                :file-list="imageList"
                accept="image/jpeg,image/png"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              
              <el-dialog v-model="dialogVisible">
                <img w-full :src="dialogImageUrl" alt="Preview Image" />
              </el-dialog>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="24">
          <el-card shadow="hover">
            <div class="upload-demo">
              <h3>手动上传</h3>
              <el-upload
                ref="uploadRef"
                class="upload-demo"
                action="https://jsonplaceholder.typicode.com/posts/"
                :auto-upload="false"
                :on-change="handleChange"
                :file-list="manualFiles"
              >
                <template #trigger>
                  <el-button type="primary">选取文件</el-button>
                </template>
                <el-button 
                  class="ml-3" 
                  type="success" 
                  @click="submitUpload"
                  :loading="uploadLoading"
                >
                  上传到服务器
                </el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    文件会先保存到本地，点击上传按钮后才会上传到服务器
                  </div>
                </template>
              </el-upload>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 轮播图列表管理 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="24">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <span>轮播图列表管理</span>
                <el-button type="primary" size="small" @click="loadBannerList">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </template>
            
            <div v-loading="loading">
              <el-empty v-if="bannerList.length === 0" description="暂无轮播图" />
              
              <el-row v-else :gutter="20">
                <el-col 
                  v-for="banner in bannerList" 
                  :key="banner.id" 
                  :lg="6" 
                  :md="8" 
                  :sm="12" 
                  :xl="4"
                  style="margin-bottom: 20px;"
                >
                  <el-card shadow="hover" class="banner-card">
                    <div class="banner-image">
                      <img :src="banner.url" :alt="banner.originalName" />
                      <div class="banner-overlay">
                        <el-button 
                          type="primary" 
                          size="small" 
                          @click="toggleBannerStatus(banner)"
                        >
                          {{ banner.status === 'active' ? '禁用' : '启用' }}
                        </el-button>
                        <el-button 
                          type="warning" 
                          size="small" 
                          @click="editBanner(banner)"
                        >
                          编辑
                        </el-button>
                        <el-button 
                          type="danger" 
                          size="small" 
                          @click="deleteBanner(banner)"
                        >
                          删除
                        </el-button>
                      </div>
                    </div>
                    
                    <div class="banner-info">
                      <div class="banner-title" v-if="banner.title">{{ banner.title }}</div>
                      <div class="banner-description" v-if="banner.description">{{ banner.description }}</div>
                      <div class="banner-name">{{ banner.originalName }}</div>
                      <div class="banner-meta">
                        <el-tag :type="banner.status === 'active' ? 'success' : 'info'" size="small">
                          {{ banner.status === 'active' ? '启用' : '禁用' }}
                        </el-tag>
                        <span class="banner-size">{{ (banner.size / 1024).toFixed(1) }}KB</span>
                      </div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 编辑轮播图对话框 -->
    <el-dialog 
      v-model="editDialogVisible" 
      title="编辑轮播图信息" 
      width="500px"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input 
            v-model="editTitle" 
            placeholder="请输入轮播图标题"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="editDescription" 
            placeholder="请输入轮播图描述"
            maxlength="30"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveBannerEdit">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { UploadFilled, Plus, Refresh } from "@element-plus/icons-vue";
import { bannerApi } from "@/api/banner";

export default {
  name: "Upload",
  components: {
    UploadFilled,
    Plus,
    Refresh
  },
  data() {
    return {
      fileList: [],
      imageList: [],
      manualFiles: [],
      dialogImageUrl: "",
      dialogVisible: false,
      uploadLoading: false,
      uploadRef: null,
      bannerList: [], // 已上传的轮播图列表
      loading: false,
      // 轮播图信息
      bannerTitle: '',
      bannerDescription: '',
      // 编辑对话框
      editDialogVisible: false,
      editingBanner: null,
      editTitle: '',
      editDescription: ''
    };
  },
  created() {
    this.loadBannerList();
  },
  methods: {
    handleFileChange(file, fileList) {
      console.log("文件选择:", file, fileList);
      console.log("文件对象详情:", {
        name: file.name,
        size: file.size,
        raw: file.raw,
        hasRaw: !!file.raw,
        isFile: file.raw instanceof File
      });
      
      // 文件选择后的处理
      if (file.raw && file.raw instanceof File) {
        // 创建预览URL
        const reader = new FileReader();
        reader.onload = (e) => {
          file.url = e.target.result;
        };
        reader.readAsDataURL(file.raw);
      }
      this.fileList = fileList;
    },
    handleRemove(file, fileList) {
      this.fileList = fileList;
      this.$message.info(`已移除文件: ${file.name}`);
    },
    handleImageChange(file, fileList) {
      console.log("图片选择:", file, fileList);
      // 图片选择后的处理
      if (file.raw) {
        // 创建预览URL
        const reader = new FileReader();
        reader.onload = (e) => {
          file.url = e.target.result;
        };
        reader.readAsDataURL(file.raw);
      }
      this.imageList = fileList;
    },
    handleImageRemove(file, fileList) {
      this.imageList = fileList;
      this.$message.info(`已移除图片: ${file.name}`);
    },
    handlePictureCardPreview(uploadFile) {
      this.dialogImageUrl = uploadFile.url;
      this.dialogVisible = true;
    },
    handleChange(file, fileList) {
      this.manualFiles = fileList;
    },
    async submitUpload() {
      if (this.fileList.length === 0) {
        this.$message.warning("请先选择要上传的轮播图");
        return;
      }

      this.uploadLoading = true;
      
      try {
        // 调试文件列表
        console.log("文件列表:", this.fileList);
        this.fileList.forEach((file, index) => {
          console.log(`文件 ${index}:`, {
            name: file.name,
            size: file.size,
            raw: file.raw,
            hasRaw: !!file.raw,
            fileType: typeof file,
            keys: Object.keys(file)
          });
        });
        
        // 上传所有选中的文件
        const uploadPromises = this.fileList.map(file => {
          console.log("原始文件对象:", file);
          console.log("文件对象类型:", typeof file);
          console.log("文件对象属性:", Object.keys(file));
          
          // Element Plus upload组件使用 file.raw 存储实际的文件对象
          let fileToUpload = null;
          
          if (file.raw && file.raw instanceof File) {
            fileToUpload = file.raw;
            console.log("使用 file.raw:", fileToUpload);
          } else {
            console.error("无法找到有效的文件对象:", file);
            this.$message.error(`文件 "${file.name}" 无法上传，请重新选择`);
            return Promise.resolve({ code: 500, msg: "文件对象无效" });
          }
          
          console.log("最终使用的文件对象:", {
            name: fileToUpload.name,
            size: fileToUpload.size,
            type: fileToUpload.type,
            constructor: fileToUpload.constructor.name
          });
          
          return bannerApi.uploadBanner(fileToUpload, this.bannerTitle, this.bannerDescription);
        });

        const results = await Promise.all(uploadPromises);
        
        // 检查上传结果
        let successCount = 0;
        let failCount = 0;
        
        results.forEach((result, index) => {
          if (result.code === 200 || result.code === 0) {
            successCount++;
            console.log(`轮播图 ${index + 1} 上传成功:`, result.data);
          } else {
            failCount++;
            console.error(`轮播图 ${index + 1} 上传失败:`, result.msg);
          }
        });

        // 显示结果
        if (successCount > 0) {
          this.$message.success(`成功上传 ${successCount} 个轮播图！`);
        }
        if (failCount > 0) {
          this.$message.error(`${failCount} 个轮播图上传失败`);
        }

        // 清空文件列表和输入框，并刷新轮播图列表
        this.fileList = [];
        this.bannerTitle = '';
        this.bannerDescription = '';
        this.loadBannerList();
        
      } catch (error) {
        console.error("上传异常:", error);
        this.$message.error("上传失败，请重试");
      } finally {
        this.uploadLoading = false;
      }
    },

    /**
     * 加载轮播图列表
     */
    async loadBannerList() {
      this.loading = true;
      try {
        const response = await bannerApi.getBannerList();
        if (response.code === 200 || response.code === 0) {
          this.bannerList = response.data || [];
        }
      } catch (error) {
        console.error("加载轮播图列表失败:", error);
        this.$message.error("加载轮播图列表失败");
      } finally {
        this.loading = false;
      }
    },

    /**
     * 删除轮播图
     */
    async deleteBanner(banner) {
      try {
        await this.$confirm(`确定删除轮播图 "${banner.originalName}" 吗？`, '确认删除', {
          type: 'warning'
        });

        const response = await bannerApi.deleteBanner(banner.id);
        if (response.code === 200 || response.code === 0) {
          this.$message.success("删除成功");
          this.loadBannerList(); // 刷新列表
        } else {
          this.$message.error(response.msg || "删除失败");
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error("删除轮播图失败:", error);
          this.$message.error("删除失败");
        }
      }
    },

    /**
     * 编辑轮播图
     */
    editBanner(banner) {
      this.editingBanner = banner;
      this.editTitle = banner.title || '';
      this.editDescription = banner.description || '';
      this.editDialogVisible = true;
    },

    /**
     * 保存轮播图编辑
     */
    async saveBannerEdit() {
      if (!this.editingBanner) return;

      try {
        const response = await bannerApi.updateBanner(
          this.editingBanner.id, 
          this.editTitle, 
          this.editDescription, 
          this.editingBanner.status
        );

        if (response.code === 200) {
          // 更新本地数据
          this.editingBanner.title = this.editTitle;
          this.editingBanner.description = this.editDescription;
          
          this.$message.success("轮播图信息更新成功");
          this.editDialogVisible = false;
        } else {
          this.$message.error(response.msg || "更新失败");
        }
      } catch (error) {
        console.error("更新轮播图信息失败:", error);
        this.$message.error("更新失败，请重试");
      }
    },

    /**
     * 切换轮播图状态
     */
    async toggleBannerStatus(banner) {
      try {
        const newStatus = banner.status === 'active' ? 'inactive' : 'active';
        const response = await bannerApi.updateBannerStatus(banner.id, newStatus);
        if (response.code === 200) {
          banner.status = newStatus;
          this.$message.success(`轮播图已${newStatus === 'active' ? '启用' : '禁用'}`);
        } else {
          this.$message.error(response.msg || "操作失败");
        }
      } catch (error) {
        console.error("更新轮播图状态失败:", error);
        this.$message.error("操作失败");
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.upload-container {
  padding: 20px;
  
  .card-header {
    font-weight: bold;
  }
  
  .upload-demo {
    h3 {
      margin-bottom: 20px;
      color: #333;
    }
    
    :deep(.el-upload-dragger) {
      width: 100%;
    }
    
    :deep(.el-upload-list__item) {
      transition: none !important;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .banner-card {
    .banner-image {
      position: relative;
      width: 100%;
      height: 150px;
      overflow: hidden;
      border-radius: 8px;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s;
      }
      
      .banner-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.6);
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
        opacity: 0;
        transition: opacity 0.3s;
      }
      
      &:hover {
        img {
          transform: scale(1.1);
        }
        
        .banner-overlay {
          opacity: 1;
        }
      }
    }
    
    .banner-info {
      padding: 10px 0;
      
      .banner-title {
        font-weight: 600;
        font-size: 14px;
        color: #333;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .banner-description {
        font-size: 12px;
        color: #666;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .banner-name {
        font-weight: 500;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-size: 12px;
        color: #999;
      }
      
      .banner-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .banner-size {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
}
</style>