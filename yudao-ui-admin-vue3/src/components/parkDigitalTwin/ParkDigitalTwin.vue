<template>
  <div class="scene park-digital-twin-root">
    <div ref="loadingWrap" class="park-twin-loading">
      正在加载模型请稍等，如果卡住，请刷新网页：<span ref="progressText"></span>
      <div class="jindu-con">
        <div ref="progressBar" class="park-twin-progress-fill"></div>
      </div>
    </div>
    <video
      ref="videoRef"
      class="park-twin-video"
      style="position: absolute; top: 0; left: 0; z-index: 100; visibility: hidden"
    ></video>
    <div ref="viewerHost" class="scene park-twin-viewer-host"></div>
    <div class="panel">
      <div class="main">
        <li class="tools-li" @click="resetScene">
          <p class="tools-name">场景重置</p>
        </li>
        <li class="tools-li" @click="autoRotateClick">
          <p class="tools-name">{{ !autoRotate ? '自动旋转' : '停止选择' }}</p>
        </li>
        <li class="tools-li" @click="billboardView">
          <p class="tools-name">视频视角</p>
        </li>
        <li class="tools-li" @click="driverView">
          <p class="tools-name">司机视角</p>
        </li>
      </div>
    </div>

  </div>

</template>

<script>
import modules from "./modules/index.js";
import * as THREE from "three";
import { CSS2DObject } from "three/examples/jsm/renderers/CSS2DRenderer.js";
import gsap from "gsap";
import { getChannel } from "@/api/iot/ibms/channel";
import { getLivePlayUrl, stopStream } from "@/api/iot/video/zlm";
import { adaptStreamPlayUrls, getDefaultPreferWebrtc, rewriteStreamPlayUrlsHost } from "@/composables/video/streamPlayUtils";
import useZlmPlayer from "@/composables/useZlmPlayer";

/** 实时预览流媒体对外域名：仅在明确配置时才覆盖后端返回 host */
const IBMS_STREAM_PUBLIC_HOST = (import.meta.env.VITE_STREAM_PUBLIC_HOST || "").trim()
const { playLive: playZlmLive, stopInstance: stopZlmInstance } = useZlmPlayer()

/** 建筑世界 AABB 在 y=max 平面上的四角（屋顶水平投影四角，与截图尖角对应） */
const CAM_CORNER_ROOF_TL = "roof_tl" // min.x, max.y, max.z — 常作「面向间隙一侧的左上」
const CAM_CORNER_ROOF_TL_NZ = "roof_tl_nz" // min.x, max.y, min.z
const CAM_CORNER_ROOF_TR = "roof_tr" // max.x, max.y, max.z — 办公大厅靠外侧右上
const CAM_CORNER_ROOF_TR_NZ = "roof_tr_nz" // max.x, max.y, min.z

let viewer = null
let office = null
/** 场景内 IBMS 摄像头 CSS2D 标记 */
let parkTwinIbmsCamMarkers = []
let oldOffice = {}
let gltf75 = {}
let tree_animate = null
let cityv1 = null
let modelSelectName = null
let modelMoveName = null
let isModelSelectName = false
let che, cheLable
// const gui = new dat.GUI();
export default {
  name: 'ParkDigitalTwin',
  data() {
    const base = import.meta.env.BASE_URL || '/'
    const normalized = base.endsWith('/') ? base : `${base}/`
    return {
      autoRotate: false,
      isDriver: false,
      /** 与 `public/park-twin` 目录对应，支持 Vite base 子路径部署 */
      assetBase: `${normalized}park-twin/`
    }
  },
  mounted() {
    this.init()
  },
  beforeUnmount() {
    this._closeIbmsInlinePreview()
    if (viewer && parkTwinIbmsCamMarkers.length) {
      parkTwinIbmsCamMarkers.forEach((m) => {
        if (m && m.parent) {
          m.parent.remove(m)
        }
      })
      parkTwinIbmsCamMarkers = []
    }
    if (this._ibmsPreviewHost && this._ibmsPreviewHost.parentNode) {
      this._ibmsPreviewHost.parentNode.removeChild(this._ibmsPreviewHost)
    }
    this._ibmsPreviewHost = null
    this._officeCamPreviewEls = null
    if (viewer) {
      viewer.dispose()
      viewer = null
    }
    office = null
    oldOffice = {}
    gltf75 = {}
    tree_animate = null
    cityv1 = null
    modelSelectName = null
    modelMoveName = null
    isModelSelectName = false
    che = undefined
    cheLable = undefined
  },
  methods: {
    /** 主场景 GLB 进度回调未必能报到 1.0（xhr.total/最后一帧），需在 onLoad 时同步关闭 */
    _hideParkTwinLoading() {
      const wrap = this.$refs.loadingWrap
      if (wrap) {
        wrap.style.display = 'none'
      }
    },
    //司机视角
    driverView() {
      this.isDriver = !this.isDriver
    },
    //切换广告牌视角
    billboardView() {
      this.isDriver = false
      gsap.to(viewer.camera.position, {
        x: 4,
        y: 20,
        z: 5,
        duration: 2,
        ease: "power1.inOut",
        onComplete: () => {
        },
      });
      gsap.to(viewer.controls.target, {
        x: 4,
        y: 20,
        z: -15,
        duration: 2,
        ease: "power1.inOut",
        onComplete: () => {
        },
      });
    },
    autoRotateClick() {
      viewer.controls.autoRotate = !viewer.controls.autoRotate
      this.autoRotate = viewer.controls.autoRotate
    },
    resetScene() {
      gsap.to(viewer.camera.position, {
        x: 17,
        y: 10,
        z: 52,
        duration: 2,
        ease: "Bounce.inOut",
      });
      gsap.to(viewer.controls.target, {
        x: 0,
        y: 0,
        z: 0,
        duration: 2,
        ease: "power1.inOut",
        onComplete: () => {
        },
      });
      gsap.to(viewer.scene.children.find(o => o.name == '人').rotation, {
        y: 0,
        duration: 2,
        ease: "power1.inOut",
      });
      this.isDriver = false
      cheLable.visible = true
      viewer.scene.children[viewer.scene.children.findIndex(o => o.name == '快递车')].visible = true
      viewer.scene.children[viewer.scene.children.findIndex(o => o.name == '树')].visible = true
      viewer.scene.children[viewer.scene.children.findIndex(o => o.name == 'cityv1')].visible = true
      viewer.scene.children[viewer.scene.children.findIndex(o => o.name == '实验楼')] = gltf75.clone()
      viewer.scene.children[viewer.scene.children.findIndex(o => o.name == '办公大厅')] = office.object = oldOffice.clone()
      modelSelectName = null
      modelMoveName = null
      isModelSelectName = false
    },

    /** 世界 AABB 屋顶角点（未吸附 mesh，仅水平角点） */
    _getRawRoofAabbCorner(box, cornerMode) {
      const min = box.min
      const max = box.max
      const center = box.getCenter(new THREE.Vector3())
      const corner = new THREE.Vector3()
      switch (cornerMode) {
        case CAM_CORNER_ROOF_TL_NZ:
          corner.set(min.x, max.y, min.z)
          break
        case CAM_CORNER_ROOF_TR_NZ:
          corner.set(max.x, max.y, min.z)
          break
        case CAM_CORNER_ROOF_TL:
          corner.set(min.x, max.y, max.z)
          break
        case CAM_CORNER_ROOF_TR:
        default:
          corner.set(max.x, max.y, max.z)
          break
      }
      // 极小内收，减少落在纯 AABB 角「楼板内侧」的概率
      corner.lerp(center, 0.015)
      corner.y += 0.2
      return corner
    },

    /** 世界坐标屋顶角点（与 Labels.addCss2dLabel 一致：直接用于 scene 根上的 CSS2D） */
    _getWorldBoxCornerForCamera(box, cornerMode) {
      return this._getRawRoofAabbCorner(box, cornerMode)
    },

    /** 共享预览 DOM（单例，挂到孪生根节点） */
    _initIbmsPreviewShell() {
      if (this._officeCamPreviewEls) {
        return
      }
      const host = document.createElement('div')
      host.className = 'park-twin-ibms-preview-host'
      host.style.cssText =
        'position:absolute;left:0;top:0;width:0;height:0;overflow:visible;pointer-events:none;z-index:0;'
      host.setAttribute('aria-hidden', 'true')
      host.innerHTML =
        '<div class="park-twin-nvr-inline-preview" hidden style="pointer-events:auto" role="dialog" aria-modal="true">' +
        '  <div class="park-twin-nvr-inline-preview__viewport">' +
        '    <div class="park-twin-nvr-inline-preview__player"></div>' +
        '    <div class="park-twin-nvr-inline-preview__loading" style="display:none">正在连接…</div>' +
        '    <div class="park-twin-nvr-inline-preview__error" style="display:none"></div>' +
        '    <button type="button" class="park-twin-nvr-inline-preview__close" aria-label="关闭预览">×</button>' +
        '  </div>' +
        '</div>'
      this.$el.appendChild(host)
      this._ibmsPreviewHost = host
      const panel = host.querySelector('.park-twin-nvr-inline-preview')
      this._officeCamPreviewEls = {
        panel,
        player: host.querySelector('.park-twin-nvr-inline-preview__player'),
        loading: host.querySelector('.park-twin-nvr-inline-preview__loading'),
        errorEl: host.querySelector('.park-twin-nvr-inline-preview__error')
      }
      const closeBtn = host.querySelector('.park-twin-nvr-inline-preview__close')
      if (closeBtn) {
        closeBtn.addEventListener('click', (e) => {
          e.stopPropagation()
          e.preventDefault()
          this._closeIbmsInlinePreview()
        })
      }
    },

    /**
     * @param {THREE.Object3D} object3d
     * @param {THREE.Box3} box
     * @param {number} channelId ibms_channel.id
     * @param {string} cornerMode CAM_CORNER_ROOF_* 之一
     */
    _mountIbmsCameraMarker(object3d, box, channelId, cornerMode) {
      if (!viewer || !object3d) {
        return
      }
      this._initIbmsPreviewShell()
      object3d.updateMatrixWorld(true)
      const corner = this._getWorldBoxCornerForCamera(box, cornerMode)
      const wrap = document.createElement('div')
      wrap.className = 'park-twin-nvr-cam'
      wrap.dataset.ibmsChannelId = String(channelId)
      wrap.title = `监控预览（通道 ${channelId}）`
      wrap.innerHTML =
        '<button type="button" class="park-twin-nvr-cam__btn" aria-label="打开监控">' +
        '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">' +
        '<path d="M17 10.5V7a1 1 0 0 0-1-1H4a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-3.5l4 4v-11l-4 4z"/>' +
        '</svg></button>'
      const btn = wrap.querySelector('button')
      if (btn) {
        btn.addEventListener('click', (e) => {
          e.stopPropagation()
          e.preventDefault()
          this.openIbmsChannelPreview(channelId, wrap)
        })
      }
      const cssObj = new CSS2DObject(wrap)
      cssObj.position.copy(corner)
      // 与「实验楼 / 办公大厅」文字标签相同：挂在 scene 根上、用 getBox() 世界 AABB 角点。
      // 建筑在场景中静止时最稳定；attach + 射线在复杂 mesh 上易产生错误世界点，司机视角下会像飘在空中。
      viewer.scene.add(cssObj)
      parkTwinIbmsCamMarkers.push(cssObj)
    },

    _showIbmsPreviewLoading(els, show) {
      if (els && els.loading) {
        els.loading.style.display = show ? 'flex' : 'none'
      }
    },

    _showIbmsPreviewError(els, show) {
      if (els && els.errorEl) {
        els.errorEl.style.display = show ? 'flex' : 'none'
      }
    },

    /** 预览挂到 body，避免被大屏 glass-panel / 顶栏 / 布局层叠遮挡 */
    _attachIbmsPreviewPortal() {
      const els = this._officeCamPreviewEls
      const anchor = this._ibmsPreviewAnchorEl
      if (!els || !els.panel || !anchor) {
        return
      }
      const panel = els.panel
      panel.classList.add('park-twin-nvr-inline-preview--portal')
      if (panel.parentNode !== document.body) {
        document.body.appendChild(panel)
      }
      this._officeHallPreviewPortalActive = true
      this._syncIbmsPreviewPanelPosition()
      this._scheduleIbmsPreviewPositionLoop()
    },

    _scheduleIbmsPreviewPositionLoop() {
      if (this._officeHallPreviewRafId) {
        cancelAnimationFrame(this._officeHallPreviewRafId)
        this._officeHallPreviewRafId = 0
      }
      const tick = () => {
        if (!this._officeHallPreviewPortalActive) {
          return
        }
        this._syncIbmsPreviewPanelPosition()
        this._officeHallPreviewRafId = requestAnimationFrame(tick)
      }
      this._officeHallPreviewRafId = requestAnimationFrame(tick)
    },

    _syncIbmsPreviewPanelPosition() {
      const els = this._officeCamPreviewEls
      const anchor = this._ibmsPreviewAnchorEl
      if (!els || !els.panel || !this._officeHallPreviewPortalActive || !anchor) {
        return
      }
      const rect = anchor.getBoundingClientRect()
      const panel = els.panel
      const margin = 10
      const vw = window.innerWidth
      const vh = window.innerHeight
      const pw = Math.min(280, vw - margin * 2)
      panel.style.width = `${pw}px`
      const ph = Math.max(panel.getBoundingClientRect().height, 168)
      let left = rect.left + rect.width / 2 - pw / 2
      let top = rect.top - ph - margin
      if (top < margin) {
        top = rect.bottom + margin
      }
      if (top + ph > vh - margin) {
        top = Math.max(margin, vh - ph - margin)
      }
      if (left < margin) {
        left = margin
      }
      if (left + pw > vw - margin) {
        left = vw - pw - margin
      }
      panel.style.left = `${Math.round(left)}px`
      panel.style.top = `${Math.round(top)}px`
    },

    _detachIbmsPreviewPortal() {
      this._officeHallPreviewPortalActive = false
      if (this._officeHallPreviewRafId) {
        cancelAnimationFrame(this._officeHallPreviewRafId)
        this._officeHallPreviewRafId = 0
      }
      const els = this._officeCamPreviewEls
      const host = this._ibmsPreviewHost
      if (!els || !els.panel || !host) {
        return
      }
      const panel = els.panel
      panel.classList.remove('park-twin-nvr-inline-preview--portal')
      panel.style.left = ''
      panel.style.top = ''
      panel.style.width = ''
      if (panel.parentNode === document.body) {
        host.appendChild(panel)
      }
    },

    _destroyIbmsMpegtsPlayer() {
      const zlmInst = this._officeHallZlmInstance
      if (zlmInst) {
        try {
          stopZlmInstance(zlmInst)
        } catch (_) {}
        this._officeHallZlmInstance = null
      }
      const els = this._officeCamPreviewEls
      if (els && els.player) {
        els.player.innerHTML = ''
      }
    },

    _closeIbmsInlinePreview() {
      const activeId = this._activeIbmsPreviewChannelId
      this._detachIbmsPreviewPortal()
      const els = this._officeCamPreviewEls
      if (els && els.panel) {
        els.panel.hidden = true
      }
      this._showIbmsPreviewLoading(els, false)
      if (els && els.errorEl) {
        els.errorEl.textContent = ''
        this._showIbmsPreviewError(els, false)
      }
      this._destroyIbmsMpegtsPlayer()
      if (activeId != null) {
        stopStream(activeId).catch(() => {})
      }
      this._activeIbmsPreviewChannelId = null
      this._ibmsPreviewAnchorEl = null
    },

    async _startIbmsInlineStream(channelId, els) {
      this._destroyIbmsMpegtsPlayer()
      if (!els || !els.player) {
        return
      }
      this._showIbmsPreviewLoading(els, true)
      els.errorEl.textContent = ''
      this._showIbmsPreviewError(els, false)
      try {
        // 与实时预览保持一致：先尝试子码流，失败后回退主码流
        const streamTypes = [1, 0]
        let lastError = null
        for (const streamType of streamTypes) {
          try {
            const raw = await getLivePlayUrl(channelId, streamType)
            const adapted = adaptStreamPlayUrls(raw) || raw
            const playUrls = IBMS_STREAM_PUBLIC_HOST
              ? rewriteStreamPlayUrlsHost(adapted, IBMS_STREAM_PUBLIC_HOST)
              : adapted
            if (!playUrls || (!playUrls.webrtcUrl && !playUrls.wsFlvUrl && !playUrls.hlsUrl && !playUrls.flvUrl)) {
              throw new Error('未获取到播放地址')
            }
            this._officeHallZlmInstance = await playZlmLive({
              container: els.player,
              urls: {
                wsFlvUrl: playUrls.wsFlvUrl,
                webrtcUrl: playUrls.webrtcUrl
              },
              preferWebrtc: getDefaultPreferWebrtc()
            })
            this._showIbmsPreviewLoading(els, false)
            return
          } catch (err) {
            lastError = err
            console.warn(`[ParkDigitalTwin] streamType=${streamType} 拉流失败，尝试回退`, err)
          }
        }
        throw lastError || new Error('视频连接失败')
      } catch (e) {
        console.error('[ParkDigitalTwin] 拉流失败', e)
        this._showIbmsPreviewLoading(els, false)
        els.errorEl.textContent = (e && e.message) || '视频连接失败'
        this._showIbmsPreviewError(els, true)
      }
    },

    async openIbmsChannelPreview(channelId, anchorWrap) {
      const els = this._officeCamPreviewEls
      if (!els || !els.panel) {
        return
      }
      if (!els.panel.hidden && this._activeIbmsPreviewChannelId === channelId) {
        this._closeIbmsInlinePreview()
        return
      }
      const prevId = this._activeIbmsPreviewChannelId
      const wasOpen = !els.panel.hidden
      if (wasOpen && prevId != null && prevId !== channelId) {
        this._destroyIbmsMpegtsPlayer()
        stopStream(prevId).catch(() => {})
      }
      this._activeIbmsPreviewChannelId = channelId
      this._ibmsPreviewAnchorEl = anchorWrap
      els.panel.removeAttribute('hidden')
      els.panel.hidden = false
      this._showIbmsPreviewError(els, false)
      els.errorEl.textContent = ''
      this._attachIbmsPreviewPortal()
      this._showIbmsPreviewLoading(els, true)
      try {
        const ch = await getChannel(channelId)
        const name = (ch && (ch.name || ch.deviceName)) || `通道 #${channelId}`
        els.panel.setAttribute('aria-label', `监控预览：${name}`)
      } catch (e) {
        console.warn('[ParkDigitalTwin] 获取通道详情失败，使用默认标题', e)
        els.panel.setAttribute('aria-label', `监控预览：通道 ${channelId}`)
      }
      await this._startIbmsInlineStream(channelId, els)
    },

    init() {
      const jindu_text_con = this.$refs.loadingWrap
      const jindu_text = this.$refs.progressText
      const jindu = this.$refs.progressBar

      viewer = new modules.Viewer(this.$refs.viewerHost, { assetBase: this.assetBase })
      const asset = (p) => viewer.assetBase + String(p).replace(/^\/+/, '')
      // viewer.addAxis()
      let labels = new modules.Labels(viewer) //初始化场景
      let skyBoxs = new modules.SkyBoxs(viewer)//添加天空盒和雾化效果
      // let EffectComposer = new modules.EffectComposer(viewer)//添加天空盒和雾化效果
      skyBoxs.addSkybox(2)
      viewer.camera.position.set(17, 10, 52) //设置相机位置
      //限制controls的上下角度范围
      viewer.controls.maxPolarAngle = Math.PI / 2.1;
      let lights = new modules.Lights(viewer)
      let ambientLight = lights.addAmbientLight()
      ambientLight.setOption({color: 0xffffff, intensity: 1})
      lights.addDirectionalLight([100, 100, -10], {
        color: 'rgb(253,253,253)',
        intensity: 3,
        castShadow: true,
        mapSize: 1024
      })
      let modeloader = new modules.ModelLoder(viewer)
      const video = this.$refs.videoRef
      video.src = asset('bi.mp4')
      video.autoplay = "autoplay"; //要设置播放
      video.loop = "loop"; //要设置循环播放
      video.muted = "muted"; //要设置静音
      let texture = new THREE.VideoTexture(video)

      //停车场栅栏
      let Mesh26
      let isopen = false
      let tiemen = {}
      modeloader.loadModelToScene(asset('city-v1.glb'), _model => {
        this._hideParkTwinLoading()
        _model.object.name = 'cityv1'
        _model.openCastShadow()
        _model.openReceiveShadow()
        _model.object.children.forEach((item, index) => {
          if (item.name === 'Mesh26') {
            //平移
            Mesh26 = item
            gsap.to(item.scale, {
              x: item.scale.x / 8,
              duration: 5,
              ease: "power1.inOut",
              onComplete: () => {
                // 离开首页后 viewer 已 dispose，避免异步回调访问 null
                if (!viewer || !viewer.scene) {
                  return
                }
                makeCurve()
                isopen = true
              }
            });
          }
        })
        tiemen = tiemen = {
          fun: moveOnCurve,
          content: che
        }
        viewer.addAnimate(tiemen)
        cityv1 = _model.object.clone()
      }, (progress) => {
        const ratio = typeof progress === 'string' ? parseFloat(progress) : Number(progress)
        const pct = Number.isFinite(ratio) ? Math.min(100, Math.round(ratio * 100)) : 0
        if (jindu_text) {
          jindu_text.textContent = pct + '%'
        }
        if (jindu) {
          jindu.style.width = pct + '%'
        }
        // 最后一帧常见为 0.98~0.99，不能依赖严格等于 100
        if (pct >= 99 || ratio >= 0.99) {
          this._hideParkTwinLoading()
        }
      }, (error) => {
        console.log(error)
        this._hideParkTwinLoading()
      })

      modeloader.loadModelToScene(asset('zuo.glb'), _model => {
        office = _model
        office.openCastShadow()
        office.openReceiveShadow()
        //旋转360度
        office.object.rotation.y = Math.PI
        office.object.position.set(16, 0, -5)
        office.object.scale.set(0.2, 0.2, 0.2)
        office.object.name = '办公大厅'
        office.object.children.forEach(item => {
          item.name = item.name.replace('zuo', '')
          if (item.name == 'ding') {
            item.name = 6
          }
          item.name--
        })
        office.object.children.sort((a, b) => a.name - b.name).forEach(v => {
          v.name = 'zuo' + v.name
        })
        office.forEach(child => {
          if (child.isMesh) {
            child.frustumCulled = false
            child.material.emissive = child.material.color;
            child.material.emissiveMap = child.material.map;
            child.material.emissiveIntensity = 1.2
            child.material.envmap = viewer.scene.background
          }
        })
        oldOffice = office.object.clone()
        let box = office.getBox()
        labels.addCss2dLabel({
          x: office.object.position.x,
          y: box.max.y,
          z: box.max.z - 5
        }, `<span class="label">${_model.object.name}</span>`)
        gsap.to(labels.label.position, {
          y: box.max.y + 2,
          repeat: -1,
          yoyo: true,
          duration: 2,
          ease: "Bounce.inOut",
        });
        this._mountIbmsCameraMarker(office.object, box, 3048, CAM_CORNER_ROOF_TR)
        this._mountIbmsCameraMarker(office.object, box, 3051, CAM_CORNER_ROOF_TL)
      })

      modeloader.loadModelToScene(asset('75.gltf'), _model => {
        _model.openCastShadow()
        _model.openReceiveShadow()
        _model.object.rotateY(Math.PI / 2)
        _model.object.position.set(-17, 0, 5)
        _model.object.scale.set(0.7, 0.7, 0.7)
        _model.object.name = '实验楼'
        gltf75 = _model.object.clone()
        let box = _model.getBox()
        console.log(_model)
        labels.addCss2dLabel({
          x: _model.object.position.x,
          y: box.max.y,
          z: _model.object.position.z
        }, `<span class="label">${_model.object.name}</span>`)
        gsap.to(labels.label.position, {
          y: box.max.y + 2,
          repeat: -1,
          yoyo: true,
          duration: 2,
          ease: "Bounce.inOut",
        });
        // 截图：实验楼屋顶「左上」尖角 → 用 TL（若仍偏可改 CAM_CORNER_ROOF_TL_NZ）
        this._mountIbmsCameraMarker(_model.object, box, 3048, CAM_CORNER_ROOF_TL)
      })

      modeloader.loadModelToScene(asset('billboard_-_lowpoly.glb'), _model => {
        _model.openCastShadow()
        _model.object.position.set(4, -20, -35)
        _model.object.rotateY(-Math.PI / 2)
        _model.object.scale.set(2.7, 2.7, 2.7)
        _model.object.name = '广告牌'
        let Object_6 = _model.object.getObjectByName('Object_6')
        Object_6.material = new THREE.MeshBasicMaterial({
          map: texture, // 设置纹理贴图
          side: THREE.DoubleSide,
          transparent: true,
        }); //材质对象Material
        let box = _model.getBox()
        modeloader.loadModelToScene(asset('drone/wrj.glb'), (res) => {
          res.openCastShadow()
          res.object.position.set(16, 12, 5)
          res.object.scale.set(0.3, 0.3, 0.3)
          res.object.name = '无人机'
          res.startAnima(0)
          gsap.to(res.object.position, {
            x: _model.object.position.x,
            y: box.max.y,
            z: _model.object.position.z,
            repeat: -1,
            yoyo: true,
            duration: 13,
            ease: "Expo.inOut",
          })
        })
      })

      modeloader.loadModelToScene(asset('car13.gltf'), _model => {
        che = _model
        _model.openCastShadow()
        _model.openReceiveShadow()
        _model.object.position.set(11.5, 0, 18)
        _model.object.scale.set(1, 1, 1)
        _model.object.name = '快递车'
        let boxx = _model.getBox()
        // let center = boxx.getCenter(new THREE.Vector3())
        // // //相机跟随
        // viewer.camera.position.set(center.x, center.y, center.z)
        // viewer.camera.lookAt(center)
        cheLable = labels.addCss2dLabel({
          x: boxx.max.x,
          y: boxx.max.y + 2,
          z: boxx.max.z
        }, `<span class="label">${_model.object.name}</span>`)
      })

      modeloader.loadModelToScene(asset('ren.glb'), _model => {
        _model.openCastShadow()
        _model.object.position.set(13, 0, 15)
        _model.object.name = '人'
        _model.startAnima(1)
        _model.cloneModel([25, 0, 29]).startAnima()
      })

      modeloader.loadModelToScene(asset('tree_animate/scene.gltf'), _model => {
        _model.openCastShadow()
        _model.object.position.set(8, 0, 26)
        _model.object.scale.set(0.08, 0.08, 0.08)
        _model.object.name = '树'
        _model.startAnima()
        tree_animate = _model.object.clone()
      })

      let curve = null;

      function makeCurve() {
        if (!viewer || !viewer.scene) {
          return
        }
        //Create a closed wavey loop
        curve = new THREE.CatmullRomCurve3([
          new THREE.Vector3(11.5, 0, 18),
          new THREE.Vector3(11.5, 0, 34),
          new THREE.Vector3(35, 0, 34),
          new THREE.Vector3(35, 0, 31),
          new THREE.Vector3(11.5, 0, 31),
        ]);
        curve.curveType = "catmullrom";
        curve.closed = true;//设置是否闭环
        curve.tension = 0; //设置线的张力，0为无弧度折线

        // 为曲线添加材质在场景中显示出来，不显示也不会影响运动轨迹，相当于一个Helper
        const points = curve.getPoints(0.1);
        const geometry = new THREE.BufferGeometry().setFromPoints(points);
        const material = new THREE.LineBasicMaterial({
          color: 0xff0000,
        });

        // Create the final object to add to the scene
        const curveObject = new THREE.Line(geometry, material);
        curveObject.position.y = -1;
        viewer.scene.add(curveObject)
      }


      let progress = 0; // 物体运动时在运动路径的初始位置，范围0~1
      const velocity = 0.001; // 影响运动速率的一个值，范围0~1，需要和渲染频率结合计算才能得到真正的速率
      // 物体沿线移动方法
      const moveOnCurve = (_model) => {
        if (!viewer || !viewer.scene) {
          return
        }
        if (curve == null || che == null) {
        } else {
          if (progress <= 1 - velocity) {
            let che = _model.object
            let boxx = _model.getBox()
            cheLable.position.set(boxx.max.x, boxx.max.y + 2, boxx.max.z)
            if (che.position.z.toFixed(2) >= 28.00 && che.position.z.toFixed(2) <= 28.10) {
              if (isopen) {
                gsap.to(Mesh26.scale, {
                  x: Mesh26.scale.x * 8,
                  duration: 5,
                  ease: "power1.inOut",
                  onComplete: () => {
                    isopen = false
                  },
                });
              } else {
                gsap.to(Mesh26.scale, {
                  x: Mesh26.scale.x / 8,
                  duration: 5,
                  ease: "power1.inOut",
                  onComplete: () => {
                    isopen = true
                    if (viewer && viewer.addAnimate) {
                      viewer.addAnimate(tiemen)
                    }
                  },
                  onStart: () => {
                    if (viewer && viewer.removeAnimate) {
                      viewer.removeAnimate(tiemen)
                    }
                  },
                });
              }
            }
            const point = curve.getPointAt(progress); //获取样条曲线指定点坐标
            const pointBox = curve.getPointAt(progress + velocity); //获取样条曲线指定点坐标

            if (point && pointBox) {
              che.position.set(point.x, point.y, point.z);
              che.lookAt(pointBox.x, pointBox.y, pointBox.z);//因为这个模型加载进来默认面部是正对Z轴负方向的，所以直接lookAt会导致出现倒着跑的现象，这里用重新设置朝向的方法来解决。
              let center = _model.getBox().getCenter(new THREE.Vector3())
              // viewer.camera.position.copy(pointBox)
              // viewer.camera.lookAt(point)
              // viewer.controls.target.set(pointBox.x, center.y, pointBox.z+10)
              if (this.isDriver) {
                viewer.camera.position.set(point.x, point.y + 2, point.z)
                viewer.camera.lookAt(pointBox.x, pointBox.y + 2, pointBox.z)
                viewer.controls.target.set(pointBox.x, pointBox.y + 2, pointBox.z)
              }
              let targetPos = pointBox   //目标位置点
              let offsetAngle = 22 //目标移动时的朝向偏移

              // //以下代码在多段路径时可重复执行
              let mtx = new THREE.Matrix4()  //创建一个4维矩阵
              // .lookAt ( eye : Vector3, target : Vector3, up : Vector3 ) : this,构造一个旋转矩阵，从eye 指向 target，由向量 up 定向。
              mtx.lookAt(che.position, targetPos, che.up) //设置朝向
              mtx.multiply(new THREE.Matrix4().makeRotationFromEuler(new THREE.Euler(0, offsetAngle, 0)))
              let toRot = new THREE.Quaternion().setFromRotationMatrix(mtx)  //计算出需要进行旋转的四元数值
              che.quaternion.slerp(toRot, 0.2)
            }
            progress += velocity;
          } else {
            progress = 0;
          }
        }
      }

      let modelSelect = ['zuo0', 'zuo1', 'zuo2', 'zuo3', 'zuo4', 'zuo5']
      viewer.startSelectEvent('mousemove', false, (model) => {
        if (model.parent && model.parent.parent && model.parent.parent.name == '办公大厅') {
          modelSelect.forEach((item) => {
            if (item == model.parent.name) {
              modelMoveName = item
              if (modelSelectName == modelMoveName) return
              office.object.getObjectByName(item).traverse(function (child) {
                if (child.isMesh) {
                  child.material = new THREE.MeshPhongMaterial({
                    color: 'yellow',
                    transparent: true,
                    opacity: 0.8,
                    emissive: child.material.color,
                    emissiveMap: child.material.map,
                    emissiveIntensity: 3
                  })
                }
              })
            } else {
              if (!isModelSelectName) {
                let oldmodel = oldOffice.getObjectByName(item)
                office.object.getObjectByName(item).traverse(function (child) {
                  if (child.isMesh) {
                    child.material = oldmodel.getObjectByName(child.name).material
                  }
                })
              } else {
                office.object.getObjectByName(item).traverse(function (child) {
                  if (child.isMesh && child.parent.name != modelSelectName) {
                    child.material = new THREE.MeshPhongMaterial({
                      color: new THREE.Color('#123ca8'),
                      transparent: true,
                      opacity: 0.5,
                      emissiveMap: child.material.map,
                    })
                  }
                })
              }
            }
          })
        }
      })

      let sceneList = ['实验楼']
      viewer.renderer.domElement.addEventListener('click', (e) => {
        const raycaster = new THREE.Raycaster()
        const mouse = new THREE.Vector2()
        mouse.x = (e.offsetX / viewer.renderer.domElement.clientWidth) * 2 - 1
        mouse.y = -(e.offsetY / viewer.renderer.domElement.clientHeight) * 2 + 1
        raycaster.setFromCamera(mouse, viewer.camera)
        const intersects = raycaster.intersectObject(viewer.scene, true)
        if (intersects.length > 0 && intersects[0] && modelMoveName) {
          let model = intersects[0].object.parent
          if (model.name.includes('zuo')) {
            if (!isModelSelectName) {
              cheLable.visible = false
              viewer.scene.children[viewer.scene.children.findIndex(o => o.name == '快递车')].visible = false
              viewer.scene.children[viewer.scene.children.findIndex(o => o.name == 'cityv1')].visible = false
              viewer.scene.children[viewer.scene.children.findIndex(o => o.name == '树')].visible = false
              sceneList.forEach(item => {
                viewer.scene.children.find(o => o.name == item).traverse((child) => {
                  child.material = new THREE.MeshPhongMaterial({
                    color: new THREE.Color('rgba(7,32,96,0.76)'),
                    transparent: true,
                    opacity: 0.1,
                    wireframe: true,
                    depthWrite: true, // 无法被选择，鼠标穿透
                  })
                })
              })
              gsap.to(viewer.scene.children.find(o => o.name == '人').rotation, {
                y: Math.PI,
                duration: 2,
                ease: "power1.inOut",
                onComplete: () => {
                  isModelSelectName = true
                },
              });
            }
            selectOffice(model)
          }
          if (!model.name.includes('zuo')) {
            if (!isModelSelectName) {
              let oldmodel = oldOffice.getObjectByName(modelMoveName)
              office.object.getObjectByName(modelMoveName).traverse(function (child) {
                if (child.isMesh) {
                  child.material = oldmodel.getObjectByName(child.name).material
                }
              })
            }
          }
        }
      })

      const selectOffice = (model) => {
        modelSelectName = model.name
        let oldmodel = oldOffice.getObjectByName(modelSelectName)
        let modelSelectIndex = modelSelect.findIndex(v => v == modelSelectName)
        office.object.children.forEach((child, index) => {
          child.children.forEach((Mesh) => {
            if (child.name === modelSelectName) {
              child.children.forEach(Mesh => {
                Mesh.material = oldmodel.getObjectByName(Mesh.name).material
              })
            } else {
              Mesh.material = new THREE.MeshPhongMaterial({
                color: new THREE.Color('#123ca8'),
                transparent: true,
                opacity: 0.5,
                emissiveMap: Mesh.material.map,
              })
            }
          })
          if (!model.userData.position && index > modelSelectIndex) {
            gsap.to(child.position, {
              y: !child.userData.position ? child.position.y + 25 : child.position.y,
              duration: 2,
              ease: "power1.inOut",
              onComplete: () => {
                child.userData.position = true
              },
            });
          }
          if (model.userData.position && index <= modelSelectIndex) {
            if (child.userData.position) {
              gsap.to(child.position, {
                y: oldOffice.getObjectByName(child.name).position.y,
                duration: 2,
                ease: "power1.inOut",
                onComplete: () => {
                  child.userData.position = false
                },
              });
            }
          }
        })
        gsap.to(viewer.controls.target, {
          x: 12,
          y: 0,
          z: -5,
          duration: 2,
          ease: "power1.inOut",
          onComplete: () => {
          },
        });
        gsap.to(viewer.camera.position, {
          x: 12,
          y: 18,
          z: 38,
          duration: 2,
          ease: "power1.inOut",
          onComplete: () => {
          },
        });
      }
    },
  },
}
</script>

<style lang="scss">
//定义全局颜色
$color: #123ca8;
.scene {
  width: 100%;
  height: 100%;
  min-height: 0;
  position: relative;

  &.park-digital-twin-root {
    overflow: hidden;
  }

  .park-twin-viewer-host {
    position: absolute;
    inset: 0;
    z-index: 1;
  }

  /* 加载层须高于底部工具栏（.panel z-index: 8） */
  .park-twin-loading {
    z-index: 20;
  }

  /* CSS2D 场景标签（实验楼 / 办公大厅 / 快递车等） */
  .label {
    display: inline-block;
    padding: 8px 10px;
    font-size: 12px;
    line-height: 1.2;
    background: $color;
    color: aliceblue;
    border-radius: 3px;
    cursor: pointer;
    box-sizing: border-box;
  }

  /* 摄像头预览 DOM 可能挂到 body，用 @at-root 避免必须挂在 .scene 下才能匹配样式 */
  @at-root {
    /* 父级 labelRenderer 为 pointer-events:none，子元素设为 auto 才可点击 */
    .park-twin-nvr-cam {
      position: relative;
      pointer-events: auto;
    }

    /* 未打开门户时：预览叠在 CSS2D 锚点上（备用，当前打开时会移到 body） */
    .park-twin-nvr-inline-preview {
      position: absolute;
      left: 50%;
      bottom: calc(100% + 10px);
      transform: translateX(-50%);
      width: 280px;
      max-width: min(40vw, 320px);
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 8px 28px rgba(0, 0, 0, 0.5);
      background: rgb(12, 18, 36);
      border: 1px solid rgba(90, 150, 255, 0.45);
      z-index: 12;
      pointer-events: auto;
    }

    /* 打开预览：挂到 body，压过后台 Layout / 大屏统计栏 / glass-panel 层叠 */
    .park-twin-nvr-inline-preview--portal {
      position: fixed !important;
      left: 0;
      top: 0;
      bottom: auto !important;
      transform: none !important;
      z-index: 99990 !important;
      max-width: min(92vw, 400px) !important;
      width: 280px !important;
      box-shadow: 0 16px 48px rgba(0, 0, 0, 0.65);
      isolation: isolate;
    }

    .park-twin-nvr-inline-preview__viewport {
      position: relative;
      width: 100%;
      aspect-ratio: 16 / 9;
      background: #000;
      border-radius: inherit;
    }

    /* 无顶栏：关闭按钮叠在画面右上角 */
    .park-twin-nvr-inline-preview__close {
      position: absolute;
      top: 6px;
      right: 6px;
      z-index: 8;
      width: 30px;
      height: 30px;
      padding: 0;
      border: 1px solid rgba(255, 255, 255, 0.35);
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.55);
      color: #fff;
      font-size: 18px;
      line-height: 1;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.35);
      transition: background 0.15s ease, transform 0.15s ease;
    }

    .park-twin-nvr-inline-preview__close:hover {
      background: rgba(180, 30, 30, 0.85);
      transform: scale(1.06);
    }

    .park-twin-nvr-inline-preview__player {
      display: block;
      width: 100%;
      height: 100%;
    }

    .park-twin-nvr-inline-preview__player video {
      width: 100%;
      height: 100%;
      object-fit: contain;
      vertical-align: top;
      opacity: 1;
      mix-blend-mode: normal;
      filter: brightness(1.08) contrast(1.05);
    }

    .park-twin-nvr-inline-preview__loading,
    .park-twin-nvr-inline-preview__error {
      position: absolute;
      inset: 0;
      z-index: 4;
      align-items: center;
      justify-content: center;
      padding: 8px;
      font-size: 12px;
      color: #fff;
      text-align: center;
      background: rgba(0, 0, 0, 0.45);
      box-sizing: border-box;
    }

    .park-twin-nvr-inline-preview__error {
      color: #ffc9c9;
    }
  }

  .park-twin-nvr-cam__btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    padding: 0;
    border: none;
    border-radius: 50%;
    background: rgba(18, 60, 168, 0.92);
    color: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.35);
    cursor: pointer;
    transition:
      transform 0.15s ease,
      background 0.15s ease;
  }

  .park-twin-nvr-cam__btn:hover {
    transform: scale(1.08);
    background: rgba(0, 120, 212, 0.95);
  }

  .jindu-con {
    width: 300px;
    height: 10px;
    border-radius: 50px;
    background-color: white;
    margin-top: 10px;
    overflow: hidden;
  }

  .park-twin-progress-fill {
    height: inherit;
    background-color: #007bff;
    width: 0;
  }

  .park-twin-loading {
    width: 300px;
    position: absolute;
    left: 0;
    right: 0;
    margin: 0 auto;
    top: 15%;
    text-align: center;
    background-color: rgba(255, 255, 255, 0.5);
    padding: 10px;
  }

  /* 必须高于 .park-twin-viewer-host (z-index:1)，否则 canvas 会盖住整行按钮 */
  .panel {
    z-index: 8;
    pointer-events: auto;
    margin: 0 auto;
    padding: 0;
    box-sizing: border-box;
    bottom: 10px;
    position: absolute;
    opacity: 0.8;
    width: 100%;
    left: 0;
    right: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    .main {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
      border-radius: 4px;
      opacity: 0.96;
      border: 1px solid #14171c;
      background: linear-gradient(0deg, #1e202a 0%, #0d1013 100%);
      box-shadow: 0px 2px 21px 0px rgba(33, 34, 39, 0.55);

      li {
        padding: 5px 10px;
        box-sizing: border-box;
        list-style: none;
        cursor: pointer;
        border: 1px solid #313642;
        border-radius: 2px;
        float: left;
        margin: 5px;
        position: relative;
        width: 70px;

        p {
          list-style: none;
          cursor: pointer;
          margin: 0;
          padding: 0;
          box-sizing: border-box;
          height: 20px;
          text-align: center;
          font-size: 12px;
          font-weight: 400;
          color: #fbfbfb;
          display: block;
        }
      }
    }
  }
}
</style>
