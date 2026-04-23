import {
    Cache,
    WebGLRenderer,
    SRGBColorSpace,
    PCFShadowMap,
    PerspectiveCamera,
    Scene,
    Color,
    AxesHelper
} from 'three'
// 二维标签渲染器
import {CSS2DRenderer} from 'three/examples/jsm/renderers/CSS2DRenderer'
import {CSS3DRenderer} from 'three/examples/jsm/renderers/CSS3DRenderer'

import TWEEN from '@tweenjs/tween.js'
import {OrbitControls} from 'three/examples/jsm/controls/OrbitControls.js'
import Stats from 'three/examples/jsm/libs/stats.module.js'
import MouseEvent from '../MouseEvent'
import SkyBoxs from '../SkyBoxs'
import Lights from '../Lights'

export default class Viewer {
    /**
     * @param domOrId 场景容器：HTMLElement 或元素 id 字符串
     * @param options.assetBase 静态资源前缀，如 `/park-twin/`
     */
    constructor(domOrId, options = {}) {
        Cache.enabled = true // 开启缓存
        this._domRef = domOrId
        let base = options.assetBase || ''
        if (base && !base.endsWith('/')) {
            base += '/'
        }
        this.assetBase = base
        this.renderer = undefined
        this.scene = undefined
        this.camera = undefined
        this.controls = undefined
        this.statsControls = undefined
        this.animateEventList = []
        this._disposed = false
        this._raf = 0
        /** 仅尺寸变化时重设画布，避免每帧 setSize 导致卡顿 */
        this._lastSizeW = -1
        this._lastSizeH = -1
        this._lastDpr = -1
        this._initViewer()
    }

    /**
     * 添加坐标轴
     */
    addAxis() {
        const axis = new AxesHelper(1000)
        this.scene.add(axis)
    }

    /**
     * 添加状态监测
     */
    addStats() {
        if (!this.statsControls) this.statsControls = new Stats()
        this.statsControls.dom.style.position = 'absolute'
        this.viewerDom.appendChild(this.statsControls.dom)
        // 添加到动画
        this.statsUpdateObject = {
            fun: this._statsUpdate,
            content: this.statsControls
        }
        this.addAnimate(this.statsUpdateObject)
    }

    /**
     * 移除状态检测
     */
    removeStats() {
        if (this.statsControls) this.viewerDom.removeChild(this.statsControls.dom)
        // 添加到动画
        this.statsUpdateObject = {
            fun: this._statsUpdate,
            content: this.statsControls
        }
        this.removeAnimate(this.statsUpdateObject)
    }

    dispose() {
        this._disposed = true
        if (this._raf) {
            cancelAnimationFrame(this._raf)
            this._raf = 0
        }
        this.stopSelectEvent()
        if (this.mouseEvent) {
            this.mouseEvent = null
        }
        if (!this.scene || !this.renderer) {
            return
        }
        this.scene.traverse((child) => {
            if (child.material) {
                const mats = Array.isArray(child.material) ? child.material : [child.material]
                mats.forEach((m) => m && m.dispose && m.dispose())
            }
            if (child.geometry) {
                child.geometry.dispose()
            }
        })
        try {
            this.renderer.forceContextLoss()
        } catch (e) {
            /* ignore */
        }
        this.renderer.dispose()
        this.scene.clear()
        if (this.viewerDom && this.renderer?.domElement?.parentNode === this.viewerDom) {
            this.viewerDom.removeChild(this.renderer.domElement)
        }
        if (this.viewerDom && this.labelRenderer?.domElement?.parentNode === this.viewerDom) {
            this.viewerDom.removeChild(this.labelRenderer.domElement)
        }
        if (this.viewerDom && this.css3DRenderer?.domElement?.parentNode === this.viewerDom) {
            this.viewerDom.removeChild(this.css3DRenderer.domElement)
        }
    }

    /**
     * 添加全局的动画事件
     * @param animate 函数加参数对象
     * 传入对象 = {
            fun: 函数名称,
            content: 函数参数
        }
     */
    addAnimate(animate) {
        this.animateEventList.push(animate)
    }

    /**
     * 移除全局的动画事件
     * @param animate 函数加参数对象
     * 传入对象 = {
            fun: 函数名称,
            content: 函数参数
        }
     */
    removeAnimate(animate) {
        this.animateEventList.map((val, i) => {
            if (val === animate) this.animateEventList.splice(i, 1)
        })
    }

    /**
     * 开启鼠标事件
     * @param mouseType
     * @param isSelect
     * @param callback
     */
    startSelectEvent(mouseType, isSelect, callback) {
        if (!this.mouseEvent) this.mouseEvent = new MouseEvent(this, isSelect, callback, mouseType)
        this.mouseEvent.startSelect()
    }

    /**
     * 关闭鼠标事件
     */
    stopSelectEvent() {
        if (this.mouseEvent) {
            this.mouseEvent.stopSelect()
        }
    }

    /**
     * 设置背景颜色
     * @param color rgb(4,4,4)
     */
    setBackground(color = 'rgb(4,4,4)') {
        this.scene.background = new Color(color)
    }

    /**
     * 状态更新
     * @param statsControls
     */
    _statsUpdate(statsControls) {
        statsControls.update()
    }

    _initViewer() {
        this._initRenderer()
        // 渲染相机
        this._initCamera()
        // 渲染场景
        this._initScene()
        // 控制器
        this._initControl()
        // 天空盒
        this._initSkybox()
        // 环境光
        this._initLight()
        // 全局调试器
        const that = this

        function animate() {
            if (that._disposed) {
                return
            }
            that._raf = requestAnimationFrame(animate)
            that._undateDom()
            TWEEN.update(performance.now())
            that._readerDom()
            that.animateEventList.forEach((event) => {
                event.fun && event.content && event.fun(event.content)
            })
        }

        animate()
    }

    /**
     * 创建初始化场景界面
     */
    _initRenderer() {
        this.viewerDom =
            typeof this._domRef === 'string' ? document.getElementById(this._domRef) : this._domRef
        if (!this.viewerDom) {
            throw new Error('[ParkDigitalTwin] viewer container DOM 未找到')
        }
        // 初始化渲染器
        this.renderer = new WebGLRenderer({
            logarithmicDepthBuffer: true,
            antialias: false,
            powerPreference: 'high-performance',
            alpha: true,
            precision: 'mediump',
            premultipliedAlpha: true
        })
        this.renderer.clearDepth();
        // this.renderer.domElement.style.zIndex = 1
        // 默认情况下，js的光强数值不真实。为了使得光强更趋于真实值，应该把渲染器的physicallyCorrectLights属性设为true
        // this.renderer.physicallyCorrectLights = true
        // this.renderer.toneMapping = ACESFilmicToneMapping // 尽管我们的贴图不是HDR，但使用tone mapping可以塑造更真实的效果。
        // this.renderer.toneMappingExposure = 4 // tone mapping的曝光度
        this.renderer.shadowMap.enabled = true
        this.renderer.shadowMap.type = PCFShadowMap
        this.renderer.outputColorSpace = SRGBColorSpace
        this.viewerDom.appendChild(this.renderer.domElement)// 一个canvas，渲染器在其上绘制输出。
        // 网页标签
        this.labelRenderer = new CSS2DRenderer()
        this.labelRenderer.domElement.style.zIndex = 2
        this.labelRenderer.domElement.style.position = 'absolute'
        this.labelRenderer.domElement.style.top = '0px'
        this.labelRenderer.domElement.style.left = '0px'
        this.labelRenderer.domElement.style.pointerEvents = 'none'// 避免HTML标签遮挡三维场景的鼠标事件
        this.viewerDom.appendChild(this.labelRenderer.domElement)
        // 三维标签
        this.css3DRenderer = new CSS3DRenderer()
        this.css3DRenderer.domElement.style.zIndex = 0
        this.css3DRenderer.domElement.style.position = 'absolute'
        this.css3DRenderer.domElement.style.top = '0px'
        this.css3DRenderer.domElement.style.left = '0px'
        this.css3DRenderer.domElement.style.pointerEvents = 'none'// 避免HTML标签遮挡三维场景的鼠标事件
        this.viewerDom.appendChild(this.css3DRenderer.domElement)
    }

    _initCamera() {
        // 渲染相机
        this.camera = new PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 500000)
        this.camera.position.set(50, 0, 50)
        this.camera.lookAt(0, 0, 0)
    }

    _initScene() {
        // 渲染场景
        this.scene = new Scene()
        this.css3dScene = new Scene()
        this.scene.background = new Color('rgb(5,24,38)')
    }

    _initControl(option) {
        this.controls = new OrbitControls(this.camera, this.renderer.domElement)
        this.controls.enableDamping = false
        this.controls.screenSpacePanning = false // 定义平移时如何平移相机的位置 控制不上下移动
    }

    /**
     * 设备像素比上限，减轻高分屏 + 大屏画布带来的 GPU 压力
     */
    _effectivePixelRatio() {
        return Math.min(typeof window !== 'undefined' ? window.devicePixelRatio || 1 : 1, 1.5)
    }

    // 更新画布尺寸（仅在宽高或 dpr 变化时重设，避免每帧 setSize）
    _undateDom() {
        const w = Math.max(1, this.viewerDom.clientWidth | 0)
        const h = Math.max(1, this.viewerDom.clientHeight | 0)
        const dpr = this._effectivePixelRatio()
        if (w !== this._lastSizeW || h !== this._lastSizeH || dpr !== this._lastDpr) {
            this._lastSizeW = w
            this._lastSizeH = h
            this._lastDpr = dpr
            this.camera.aspect = w / h
            this.camera.updateProjectionMatrix()
            this.renderer.setSize(w, h, false)
            this.renderer.setPixelRatio(dpr)
            this.labelRenderer.setSize(w, h)
            this.css3DRenderer.setSize(w, h)
        }
        this.controls.update()
    }

    // 渲染dom
    _readerDom() {
        this.renderer.render(this.scene, this.camera)
        this.labelRenderer.render(this.scene, this.camera)
        this.css3DRenderer.render(this.css3dScene, this.camera)
    }

    // 添加skybox
    _initSkybox() {
        if (!this.skyboxs) this.skyboxs = new SkyBoxs(this)
        this.skyboxs.addSkybox()
    }

    // 灯光处理
    _initLight() {
        if (!this.lights) this.lights = new Lights(this)
    }
}
