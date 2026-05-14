/**
 * 后台展示用：将宠物性别转为中文（兼容英文枚举与常见写法）
 */
export function formatPetGenderForDisplay(value) {
  if (value == null || value === '') return '-'
  const s = String(value).trim()
  if (['母', '雌性', '女'].includes(s)) return '母'
  if (['公', '雄性', '男'].includes(s)) return '公'
  const lower = s.toLowerCase()
  if (['f', 'female'].includes(lower)) return '母'
  if (['m', 'male'].includes(lower)) return '公'
  if (['unknown', 'unk', '未知', '-', '—', 'none', 'n/a', 'na'].includes(lower)) return '-'
  return s
}
