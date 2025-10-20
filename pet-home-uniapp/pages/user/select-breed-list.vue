<template>
  <view class="select-breed-list-container">
    <!-- 自定义白色导航栏 -->
    <view class="custom-white-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <view class="navbar-title">
          <text>选择品种</text>
        </view>
        <view class="navbar-right"></view>
      </view>
    </view>

    <!-- 页面内容 -->
    <scroll-view
      scroll-y
      class="breed-list-scroll-content"
      :style="{ paddingTop: navBarTotalHeight + 'px' }"
      :scroll-into-view="scrollViewId"
      scroll-with-animation
    >
      <!-- 搜索框 -->
      <view class="search-section">
        <view class="search-input-container">
          <text class="search-icon">🔍</text>
          <input
            class="search-input"
            type="text"
            v-model="searchText"
            placeholder="点击搜索品种"
            @input="onSearchInput"
          />
        </view>
      </view>

      <!-- 热门品种 -->
      <view class="hot-breeds-section" v-if="!searchText || filteredHotBreeds.length > 0">
        <view class="section-title">热门品种</view>
        <view class="hot-breeds-grid">
          <view
            class="breed-tag"
            v-for="(breed, index) in filteredHotBreeds"
            :key="index"
            @click="selectBreed(breed)"
          >
            <text>{{ breed }}</text>
          </view>
        </view>
      </view>

      <!-- 按字母排序的品种列表 -->
      <view class="alphabetical-breeds-section">
        <view v-for="(group, index) in filteredAlphabeticalBreeds" :key="index">
          <view class="initial-group" :id="'initial-' + group.initial">
            <view class="initial-header">{{ group.initial }}</view>
            <view
              class="breed-item"
              v-for="(breed, bIndex) in group.breeds"
              :key="bIndex"
              @click="selectBreed(breed)"
            >
              <text>{{ breed }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 右侧字母索引 -->
    <view class="alphabet-index" :style="{ paddingTop: navBarTotalHeight + 'px' }">
      <view
        class="index-item"
        v-for="(initial, index) in alphabetIndex"
        :key="index"
        @click="scrollToInitial(initial)"
      >
        <text :class="{ 'active': activeInitial === initial }">{{ initial }}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,
      searchText: '',
      petType: '', // 当前选择的宠物类型
      hotBreeds: [],
      allBreeds: [],
      // 猫咪品种数据
      catBreeds: [
        { initial: 'A', breeds: ['阿比西尼亚猫', '埃及猫', '奥西猫', '矮脚蓝猫', '矮脚蓝白'] },
        { initial: 'B', breeds: ['白猫', '巴厘猫', '波米拉猫', '波斯猫', '伯曼猫', '布偶猫'] },
        { initial: 'C', breeds: ['串串', '长毛蓝白', '长毛虎斑', '长毛银渐层', '长毛金渐层', '长毛紫金渐层', '长毛蓝猫'] },
        { initial: 'D', breeds: ['玳瑁猫', '德文卷毛猫', '东方短毛猫', '东方猫', '东奇尼猫'] },
        { initial: 'E', breeds: ['俄罗斯蓝猫', '高地猫'] },
        { initial: 'H', breeds: ['黑猫', '黑足猫', '哈瓦那棕猫', '呵叻猫', '虎斑暹罗'] },
        { initial: 'J', breeds: ['加菲猫', '简州猫', '加拿大无毛猫', '金吉拉', '橘猫', '金渐层虎斑'] },
        { initial: 'K', breeds: ['柯尼思卷毛猫'] },
        { initial: 'L', breeds: ['拉邦猫', '褴褛猫', '蓝金渐层长毛', '狸花猫', '临清狮猫'] },
        { initial: 'M', breeds: ['马恩岛猫', '曼基康猫', '美国短毛猫', '美国短尾猫', '美国刚毛猫', '美国卷耳猫', '美国卷毛猫', '孟加拉豹猫', '孟买猫', '米努特猫', '缅甸猫', '缅因猫', '美短虎斑'] },
        { initial: 'N', breeds: ['拿破仑猫', '奶牛猫', '挪威森林猫', '欧洲缅甸猫', '起司猫', '巧克力金渐层'] },
        { initial: 'R', breeds: ['日本短尾猫'] },
        { initial: 'S', breeds: ['山猫纹金点', '塞尔凯克卷毛猫', '三花猫', '沙特尔猫', '狮子猫', '斯芬克斯猫', '苏格兰折耳猫', '苏格兰立耳猫', '索马里猫'] },
        { initial: 'T', breeds: ['土耳其安哥拉猫', '土耳其梵猫', '铁锈豹猫'] },
        { initial: 'X', breeds: ['西伯利亚森林猫', '喜马拉雅猫', '暹罗猫', '新加坡猫'] },
        { initial: 'Y', breeds: ['英短紫金渐层', '异国短毛猫', '英短丁香', '英短鱼骨纹', '英国短毛猫', '英短蓝白', '英短红白', '英短蓝猫', '英短银点', '英短银渐层', '英短金渐层', '英短蓝金渐层', '英短黑金渐层', '英短白猫', '英短凯米尔渐层', '英短黑猫', '英短蓝乳', '英短三花', '英短梵猫', '英短虎斑', '英短乳色', '英短乳白', '英国长毛猫', '英短金点'] },
        { initial: 'Z', breeds: ['中华田园猫', '重点色英短', '重点色短毛猫'] }
      ],
      // 狗狗品种数据
      dogBreeds: [
        { initial: 'A', breeds: ['阿登牧牛犬', '阿富汗猎犬', '阿根廷杜高犬', '阿拉伯灵缇', '阿拉斯加犬', '阿兰多獒犬', '阿彭则牧牛犬', '阿特拉斯牧羊犬', '阿图瓦猎犬', '阿札瓦克犬', '埃斯特卑拉山犬', '艾瑞格斯犬', '艾瑞格指示犬', '爱尔兰梗', '爱尔兰猎狼犬', '爱尔兰软毛梗', '爱尔兰水猎犬', '爱尔兰水犬', '爱尔兰峡谷梗', '爱尔兰雪达犬', '獒犬', '奥达猎犬', '奥地利宾莎犬', '奥地利黑褐猎犬', '奥弗涅指示犬', '澳大利亚梗', '澳大利亚牧牛犬', '澳大利亚牧羊犬', '澳洲丝毛梗'] },
        { initial: 'B', breeds: ['八哥犬', '巴哥犬', '巴吉度犬', '巴西獒犬', '巴西梗犬', '巴仙吉犬', '巴辛吉犬', '白色瑞士牧羊犬', '北海道犬', '北京犬', '贝灵顿梗', '比格猎犬', '比格猎兔犬', '比格牧羊犬', '比格犬', '比利牛斯獒犬', '比利牛斯牧羊犬', '比利牛斯山地犬', '比利犬', '比利时格里芬犬', '比利时牧羊犬', '比利时特伏丹犬', '边境梗', '边境牧羊犬', '标准贵宾犬', '标准腊肠犬', '标准雪纳瑞', '冰岛牧羊犬', '波尔多犬', '波兰低地牧羊犬', '波兰猎犬', '波兰灵缇', '波利犬', '波伦亚伴随犬', '波密犬', '波旁指示犬', '波萨维茨猎犬', '波士顿梗', '波音达', '伯恩山犬', '伯瑞牧羊犬', '博得猎狐犬', '博格斯指示犬', '博美', '博伊金猎犬', '捕鼠梗', '布雷猎犬', '布列塔尼犬', '布鲁克浣熊猎犬'] },
        { initial: 'C', breeds: ['查尔斯王猎犬', '柴犬', '瓷器犬', '粗毛柯利犬', '粗毛意大利猎犬', '长须牧羊犬', '中国冠毛犬', '中国沙皮犬', '中华田园犬', '中型德国尖嘴犬', '中型贵宾犬', '中亚牧羊犬'] },
        { initial: 'D', breeds: ['大白熊犬', '大丹犬', '大格林芬犬', '大加斯科涅蓝犬', '大麦町犬', '大明斯特兰德犬', '大瑞士山地犬', '大英法黑白猎犬', '大英法黄白猎犬', '大英法三色猎犬', '丹迪丁蒙梗', '丹麦布罗荷马獒', '丹麦老式指示犬', '德国宾莎犬', '德国博美犬', '德国粗毛指示犬', '德国短毛波音达', '德国短毛指示犬', '德国猎梗', '德国猎犬', '德国牧羊犬', '德国拳狮犬', '德国硬毛波音达', '德国硬毛指示犬', '德国长毛指示犬', '斗牛獒犬', '斗牛梗', '斗牛犬', '杜宾犬', '短脚长身梗', '短毛猎狐梗', '短毛牧羊犬', '短毛意大利猎犬'] },
        { initial: 'E', breeds: ['俄罗斯黑梗犬', '俄罗斯玩具犬'] },
        { initial: 'F', breeds: ['法国斗牛犬', '法国黑白猎犬', '法国黄白猎犬', '法国狼犬', '法国猎犬', '法国三色猎犬', '法国水犬', '法老王猎犬', '芬兰尖嘴', '芬兰拉普猎犬', '芬兰猎犬', '芬兰驯鹿犬', '佛兰德斯牧牛犬', '佛瑞斯安水犬', '弗莱特寻回犬', '刚毛猎狐梗', '高加索牧羊犬', '戈登蹲猎犬', '戈登雪达犬', '格雷伊猎犬', '格里芬尼韦奈犬', '格陵兰犬', '古代牧羊犬', '贵宾犬'] },
        { initial: 'H', breeds: ['哈尔登猎犬', '哈士奇', '哈瓦那犬', '哈威那伴随犬', '海根猎犬', '韩国金刀犬', '汉密尔顿猎犬', '汉诺威嗅猎犬', '荷兰猎鸟犬', '荷兰毛狮犬', '荷兰牧羊犬', '黑俄罗斯梗', '黑褐猎浣熊犬', '黑色挪威猎鹿犬', '红骨猎浣熊犬', '猴面宾莎犬', '猴头梗', '湖畔梗', '蝴蝶犬', '灰色挪威猎鹿犬', '惠比特犬', '霍夫瓦尔特犬'] },
        { initial: 'J', breeds: ['吉娃娃', '纪州犬', '加那利沃伦猎犬', '加斯科猎犬', '加斯科涅小蓝犬', '迦南犬', '甲斐犬', '杰克罗素梗', '捷克梗', '金毛', '巨型德国尖嘴犬', '巨型雪纳瑞犬', '卷毛比熊犬', '卷毛寻回犬', '卷毛指示犬'] },
        { initial: 'K', breeds: ['卡累利亚熊犬', '卡斯罗', '卡斯特牧羊犬', '凯安梗', '凯恩梗', '凯利蓝梗', '凯斯梗', '柯基', '柯利犬', '可蒙犬', '克龙弗兰德犬', '克伦勃猎犬', '克罗地亚牧羊犬', '库瓦兹犬'] },
        { initial: 'L', breeds: ['拉布拉多', '拉萨犬', '腊肠', '兰波格犬', '兰伯格犬', '兰希尔犬', '蓝色皮卡第猎犬', '猎鹿犬', '猎水獭犬', '猎兔犬', '灵缇', '罗德西亚脊背犬', '罗曼娜水犬', '罗秦犬', '罗威纳犬'] },
        { initial: 'M', breeds: ['马地犬', '马里努阿犬', '马尔济斯犬', '马略卡獒犬', '马士提夫獒犬', '玛尔济斯犬', '曼彻斯特梗', '美国可卡犬', '美国猎狐犬', '美国秋田犬', '美国水猎犬', '美国斯塔福郡梗', '迷你德国尖嘴犬', '迷你杜宾犬', '迷你贵宾犬', '迷你腊肠犬', '迷你牛头梗犬', '迷你雪纳瑞', '秘鲁无毛犬', '墨西哥无毛犬', '牧师罗素梗', '美国恶霸犬'] },
        { initial: 'N', breeds: ['那不勒斯獒犬', '南俄罗斯牧羊犬', '南斯拉夫牧羊犬', '牛头梗犬', '纽芬兰犬', '挪威布哈德犬', '挪威梗', '挪威猎鹿犬', '挪威猎犬', '挪威卢德杭犬', '挪威伦德猎犬', '挪威牧羊犬', '诺波丹狐狸犬', '诺福克梗', '诺维茨梗'] },
        { initial: 'O', breeds: ['欧式俄国莱卡犬', '欧亚大陆犬'] },
        { initial: 'P', breeds: ['帕尔森罗塞尔梗', '佩狄芬犬', '蓬托德梅尔猎犬', '皮卡第猎犬', '平毛猎狐梗', '平毛寻回猎犬', '葡萄牙波登哥犬', '葡萄牙牧羊犬', '葡萄牙水犬', '葡萄牙指示犬', '普罗特猎犬'] },
        { initial: 'Q', breeds: ['奇努克犬', '秋田犬', '拳师犬'] },
        { initial: 'R', breeds: ['日本梗', '日本狐狸犬', '日本犬', '绒毛尖嘴狼犬', '瑞典柯基犬', '瑞典拉普猎犬', '瑞典腊肠犬', '瑞典猎鹿犬', '瑞典瓦汉德犬', '瑞士猎犬'] },
        { initial: 'S', breeds: ['萨卢基猎犬', '萨卢斯猎狼犬', '萨路基猎犬', '萨摩耶', '塞尔维亚猎犬', '沙皮犬', '山地犬', '圣伯纳犬', '史毕诺犬', '树丛浣熊猎犬', '丝毛梗', '斯凯梗', '斯洛伐克猎犬', '斯莫兰德猎犬', '斯恰潘道斯犬', '斯塔比荷猎犬', '斯塔福郡斗牛梗', '四国犬', '松狮犬', '苏俄猎狼犬', '苏格兰梗', '苏格兰猎鹿犬', '苏格兰牧羊犬', '苏赛克斯猎犬'] },
        { initial: 'T', breeds: ['台湾犬', '泰国脊背犬', '泰托拉牧羊犬', '提洛尔猎犬', '田野猎犬', '田野小猎犬', '土佐犬', '兔型腊肠犬', '泰迪'] },
        { initial: 'X', breeds: ['西班牙獒犬', '西班牙猎犬', '西班牙灵缇', '西班牙水犬', '西班牙小猎犬', '西藏獒犬', '西藏梗犬', '西藏猎犬', '西高地白梗', '西高地白梗犬', '西里汉姆梗', '西帕基犬', '西帕凯牧羊犬', '西施犬', '西西里猎犬', '希腊猎犬', '锡利哈姆梗', '席勒猎犬', '喜乐蒂牧羊犬', '小明斯特兰德犬', '小瑞士猎犬', '小型斗牛梗', '小型荷兰水猎犬', '小型雪纳瑞犬', '匈牙利灵缇', '匈牙利牧羊犬', '雪纳瑞', '寻血猎犬'] },
        { initial: 'Y', breeds: ['伊比赞猎犬', '依维萨沃伦猎犬', '意大利狐狸犬', '意大利灰狗', '意大利灵缇', '意大利指示犬', '英法小型犬', '英格兰雪达犬', '英国斗牛犬', '英国可卡犬', '英国猎狐犬', '英国史宾格猎犬', '英国跳猎犬', '英国玩具梗', '英国玩具犬', '英国雪达蹲猎犬', '英国指示犬', '约克夏梗'] },
        { initial: 'Z', breeds: ['藏獒', '中华田园犬'] }
      ],
      // 其他宠物品种数据
      otherBreeds: [
        { initial: 'A', breeds: ['安哥拉兔'] },
        { initial: 'B', breeds: ['豹纹守宫', '捕鸟蛛', '白桂鸡', '保贝', '白头翁'] },
        { initial: 'C', breeds: ['仓鼠', '刺猬', '垂耳兔'] },
        { initial: 'D', breeds: ['道奇兔', '大眼飞鼠', '道奇侏儒兔'] },
        { initial: 'E', breeds: ['鹅', '鳄鱼', '蛾'] },
        { initial: 'F', breeds: ['肥尾守宫', '鸽子'] },
        { initial: 'H', breeds: ['狐狸', '花枝鼠', '荷兰猪', '虎皮鹦鹉', '和尚鹦鹉', '蝴蝶', '红腹松鼠'] },
        { initial: 'J', breeds: ['寄居蟹', '睫鱼守宫', '金丝熊', '角蛙', '甲虫', '金太阳鹦鹉'] },
        { initial: 'K', breeds: ['柯尔鸭'] },
        { initial: 'L', breeds: ['龙猫', '六角恐龙', '鹩哥', '瘤尾守宫', '芦丁鸡', '蓝舌石龙子'] },
        { initial: 'M', breeds: ['蜜袋鼯', '牡丹鹦鹉', '猫猫兔'] },
        { initial: 'N', breeds: ['鸟'] },
        { initial: 'R', breeds: ['蝾螈'] },
        { initial: 'S', breeds: ['松鼠', '蛇', '水獭', '塞拉玛矮脚鸡', '树蛙', '狮子兔'] },
        { initial: 'T', breeds: ['兔子', '豚鼠', '通心粉鼠'] },
        { initial: 'W', breeds: ['王蛇', '文鸟'] },
        { initial: 'X', breeds: ['蜥蜴', '雪貂', '玄凤鹦鹉', '小太阳鹦鹉', '小丑蛙', '小鼯鼠', '鸭子', '羊', '羊驼', '银狐', '鹦鹉', '玉米蛇', '雨林蝎', '元宝鸡'] },
        { initial: 'Y', breeds: ['鸭子', '羊', '羊驼', '银狐', '鹦鹉', '玉米蛇', '雨林蝎', '元宝鸡'] },
        { initial: 'Z', breeds: ['猪', '蜘蛛', '侏儒兔', '鬃狮蜥蜴', '中华大蟾蜍'] }
      ],
      scrollViewId: '',
      activeInitial: 'A'
    };
  },
  computed: {
    navBarTotalHeight() {
      return this.statusBarHeight + this.navBarHeight;
    },
    alphabetIndex() {
      return this.allBreeds.map(group => group.initial);
    },
    filteredHotBreeds() {
      if (!this.searchText) {
        return this.hotBreeds;
      }
      const lowerCaseSearchText = this.searchText.toLowerCase();
      return this.hotBreeds.filter(breed => breed.toLowerCase().includes(lowerCaseSearchText));
    },
    filteredAlphabeticalBreeds() {
      if (!this.searchText) {
        return this.allBreeds;
      }
      const lowerCaseSearchText = this.searchText.toLowerCase();
      return this.allBreeds
        .map(group => ({
          initial: group.initial,
          breeds: group.breeds.filter(breed => breed.toLowerCase().includes(lowerCaseSearchText))
        }))
        .filter(group => group.breeds.length > 0);
    }
  },
  onLoad(options) {
    // 获取传递的宠物类型参数
    if (options.petType) {
      this.petType = options.petType;
      this.loadBreedData();
    }
    
    uni.getSystemInfo({
      success: res => {
        this.statusBarHeight = res.statusBarHeight;
      }
    });
  },
  methods: {
    // 根据宠物类型加载对应的品种数据
    loadBreedData() {
      switch (this.petType) {
        case 'cat':
          this.hotBreeds = [
            '中华田园猫', '狸花猫', '橘猫', '奶牛猫', '黑猫', '英短蓝白', '英短蓝猫', '英短银渐层',
            '串串', '布偶猫', '波斯猫', '加菲猫', '暹罗猫', '美国短毛猫', '金吉拉', '缅因猫'
          ];
          this.allBreeds = this.catBreeds;
          break;
        case 'dog':
          this.hotBreeds = [
            '中华田园犬', '金毛', '拉布拉多', '泰迪', '柯基', '萨摩耶', '柴犬', '边境牧羊犬',
            '哈士奇', '阿拉斯加犬', '德国牧羊犬', '比熊', '博美', '吉娃娃', '法国斗牛犬', '英国斗牛犬'
          ];
          this.allBreeds = this.dogBreeds;
          break;
        case 'other':
          this.hotBreeds = [
            '仓鼠', '兔子', '龙猫', '荷兰猪', '刺猬', '松鼠', '蜜袋鼯', '花枝鼠',
            '虎皮鹦鹉', '玄凤鹦鹉', '和尚鹦鹉', '牡丹鹦鹉', '鸽子', '鸟', '蛇', '蜥蜴'
          ];
          this.allBreeds = this.otherBreeds;
          break;
        default:
          this.hotBreeds = [];
          this.allBreeds = [];
      }
    },
    goBack() {
      uni.navigateBack();
    },
    selectBreed(breedName) {
      console.log('选择的品种:', breedName);
      // 将选择的品种通过页面参数传回上一个页面
      const pages = getCurrentPages();
      const prevPage = pages[pages.length - 2];
      if (prevPage && prevPage.$vm) {
        // 尝试调用handleSelectedBreed方法
        if (prevPage.$vm.handleSelectedBreed) {
          console.log('调用上一页面的handleSelectedBreed方法');
          prevPage.$vm.handleSelectedBreed(breedName);
        } else {
          // 如果没有handleSelectedBreed方法，设置selectedBreed属性
          console.log('设置上一页面的selectedBreed属性');
          prevPage.$vm.selectedBreed = breedName;
        }
      } else {
        console.log('无法找到上一页面');
      }
      uni.navigateBack();
    },
    onSearchInput() {
      // 搜索时重置滚动位置和激活字母
      this.scrollViewId = '';
      this.activeInitial = '';
    },
    scrollToInitial(initial) {
      this.scrollViewId = 'initial-' + initial;
      this.activeInitial = initial;
    }
  }
};
</script>

<style lang="scss">
.select-breed-list-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f8f8f8;
}

/* 自定义白色导航栏 */
.custom-white-navbar {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  background-color: #fff;
  z-index: 1000;
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.05);
}

.navbar-content {
  display: flex;
  align-items: center;
  height: 44px;
  padding: 0 30rpx;
  position: relative;
}

.navbar-left {
  width: 60rpx;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  z-index: 10;
}

.back-icon {
  font-size: 48rpx;
  color: #333;
  font-weight: bold;
}

.navbar-title {
  position: absolute;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 32rpx;
  color: #333;
  font-weight: 500;
  z-index: 5;
}

.navbar-right {
  width: 60rpx;
  z-index: 10;
}

/* 滚动内容区域 */
.breed-list-scroll-content {
  flex: 1;
  height: 100%;
  box-sizing: border-box;
  position: relative;
  z-index: 1;
}

/* 搜索框 */
.search-section {
  padding: 20rpx 30rpx;
  background-color: #fff;
  margin-bottom: 20rpx;
}

.search-input-container {
  display: flex;
  align-items: center;
  background-color: #f0f0f0;
  border-radius: 40rpx;
  padding: 16rpx 24rpx;
}

.search-icon {
  font-size: 32rpx;
  color: #999;
  margin-right: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  height: 40rpx;
  line-height: 40rpx;
}

/* 热门品种 */
.hot-breeds-section {
  background-color: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 30rpx;
}

.hot-breeds-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
}

.breed-tag {
  background-color: #f8f8f8;
  border-radius: 10rpx;
  padding: 20rpx 0;
  text-align: center;
  font-size: 28rpx;
  color: #666;
  transition: background-color 0.2s ease;
}

.breed-tag:active {
  background-color: #e0e0e0;
}

/* 按字母排序的品种列表 */
.alphabetical-breeds-section {
  background-color: #fff;
}

.initial-group {
  padding-left: 30rpx;
}

.initial-header {
  font-size: 28rpx;
  color: #999;
  padding: 20rpx 0;
  background-color: #f8f8f8;
  position: sticky;
  top: 0;
  z-index: 2;
  padding-left: 30rpx;
  margin-left: -30rpx;
}

.breed-item {
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  font-size: 30rpx;
  color: #333;
  transition: background-color 0.2s ease;
}

.breed-item:last-child {
  border-bottom: none;
}

.breed-item:active {
  background-color: #f8f8f8;
}

/* 右侧字母索引 */
.alphabet-index {
  position: fixed;
  right: 0;
  top: 0;
  bottom: 0;
  width: 60rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.8);
  z-index: 1001;
  padding-bottom: env(safe-area-inset-bottom);
}

.index-item {
  padding: 8rpx 0;
  font-size: 22rpx;
  color: #666;
  font-weight: 500;
  text-align: center;
  width: 100%;
}

.index-item .active {
  color: #ff8c00;
  font-weight: bold;
  transform: scale(1.2);
}
</style>
