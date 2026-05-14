/**
 * dayjs 补丁文件 - 修复 Element Plus 兼容性问题
 */

// 确保在 Element Plus 加载前执行
(function() {
  'use strict';
  
  // 检查是否已经有 dayjs
  if (typeof window !== 'undefined' && window.dayjs) {
    const dayjs = window.dayjs;
    
    // 确保所有必要的插件都已加载
    const requiredPlugins = [
      'customParseFormat',
      'advancedFormat', 
      'weekOfYear',
      'weekYear',
      'dayOfYear',
      'isSameOrAfter',
      'isSameOrBefore',
      'isBetween',
      'quarterOfYear',
      'isoWeek',
      'isoWeeksInYear',
      'isLeapYear',
      'minMax',
      'utc',
      'timezone',
      'duration',
      'relativeTime',
      'updateLocale',
      'localeData',
      'weekday',
      'isToday',
      'isTomorrow',
      'isYesterday',
      'isMoment',
      'objectSupport',
      'arraySupport',
      'localizedFormat',
      'calendar',
      'toArray',
      'toObject'
    ];
    
    // 检查并加载缺失的插件
    requiredPlugins.forEach(pluginName => {
      if (!dayjs.prototype[pluginName]) {
        try {
          // 动态加载插件
          const script = document.createElement('script');
          script.src = `https://cdn.jsdelivr.net/npm/dayjs@1.11.18/plugin/${pluginName}.js`;
          script.onload = function() {
            console.log(`dayjs plugin ${pluginName} loaded`);
          };
          script.onerror = function() {
            console.warn(`Failed to load dayjs plugin ${pluginName}`);
          };
          document.head.appendChild(script);
        } catch (e) {
          console.warn(`Error loading dayjs plugin ${pluginName}:`, e);
        }
      }
    });
    
    // 确保 add 方法可用
    if (!dayjs.prototype.add) {
      console.warn('dayjs add method not available, attempting to load duration plugin');
      try {
        const script = document.createElement('script');
        script.src = 'https://cdn.jsdelivr.net/npm/dayjs@1.11.18/plugin/duration.js';
        script.onload = function() {
          console.log('dayjs duration plugin loaded for add method');
        };
        document.head.appendChild(script);
      } catch (e) {
        console.error('Failed to load dayjs duration plugin:', e);
      }
    }
    
    // 设置中文语言环境
    if (dayjs.locale) {
      dayjs.locale('zh-cn');
    }
    
    // 确保 dayjs 在全局可用
    window.dayjs = dayjs;
    
    console.log('dayjs patch applied successfully');
  } else {
    console.warn('dayjs not found in window object');
  }
})();

export default window.dayjs;
