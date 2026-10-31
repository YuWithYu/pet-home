<#
  停止本地中间件（RabbitMQ 优雅关闭；Kafka 按端口结束进程）
  用法：  powershell -ExecutionPolicy Bypass -File scripts\stop-infra.ps1
#>
$ErrorActionPreference = "Continue"
$root  = Split-Path -Parent $PSScriptRoot
$infra = Join-Path $root "infra"
$stopped = @()

function Test-Listening([int]$port) {
    return [bool](Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue)
}

# ---------------- RabbitMQ：优先用 rabbitmqctl 优雅停止（避免强杀 erl 造成数据页损坏） ----------------
$rmqDir = Get-ChildItem -Path (Join-Path $infra "rabbitmq") -Directory -ErrorAction SilentlyContinue |
          Where-Object { Test-Path (Join-Path $_.FullName "sbin\rabbitmqctl.bat") } | Select-Object -First 1
$erlangHome = Join-Path $infra "erlang"

if ($rmqDir -and (Test-Path (Join-Path $erlangHome "bin\erl.exe")) -and (Test-Listening 5672)) {
    $env:ERLANG_HOME   = $erlangHome
    $env:RABBITMQ_BASE = Join-Path $infra "data\rabbitmq"
    $env:PATH          = (Join-Path $erlangHome "bin") + ";" + $env:PATH
    & (Join-Path $rmqDir.FullName "sbin\rabbitmqctl.bat") stop | Out-Null
    $stopped += "RabbitMQ (rabbitmqctl stop)"
}

# ---------------- 兜底：仍占用端口的进程直接结束 ----------------
$ports = @{ 9092 = "Kafka"; 9093 = "Kafka(controller)"; 5672 = "RabbitMQ"; 15672 = "RabbitMQ(管理台)" }
foreach ($p in $ports.Keys) {
    $conns = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue
    foreach ($c in $conns) {
        $proc = Get-Process -Id $c.OwningProcess -ErrorAction SilentlyContinue
        if ($proc) {
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
            $stopped += "$($ports[$p]) (PID $($proc.Id), 端口 $p)"
        }
    }
}

if ($stopped.Count -eq 0) { Write-Host "没有正在运行的 Kafka / RabbitMQ。" }
else { $stopped | ForEach-Object { Write-Host "已停止: $_" } }
