<template>
  <div class="admin-community-page">
    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <el-row :gutter="20">
        <el-col :span="5">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.totalPosts }}</div>
              <div class="stat-label">总帖子数</div>
            </div>
            <i class="el-icon-document stat-icon"></i>
          </el-card>
        </el-col>
        <el-col :span="5">
          <el-card class="stat-card stat-card-warning" @click.native="filterPendingPosts">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.pendingPosts || 0 }}</div>
              <div class="stat-label">待审核</div>
            </div>
            <i class="el-icon-warning stat-icon"></i>
          </el-card>
        </el-col>
        <el-col :span="5">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.activePosts }}</div>
              <div class="stat-label">活跃帖子</div>
            </div>
            <i class="el-icon-check stat-icon"></i>
          </el-card>
        </el-col>
        <el-col :span="5">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.totalComments }}</div>
              <div class="stat-label">总评论数</div>
            </div>
            <i class="el-icon-chat-line-round stat-icon"></i>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.totalFollows }}</div>
              <div class="stat-label">关注关系</div>
            </div>
            <i class="el-icon-user stat-icon"></i>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- 帖子管理 -->
      <el-tab-pane label="帖子管理" name="posts">
        <div class="tab-content">
          <!-- 搜索和筛选 -->
          <div class="search-bar">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-input
                  v-model="postSearch.keyword"
                  placeholder="搜索帖子标题或内容"
                  @keyup.enter="searchPosts"
                >
                  <el-button slot="append" icon="el-icon-search" @click="searchPosts"></el-button>
                </el-input>
              </el-col>
              <el-col :span="4">
                <el-select v-model="postSearch.status" placeholder="状态筛选" clearable>
                  <el-option label="正常" :value="1"></el-option>
                  <el-option label="待审核" :value="2"></el-option>
                  <el-option label="已删除" :value="0"></el-option>
                </el-select>
              </el-col>
              <el-col :span="4">
                <el-select v-model="postSearch.tag" placeholder="标签筛选" clearable>
                  <el-option label="全部" value=""></el-option>
                  <el-option
                    v-for="t in tagOptions"
                    :key="t"
                    :label="t"
                    :value="t"
                  />
                </el-select>
              </el-col>
              <el-col :span="4" class="search-actions">
                <el-button type="primary" @click="searchPosts">搜索</el-button>
                <el-button @click="resetPostSearch">重置</el-button>
                <el-button type="text" @click="openTagDialog">标签管理</el-button>
              </el-col>
            </el-row>
          </div>

          <!-- 帖子列表 -->
          <el-table :data="filteredPosts" row-key="id" v-loading="postsLoading" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip></el-table-column>
            <el-table-column prop="userName" label="作者" width="120"></el-table-column>
            <el-table-column prop="category" label="分类" width="100"></el-table-column>
            <el-table-column prop="likesCount" label="点赞" width="80"></el-table-column>
            <el-table-column prop="commentsCount" label="评论" width="80"></el-table-column>
            <el-table-column prop="viewsCount" label="浏览" width="80"></el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : scope.row.status === 2 ? 'warning' : 'danger'">
                  {{ scope.row.status === 1 ? '正常' : scope.row.status === 2 ? '待审核' : '已删除' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="置顶" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.isTop ? 'warning' : 'info'" size="small">
                  {{ scope.row.isTop ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="热门" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.isHot ? 'danger' : 'info'" size="small">
                  {{ scope.row.isHot ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <div style="display: flex; gap: 8px; align-items: center;">
                  <el-button size="small" type="primary" @click="viewPost(scope.row)">查看</el-button>
                  <el-dropdown @command="(command) => handlePostAction(command, scope.row)">
                    <el-button size="small" type="info">
                      更多<i class="el-icon-arrow-down el-icon--right"></i>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item
                          v-if="scope.row.status === 2"
                          command="approve"
                          icon="el-icon-check"
                        >
                          审核通过
                        </el-dropdown-item>
                        <el-dropdown-item
                          v-if="scope.row.status === 2"
                          command="reject"
                          icon="el-icon-close"
                        >
                          审核拒绝
                        </el-dropdown-item>
                        <el-dropdown-item 
                          :command="scope.row.isTop ? 'unsetTop' : 'setTop'"
                          :icon="scope.row.isTop ? 'el-icon-arrow-up' : 'el-icon-top'"
                        >
                          {{ scope.row.isTop ? '取消置顶' : '设为置顶' }}
                        </el-dropdown-item>
                        <el-dropdown-item 
                          :command="scope.row.isHot ? 'unsetHot' : 'setHot'"
                          :icon="scope.row.isHot ? 'el-icon-star-off' : 'el-icon-star-on'"
                        >
                          {{ scope.row.isHot ? '取消热门' : '设为热门' }}
                        </el-dropdown-item>
                        <el-dropdown-item command="delete" icon="el-icon-delete" divided>
                          删除帖子
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <el-pagination
            @size-change="handlePostSizeChange"
            @current-change="handlePostCurrentChange"
            :current-page="postPagination.current"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="postPagination.size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="postPagination.total"
            style="margin-top: 20px; text-align: right;"
          ></el-pagination>
        </div>
      </el-tab-pane>

      <!-- 宠物专题管理 -->
      <el-tab-pane label="宠物专题管理" name="daily-topics">
        <div class="tab-content">
          <!-- 专题管理 -->
          <div style="margin-bottom: 30px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
              <h3 style="margin: 0;">宠物专题管理</h3>
              <div>
                <el-button type="default" @click="openThemeManagement" style="margin-right: 10px;">主题分类管理</el-button>
                <el-button type="primary" @click="showTopicDialog(null)">
                  <i class="el-icon-plus"></i> 创建专题
                </el-button>
              </div>
            </div>
            
            <!-- 专题列表 -->
            <el-table :data="dailyTopics" row-key="id" v-loading="dailyTopicsLoading" style="width: 100%; margin-top: 20px;">
              <el-table-column prop="id" label="ID" width="80"></el-table-column>
              <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip></el-table-column>
              <el-table-column prop="theme" label="主题" width="120">
                <template #default="scope">
                  {{ getThemeLabel(scope.row.theme) }}
                </template>
              </el-table-column>
              <el-table-column prop="publishDate" label="发布日期" width="120"></el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : scope.row.status === 0 ? 'info' : 'danger'">
                    {{ scope.row.status === 1 ? '已发布' : scope.row.status === 0 ? '草稿' : '已下线' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="viewCount" label="浏览量" width="70"></el-table-column>
              <el-table-column prop="likeCount" label="点赞数" width="70"></el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="150"></el-table-column>
              <el-table-column label="操作" width="300" fixed="right">
                <template #default="scope">
                  <div style="display: flex; gap: 6px; align-items: center; flex-wrap: wrap;">
                    <el-button size="small" type="primary" @click="showTopicDialog(scope.row)">
                      编辑
                    </el-button>
                    <el-button size="small" type="success" v-if="scope.row.status !== 1" @click="publishTopic(scope.row)">
                      发布
                    </el-button>
                    <el-button size="small" type="warning" v-if="scope.row.status === 1" @click="offlineTopic(scope.row)">
                      下线
                    </el-button>
                    <el-button size="small" type="danger" @click="deleteTopic(scope.row)">
                      删除
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 专题分页 -->
            <el-pagination
              @size-change="handleTopicSizeChange"
              @current-change="handleTopicCurrentChange"
              :current-page="topicPagination.current"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="topicPagination.size"
              layout="total, sizes, prev, pager, next, jumper"
              :total="topicPagination.total"
              style="margin-top: 20px; text-align: right;"
            ></el-pagination>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 标签管理对话框 -->
    <el-dialog
      title="标签管理"
      v-model="tagDialogVisible"
      width="480px"
      destroy-on-close
      @open="loadTagOptions"
    >
      <el-form :inline="true" @submit.native.prevent>
        <el-form-item label="新标签">
          <el-input
            v-model="tagForm.name"
            placeholder="例如：猫咪"
            maxlength="20"
            style="width: 220px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="addTag">添加</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tagList" size="small" style="width: 100%; margin-top: 10px;">
        <el-table-column label="标签" min-width="200">
          <template #default="scope">
            {{ scope.row }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button
              type="text"
              size="small"
              style="color:#f56c6c"
              @click="deleteTag(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="tagDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 帖子详情对话框 -->
    <el-dialog title="帖子详情" v-model="postDialogVisible" width="65%" class="post-detail-dialog">
      <div v-if="currentPost" class="post-detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentPost.id }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ currentPost.userName }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentPost.category }}</el-descriptions-item>
          <el-descriptions-item label="点赞数">{{ currentPost.likesCount }}</el-descriptions-item>
          <el-descriptions-item label="评论数">{{ currentPost.commentsCount }}</el-descriptions-item>
          <el-descriptions-item label="浏览数">{{ currentPost.viewsCount }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ currentPost.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="post-detail-section">
          <h4>标题</h4>
          <p class="post-title">{{ currentPost.title || '-' }}</p>
        </div>

        <div class="post-detail-section">
          <h4>内容</h4>
          <p class="post-content">{{ currentPost.content || '-' }}</p>
        </div>

        <div class="post-detail-section" v-if="getPostCoverUrl(currentPost)">
          <h4>封面</h4>
          <el-image
            :src="getPostCoverUrl(currentPost)"
            fit="contain"
            style="width: 100%; max-width: 480px; max-height: 320px; border-radius: 8px; background: #f5f5f5;"
            :preview-src-list="[getPostCoverUrl(currentPost)]"
          />
        </div>

        <div class="post-detail-section" v-if="getPostImages(currentPost).length > 0">
          <h4>图片 ({{ getPostImages(currentPost).length }})</h4>
          <div class="post-images-grid">
            <el-image
              v-for="(img, idx) in getPostImages(currentPost)"
              :key="idx"
              :src="img"
              fit="cover"
              class="post-detail-img"
              :preview-src-list="getPostImages(currentPost)"
              :initial-index="idx"
            />
          </div>
        </div>

        <div class="post-detail-section" v-if="getPostVideos(currentPost).length > 0">
          <h4>视频 ({{ getPostVideos(currentPost).length }})</h4>
          <div class="post-videos-list">
            <div v-for="(v, idx) in getPostVideos(currentPost)" :key="idx" class="post-video-item">
              <video
                v-if="videoSrc(v)"
                :src="videoSrc(v)"
                controls
                class="post-detail-video"
                @error="onPostVideoError(idx)"
              />
              <div v-else class="post-video-placeholder">无有效视频地址</div>
              <div v-if="postVideoErrors[idx]" class="post-video-error-hint">视频加载失败，请检查地址或网络</div>
            </div>
          </div>
        </div>

        <div class="post-detail-section">
          <h4>评论管理</h4>
          <div class="search-bar">
            <el-row :gutter="20">
              <el-col :span="10">
                <el-input
                  v-model="postCommentSearch.keyword"
                  placeholder="搜索该帖评论内容"
                  @keyup.enter="searchPostComments"
                >
                  <el-button slot="append" icon="el-icon-search" @click="searchPostComments"></el-button>
                </el-input>
              </el-col>
              <el-col :span="4">
                <el-select v-model="postCommentSearch.status" placeholder="状态筛选" clearable>
                  <el-option label="正常" :value="1"></el-option>
                  <el-option label="已删除" :value="0"></el-option>
                </el-select>
              </el-col>
              <el-col :span="4">
                <el-button type="primary" @click="searchPostComments">搜索</el-button>
                <el-button @click="resetPostCommentSearch">重置</el-button>
              </el-col>
            </el-row>
          </div>
          <el-table :data="postComments" row-key="id" v-loading="postCommentsLoading" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="content" label="评论内容" min-width="280" show-overflow-tooltip></el-table-column>
            <el-table-column prop="userName" label="评论者" width="120"></el-table-column>
            <el-table-column prop="likesCount" label="点赞" width="80"></el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                  {{ scope.row.status === 1 ? '正常' : '已删除' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160"></el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <div style="display: flex; gap: 8px; align-items: center;">
                  <el-button size="small" @click="viewComment(scope.row)">查看</el-button>
                  <el-button
                    size="small"
                    type="danger"
                    @click="deleteComment(scope.row)"
                  >
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            @size-change="handlePostCommentSizeChange"
            @current-change="handlePostCommentCurrentChange"
            :current-page="postCommentPagination.current"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="postCommentPagination.size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="postCommentPagination.total"
            style="margin-top: 20px; text-align: right;"
          ></el-pagination>
        </div>
      </div>
    </el-dialog>

    <!-- 评论详情对话框 -->
    <el-dialog title="评论详情" v-model="commentDialogVisible" width="50%">
      <div v-if="currentComment">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentComment.id }}</el-descriptions-item>
          <el-descriptions-item label="评论者">{{ currentComment.userName }}</el-descriptions-item>
          <el-descriptions-item label="帖子ID">{{ currentComment.postId }}</el-descriptions-item>
          <el-descriptions-item label="点赞数">{{ currentComment.likesCount }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentComment.createTime }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 20px;">
          <h4>评论内容：</h4>
          <p>{{ currentComment.content }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- 专题编辑对话框 -->
    <el-dialog :title="currentTopic && currentTopic.id ? '编辑专题' : '创建专题'" v-model="topicDialogVisible" width="70%">
      <el-form :model="currentTopic" label-width="120px" v-if="currentTopic">
        <el-form-item label="专题标题" required>
          <el-input v-model="currentTopic.title" placeholder="请输入专题标题"></el-input>
        </el-form-item>
        <el-form-item label="正文内容">
          <div class="topic-content-blocks">
            <p class="topic-content-hint">正文支持文字与图片混排，可自由添加、排序。下方是正文块列表：</p>
            <div v-for="(block, idx) in topicContentBlocks" :key="idx" class="topic-block-item">
              <div class="topic-block-type">{{ block.t === 'text' ? '文字' : '图片' }}</div>
              <div v-if="block.t === 'text'" class="topic-block-body">
                <el-input type="textarea" v-model="block.v" :rows="3" placeholder="输入段落文字"></el-input>
              </div>
              <div v-else class="topic-block-body topic-block-img">
                <el-upload
                  :action="coverUploadAction"
                  :headers="coverUploadHeaders"
                  :show-file-list="false"
                  :before-upload="beforeCoverUpload"
                  accept="image/*"
                  :on-success="(res) => handleContentImageSuccess(res, idx)"
                >
                  <div v-if="block.v" class="topic-block-img-preview">
                    <img :src="normalizeContentImageUrl(block.v)" alt="内容图片" />
                    <span class="topic-block-img-mask">点击替换</span>
                  </div>
                  <div v-else class="topic-block-img-placeholder">
                    <span>+ 上传图片</span>
                  </div>
                </el-upload>
              </div>
              <div class="topic-block-actions">
                <el-button size="small" type="danger" link @click="removeContentBlock(idx)">删除</el-button>
                <el-button size="small" type="primary" link :disabled="idx === 0" @click="moveContentBlock(idx, -1)">上移</el-button>
                <el-button size="small" type="primary" link :disabled="idx === topicContentBlocks.length - 1" @click="moveContentBlock(idx, 1)">下移</el-button>
              </div>
            </div>
            <div class="topic-block-add">
              <el-button size="small" @click="addContentBlock('text')">+ 添加文字</el-button>
              <el-button size="small" @click="addContentBlock('img')">+ 添加图片</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="封面图片" required>
          <div class="topic-cover-upload">
            <el-upload
              class="topic-cover-upload__trigger"
              :action="coverUploadAction"
              :headers="coverUploadHeaders"
              :show-file-list="false"
              :before-upload="beforeCoverUpload"
              :on-success="handleCoverUploadSuccess"
              :on-error="handleCoverUploadError"
              accept="image/*"
            >
              <div v-if="currentTopic.coverImage" class="topic-cover-upload__preview">
                <img :src="currentTopic.coverImage" alt="封面预览" />
                <div class="topic-cover-upload__mask">点击重新上传</div>
              </div>
              <div v-else class="topic-cover-upload__placeholder">
                <span class="topic-cover-upload__icon">+</span>
                <p>上传封面图片</p>
                <small>支持 JPG/PNG，≤5MB</small>
              </div>
            </el-upload>
            <div class="topic-cover-upload__hint">
              推荐尺寸 750×400。如需替换，直接再次点击上传区域。
            </div>
          </div>
        </el-form-item>
        <el-form-item label="主题分类">
          <el-select v-model="currentTopic.theme" placeholder="请选择主题" style="width: 100%;">
            <el-option
              v-for="t in topicThemeList"
              :key="t.code"
              :label="t.name"
              :value="t.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发布日期" required>
          <el-date-picker
            v-model="currentTopic.publishDate"
            type="date"
            placeholder="选择发布日期"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            style="width: 100%;"
            teleported
            popper-class="topic-publish-date-popper"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="currentTopic.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">已发布</el-radio>
            <el-radio :label="2">已下线</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="currentTopic.sortOrder" :min="0"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topicDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTopic">保存</el-button>
      </template>
    </el-dialog>

    <!-- 主题分类管理 -->
    <el-dialog title="主题分类管理" v-model="themeManagementVisible" width="640px" destroy-on-close @open="loadThemeListForManagement">
      <p style="color: #909399; font-size: 12px; margin-bottom: 12px;">在此增删改主题后，创建/编辑专题时的下拉与小程序「宠物专题」Tab 会同步更新。</p>
      <el-table :data="themeListFull" row-key="id" size="small">
        <el-table-column prop="code" label="标识" width="120" />
        <el-table-column prop="name" label="名称" width="140" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="scope">
            <el-button type="text" size="small" @click="editThemeRow(scope.row)">编辑</el-button>
            <el-button type="text" size="small" style="color: #f56c6c;" @click="deleteThemeRow(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 12px;">
        <el-button type="primary" size="small" @click="showAddTheme">添加主题</el-button>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="themeManagementVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加/编辑主题 -->
    <el-dialog :title="editingTheme ? '编辑主题' : '添加主题'" v-model="themeFormVisible" width="400px" destroy-on-close>
      <el-form :model="themeForm" label-width="80px">
        <el-form-item label="标识" required>
          <el-input v-model="themeForm.code" placeholder="如 recommend、basic" :disabled="!!editingTheme" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="themeForm.name" placeholder="如 推荐、基本知识" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="themeForm.sortOrder" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="状态" v-if="editingTheme">
          <el-radio-group v-model="themeForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="themeFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTheme">保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script>
import { getCommunityStatistics, getAdminPosts, getAdminComments, updatePostStatus, updatePostTop, updatePostHot, deletePost as deletePostApi, deleteComment, getDailyTopicsList, getDailyTopicDetail, createDailyTopic, updateDailyTopic, deleteDailyTopic, publishDailyTopic, offlineDailyTopic, getDailyTopicThemes, createDailyTopicTheme, updateDailyTopicTheme, deleteDailyTopicTheme } from '@/api/admin'
import { tagApi } from '@/api/tag'
import { baseURL } from '@/config'

export default {
  name: 'AdminCommunity',
  data() {
    return {
      activeTab: 'posts',
      statistics: {
        totalPosts: 0,
        activePosts: 0,
        totalComments: 0,
        totalFollows: 0
      },
      
      // 帖子相关
      posts: [],
      postsLoading: false,
      postSearch: {
        keyword: '',
        status: null,
        tag: ''
      },
      postPagination: {
        current: 1,
        size: 10,
        total: 0
      },
      // 帖子详情内评论管理
      postComments: [],
      postCommentsLoading: false,
      postCommentSearch: {
        keyword: '',
        status: null
      },
      postCommentPagination: {
        current: 1,
        size: 10,
        total: 0
      },
      
      // 对话框
      postDialogVisible: false,
      commentDialogVisible: false,
      currentPost: null,
      currentComment: null,
      postVideoErrors: {},
      
      // 专题管理
      dailyTopics: [],
      dailyTopicsLoading: false,
      topicPagination: {
        current: 1,
        size: 10,
        total: 0
      },
      topicDialogVisible: false,
      currentTopic: null,
      /** 主题分类（仅启用，用于专题表单下拉与表格展示） */
      topicThemeList: [],
      /** 主题分类管理弹窗 */
      themeManagementVisible: false,
      themeListFull: [],
      themeFormVisible: false,
      editingTheme: null,
      themeForm: { code: '', name: '', sortOrder: 0, status: 1 },
      
      // 封面上传
      coverUploadAction: '',
      coverUploadHeaders: {},
      // 专题正文内容块 [{t:'text',v:'...'},{t:'img',v:'url'}]
      topicContentBlocks: [],

      // 标签管理
      tagOptions: [],
      tagDialogVisible: false,
      tagList: [],
      tagForm: { name: '' }
    }
  },
  computed: {
    // 按标签在前端过滤帖子（仅管理界面用，方便你的毕设演示）
    filteredPosts() {
      const tag = (this.postSearch.tag || '').trim()
      if (!tag) return this.posts
      return (this.posts || []).filter(post => {
        const tagsStr = (post.tags || '').toString()
        if (!tagsStr) return false
        return tagsStr.split(',').map(t => t.trim()).includes(tag)
      })
    }
  },
  
  mounted() {
    this.coverUploadAction = this.resolveCoverUploadAction()
    this.refreshCoverUploadHeaders()
    this.loadStatistics()
    this.loadTabContentByName(this.activeTab)
    this.loadTagOptions()
  },
  
  methods: {
    async loadTagOptions() {
      try {
        const res = await tagApi.getAllTags()
        const data = (res && (res.data || res)) || []
        const list = Array.isArray(data) ? data : []
        this.tagOptions = list
        this.tagList = list
      } catch (e) {
        console.error('加载标签列表失败:', e)
        this.tagOptions = []
        this.tagList = []
      }
    },
    openTagDialog() {
      this.tagDialogVisible = true
      if (!this.tagList || this.tagList.length === 0) {
        this.loadTagOptions()
      }
    },
    async addTag() {
      const name = (this.tagForm.name || '').trim()
      if (!name) {
        this.$message.warning('请输入标签名称')
        return
      }
      try {
        await tagApi.createTag({ name })
        this.$message.success('添加成功')
        this.tagForm.name = ''
        await this.loadTagOptions()
      } catch (e) {
        console.error('添加标签失败:', e)
        this.$message.error(e?.response?.data?.msg || e?.message || '添加失败')
      }
    },
    async deleteTag(name) {
      if (!name) return
      try {
        await this.$confirm(`确定删除标签「${name}」吗？`, '确认删除', { type: 'warning' })
        await tagApi.deleteTag(name)
        this.$message.success('删除成功')
        await this.loadTagOptions()
      } catch (e) {
        if (e !== 'cancel') {
          console.error('删除标签失败:', e)
          this.$message.error(e?.response?.data?.msg || e?.message || '删除失败')
        }
      }
    },
    resolveActiveTabName(tab) {
      if (!tab) {
        return this.activeTab
      }
      if (typeof tab === 'string') {
        return tab
      }
      if (tab.paneName) {
        return tab.paneName
      }
      if (tab.props && tab.props.name) {
        return tab.props.name
      }
      if (tab.name) {
        return tab.name
      }
      return this.activeTab
    },

    async loadTabContentByName(tabName = this.activeTab) {
      if (tabName === 'posts') {
        await this.loadPosts()
        return
      }
      if (tabName === 'daily-topics') {
        await this.loadTopicThemes()
        await this.loadDailyTopics()
      }
    },

    // 加载统计数据
    async loadStatistics() {
      try {
        const response = await getCommunityStatistics()
        if (response.code === 0 || response.code === 200) {
          this.statistics = response.data
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },
    
    // 刷新数据
    async refreshData() {
      await this.loadStatistics()
      await this.loadTabContentByName(this.activeTab)
      this.$message.success('数据已刷新')
    },
    
    // 标签页切换
    handleTabClick(tab) {
      const tabName = this.resolveActiveTabName(tab)
      this.loadTabContentByName(tabName)
    },
    
    // ==================== 帖子管理 ====================
    
    async loadPosts() {
      this.postsLoading = true
      try {
        const params = {
          page: this.postPagination.current,
          size: this.postPagination.size,
          keyword: this.postSearch.keyword || undefined,
          status: this.postSearch.status,
          // 仍然把原有 category 字段传给后端，避免破坏原有接口
          category: undefined
        }
        
        const response = await getAdminPosts(params)
        if (response.code === 0 || response.code === 200) {
          this.posts = response.data.posts || []
          this.postPagination.total = response.data.total || 0
        }
      } catch (error) {
        console.error('加载帖子列表失败:', error)
        this.$message.error('加载帖子列表失败')
      } finally {
        this.postsLoading = false
      }
    },
    
    async searchPosts() {
      this.postPagination.current = 1
      await this.loadPosts()
    },
    filterPendingPosts() {
      this.activeTab = 'posts'
      this.postSearch.status = 2
      this.postPagination.current = 1
      this.loadPosts()
    },
    
    resetPostSearch() {
      this.postSearch.keyword = ''
      this.postSearch.status = null
      this.postSearch.tag = ''
      this.postPagination.current = 1
      this.loadPosts()
    },
    
    handlePostSizeChange(size) {
      this.postPagination.size = size
      this.postPagination.current = 1
      this.loadPosts()
    },
    
    handlePostCurrentChange(current) {
      this.postPagination.current = current
      this.loadPosts()
    },

    
    handlePostAction(command, post) {
      switch (command) {
        case 'approve':
          this.approvePost(post)
          break
        case 'reject':
          this.rejectPost(post)
          break
        case 'setTop':
        case 'unsetTop':
          this.togglePostTop(post)
          break
        case 'setHot':
        case 'unsetHot':
          this.togglePostHot(post)
          break
        case 'delete':
          this.deletePost(post)
          break
      }
    },
    async approvePost(post) {
      try {
        const response = await updatePostStatus(post.id, 1)
        if (response.code === 0 || response.code === 200) {
          post.status = 1
          this.$message.success('审核通过')
          this.loadStatistics()
          this.loadPosts()
        } else {
          this.$message.error(response.msg || '操作失败')
        }
      } catch (error) {
        console.error('审核失败:', error)
        this.$message.error('审核失败')
      }
    },
    async rejectPost(post) {
      try {
        await this.$confirm('确定要拒绝该帖子吗？拒绝后帖子将不再展示。', '审核拒绝', {
          confirmButtonText: '确定拒绝',
          cancelButtonText: '取消',
          type: 'warning'
        })
        const response = await updatePostStatus(post.id, 0)
        if (response.code === 0 || response.code === 200) {
          post.status = 0
          this.$message.success('已拒绝')
          this.loadStatistics()
          this.loadPosts()
        } else {
          this.$message.error(response.msg || '操作失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('操作失败:', error)
          this.$message.error('操作失败')
        }
      }
    },
    
    viewPost(post) {
      this.currentPost = post
      this.postVideoErrors = {}
      this.postCommentSearch = { keyword: '', status: null }
      this.postCommentPagination.current = 1
      this.postCommentPagination.size = 10
      this.postCommentPagination.total = 0
      this.postComments = []
      this.postDialogVisible = true
      this.loadPostComments()
    },
    videoSrc(v) {
      if (!v) return ''
      const url = (v && (v.url || v.src)) ? (v.url || v.src) : (typeof v === 'string' ? v : '')
      return url ? this.resolvePostMediaUrl(url) : ''
    },
    onPostVideoError(idx) {
      this.postVideoErrors = { ...this.postVideoErrors, [idx]: true }
    },
    resolvePostMediaUrl(url) {
      if (!url || typeof url !== 'string') return ''
      const base = this.resolveUploadBaseOrigin()
      if (/^https?:\/\//i.test(url)) {
        try {
          const u = new URL(url)
          const pathLower = (u.pathname || '').toLowerCase()
          if (pathLower.includes('/video') || /\.(mp4|webm|mov|m3u8)(\?|$)/i.test(u.pathname || '')) {
            return url
          }
          return base + u.pathname
        } catch {
          return url
        }
      }
      let path = url.trim()
      if (!path) return ''
      if (path.startsWith('/') && (path.startsWith('/upload') || path.startsWith('/static'))) {
        return base + path
      }
      if (!path.includes('/')) {
        return base + '/upload/images/' + path.replace(/^\/+/, '')
      }
      if (!path.startsWith('/')) path = '/' + path.replace(/^\/+/, '')
      if (!path.startsWith('/upload') && !path.startsWith('/static')) {
        path = '/upload' + (path.startsWith('/') ? path : '/' + path)
      }
      return base + path
    },
    getPostCoverUrl(post) {
      if (!post) return ''
      const url = post.coverImage || post.cover_image
      if (url) return this.resolvePostMediaUrl(url)
      const imgs = this.getPostImages(post)
      if (imgs.length > 0) return imgs[0]
      const vids = this.getPostVideos(post)
      if (vids.length > 0 && vids[0].thumb) return this.resolvePostMediaUrl(vids[0].thumb)
      return ''
    },
    isVideoUrl(url) {
      if (!url || typeof url !== 'string') return false
      return /\.(mp4|webm|mov|avi|mkv|m3u8)(\?|$)/i.test(url) || /\/videos?\//i.test(url)
    },
    getPostImages(post) {
      if (!post) return []
      let raw = post.images || post.imageUrls || ''
      if (!raw || typeof raw !== 'string') return []
      const resolve = (u) => u ? this.resolvePostMediaUrl(u) : ''
      let urls = []
      try {
        if (raw.trim().startsWith('[')) {
          const arr = JSON.parse(raw)
          urls = (Array.isArray(arr) ? arr : []).map(u => {
            const url = typeof u === 'string' ? u : (u && (u.url || u.src)) || ''
            return resolve(url)
          }).filter(Boolean)
        } else {
          urls = raw.split(',').map(s => s.trim()).filter(Boolean).map(resolve)
        }
      } catch {
        urls = raw.split(',').map(s => s.trim()).filter(Boolean).map(resolve)
      }
      return urls.filter(u => !this.isVideoUrl(u))
    },
    getPostVideos(post) {
      if (!post) return []
      const raw = post.videos
      if (!raw || typeof raw !== 'string') return []
      try {
        const arr = JSON.parse(raw)
        if (!Array.isArray(arr)) return []
        return arr.map((v) => {
          if (v && typeof v === 'object' && (v.url != null || v.src != null)) {
            return { url: v.url || v.src || '', thumb: v.thumb || v.customThumb }
          }
          if (typeof v === 'string' && v.trim()) return { url: v.trim(), thumb: '' }
          return null
        }).filter(Boolean)
      } catch {
        return []
      }
    },
    
    async togglePostTop(post) {
      try {
        const response = await updatePostTop(post.id, !post.isTop)
        if (response.code === 0 || response.code === 200) {
          post.isTop = !post.isTop
          this.$message.success(post.isTop ? '已设为置顶' : '已取消置顶')
        }
      } catch (error) {
        console.error('操作失败:', error)
        this.$message.error('操作失败')
      }
    },
    
    async togglePostHot(post) {
      try {
        const response = await updatePostHot(post.id, !post.isHot)
        if (response.code === 0 || response.code === 200) {
          post.isHot = !post.isHot
          this.$message.success(post.isHot ? '已设为热门' : '已取消热门')
        }
      } catch (error) {
        console.error('操作失败:', error)
        this.$message.error('操作失败')
      }
    },
    
    async deletePost(post) {
      try {
        await this.$confirm(
          '确定要从数据库永久删除该帖子吗？相关评论与互动将一并清除，且不可恢复。',
          '永久删除帖子',
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        const response = await deletePostApi(post.id)
        if (response.code === 0 || response.code === 200) {
          this.$message.success('删除成功')
          await this.loadStatistics()
          if (this.activeTab === 'posts') await this.loadPosts()
        } else {
          this.$message.error(response.msg || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }
    },
    
    // ==================== 帖子详情内评论管理 ====================

    async loadPostComments() {
      if (!this.currentPost || !this.currentPost.id) return
      this.postCommentsLoading = true
      try {
        const params = {
          page: this.postCommentPagination.current,
          size: this.postCommentPagination.size,
          keyword: this.postCommentSearch.keyword || undefined,
          status: this.postCommentSearch.status,
          postId: this.currentPost.id
        }

        const response = await getAdminComments(params)
        if (response.code === 0 || response.code === 200) {
          this.postComments = response.data.comments || []
          this.postCommentPagination.total = response.data.total || 0
        }
      } catch (error) {
        console.error('加载帖子评论列表失败:', error)
        this.$message.error('加载帖子评论列表失败')
      } finally {
        this.postCommentsLoading = false
      }
    },

    async searchPostComments() {
      this.postCommentPagination.current = 1
      await this.loadPostComments()
    },

    resetPostCommentSearch() {
      this.postCommentSearch = {
        keyword: '',
        status: null
      }
      this.postCommentPagination.current = 1
      this.loadPostComments()
    },

    handlePostCommentSizeChange(size) {
      this.postCommentPagination.size = size
      this.postCommentPagination.current = 1
      this.loadPostComments()
    },

    handlePostCommentCurrentChange(current) {
      this.postCommentPagination.current = current
      this.loadPostComments()
    },
    
    viewComment(comment) {
      this.currentComment = comment
      this.commentDialogVisible = true
    },
    
    async deleteComment(comment) {
      try {
        await this.$confirm('确定要删除这个评论吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const response = await deleteComment(comment.id)
        if (response.code === 0 || response.code === 200) {
          comment.status = 0
          this.$message.success('删除成功')
          await this.loadPostComments()
          await this.loadStatistics()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }
    },
    
    // ==================== 每日专题管理 ====================
    
    getThemeLabel(theme) {
      if (!theme) return '-'
      const t = this.topicThemeList.find(x => x.code === theme)
      return t ? t.name : theme
    },

    async loadTopicThemes() {
      try {
        const res = await getDailyTopicThemes()
        if (res && (res.code === 0 || res.code === 200) && Array.isArray(res.data)) {
          this.topicThemeList = res.data.filter(x => x.status === 1)
        } else {
          this.topicThemeList = []
        }
      } catch (e) {
        console.error('加载主题分类失败', e)
        this.topicThemeList = []
      }
    },

    openThemeManagement() {
      this.themeManagementVisible = true
    },
    async loadThemeListForManagement() {
      try {
        const res = await getDailyTopicThemes()
        if (res && (res.code === 0 || res.code === 200) && Array.isArray(res.data)) {
          this.themeListFull = res.data
        } else {
          this.themeListFull = []
        }
      } catch (e) {
        console.error('加载主题列表失败', e)
        this.themeListFull = []
      }
    },
    showAddTheme() {
      this.editingTheme = null
      this.themeForm = { code: '', name: '', sortOrder: this.themeListFull.length, status: 1 }
      this.themeFormVisible = true
    },
    editThemeRow(row) {
      this.editingTheme = row
      this.themeForm = { code: row.code, name: row.name, sortOrder: row.sortOrder ?? 0, status: row.status ?? 1 }
      this.themeFormVisible = true
    },
    async saveTheme() {
      if (!this.themeForm.name || !this.themeForm.name.trim()) {
        this.$message.warning('请填写名称')
        return
      }
      if (!this.editingTheme && (!this.themeForm.code || !this.themeForm.code.trim())) {
        this.$message.warning('请填写标识')
        return
      }
      try {
        if (this.editingTheme) {
          await updateDailyTopicTheme(this.editingTheme.id, {
            name: this.themeForm.name.trim(),
            sortOrder: this.themeForm.sortOrder,
            status: this.themeForm.status
          })
          this.$message.success('更新成功')
        } else {
          await createDailyTopicTheme({
            code: this.themeForm.code.trim(),
            name: this.themeForm.name.trim(),
            sortOrder: this.themeForm.sortOrder ?? 0,
            status: 1
          })
          this.$message.success('添加成功')
        }
        this.themeFormVisible = false
        await this.loadThemeListForManagement()
        await this.loadTopicThemes()
      } catch (err) {
        this.$message.error(err?.response?.data?.msg || err?.message || '操作失败')
      }
    },
    deleteThemeRow(row) {
      this.$confirm(`确定删除主题「${row.name}」吗？`, '确认删除', { type: 'warning' }).then(async () => {
        try {
          await deleteDailyTopicTheme(row.id)
          this.$message.success('删除成功')
          await this.loadThemeListForManagement()
          await this.loadTopicThemes()
        } catch (err) {
          this.$message.error(err?.response?.data?.msg || err?.message || '删除失败')
        }
      }).catch(() => {})
    },

    // 加载专题列表
    async loadDailyTopics() {
      this.dailyTopicsLoading = true
      try {
        const params = {
          page: this.topicPagination.current,
          size: this.topicPagination.size
        }
        const response = await getDailyTopicsList(params)
        if (response.code === 0 || response.code === 200) {
          this.dailyTopics = response.data.topics || []
          this.topicPagination.total = response.data.total || 0
        }
      } catch (error) {
        console.error('加载专题列表失败:', error)
        this.$message.error('加载专题列表失败')
      } finally {
        this.dailyTopicsLoading = false
      }
    },
    
    handleTopicSizeChange(size) {
      this.topicPagination.size = size
      this.topicPagination.current = 1
      this.loadDailyTopics()
    },
    
    handleTopicCurrentChange(current) {
      this.topicPagination.current = current
      this.loadDailyTopics()
    },
    
    // 显示专题编辑对话框
    async showTopicDialog(topic) {
      if (topic) {
        // 编辑模式，加载详情
        try {
          const response = await getDailyTopicDetail(topic.id)
          if (response.code === 0 || response.code === 200) {
            this.currentTopic = { ...response.data }
            // 解析正文内容块
            this.topicContentBlocks = this.parseTopicContent(response.data.content)
          }
        } catch (error) {
          console.error('加载专题详情失败:', error)
          this.$message.error('加载专题详情失败')
          return
        }
      } else {
        // 新建模式
        this.currentTopic = {
          title: '',
          description: '',
          content: '',
          coverImage: '',
          theme: '',
          publishDate: new Date().toISOString().split('T')[0],
          status: 0,
          sortOrder: 0
        }
        this.topicContentBlocks = []
      }
      this.topicDialogVisible = true
    },
    parseTopicContent(content) {
      if (!content || typeof content !== 'string') return []
      try {
        const arr = JSON.parse(content)
        return Array.isArray(arr) ? arr.filter(b => b && (b.t === 'text' || b.t === 'img')) : []
      } catch {
        return []
      }
    },
    addContentBlock(type) {
      this.topicContentBlocks.push({ t: type, v: type === 'text' ? '' : '' })
    },
    removeContentBlock(idx) {
      this.topicContentBlocks.splice(idx, 1)
    },
    moveContentBlock(idx, delta) {
      const newIdx = idx + delta
      if (newIdx < 0 || newIdx >= this.topicContentBlocks.length) return
      const tmp = this.topicContentBlocks[idx]
      this.topicContentBlocks.splice(idx, 1)
      this.topicContentBlocks.splice(newIdx, 0, tmp)
    },
    handleContentImageSuccess(response, idx) {
      if (!response || (response.code !== 0 && response.code !== 200)) {
        this.$message.error((response && response.msg) || '图片上传失败')
        return
      }
      // 支持 data 为字符串或对象 { url/filePath/fileUrl }
      let url = null
      const d = response.data
      if (typeof d === 'string' && d) {
        url = d
      } else if (d && typeof d === 'object') {
        url = d.url || d.filePath || d.fileUrl || (typeof d === 'string' ? d : null)
      }
      if (!url) {
        this.$message.error('未能获取图片地址')
        return
      }
      url = this.normalizeCoverImageUrl(url)
      // 使用 splice 确保 Vue 正确追踪更新并刷新预览
      const block = this.topicContentBlocks[idx]
      this.topicContentBlocks.splice(idx, 1, { t: block.t, v: url })
      this.$message.success('图片上传成功')
    },
    normalizeContentImageUrl(url) {
      return this.normalizeCoverImageUrl(url)
    },

    resolveUploadBaseOrigin() {
      if (baseURL) {
        const u = baseURL.replace(/\/api\/?$/, '')
        if (u && u.startsWith('http')) return u
      }
      if (typeof window === 'undefined') return 'http://localhost:8080'
      const { protocol, hostname } = window.location
      return `${protocol}//${hostname}:8080`
    },

    resolveCoverUploadAction() {
      return `${this.resolveUploadBaseOrigin()}/api/upload/image`
    },

    refreshCoverUploadHeaders() {
      const token =
        (this.$store && this.$store.state && this.$store.state.user && this.$store.state.user.accessToken) ||
        localStorage.getItem('vue-admin-better-2024') ||
        sessionStorage.getItem('vue-admin-better-2024') ||
        localStorage.getItem('token') ||
        ''
      this.coverUploadHeaders = token ? { Authorization: `Bearer ${token}` } : {}
    },

    beforeCoverUpload(file) {
      this.refreshCoverUploadHeaders()
      const isImage = file && file.type && file.type.startsWith('image/')
      const isLt5M = file && file.size / 1024 / 1024 < 5
      if (!isImage) {
        this.$message.error('仅支持上传图片文件')
      }
      if (!isLt5M) {
        this.$message.error('图片大小不能超过5MB')
      }
      return isImage && isLt5M
    },

    handleCoverUploadSuccess(response) {
      if ((response.code === 0 || response.code === 200) && response.data) {
        if (!this.currentTopic) {
          this.currentTopic = {}
        }
        this.currentTopic.coverImage = this.normalizeCoverImageUrl(response.data)
        this.$message.success('封面上传成功')
      } else {
        this.$message.error((response && response.msg) || '封面上传失败')
      }
    },

    handleCoverUploadError(error) {
      console.error('封面上传失败:', error)
      this.$message.error('封面上传失败，请稍后重试')
    },

    /** Element Plus 日期绑定为 YYYY-MM-DD 字符串，提交前再规范为后端可解析的 ISO 日期 */
    formatPublishDateForApi(value) {
      if (value == null || value === '') {
        const d = new Date()
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
      }
      if (value instanceof Date) {
        const y = value.getFullYear()
        const m = String(value.getMonth() + 1).padStart(2, '0')
        const day = String(value.getDate()).padStart(2, '0')
        return `${y}-${m}-${day}`
      }
      const s = String(value).trim()
      const m = s.match(/^(\d{4})-(\d{2})-(\d{2})/)
      if (m) return `${m[1]}-${m[2]}-${m[3]}`
      return s
    },

    normalizeCoverImageUrl(url) {
      if (!url) return ''
      // 开发环境：后端返回 https://localhost:8443/upload/... 自签名证书导致 ERR_CERT_AUTHORITY_INVALID
      // 转为相对路径 /upload/... 走 dev 代理访问 HTTP 后端，图片才能正常加载
      try {
        if (typeof url === 'string' && /^https?:\/\//i.test(url)) {
          const u = new URL(url)
          if ((u.hostname === 'localhost' || u.hostname === '127.0.0.1') && u.pathname.startsWith('/upload/')) {
            return u.pathname
          }
        }
      } catch (e) {}
      if (/^https?:\/\//i.test(url)) return url
      if (url.startsWith('/')) {
        return `${this.resolveUploadBaseOrigin()}${url}`
      }
      return `${this.resolveUploadBaseOrigin()}/${url.replace(/^\/+/, '')}`
    },
    
    // 保存专题
    async saveTopic() {
      try {
        const contentJson = this.topicContentBlocks.length > 0
          ? JSON.stringify(this.topicContentBlocks)
          : ''
        const data = {
          title: this.currentTopic.title,
          description: this.currentTopic.description || '',
          content: contentJson,
          coverImage: this.currentTopic.coverImage,
          theme: this.currentTopic.theme,
          publishDate: this.formatPublishDateForApi(this.currentTopic.publishDate),
          status: this.currentTopic.status,
          sortOrder: this.currentTopic.sortOrder,
          postIds: []
        }
        
        let response
        if (this.currentTopic.id) {
          // 更新
          response = await updateDailyTopic(this.currentTopic.id, data)
        } else {
          // 创建
          response = await createDailyTopic(data)
        }
        
        if (response.code === 0 || response.code === 200) {
          this.$message.success(this.currentTopic.id ? '更新成功' : '创建成功')
          this.topicDialogVisible = false
          this.loadDailyTopics()
        }
      } catch (error) {
        console.error('保存专题失败:', error)
        const message = this.resolveApiErrorMessage(error, '保存专题失败，请稍后再试')
        this.$message.error(message)
      }
    },
    
    // 发布专题
    async publishTopic(topic) {
      try {
        await this.$confirm('确定要发布这个专题吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const response = await publishDailyTopic(topic.id)
        if (response.code === 0 || response.code === 200) {
          this.$message.success('发布成功')
          this.loadDailyTopics()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('发布失败:', error)
          this.$message.error('发布失败')
        }
      }
    },
    
    // 下线专题
    async offlineTopic(topic) {
      try {
        await this.$confirm('确定要下线这个专题吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const response = await offlineDailyTopic(topic.id)
        if (response.code === 0 || response.code === 200) {
          this.$message.success('下线成功')
          this.loadDailyTopics()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('下线失败:', error)
          this.$message.error('下线失败')
        }
      }
    },
    
    // 删除专题
    async deleteTopic(topic) {
      try {
        await this.$confirm('确定要删除这个专题吗？删除后无法恢复！', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const response = await deleteDailyTopic(topic.id)
        if (response.code === 0 || response.code === 200) {
          this.$message.success('删除成功')
          this.loadDailyTopics()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }
    },

    resolveApiErrorMessage(error, fallback = '操作失败') {
      if (!error) {
        return fallback
      }
      if (typeof error === 'string') {
        const match = error.match(/\{.*\}$/)
        if (match) {
          try {
            const payload = JSON.parse(match[0])
            if (payload && payload.msg) {
              return payload.msg
            }
          } catch (e) {
            console.warn('解析接口错误信息失败:', e)
          }
        }
        return error
      }
      if (error.msg) {
        return error.msg
      }
      if (error.message) {
        return error.message
      }
      if (error.response && error.response.data && error.response.data.msg) {
        return error.response.data.msg
      }
      return fallback
    }
  }
}
</script>

<style scoped>
.admin-community-page {
  padding: 20px;
}

.statistics-cards {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card .stat-content {
  position: relative;
  z-index: 2;
}

.stat-card .stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-card .stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-card .stat-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40px;
  color: #409EFF;
  opacity: 0.3;
}

.stat-card-warning {
  cursor: pointer;
}

.stat-card-warning .stat-number {
  color: #E6A23C;
}

.stat-card-warning .stat-icon {
  color: #E6A23C;
}

/* 帖子详情对话框 */
.post-detail-content .post-detail-section {
  margin-top: 20px;
}

.post-detail-content {
  max-width: 980px;
  margin: 0 auto;
  max-height: 72vh;
  overflow-y: auto;
  padding-right: 6px;
}

.post-detail-content .post-detail-section h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

.post-detail-content .post-title {
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  word-break: break-word;
}

.post-detail-content .post-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  margin: 0;
  color: #303133;
}

.post-images-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.post-detail-img {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  cursor: pointer;
}

.post-videos-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 15px;
}

.post-detail-video {
  width: 100%;
  max-height: 460px;
  border-radius: 8px;
  background: #000;
  display: block;
}

.post-video-placeholder {
  padding: 40px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #909399;
  text-align: center;
}
.post-video-error-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #f56c6c;
}

.tab-content {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.topic-cover-upload {
  display: flex;
  flex-direction: column;
}

.topic-cover-upload__trigger {
  width: 220px;
}

.topic-cover-upload__trigger :deep(.el-upload) {
  width: 200px;
  height: 140px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  transition: border-color 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.topic-cover-upload__trigger :deep(.el-upload):hover {
  border-color: #409eff;
}

.topic-cover-upload__placeholder {
  text-align: center;
  color: #909399;
}

.topic-cover-upload__icon {
  display: inline-block;
  font-size: 28px;
  color: #409eff;
  line-height: 1;
}

.topic-cover-upload__preview {
  position: relative;
  width: 200px;
  height: 140px;
  overflow: hidden;
}

.topic-cover-upload__preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.topic-cover-upload__mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.topic-cover-upload__preview:hover .topic-cover-upload__mask {
  opacity: 1;
}

.topic-content-blocks { border: 1px solid #dcdfe6; border-radius: 6px; padding: 12px; background: #fafafa; }
.topic-content-hint { margin: 0 0 12px 0; font-size: 13px; color: #606266; }
.topic-block-item { display: flex; flex-wrap: wrap; align-items: flex-start; gap: 8px; margin-bottom: 12px; padding: 10px; background: #fff; border: 1px solid #ebeef5; border-radius: 4px; }
.topic-block-type { flex: 0 0 48px; font-size: 12px; color: #909399; }
.topic-block-body { flex: 1; min-width: 200px; }
.topic-block-body.topic-block-img .el-upload { display: block; }
.topic-block-img-preview { position: relative; width: 200px; height: 120px; border: 1px dashed #dcdfe6; border-radius: 4px; overflow: hidden; }
.topic-block-img-preview img { width: 100%; height: 100%; object-fit: cover; }
.topic-block-img-mask { position: absolute; inset: 0; background: rgba(0,0,0,0.4); color: #fff; font-size: 12px; display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.2s; }
.topic-block-img-preview:hover .topic-block-img-mask { opacity: 1; }
.topic-block-img-placeholder { width: 200px; height: 80px; border: 1px dashed #dcdfe6; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #909399; font-size: 13px; cursor: pointer; }
.topic-block-img-placeholder:hover { border-color: #409eff; color: #409eff; }
.topic-block-actions { flex: 0 0 auto; display: flex; flex-direction: column; gap: 4px; }
.topic-block-add { margin-top: 8px; }

.topic-cover-upload__hint {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

/* 日期面板需高于「创建/编辑专题」对话框，否则可能点不到或显示异常 */
:deep(.topic-publish-date-popper.el-picker__popper) {
  z-index: 5000 !important;
}

.search-bar {
  margin-bottom: 20px;
}

.post-detail-section .search-bar {
  margin-top: 12px;
}

.el-table {
  margin-bottom: 20px;
}
</style>
