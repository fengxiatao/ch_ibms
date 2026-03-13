import mpegts from 'mpegts.js'

// ZLMediaKit 播放地址类型（与后端 PlayUrlRespVO 保持字段一致）
export interface ZlmPlayUrls {
  wsFlvUrl?: string
  /** ZLMediaKit WebRTC API 地址：http(s)://host:port/index/api/webrtc?app=live&stream=xxx&type=play，外网由后端/前端适配公网 host */
  webrtcUrl?: string
}

export type ZlmPlayMode = 'flv' | 'webrtc'

export interface ZlmPlayerInstance {
  mode: ZlmPlayMode
  destroy: () => void
}

interface PlayLiveParams {
  container: HTMLElement
  urls: ZlmPlayUrls
  preferWebrtc?: boolean
}

/**
 * ZLMediaKit 播放封装
 * - 目前优先尝试 WebRTC（预留接口），失败后回退到 ws-flv + mpegts.js
 */
export function useZlmPlayer() {
  const isFirefox = typeof navigator !== 'undefined' && navigator.userAgent.toLowerCase().includes('firefox')

  // Firefox 的 MSE 并发限制，需要串行初始化播放器
  let playQueue: Promise<void> = Promise.resolve()

  const createFlvPlayerCore = async (
    container: HTMLElement,
    wsFlvUrl: string
  ): Promise<ZlmPlayerInstance> => {
    if (!mpegts.isSupported()) {
      throw new Error('当前浏览器不支持 MSE，无法播放 FLV 流')
    }

    // 清空容器，创建 <video> 元素
    container.innerHTML = ''
    const videoEl = document.createElement('video')
    videoEl.style.width = '100%'
    videoEl.style.height = '100%'
    videoEl.autoplay = true
    videoEl.muted = true
    videoEl.playsInline = true
    videoEl.setAttribute('playsinline', 'true')
    container.appendChild(videoEl)

    const player = mpegts.createPlayer(
      {
        type: 'flv',
        url: wsFlvUrl,
        isLive: true,
        hasAudio: false,
        hasVideo: true
      },
      {
        enableWorker: false,
        enableStashBuffer: true,
        stashInitialSize: isFirefox ? 256 : 128,
        lazyLoad: false,
        // mpegts.js 在部分直播流时间戳场景会计算出负区间，触发 SourceBuffer.remove(-xxx, 0) 异常
        // 关闭自动清理，交给浏览器/播放器自然滚动，优先保证稳定播放
        autoCleanupSourceBuffer: false,
        autoCleanupMaxBackwardDuration: isFirefox ? 5 : 3,
        liveBufferLatencyChasing: true,
        liveBufferLatencyMaxLatency: isFirefox ? 2.0 : 1.5,
        liveSync: true
      }
    )

    player.attachMediaElement(videoEl)
    player.load()

    player.on(mpegts.Events.ERROR, (_type: any, detail: any) => {
      // 部分 MSE 错误在 Firefox 上较频繁，交给上层根据 isPlaying 状态兜底
      if (String(detail || '').includes('SourceBuffer') || String(detail || '').includes('MSEError')) {
        console.warn('[ZLM] MSE/SourceBuffer 警告:', detail)
        return
      }
      console.error('[ZLM] 播放错误:', detail)
    })

    const playDelay = isFirefox ? 500 : 100

    await new Promise<void>((resolve, reject) => {
      setTimeout(async () => {
        try {
          await player.play()
          resolve()
        } catch (e) {
          reject(e)
        }
      }, playDelay)
    })

    const destroy = () => {
      try {
        player.pause()
      } catch {}
      try {
        player.unload()
      } catch {}
      try {
        player.detachMediaElement()
      } catch {}
      try {
        player.destroy()
      } catch {}

      if (videoEl) {
        try {
          videoEl.srcObject = null
          videoEl.src = ''
        } catch {}
      }
      container.innerHTML = ''
    }

    return {
      mode: 'flv',
      destroy
    }
  }

  const playFlv = async (container: HTMLElement, wsFlvUrl: string): Promise<ZlmPlayerInstance> => {
    if (isFirefox) {
      // Firefox：串行播放，避免 MSE 并发问题
      return new Promise<ZlmPlayerInstance>((resolve, reject) => {
        playQueue = playQueue.then(async () => {
          try {
            const inst = await createFlvPlayerCore(container, wsFlvUrl)
            // 稍作间隔，避免频繁创建销毁
            await new Promise((r) => setTimeout(r, 300))
            resolve(inst)
          } catch (e) {
            reject(e)
          }
        })
      })
    }

    return createFlvPlayerCore(container, wsFlvUrl)
  }

  /**
   * ZLMediaKit WebRTC 播放
   * 流程：创建 RTCPeerConnection(recvonly) -> createOffer -> POST offer 到 webrtcUrl -> setRemoteDescription(answer) -> ontrack 渲染
   * 信令格式见：https://docs.zlmediakit.com/zh/guide/protocol/webrtc/webrtc_signaling_interaction_format.html
   */
  const playWebrtc = async (container: HTMLElement, webrtcUrl: string): Promise<ZlmPlayerInstance> => {
    container.innerHTML = ''
    const videoEl = document.createElement('video')
    videoEl.style.width = '100%'
    videoEl.style.height = '100%'
    videoEl.autoplay = true
    videoEl.muted = true
    videoEl.playsInline = true
    videoEl.setAttribute('playsinline', 'true')
    container.appendChild(videoEl)

    // 旧版验证可用的 WebRTC 播放实现（避免“信令成功但黑屏”）
    const pc = new RTCPeerConnection({
      iceServers: [{ urls: 'stun:stun.l.google.com:19302' }],
      bundlePolicy: 'max-bundle'
    })

    pc.addTransceiver('video', { direction: 'recvonly' })
    pc.addTransceiver('audio', { direction: 'recvonly' })

    // 关键：必须在发起信令/设置 remote desc 之前绑定 ontrack
    // 否则某些浏览器/网络下 track 事件可能在我们绑定前触发，导致“信令成功但一直等不到画面”
    let firstStream: MediaStream | null = null
    pc.ontrack = (e: RTCTrackEvent) => {
      if (firstStream) return
      if (e.streams && e.streams[0]) {
        firstStream = e.streams[0]
        try {
          videoEl.srcObject = firstStream
          videoEl.play().catch(() => {})
        } catch {
          // ignore
        }
      }
    }

    const offer = await pc.createOffer()
    await pc.setLocalDescription(offer)

    const offerSdp = pc.localDescription?.sdp
    if (!offerSdp) {
      pc.close()
      throw new Error('创建 WebRTC offer 失败')
    }

    // 兜底：确保 webrtcUrl 必须包含 type=play（ZLM 会对缺参返回 -300）
    let normalizedWebrtcUrl = webrtcUrl
    try {
      const u = new URL(webrtcUrl)
      if (!u.searchParams.get('type')) {
        u.searchParams.set('type', 'play')
      }
      normalizedWebrtcUrl = u.toString()
    } catch {
      // ignore，交给 fetch 处理
    }

    // 旧版策略：stream not found 自动重试 3 次（给后端/ZLM 拉流留时间）
    const MAX_WEBRTC_RETRIES = 3
    const STREAM_NOT_FOUND_RETRY_DELAY_MS = 2500
    let lastError: Error | null = null

    let answerSdp = ''
    for (let attempt = 1; attempt <= MAX_WEBRTC_RETRIES; attempt++) {
      if (attempt > 1) {
        await new Promise((r) => setTimeout(r, STREAM_NOT_FOUND_RETRY_DELAY_MS))
      }

      const res = await fetch(normalizedWebrtcUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain;charset=utf-8' },
        body: offerSdp,
        mode: 'cors'
      })

      const responseText = await res.text()

      if (!res.ok) {
        lastError = new Error(`WebRTC 信令请求失败: ${res.status} ${res.statusText}`)
        if (attempt < MAX_WEBRTC_RETRIES) continue
        break
      }

      try {
        const json = JSON.parse(responseText)
        if (json.code === 0 && json.sdp) {
          answerSdp = String(json.sdp)
        } else {
          const msg = String(json.msg || '').toLowerCase()
          if (msg.includes('stream not found') && attempt < MAX_WEBRTC_RETRIES) {
            lastError = new Error(json.msg || 'stream not found')
            continue
          }
          throw new Error(json.msg || `WebRTC 信令错误: code=${json.code}`)
        }
      } catch (e: any) {
        if (responseText.trim().startsWith('v=')) {
          answerSdp = responseText.trim()
        } else if (String(e?.message || '').includes('stream not found') && attempt < MAX_WEBRTC_RETRIES) {
          lastError = e
          continue
        } else {
          lastError = e instanceof Error ? e : new Error(String(e?.message || e))
          if (attempt < MAX_WEBRTC_RETRIES) continue
          break
        }
      }

      if (!answerSdp || !answerSdp.startsWith('v=')) {
        lastError = new Error('无效的 SDP 响应')
        if (attempt < MAX_WEBRTC_RETRIES) continue
        break
      }

      lastError = null
      break
    }

    if (lastError) {
      pc.close()
      throw lastError
    }

    await pc.setRemoteDescription(new RTCSessionDescription({ type: 'answer', sdp: answerSdp }))

    return new Promise<ZlmPlayerInstance>((resolve, reject) => {
      // 旧版兜底：10 秒内没出 track 就认为超时
      const timeout = setTimeout(() => {
        if (!resolved) {
          resolved = true
          pc.close()
          reject(new Error('WebRTC 连接超时'))
        }
      }, 10000)

      let resolved = false

      pc.oniceconnectionstatechange = () => {
        if (pc.iceConnectionState === 'failed' || pc.iceConnectionState === 'closed') {
          clearTimeout(timeout)
          if (!resolved) {
            resolved = true
            pc.close()
            reject(new Error(`WebRTC 连接失败: ${pc.iceConnectionState}`))
          }
        }
      }

      pc.onconnectionstatechange = () => {
        if (pc.connectionState === 'failed') {
          clearTimeout(timeout)
          if (!resolved) {
            resolved = true
            pc.close()
            reject(new Error('WebRTC 连接失败'))
          }
        }
      }

      // 一旦 track 到来即视为成功（旧版行为）
      const successPoll = setInterval(() => {
        if (resolved) {
          clearInterval(successPoll)
          return
        }
        if (firstStream || videoEl.srcObject) {
          clearInterval(successPoll)
          clearTimeout(timeout)
          resolved = true
          resolve({
            mode: 'webrtc',
            destroy: () => {
              try {
                videoEl.srcObject = null
                videoEl.src = ''
              } catch {}
              pc.close()
              container.innerHTML = ''
            }
          })
        }
      }, 100)
    })
  }

  /**
   * 按优先级播放实时流
   * - 优先 WebRTC（若 webrtcUrl 存在且实现成功）
   * - 失败或未提供 webrtcUrl 时回退到 ws-flv
   */
  const playLive = async (params: PlayLiveParams): Promise<ZlmPlayerInstance> => {
    const { container, urls, preferWebrtc = true } = params

    let lastError: any = null

    if (preferWebrtc && urls.webrtcUrl) {
      try {
        return await playWebrtc(container, urls.webrtcUrl)
      } catch (e) {
        console.warn('[ZLM] WebRTC 播放失败，回退到 FLV:', e)
        lastError = e
      }
    }

    if (urls.wsFlvUrl) {
      return await playFlv(container, urls.wsFlvUrl)
    }

    throw lastError || new Error('没有可用的播放地址')
  }

  const stopInstance = (instance: ZlmPlayerInstance | null | undefined) => {
    if (!instance) return
    try {
      instance.destroy()
    } catch (e) {
      console.warn('[ZLM] 销毁播放器异常:', e)
    }
  }

  return {
    playLive,
    stopInstance
  }
}

export default useZlmPlayer

