/**
 * 室内坐标转换工具类
 * 
 * 坐标系说明：
 * - 建筑坐标系：原点在建筑西南角（左下角），X轴向东，Y轴向北，单位：米
 * - 像素坐标系：原点在Canvas左上角，X轴向右，Y轴向下，单位：像素
 */

export interface FloorPlanConfig {
  /** 楼层ID */
  floorId: number
  /** 平面图URL */
  imageUrl?: string
  /** 建筑实际宽度（米） */
  buildingWidth: number
  /** 建筑实际长度（米） */
  buildingHeight: number
  /** 像素/米比例（像素数量表示1米） */
  pixelsPerMeter: number
  /** Canvas宽度（像素） */
  canvasWidth: number
  /** Canvas高度（像素） */
  canvasHeight: number
  /** X轴偏移（像素，用于居中显示） */
  offsetX: number
  /** Y轴偏移（像素，用于居中显示） */
  offsetY: number
}

export interface Point {
  x: number
  y: number
}

export interface RealCoordinate {
  /** X坐标（米，向东为正） */
  x: number
  /** Y坐标（米，向北为正） */
  y: number
}

/**
 * 坐标转换类
 */
export class CoordinateConverter {
  private config: FloorPlanConfig

  constructor(config: FloorPlanConfig) {
    this.config = config
  }

  /**
   * 像素坐标 → 真实坐标（米）
   * 
   * @param pixelX Canvas上的X像素位置
   * @param pixelY Canvas上的Y像素位置
   * @returns 建筑坐标系中的真实坐标（米）
   */
  pixelToReal(pixelX: number, pixelY: number): RealCoordinate {
    const { offsetX, offsetY, pixelsPerMeter, buildingHeight } = this.config

    // 1. 减去偏移量，得到相对于建筑西南角的像素位置
    const relativePixelX = pixelX - offsetX
    const relativePixelY = pixelY - offsetY

    // 2. 转换为米制坐标
    // 注意：Y轴需要反转，因为Canvas的Y轴向下，而建筑坐标系的Y轴向北（上）
    const realX = relativePixelX / pixelsPerMeter
    const realY = buildingHeight - relativePixelY / pixelsPerMeter

    return {
      x: Math.round(realX * 100) / 100, // 保留2位小数
      y: Math.round(realY * 100) / 100
    }
  }

  /**
   * 真实坐标（米）→ 像素坐标
   * 
   * @param realX 建筑坐标系X坐标（米）
   * @param realY 建筑坐标系Y坐标（米）
   * @returns Canvas上的像素坐标
   */
  realToPixel(realX: number, realY: number): Point {
    const { offsetX, offsetY, pixelsPerMeter, buildingHeight } = this.config

    // 1. 转换为相对于建筑西南角的像素位置
    const relativePixelX = realX * pixelsPerMeter
    const relativePixelY = (buildingHeight - realY) * pixelsPerMeter // Y轴反转

    // 2. 加上偏移量
    const pixelX = relativePixelX + offsetX
    const pixelY = relativePixelY + offsetY

    return {
      x: Math.round(pixelX),
      y: Math.round(pixelY)
    }
  }

  /**
   * 检查坐标是否在建筑范围内
   */
  isInBounds(realX: number, realY: number): boolean {
    return (
      realX >= 0 &&
      realX <= this.config.buildingWidth &&
      realY >= 0 &&
      realY <= this.config.buildingHeight
    )
  }

  /**
   * 计算自动缩放比例和偏移量（居中显示）
   */
  static calculateAutoFit(
    buildingWidth: number,
    buildingHeight: number,
    canvasWidth: number,
    canvasHeight: number,
    padding: number = 40
  ): { pixelsPerMeter: number; offsetX: number; offsetY: number } {
    // 计算可用空间（减去padding）
    const availableWidth = canvasWidth - padding * 2
    const availableHeight = canvasHeight - padding * 2

    // 计算缩放比例（选择较小的比例以确保完全显示）
    const scaleX = availableWidth / buildingWidth
    const scaleY = availableHeight / buildingHeight
    const pixelsPerMeter = Math.min(scaleX, scaleY)

    // 计算偏移量（居中显示）
    const actualWidth = buildingWidth * pixelsPerMeter
    const actualHeight = buildingHeight * pixelsPerMeter
    const offsetX = (canvasWidth - actualWidth) / 2
    const offsetY = (canvasHeight - actualHeight) / 2

    return {
      pixelsPerMeter: Math.round(pixelsPerMeter * 100) / 100,
      offsetX: Math.round(offsetX),
      offsetY: Math.round(offsetY)
    }
  }
}

/**
 * 距离计算工具
 */
export class DistanceCalculator {
  /**
   * 计算两点之间的欧几里得距离（米）
   */
  static distance(point1: RealCoordinate, point2: RealCoordinate): number {
    const dx = point2.x - point1.x
    const dy = point2.y - point1.y
    return Math.sqrt(dx * dx + dy * dy)
  }

  /**
   * 格式化距离显示
   */
  static formatDistance(meters: number): string {
    if (meters < 1) {
      return `${Math.round(meters * 100)}cm`
    } else if (meters < 1000) {
      return `${meters.toFixed(2)}m`
    } else {
      return `${(meters / 1000).toFixed(2)}km`
    }
  }
}



















































