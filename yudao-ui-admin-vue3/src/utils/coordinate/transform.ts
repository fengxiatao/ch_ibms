/**
 * 空间坐标转换工具类
 * 用于室内外坐标系统的相互转换
 */

export interface LocalCoordinate {
  x: number // 本地X坐标（米）
  y: number // 本地Y坐标（米）
  z: number // 本地Z坐标（米）
}

export interface GlobalCoordinate {
  longitude: number // 经度
  latitude: number  // 纬度
  altitude: number  // 海拔高度（米）
}

export interface Building {
  longitude: number  // 建筑中心点经度
  latitude: number   // 建筑中心点纬度
  elevation: number  // 建筑地面海拔（米）
  azimuth: number    // 建筑方位角（度，北为0，顺时针）
}

export interface CanvasCoordinate {
  x: number // Canvas X坐标（像素）
  y: number // Canvas Y坐标（像素）
}

/**
 * 本地坐标转全局坐标（经纬度）
 * @param building 建筑信息
 * @param local 本地坐标
 * @returns 全局坐标（经纬度）
 */
export function localToGlobal(
  building: Building,
  local: LocalCoordinate
): GlobalCoordinate {
  // 1. 建筑方位角转弧度
  const azimuthRad = (building.azimuth * Math.PI) / 180

  // 2. 旋转本地坐标（考虑建筑朝向）
  const rotatedX = local.x * Math.cos(azimuthRad) - local.y * Math.sin(azimuthRad)
  const rotatedY = local.x * Math.sin(azimuthRad) + local.y * Math.cos(azimuthRad)

  // 3. 将米转换为经纬度偏移
  // 纬度1度 ≈ 110.54 km
  // 经度1度 ≈ 111.32 km * cos(纬度)
  const latRad = (building.latitude * Math.PI) / 180
  const lonOffset = rotatedX / (111320.0 * Math.cos(latRad))
  const latOffset = rotatedY / 110540.0

  // 4. 计算全局坐标
  return {
    longitude: building.longitude + lonOffset,
    latitude: building.latitude + latOffset,
    altitude: building.elevation + local.z
  }
}

/**
 * 全局坐标（经纬度）转本地坐标
 * @param building 建筑信息
 * @param global 全局坐标
 * @returns 本地坐标
 */
export function globalToLocal(
  building: Building,
  global: GlobalCoordinate
): LocalCoordinate {
  // 1. 计算经纬度差并转换为米
  const latRad = (building.latitude * Math.PI) / 180
  const deltaX = (global.longitude - building.longitude) * 111320.0 * Math.cos(latRad)
  const deltaY = (global.latitude - building.latitude) * 110540.0

  // 2. 反向旋转（考虑建筑朝向）
  const azimuthRad = (-building.azimuth * Math.PI) / 180
  const localX = deltaX * Math.cos(azimuthRad) - deltaY * Math.sin(azimuthRad)
  const localY = deltaX * Math.sin(azimuthRad) + deltaY * Math.cos(azimuthRad)
  const localZ = global.altitude - building.elevation

  return { x: localX, y: localY, z: localZ }
}

/**
 * 解析 PostGIS GEOMETRY 字符串
 * 支持格式:
 * - POLYGON((x1 y1, x2 y2, ...))
 * - LINESTRING(x1 y1, x2 y2, ...)
 * - POINT(x y)
 * - MULTIPOLYGON(((x1 y1, x2 y2, ...)))
 * 
 * @param geomStr PostGIS 几何字符串
 * @returns 坐标数组 [[x1, y1], [x2, y2], ...]
 */
export function parseGeometry(geomStr: string): number[][] {
  if (!geomStr || typeof geomStr !== 'string') return []

  try {
    // 去除几何类型前缀
    const coordsStr = geomStr
      .replace(/^(MULTI)?POLYGON\s*/i, '')
      .replace(/^LINESTRING\s*/i, '')
      .replace(/^POINT\s*/i, '')
      .trim()

    // 提取最内层括号中的坐标
    const matches = coordsStr.match(/\(\(([^)]+)\)\)/) || coordsStr.match(/\(([^)]+)\)/)
    if (!matches || !matches[1]) return []

    // 解析坐标对
    const coords = matches[1]
      .split(',')
      .map((pair) => {
        const [x, y] = pair.trim().split(/\s+/).map(Number)
        return [x, y]
      })
      .filter(([x, y]) => !isNaN(x) && !isNaN(y))

    return coords
  } catch (error) {
    console.error('解析几何数据失败:', error, geomStr)
    return []
  }
}

/**
 * 解析 PostGIS LINESTRING 为贝塞尔曲线点（用于弯曲走廊）
 * @param geomStr PostGIS LINESTRING 字符串
 * @returns 坐标数组（可能包含控制点）
 */
export function parseLineString(geomStr: string): number[][] {
  if (!geomStr || typeof geomStr !== 'string') return []
  
  // LINESTRING 可能包含曲线控制点，格式: LINESTRING(x1 y1, x2 y2, x3 y3, ...)
  const coords = parseGeometry(geomStr)
  return coords
}

/**
 * 将本地坐标（米）转换为 Canvas 坐标（像素）
 * 
 * 坐标系转换:
 * - PostGIS/本地坐标: 原点在建筑西南角，X向东，Y向北（笛卡尔坐标系）
 * - Canvas坐标: 原点在左上角，X向右，Y向下（屏幕坐标系）
 * 
 * @param localCoords 本地坐标数组 [[x1,y1], [x2,y2], ...]
 * @param scale 缩放比例（像素/米），例如 10 表示 1米=10像素
 * @param offsetX Canvas X轴偏移（像素）
 * @param offsetY Canvas Y轴偏移（像素）
 * @returns Canvas 坐标数组 [x1, y1, x2, y2, ...]（Konva 格式）
 */
export function localToCanvas(
  localCoords: number[][],
  scale: number = 10,
  offsetX: number = 50,
  offsetY: number = 50
): number[] {
  const points: number[] = []

  localCoords.forEach(([x, y]) => {
    // X轴: 保持不变（都是向右）
    // Y轴: 翻转（本地坐标向上，Canvas向下）
    points.push(x * scale + offsetX)
    points.push(-y * scale + offsetY)  // 注意负号：Y轴翻转
  })

  return points
}

/**
 * 将 Canvas 坐标（像素）转换为本地坐标（米）
 * @param canvasX Canvas X坐标
 * @param canvasY Canvas Y坐标
 * @param scale 缩放比例（像素/米）
 * @param offsetX Canvas X轴偏移
 * @param offsetY Canvas Y轴偏移
 * @returns 本地坐标
 */
export function canvasToLocal(
  canvasX: number,
  canvasY: number,
  scale: number = 10,
  offsetX: number = 50,
  offsetY: number = 50
): { x: number; y: number } {
  return {
    x: (canvasX - offsetX) / scale,
    y: -(canvasY - offsetY) / scale  // Y轴翻转
  }
}

/**
 * 计算多边形中心点
 * @param points Konva 格式坐标数组 [x1, y1, x2, y2, ...]
 * @returns 中心点坐标 {x, y}
 */
export function getPolygonCenter(points: number[]): { x: number; y: number } {
  let sumX = 0
  let sumY = 0
  let count = 0

  for (let i = 0; i < points.length; i += 2) {
    sumX += points[i]
    sumY += points[i + 1]
    count++
  }

  return {
    x: sumX / count,
    y: sumY / count
  }
}

/**
 * 生成平滑的贝塞尔曲线点（用于弯曲走廊）
 * @param points 关键点数组 [[x1,y1], [x2,y2], ...]
 * @param tension 曲线张力 (0-1)，0为直线，1为最大曲线
 * @param numOfSegments 每段曲线的分段数
 * @returns 平滑曲线点数组
 */
export function createSmoothCurve(
  points: number[][],
  tension: number = 0.5,
  numOfSegments: number = 16
): number[] {
  if (points.length < 2) return []
  if (points.length === 2) {
    // 只有两个点，直接返回直线
    return [points[0][0], points[0][1], points[1][0], points[1][1]]
  }

  const result: number[] = []

  // Catmull-Rom 样条曲线算法
  for (let i = 0; i < points.length - 1; i++) {
    const p0 = i > 0 ? points[i - 1] : points[i]
    const p1 = points[i]
    const p2 = points[i + 1]
    const p3 = i < points.length - 2 ? points[i + 2] : p2

    for (let t = 0; t <= numOfSegments; t++) {
      const tt = t / numOfSegments
      const tt2 = tt * tt
      const tt3 = tt2 * tt

      const q1 = -tension * tt3 + 2 * tension * tt2 - tension * tt
      const q2 = (2 - tension) * tt3 + (tension - 3) * tt2 + 1
      const q3 = (tension - 2) * tt3 + (3 - 2 * tension) * tt2 + tension * tt
      const q4 = tension * tt3 - tension * tt2

      const x = q1 * p0[0] + q2 * p1[0] + q3 * p2[0] + q4 * p3[0]
      const y = q1 * p0[1] + q2 * p1[1] + q3 * p2[1] + q4 * p3[1]

      result.push(x, y)
    }
  }

  return result
}

/**
 * 判断点是否在多边形内部
 * @param point 点坐标 {x, y}
 * @param polygon Konva 格式多边形坐标 [x1, y1, x2, y2, ...]
 * @returns 是否在多边形内
 */
export function isPointInPolygon(point: { x: number; y: number }, polygon: number[]): boolean {
  let inside = false
  const x = point.x
  const y = point.y

  for (let i = 0, j = polygon.length - 2; i < polygon.length; i += 2) {
    const xi = polygon[i]
    const yi = polygon[i + 1]
    const xj = polygon[j]
    const yj = polygon[j + 1]

    const intersect = yi > y !== yj > y && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi

    if (intersect) inside = !inside

    j = i
  }

  return inside
}

/**
 * 计算两点之间的距离
 * @param p1 点1
 * @param p2 点2
 * @returns 距离（米）
 */
export function distance(
  p1: { x: number; y: number },
  p2: { x: number; y: number }
): number {
  const dx = p2.x - p1.x
  const dy = p2.y - p1.y
  return Math.sqrt(dx * dx + dy * dy)
}

/**
 * 格式化坐标显示
 * @param coord 坐标值
 * @param digits 小数位数
 * @returns 格式化后的字符串
 */
export function formatCoordinate(coord: number, digits: number = 2): string {
  return coord.toFixed(digits)
}

