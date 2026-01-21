/**
 * wsPlayer - WebSocket-FMP4 播放器
 * 基于 https://github.com/tokamakz/wsPlayer
 */

class WsPlayer {
  constructor(videoId, url) {
    this.videoElement = document.getElementById(videoId);
    if (!this.videoElement) {
      throw new Error(`Video element with id "${videoId}" not found`);
    }
    
    this.url = url;
    this.ws = null;
    this.mediaSource = null;
    this.sourceBuffer = null;
    this.mp4boxfile = null;
    this.queue = [];
    this.isUpdating = false;
  }

  open() {
    if (!('MediaSource' in window)) {
      console.error('[wsPlayer] 浏览器不支持 MediaSource API');
      return;
    }

    if (typeof MP4Box === 'undefined') {
      console.error('[wsPlayer] mp4box.js 未加载，请确保在 index.html 中引入');
      return;
    }

    this.mediaSource = new MediaSource();
    this.videoElement.src = URL.createObjectURL(this.mediaSource);

    this.mediaSource.addEventListener('sourceopen', () => {
      console.log('[wsPlayer] MediaSource opened');
      
      // 创建 MP4Box 实例用于解析 fmp4
      this.mp4boxfile = MP4Box.createFile();
      
      // 监听 moov 解析完成（包含 codec 信息）
      this.mp4boxfile.onReady = (info) => {
        console.log('[wsPlayer] ✅ MP4 Info ready:', info);
        
        // 从 info 中提取 codec 字符串
        const videoTrack = info.videoTracks[0];
        const audioTrack = info.audioTracks[0];
        
        let codecs = [];
        if (videoTrack) {
          codecs.push(videoTrack.codec);
        }
        if (audioTrack) {
          codecs.push(audioTrack.codec);
        }
        
        const mimeCodec = `video/mp4; codecs="${codecs.join(',')}"`;
        console.log('[wsPlayer] MIME Codec:', mimeCodec);
        
        if (MediaSource.isTypeSupported(mimeCodec)) {
          this.sourceBuffer = this.mediaSource.addSourceBuffer(mimeCodec);
          
          this.sourceBuffer.addEventListener('updateend', () => {
            this.isUpdating = false;
            this.processQueue();
          });
          
          this.sourceBuffer.addEventListener('error', (e) => {
            console.error('[wsPlayer] SourceBuffer error:', e);
          });
          
          console.log('[wsPlayer] ✅ SourceBuffer created successfully');
        } else {
          console.error('[wsPlayer] ❌ 不支持的编码格式:', mimeCodec);
        }
      };
      
      // 监听 fmp4 segment
      this.mp4boxfile.onSegment = (id, user, buffer, sampleNum, is_last) => {
        if (this.sourceBuffer) {
          this.queue.push(buffer);
          this.processQueue();
        }
      };
      
      // 创建 WebSocket 连接
      this.connectWebSocket();
    });
  }

  connectWebSocket() {
    console.log('[wsPlayer] 连接 WebSocket:', this.url);
    this.ws = new WebSocket(this.url);
    this.ws.binaryType = 'arraybuffer';

    this.ws.onopen = () => {
      console.log('[wsPlayer] ✅ WebSocket connected');
    };

    this.ws.onmessage = (event) => {
      const data = event.data;
      
      // 将 ArrayBuffer 转换为 Uint8Array 并添加到 MP4Box
      const uint8Array = new Uint8Array(data);
      uint8Array.fileStart = this.mp4boxfile.appendBuffer(uint8Array);
      
      // 开始解析
      this.mp4boxfile.flush();
    };

    this.ws.onerror = (error) => {
      console.error('[wsPlayer] ❌ WebSocket error:', error);
    };

    this.ws.onclose = () => {
      console.log('[wsPlayer] WebSocket closed');
      if (this.sourceBuffer && !this.sourceBuffer.updating && this.mediaSource.readyState === 'open') {
        try {
          this.mediaSource.endOfStream();
        } catch (e) {
          console.warn('[wsPlayer] Failed to end MediaSource:', e);
        }
      }
    };
  }

  processQueue() {
    if (this.isUpdating || this.queue.length === 0 || !this.sourceBuffer) {
      return;
    }
    
    try {
      const buffer = this.queue.shift();
      this.sourceBuffer.appendBuffer(buffer);
      this.isUpdating = true;
      
      // 自动跳转到最新时间点（实现低延时）
      if (this.videoElement.buffered.length > 0) {
        const end = this.videoElement.buffered.end(0);
        if (end - this.videoElement.currentTime > 2) {
          this.videoElement.currentTime = end - 0.5;
        }
      }
    } catch (error) {
      console.error('[wsPlayer] appendBuffer error:', error);
    }
  }

  close() {
    console.log('[wsPlayer] 关闭播放器');
    
    if (this.ws && this.ws.readyState !== WebSocket.CLOSED) {
      this.ws.close();
    }
    
    if (this.videoElement) {
      this.videoElement.pause();
      this.videoElement.src = '';
      this.videoElement.load();
    }
    
    this.ws = null;
    this.mediaSource = null;
    this.sourceBuffer = null;
    this.mp4boxfile = null;
    this.queue = [];
  }
}

// 全局导出
window.WsPlayer = WsPlayer;










