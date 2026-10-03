/**
 * 仅压缩「体积较大」的 PNG/JPEG，且仅在压缩后更小才覆盖，避免反复压反而变大。
 * 用法：node scripts/compress-static-images.mjs
 */
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'
import sharp from 'sharp'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const IMAGES_DIR = path.join(__dirname, '..', 'static', 'images')
const MIN_BYTES = 80 * 1024
const MAX_EDGE = 512

function walk (dir) {
  const out = []
  for (const name of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, name.name)
    if (name.isDirectory()) out.push(...walk(p))
    else out.push(p)
  }
  return out
}

async function tryCompress (file) {
  const before = fs.statSync(file).size
  if (before < MIN_BYTES) return null
  const ext = path.extname(file).toLowerCase()
  if (!/\.(png|jpe?g)$/i.test(ext)) return null

  const meta = await sharp(file).metadata().catch(() => null)
  if (!meta || !meta.format) return null

  let pipeline = sharp(file)
  if (meta.width && meta.height && (meta.width > MAX_EDGE || meta.height > MAX_EDGE)) {
    pipeline = pipeline.resize({
      width: meta.width >= meta.height ? MAX_EDGE : undefined,
      height: meta.height > meta.width ? MAX_EDGE : undefined,
      fit: 'inside',
      withoutEnlargement: true
    })
  }

  let buf
  if (ext === '.png' || meta.format === 'png') {
    buf = await pipeline.png({ compressionLevel: 9 }).toBuffer()
  } else {
    buf = await pipeline.jpeg({ quality: 82, mozjpeg: true }).toBuffer()
  }

  if (buf.length >= before) return null
  return { before, after: buf.length, buf, name: path.basename(file) }
}

async function main () {
  if (!fs.existsSync(IMAGES_DIR)) {
    console.error('未找到目录:', IMAGES_DIR)
    process.exit(1)
  }
  const files = walk(IMAGES_DIR)
  let saved = 0
  let touched = 0
  for (const file of files) {
    let out
    try {
      out = await tryCompress(file)
    } catch (e) {
      console.warn('跳过:', path.basename(file), String(e.message || e))
      continue
    }
    if (!out) continue
    fs.writeFileSync(file, out.buf)
    console.log(`${out.name}  ${(out.before / 1024).toFixed(1)} KB → ${(out.after / 1024).toFixed(1)} KB`)
    saved += out.before - out.after
    touched++
  }
  console.log(touched ? `完成，共处理 ${touched} 个文件，约减少 ${(saved / 1024).toFixed(1)} KB` : '无符合阈值(≥80KB)且可缩小的栅格图。')
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
