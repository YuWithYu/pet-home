#!/usr/bin/env node

process.noDeprecation = true

const { rspack } = require("@rspack/core")
const path = require("path")
const fs = require("fs")
const { donationConsole } = require("./layouts")


// 读取 Rspack 配置
const configPath = path.resolve(__dirname, "rspack.config.js")
const config = require(configPath)

// 判断模式
const mode = process.argv[2] === "build" ? "production" : "development"

// 设置统一环境变量
process.env.NODE_ENV = mode
process.env.WEBPACK_ENV = mode
process.env.BABEL_ENV = mode

// Mock 环境变量
process.env.VUE_APP_MOCK_ENABLE = mode === "production" ? "false" : "true"

console.log("环境变量 NODE_ENV =", process.env.NODE_ENV)
console.log("环境变量 VUE_APP_MOCK_ENABLE =", process.env.VUE_APP_MOCK_ENABLE)

config.mode = mode

if (mode === "production") {
  console.log("⚡ 正在极速打包生产环境，请稍候...")

  // 优化长期缓存
  config.optimization = {
    ...config.optimization,
    moduleIds: "deterministic",
    chunkIds: "deterministic",
    removeEmptyChunks: true,
  }

  rspack(config).run((err, stats) => {
    if (err) {
      console.error(err.stack || err)
      if (err.details) console.error(err.details)
      process.exit(1)
      return
    }

    const info = stats.toJson()
    if (stats.hasErrors()) console.error(info.errors)
    if (stats.hasWarnings()) console.warn(info.warnings)

    console.log(
      stats.toString({
        colors: true,
        modules: false,
        children: false,
        chunks: false,
        chunkModules: false,
      })
    )
  })
} else {
  // 开发环境
  try {
    const { RspackDevServer } = require("@rspack/dev-server")
    const compiler = rspack(config)
    const devServerOptions = config.devServer || {}

    // 开发环境始终启用 Mock
    const server = new RspackDevServer(devServerOptions, compiler)
    server.start().catch((err) => {
      console.error("启动 RspackDevServer 失败:", err)
    })
  } catch (err) {
    console.error("加载 @rspack/dev-server 失败:", err)
    process.exit(1)
  }
}
