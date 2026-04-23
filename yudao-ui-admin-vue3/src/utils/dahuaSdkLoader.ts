let dahuaSdkLoadingPromise: Promise<void> | null = null
let workerPatched = false

const patchWorkerPath = () => {
  if (workerPatched || typeof window === 'undefined' || typeof window.Worker === 'undefined') return
  const OriginalWorker = window.Worker
  window.Worker = function (scriptURL: string | URL, options?: WorkerOptions): Worker {
    let nextUrl = scriptURL
    if (typeof nextUrl === 'string' && nextUrl.endsWith('.worker.js')) {
      const workerFileName = nextUrl.split('/').pop()
      nextUrl = `/dahua/${workerFileName}`
      console.log('[Dahua SDK] Worker 路径修正:', nextUrl)
    }
    return new OriginalWorker(nextUrl as string | URL, options)
  } as typeof Worker
  window.Worker.prototype = OriginalWorker.prototype
  workerPatched = true
}

const loadScript = (src: string): Promise<void> => {
  return new Promise((resolve, reject) => {
    const existed = document.querySelector(`script[data-dahua-sdk="${src}"]`) as HTMLScriptElement | null
    if (existed) {
      if ((window as any).PlayerControl) {
        resolve()
        return
      }
      existed.addEventListener('load', () => resolve(), { once: true })
      existed.addEventListener('error', () => reject(new Error(`加载脚本失败: ${src}`)), { once: true })
      return
    }

    const script = document.createElement('script')
    script.src = src
    script.async = true
    script.setAttribute('data-dahua-sdk', src)
    script.onload = () => resolve()
    script.onerror = () => reject(new Error(`加载脚本失败: ${src}`))
    document.head.appendChild(script)
  })
}

export const ensureDahuaSdkLoaded = async (): Promise<void> => {
  if (typeof window === 'undefined') return
  if ((window as any).PlayerControl && (window as any).RPC) return
  if (dahuaSdkLoadingPromise) return dahuaSdkLoadingPromise

  dahuaSdkLoadingPromise = (async () => {
    patchWorkerPath()
    await loadScript('/dahua/PlayerControl.js')
  })()

  try {
    await dahuaSdkLoadingPromise
  } finally {
    dahuaSdkLoadingPromise = null
  }
}

