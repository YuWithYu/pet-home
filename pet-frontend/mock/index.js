/**
 * mock/index.js
 * 开发环境启用 Mock，生产环境禁用 Mock
 */

const chokidar = require('chokidar')
const bodyParser = require('body-parser')
const chalk = require('chalk')
const path = require('path')
const { mock } = require('mockjs')
const { baseURL } = require('../src/config')
const mockDir = path.join(process.cwd(), 'mock')
const { handleMockArray } = require('./utils')

// 生产环境禁用 Mock
if (process.env.NODE_ENV === 'production' || process.env.VITE_NODE_ENV === 'production') {
  module.exports = (app) => {
    console.log('> 生产环境禁用 Mock')
  }
  return
}

/**
 * 生成单条 Mock 接口
 * @param url
 * @param type
 * @param respond
 */
const responseFake = (url, type, respond) => {
  const base = baseURL.endsWith('/') ? baseURL.slice(0, -1) : baseURL
  const apiUrl = url.startsWith('/') ? url : `/${url}`
  return {
    url: new RegExp(`${base}${apiUrl}`),
    type: type || 'get',
    response(req, res) {
      res.status(200)
      if (JSON.stringify(req.body) !== '{}') {
        console.log(chalk.green(`> 请求地址：${req.path}`))
        console.log(chalk.green(`> 请求参数：${JSON.stringify(req.body)}\n`))
      } else {
        console.log(chalk.green(`> 请求地址：${req.path}\n`))
      }
      res.json(mock(respond instanceof Function ? respond(req, res) : respond))
    },
  }
}

/**
 * 注册 Mock 路由
 * @param app
 */
const registerRoutes = (app) => {
  let mockLastIndex
  const mocks = []
  const mockArray = handleMockArray()
  mockArray.forEach((item) => {
    const obj = require(item)
    mocks.push(...obj)
  })

  const mocksForServer = mocks.map((route) =>
    responseFake(route.url, route.type, route.response)
  )

  for (const mock of mocksForServer) {
    app[mock.type](mock.url, mock.response)
    mockLastIndex = app._router.stack.length
  }

  const mockRoutesLength = Object.keys(mocksForServer).length
  return {
    mockRoutesLength: mockRoutesLength,
    mockStartIndex: mockLastIndex - mockRoutesLength,
  }
}

/**
 * 导出挂载 Mock 的函数
 */
module.exports = (app) => {
  // 使用 body-parser 解析 JSON 和 URL-encoded
  app.use(bodyParser.json())
  app.use(
    bodyParser.urlencoded({
      extended: true,
    })
  )

  // 注册 Mock 路由
  const mockRoutes = registerRoutes(app)
  let mockRoutesLength = mockRoutes.mockRoutesLength
  let mockStartIndex = mockRoutes.mockStartIndex

  // 监听 mock 目录变化，热更新 Mock
  chokidar
    .watch(mockDir, {
      ignored: /mock-server/,
      ignoreInitial: true,
    })
    .on('all', (event) => {
      if (event === 'change' || event === 'add') {
        try {
          // 删除旧的路由
          app._router.stack.splice(mockStartIndex, mockRoutesLength)

          // 删除 require 缓存
          Object.keys(require.cache).forEach((item) => {
            if (item.includes(mockDir)) {
              delete require.cache[require.resolve(item)]
            }
          })

          // 重新注册
          const mockRoutes = registerRoutes(app)
          mockRoutesLength = mockRoutes.mockRoutesLength
          mockStartIndex = mockRoutes.mockStartIndex
          console.log(chalk.blue('> Mock 热更新完成'))
        } catch (error) {
          console.log(chalk.red(error))
        }
      }
    })

  console.log(chalk.yellow(`> 开发环境启用 Mock，拦截 ${mockRoutesLength} 个接口`))
}
