<#
  本地中间件一键启动（Kafka；若已安装 Erlang + RabbitMQ 则一并启动）
  用法：  powershell -ExecutionPolicy Bypass -File scripts\start-infra.ps1
  说明：  二进制放在 infra/ 下（已被 .gitignore 忽略，不入库）。
          业务侧可通过 APP_MQ_ENABLED / APP_KAFKA_ENABLED 关闭对应开关。
#>
$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $PSScriptRoot
$infra = Join-Path $root "infra"
$logsDir = Join-Path $infra "logs"
New-Item -ItemType Directory -Force -Path $logsDir | Out-Null

function Test-Listening([int]$port) {
    return [bool](Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue)
}

# ---------------- Kafka（KRaft 单节点） ----------------
$kafkaDir = Get-ChildItem -Path (Join-Path $infra "kafka") -Directory -ErrorAction SilentlyContinue |
            Where-Object { Test-Path (Join-Path $_.FullName "libs") } | Select-Object -First 1

if (-not $kafkaDir) {
    Write-Host "[Kafka] 未找到 infra\kafka\*\libs，跳过。"
} elseif (Test-Listening 9092) {
    Write-Host "[Kafka] 已在运行 (127.0.0.1:9092)"
} else {
    $config   = Join-Path $infra "kafka-config\server.properties"
    $dataDir  = Join-Path $infra "data\kafka-logs"
    $idFile   = Join-Path $infra "kafka-config\cluster-id.txt"
    $libs     = Join-Path $kafkaDir.FullName "libs"

    # 首次启动：生成集群 ID 并格式化存储目录
    if (-not (Test-Path (Join-Path $dataDir "meta.properties"))) {
        $clusterId = if (Test-Path $idFile) { (Get-Content $idFile -Raw).Trim() } else { $null }
        if (-not $clusterId) {
            $clusterId = (& java "-cp" "$libs\*" "kafka.tools.StorageTool" "random-uuid").Trim()
            Set-Content -Path $idFile -Value $clusterId -NoNewline
        }
        Write-Host "[Kafka] 首次初始化存储，clusterId=$clusterId"
        New-Item -ItemType Directory -Force -Path $dataDir | Out-Null
        & java "-cp" "$libs\*" "kafka.tools.StorageTool" "format" "-t" $clusterId "-c" $config "-i"
    }

    Start-Process -FilePath "java.exe" `
        -ArgumentList ('-cp "' + $libs + '\*" kafka.Kafka "' + $config + '"') `
        -RedirectStandardOutput (Join-Path $logsDir "kafka-out.log") `
        -RedirectStandardError  (Join-Path $logsDir "kafka-err.log") `
        -WindowStyle Hidden | Out-Null

    for ($i = 0; $i -lt 40; $i++) {
        Start-Sleep -Milliseconds 500
        if (Test-Listening 9092) { break }
    }
    if (Test-Listening 9092) { Write-Host "[Kafka] 启动成功 (127.0.0.1:9092, 控制器 9093)" }
    else { Write-Host "[Kafka] 启动失败，查看 infra\logs\kafka-err.log" }
}

# ---------------- RabbitMQ（可选，需要 Erlang 27+） ----------------
$rmqDir = Get-ChildItem -Path (Join-Path $infra "rabbitmq") -Directory -ErrorAction SilentlyContinue |
          Where-Object { Test-Path (Join-Path $_.FullName "sbin\rabbitmq-server.bat") } | Select-Object -First 1
$erlangHome = Join-Path $infra "erlang"

if (-not $rmqDir) {
    Write-Host "[RabbitMQ] 未安装（infra\rabbitmq 不存在），跳过。MQ 相关功能会自动降级为定时任务 + Redis。"
} elseif (-not (Test-Path (Join-Path $erlangHome "bin\erl.exe"))) {
    Write-Host "[RabbitMQ] 找到 RabbitMQ 但缺少 Erlang（infra\erlang\bin\erl.exe），跳过。"
} elseif (Test-Listening 5672) {
    Write-Host "[RabbitMQ] 已在运行 (127.0.0.1:5672)"
} else {
    $env:ERLANG_HOME    = $erlangHome
    $env:RABBITMQ_BASE  = Join-Path $infra "data\rabbitmq"
    $env:PATH           = (Join-Path $erlangHome "bin") + ";" + $env:PATH
    New-Item -ItemType Directory -Force -Path $env:RABBITMQ_BASE | Out-Null

    Start-Process -FilePath (Join-Path $rmqDir.FullName "sbin\rabbitmq-server.bat") `
        -RedirectStandardOutput (Join-Path $logsDir "rabbitmq.log") `
        -RedirectStandardError  (Join-Path $logsDir "rabbitmq-err.log") `
        -WindowStyle Hidden | Out-Null

    for ($i = 0; $i -lt 60; $i++) {
        Start-Sleep -Milliseconds 500
        if (Test-Listening 5672) { break }
    }
    if (Test-Listening 5672) { Write-Host "[RabbitMQ] 启动成功 (127.0.0.1:5672, 管理台 http://127.0.0.1:15672 guest/guest)" }
    else { Write-Host "[RabbitMQ] 启动失败，查看 infra\logs\rabbitmq.log" }
}

Write-Host ""
Write-Host "完成。Kafka: 127.0.0.1:9092 / RabbitMQ: 127.0.0.1:5672"
