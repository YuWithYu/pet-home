import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

// Element Plus 需要的插件
import customParseFormat from 'dayjs/plugin/customParseFormat'
import advancedFormat from 'dayjs/plugin/advancedFormat'
import weekOfYear from 'dayjs/plugin/weekOfYear'
import weekYear from 'dayjs/plugin/weekYear'
import dayOfYear from 'dayjs/plugin/dayOfYear'
import isSameOrAfter from 'dayjs/plugin/isSameOrAfter'
import isSameOrBefore from 'dayjs/plugin/isSameOrBefore'
import isBetween from 'dayjs/plugin/isBetween'
import quarterOfYear from 'dayjs/plugin/quarterOfYear'
import isoWeek from 'dayjs/plugin/isoWeek'
import isoWeeksInYear from 'dayjs/plugin/isoWeeksInYear'
import isLeapYear from 'dayjs/plugin/isLeapYear'
import minMax from 'dayjs/plugin/minMax'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'
import duration from 'dayjs/plugin/duration'
import relativeTime from 'dayjs/plugin/relativeTime'
import updateLocale from 'dayjs/plugin/updateLocale'
import localeData from 'dayjs/plugin/localeData'
import weekday from 'dayjs/plugin/weekday'
import isToday from 'dayjs/plugin/isToday'
import isTomorrow from 'dayjs/plugin/isTomorrow'
import isYesterday from 'dayjs/plugin/isYesterday'
import isMoment from 'dayjs/plugin/isMoment'
import objectSupport from 'dayjs/plugin/objectSupport'
import arraySupport from 'dayjs/plugin/arraySupport'
import localizedFormat from 'dayjs/plugin/localizedFormat'
import calendar from 'dayjs/plugin/calendar'
import toArray from 'dayjs/plugin/toArray'
import toObject from 'dayjs/plugin/toObject'

// 扩展 dayjs - 只加载必要的插件
dayjs.extend(customParseFormat)
dayjs.extend(advancedFormat)
dayjs.extend(weekOfYear)
dayjs.extend(weekYear)
dayjs.extend(dayOfYear)
dayjs.extend(isSameOrAfter)
dayjs.extend(isSameOrBefore)
dayjs.extend(isBetween)
dayjs.extend(quarterOfYear)
dayjs.extend(isoWeek)
dayjs.extend(isoWeeksInYear)
dayjs.extend(isLeapYear)
dayjs.extend(minMax)
dayjs.extend(utc)
dayjs.extend(timezone)
dayjs.extend(duration)
dayjs.extend(relativeTime)
dayjs.extend(updateLocale)
dayjs.extend(localeData)
dayjs.extend(weekday)
dayjs.extend(isToday)
dayjs.extend(isTomorrow)
dayjs.extend(isYesterday)
dayjs.extend(isMoment)
dayjs.extend(objectSupport)
dayjs.extend(arraySupport)
dayjs.extend(localizedFormat)
dayjs.extend(calendar)
dayjs.extend(toArray)
dayjs.extend(toObject)

// 设置默认语言
dayjs.locale('zh-cn')

// 修复 Element Plus 在调用 locale(undefined) 时返回字符串导致的类型错误
const originalLocaleFn = dayjs.prototype.locale
dayjs.prototype.locale = function(preset, ...args) {
  if (arguments.length > 0 && preset === undefined) {
    // 显式传入 undefined 时保持链式调用能力
    return this
  }
  return originalLocaleFn.call(this, preset, ...args)
}

// 确保 dayjs 在全局可用
if (typeof window !== 'undefined') {
  window.dayjs = dayjs
}

export default dayjs
