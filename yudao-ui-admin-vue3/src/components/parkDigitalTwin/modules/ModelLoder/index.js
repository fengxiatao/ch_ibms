import {GLTFLoader} from 'three/examples/jsm/loaders/GLTFLoader.js'
import {DRACOLoader} from 'three/examples/jsm/loaders/DRACOLoader.js'
import DsModel from '../DsModel'
import {FBXLoader} from "three/examples/jsm/loaders/FBXLoader";

/**
 * 模型加载类（只能加载GLTF及GLB格式）
 * 其他格式可通过windows 电脑默认模型软件打开然后另存为glb
 */
export default class ModelLoder {
    /**
     * 构造函数
     * @param viewer 场景对象添加
     */
    constructor(_viewer, resourcesUrl) {
        this.viewer = _viewer
        this.scene = _viewer.scene
        this.loaderGltf = new GLTFLoader()// 实例化加载器
        this.loaderFBX = new FBXLoader()// 实例化加载器
        this.dracoLoader = new DRACOLoader()
        let decoderPath =
            resourcesUrl || `${_viewer.assetBase || '/'}resources/draco/gltf/`
        if (decoderPath && !decoderPath.endsWith('/')) {
            decoderPath += '/'
        }
        this.dracoLoader.setDecoderPath(decoderPath)
        this.loaderGltf.setDRACOLoader(this.dracoLoader)
    }

    /**
     * 添加模型数据
     * @param url 模型的路径
     * @param callback 返回模型对象，常用一些功能挂接在模型对象上
     * @param progress 返回加载进度，还有问题，需要修改
     */
    loadModelToScene(url, callback, progress, onError) {
        this.loadModel(url, model => {
            this.scene.add(model.object)
            callback && callback(model)
        }, num => {
            progress && progress(num) // 输出加载进度
        }, onError)
    }

    /**
     * 加载模型
     * @param url 模型路径
     * @param callback 回调模型
     * @param progress 返回加载进度
     */
    loadModel(url, callback, progress, onError) {
        // .load（url:字符串，onLoad:函数，onProgress:函数，onError:函数）
        //判断是否是fbx格式
        const onProg = xhr => {
            if (!progress) {
                return
            }
            const total = xhr.total
            const ratio = total > 0 ? xhr.loaded / total : 0
            progress(Number(ratio.toFixed(4)))
        }
        const onErr = err => {
            onError && onError(err)
        }
        if (url.indexOf('.fbx') > -1) {
            this.loaderFBX.load(url, gltf => {
                callback && callback(new DsModel(gltf, this.viewer))
            }, onProg, onErr)
        } else {
            this.loaderGltf.load(url, gltf => {
                callback && callback(new DsModel(gltf, this.viewer))
            }, onProg, onErr)
        }

    }
}
