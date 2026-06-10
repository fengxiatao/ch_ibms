/**
 * DXF到SVG转换工具
 * 使用dxf-parser库在前端直接解析DXF，无水印
 * 
 * @author 智能化系统统一管理平台
 * @date 2025-11-03
 */

// @ts-ignore - dxf-parser没有完整的类型定义
import DxfParser from 'dxf-parser'

export interface DxfEntity {
  type: string
  layer: string
  vertices?: Array<{ x: number; y: number; z?: number }>
  center?: { x: number; y: number; z?: number }
  radius?: number
  startAngle?: number
  endAngle?: number
  startPoint?: { x: number; y: number; z?: number }
  endPoint?: { x: number; y: number; z?: number }
  color?: number
  lineType?: string
}

export interface DxfBounds {
  minX: number
  minY: number
  maxX: number
  maxY: number
  width: number
  height: number
}

export interface ConversionResult {
  svg: string
  bounds: DxfBounds
  entityCount: number
  layerCount: number
  buildingWidth: number  // 建筑宽度（米）
  buildingLength: number // 建筑长度（米）
  coordinateScale: number // 坐标比例（像素/米）
  dxfOffsetX?: number  // DXF→SVG 的 X 偏移
  dxfOffsetY?: number  // DXF→SVG 的 Y 偏移
}

/**
 * 解析DXF文件
 */
export function parseDxf(dxfContent: string): any {
  try {
    const parser = new DxfParser()
    const dxf = parser.parseSync(dxfContent)
    if (!dxf) {
      throw new Error('DXF解析返回空值')
    }
    console.log('[DXF解析] 解析成功，实体数量:', dxf.entities?.length || 0)
    return dxf
  } catch (error) {
    console.error('[DXF解析] 解析失败:', error)
    throw new Error('DXF文件解析失败: ' + (error as Error).message)
  }
}

/**
 * 提取指定图层的实体
 */
export function extractLayerEntities(dxf: any, layerNames: string[] = ['0']): DxfEntity[] {
  if (!dxf.entities) {
    console.warn('[DXF解析] 没有找到实体')
    return []
  }

  const entities = dxf.entities
    .filter((entity: any) => {
      const layer = entity.layer || '0'
      return layerNames.includes(layer)
    })
    .map((entity: any) => {
      const mapped: DxfEntity = {
        type: entity.type,
        layer: entity.layer || '0',
        color: entity.color,
        lineType: entity.lineType
      }

      // 处理不同类型的实体
      if (entity.vertices) {
        mapped.vertices = entity.vertices
      }
      if (entity.center) {
        mapped.center = entity.center
      }
      if (entity.radius !== undefined) {
        mapped.radius = entity.radius
      }
      if (entity.startAngle !== undefined) {
        mapped.startAngle = entity.startAngle
      }
      if (entity.endAngle !== undefined) {
        mapped.endAngle = entity.endAngle
      }
      if (entity.startPoint) {
        mapped.startPoint = entity.startPoint
      }
      if (entity.endPoint) {
        mapped.endPoint = entity.endPoint
      }

      return mapped
    })

  console.log('[DXF解析] 提取图层实体:', layerNames, '数量:', entities.length)
  return entities
}

/**
 * 计算实体边界
 */
export function calculateBounds(entities: DxfEntity[]): DxfBounds {
  let minX = Infinity
  let minY = Infinity
  let maxX = -Infinity
  let maxY = -Infinity

  let pointCount = 0

  entities.forEach((entity) => {
    // 处理顶点
    if (entity.vertices) {
      entity.vertices.forEach((v) => {
        minX = Math.min(minX, v.x)
        minY = Math.min(minY, v.y)
        maxX = Math.max(maxX, v.x)
        maxY = Math.max(maxY, v.y)
        pointCount++
      })
    }

    // 处理圆形/圆弧
    if (entity.center && entity.radius) {
      minX = Math.min(minX, entity.center.x - entity.radius)
      minY = Math.min(minY, entity.center.y - entity.radius)
      maxX = Math.max(maxX, entity.center.x + entity.radius)
      maxY = Math.max(maxY, entity.center.y + entity.radius)
      pointCount++
    }

    // 处理线段
    if (entity.startPoint) {
      minX = Math.min(minX, entity.startPoint.x)
      minY = Math.min(minY, entity.startPoint.y)
      maxX = Math.max(maxX, entity.startPoint.x)
      maxY = Math.max(maxY, entity.startPoint.y)
      pointCount++
    }
    if (entity.endPoint) {
      minX = Math.min(minX, entity.endPoint.x)
      minY = Math.min(minY, entity.endPoint.y)
      maxX = Math.max(maxX, entity.endPoint.x)
      maxY = Math.max(maxY, entity.endPoint.y)
      pointCount++
    }
  })

  const bounds = {
    minX,
    minY,
    maxX,
    maxY,
    width: maxX - minX,
    height: maxY - minY
  }

  console.log('[DXF解析] 计算边界:', bounds, '点数:', pointCount)
  return bounds
}

/**
 * 将已缩放的DXF实体直接转换为SVG（不使用viewBox，与Aspose.CAD一致）
 */
export function entitiesToSvgDirect(
  scaledEntities: DxfEntity[],
  scaledBounds: DxfBounds,
  width: number = 1920,
  height: number = 1080
): { svg: string; offsetX: number; offsetY: number } {
  if (scaledEntities.length === 0) {
    console.warn('[DXF转SVG] 没有实体可转换')
    const svg = `<svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
      <text x="50%" y="50%" text-anchor="middle" fill="#999">无图层数据</text>
    </svg>`
    return { svg, offsetX: 0, offsetY: 0 }
  }

  console.log('[DXF转SVG] 🎯 直接生成像素坐标SVG（与Aspose.CAD一致）')
  console.log('[DXF转SVG] 缩放后边界:', scaledBounds)

  // 计算居中偏移
  const offsetX = (width - scaledBounds.width) / 2 - scaledBounds.minX
  const offsetY = (height - scaledBounds.height) / 2 - scaledBounds.minY

  console.log('[DXF转SVG] 居中偏移:', offsetX.toFixed(2), ',', offsetY.toFixed(2))

  // Y轴翻转（DXF Y轴向上，SVG Y轴向下）
  const flipY = (y: number) => height - (y + offsetY)

  // 坐标转换（只需加偏移）
  const tx = (x: number) => x + offsetX
  const ty = (y: number) => flipY(y)

  // 计算线宽
  const strokeWidth = Math.max(width, height) / 2000

  // 生成SVG路径
  const paths: string[] = []
  let pathCount = 0

  scaledEntities.forEach((entity) => {
    try {
      switch (entity.type) {
        case 'LINE':
          if (entity.startPoint && entity.endPoint) {
            paths.push(
              `<line x1="${tx(entity.startPoint.x).toFixed(2)}" y1="${ty(entity.startPoint.y).toFixed(2)}" ` +
                `x2="${tx(entity.endPoint.x).toFixed(2)}" y2="${ty(entity.endPoint.y).toFixed(2)}" ` +
                `stroke="#4a90e2" stroke-width="${strokeWidth}" />`
            )
            pathCount++
          } else if (entity.vertices && entity.vertices.length >= 2) {
            const v1 = entity.vertices[0]
            const v2 = entity.vertices[1]
            paths.push(
              `<line x1="${tx(v1.x).toFixed(2)}" y1="${ty(v1.y).toFixed(2)}" ` +
                `x2="${tx(v2.x).toFixed(2)}" y2="${ty(v2.y).toFixed(2)}" ` +
                `stroke="#4a90e2" stroke-width="${strokeWidth}" />`
            )
            pathCount++
          }
          break

        case 'POLYLINE':
        case 'LWPOLYLINE':
          if (entity.vertices && entity.vertices.length > 1) {
            const pathData = entity.vertices
              .map((v, i) => `${i === 0 ? 'M' : 'L'} ${tx(v.x).toFixed(2)} ${ty(v.y).toFixed(2)}`)
              .join(' ')
            paths.push(
              `<path d="${pathData}" stroke="#4a90e2" stroke-width="${strokeWidth}" fill="none" />`
            )
            pathCount++
          }
          break

        case 'CIRCLE':
          if (entity.center && entity.radius) {
            if (entity.radius > 0.1) {
              paths.push(
                `<circle cx="${tx(entity.center.x).toFixed(2)}" cy="${ty(entity.center.y).toFixed(2)}" ` +
                  `r="${entity.radius.toFixed(2)}" stroke="#4a90e2" stroke-width="${strokeWidth}" fill="none" />`
              )
              pathCount++
            }
          }
          break

        case 'ARC':
          if (
            entity.center &&
            entity.radius &&
            entity.startAngle !== undefined &&
            entity.endAngle !== undefined
          ) {
            if (entity.radius > 0.1) {
              const cx = tx(entity.center.x)
              const cy = ty(entity.center.y)
              const r = entity.radius

              const startRad = (entity.startAngle * Math.PI) / 180
              const endRad = (entity.endAngle * Math.PI) / 180

              const x1 = cx + r * Math.cos(startRad)
              const y1 = cy + r * Math.sin(startRad)
              const x2 = cx + r * Math.cos(endRad)
              const y2 = cy + r * Math.sin(endRad)

              let angleDiff = entity.endAngle - entity.startAngle
              if (angleDiff < 0) angleDiff += 360
              const largeArc = angleDiff > 180 ? 1 : 0

              paths.push(
                `<path d="M ${x1.toFixed(2)} ${y1.toFixed(2)} A ${r.toFixed(2)} ${r.toFixed(2)} 0 ${largeArc} 1 ${x2.toFixed(2)} ${y2.toFixed(2)}" ` +
                  `stroke="#4a90e2" stroke-width="${strokeWidth}" fill="none" />`
              )
              pathCount++
            }
          }
          break

        case 'SPLINE':
          console.debug('[DXF转SVG] 跳过SPLINE实体')
          break

        default:
          console.debug(`[DXF转SVG] 未处理的实体类型: ${entity.type}`)
      }
    } catch (error) {
      console.error(`[DXF转SVG] 处理实体失败:`, entity.type, error)
    }
  })

  console.log('[DXF转SVG] 成功转换路径:', pathCount, '个')

  // 生成完整SVG（添加viewBox以便自适应缩放）
  // 🎨 深色科技风配色方案
  const svg = `<svg width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg" style="background:#1a1f35" preserveAspectRatio="xMidYMid meet">
  <g id="layer-0" stroke-linecap="round" stroke-linejoin="round">
    ${paths.join('\n    ')}
  </g>
</svg>`

  return { svg, offsetX, offsetY }  // 🔑 返回SVG和偏移参数
}

/**
 * 将DXF实体转换为SVG路径（使用viewBox）
 */
export function entitiesToSvg(
  entities: DxfEntity[],
  width: number = 1920,
  height: number = 1080
): string {
  if (entities.length === 0) {
    console.warn('[DXF转SVG] 没有实体可转换')
    return `<svg width="${width}" height="${height}" xmlns="http://www.w3.org/2000/svg">
      <text x="50%" y="50%" text-anchor="middle" fill="#999">无图层数据</text>
    </svg>`
  }

  const bounds = calculateBounds(entities)

  // 输出边界信息（调试用）
  console.log('[DXF转SVG] 边界信息:', {
    minX: bounds.minX.toFixed(2),
    minY: bounds.minY.toFixed(2),
    maxX: bounds.maxX.toFixed(2),
    maxY: bounds.maxY.toFixed(2),
    width: bounds.width.toFixed(2),
    height: bounds.height.toFixed(2)
  })

  // 添加10%边距
  const margin = 0.1
  const paddedMinX = bounds.minX - bounds.width * margin
  const paddedMinY = bounds.minY - bounds.height * margin
  const paddedWidth = bounds.width * (1 + 2 * margin)
  const paddedHeight = bounds.height * (1 + 2 * margin)

  console.log('[DXF转SVG] 使用viewBox:', `${paddedMinX.toFixed(2)} ${paddedMinY.toFixed(2)} ${paddedWidth.toFixed(2)} ${paddedHeight.toFixed(2)}`)

  // Y轴翻转（DXF坐标系Y轴向上，SVG Y轴向下）
  const flipY = (y: number) => -y

  // 计算线宽（基于viewBox大小，保持视觉一致）
  const strokeWidth = Math.max(paddedWidth, paddedHeight) / 1000

  // 生成SVG路径
  const paths: string[] = []
  let pathCount = 0

  entities.forEach((entity) => {
    try {
      switch (entity.type) {
        case 'LINE':
          if (entity.startPoint && entity.endPoint) {
            paths.push(
              `<line x1="${entity.startPoint.x.toFixed(2)}" y1="${flipY(entity.startPoint.y).toFixed(2)}" ` +
                `x2="${entity.endPoint.x.toFixed(2)}" y2="${flipY(entity.endPoint.y).toFixed(2)}" ` +
                `stroke="#4a90e2" stroke-width="${strokeWidth}" />`
            )
            pathCount++
          } else if (entity.vertices && entity.vertices.length >= 2) {
            const v1 = entity.vertices[0]
            const v2 = entity.vertices[1]
            paths.push(
              `<line x1="${v1.x.toFixed(2)}" y1="${flipY(v1.y).toFixed(2)}" ` +
                `x2="${v2.x.toFixed(2)}" y2="${flipY(v2.y).toFixed(2)}" ` +
                `stroke="#4a90e2" stroke-width="${strokeWidth}" />`
            )
            pathCount++
          }
          break

        case 'POLYLINE':
        case 'LWPOLYLINE':
          if (entity.vertices && entity.vertices.length > 1) {
            const pathData = entity.vertices
              .map((v, i) => `${i === 0 ? 'M' : 'L'} ${v.x.toFixed(2)} ${flipY(v.y).toFixed(2)}`)
              .join(' ')
            paths.push(
              `<path d="${pathData}" stroke="#4a90e2" stroke-width="${strokeWidth}" fill="none" />`
            )
            pathCount++
          }
          break

        case 'CIRCLE':
          if (entity.center && entity.radius) {
            if (entity.radius > 0.1) {
              // 过滤太小的圆
              paths.push(
                `<circle cx="${entity.center.x.toFixed(2)}" cy="${flipY(entity.center.y).toFixed(2)}" ` +
                  `r="${entity.radius.toFixed(2)}" stroke="#4a90e2" stroke-width="${strokeWidth}" fill="none" />`
              )
              pathCount++
            }
          }
          break

        case 'ARC':
          if (
            entity.center &&
            entity.radius &&
            entity.startAngle !== undefined &&
            entity.endAngle !== undefined
          ) {
            if (entity.radius > 0.1) {
              const cx = entity.center.x
              const cy = flipY(entity.center.y)
              const r = entity.radius

              // 转换角度为弧度（注意Y轴翻转后角度也需要调整）
              const startRad = (entity.startAngle * Math.PI) / 180
              const endRad = (entity.endAngle * Math.PI) / 180

              const x1 = cx + r * Math.cos(startRad)
              const y1 = cy + r * Math.sin(startRad) // Y轴已翻转，这里用+
              const x2 = cx + r * Math.cos(endRad)
              const y2 = cy + r * Math.sin(endRad)

              let angleDiff = entity.endAngle - entity.startAngle
              if (angleDiff < 0) angleDiff += 360
              const largeArc = angleDiff > 180 ? 1 : 0

              paths.push(
                `<path d="M ${x1.toFixed(2)} ${y1.toFixed(2)} A ${r.toFixed(2)} ${r.toFixed(2)} 0 ${largeArc} 1 ${x2.toFixed(2)} ${y2.toFixed(2)}" ` +
                  `stroke="#4a90e2" stroke-width="${strokeWidth}" fill="none" />`
              )
              pathCount++
            }
          }
          break

        case 'SPLINE':
          // 暂不支持样条曲线，可以后续扩展
          console.debug('[DXF转SVG] 跳过SPLINE实体')
          break

        default:
          console.debug(`[DXF转SVG] 未处理的实体类型: ${entity.type}`)
      }
    } catch (error) {
      console.error(`[DXF转SVG] 处理实体失败:`, entity.type, error)
    }
  })

  console.log('[DXF转SVG] 成功转换路径:', pathCount, '个')

  // 生成完整SVG（使用viewBox自动适配，保留完整内容）
  const svg = `<svg width="${width}" height="${height}" viewBox="${paddedMinX.toFixed(2)} ${-bounds.maxY - bounds.height * margin} ${paddedWidth.toFixed(2)} ${paddedHeight.toFixed(2)}" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="xMidYMid meet">
  <g id="layer-0" stroke-linecap="round" stroke-linejoin="round">
    ${paths.join('\n    ')}
  </g>
</svg>`

  return svg
}

/**
 * 完整转换流程：DXF -> SVG（使用后端坐标比例，与Aspose.CAD一致）
 * 
 * @param dxfContent DXF文件内容
 * @param layerNames 要转换的图层名称
 * @param backendCoordinateScale 后端Aspose.CAD计算的坐标比例（像素/米）
 * @param width 目标SVG宽度（默认1920）
 * @param height 目标SVG高度（默认1080）
 */
export function convertDxfToSvgWithBackendScale(
  dxfContent: string,
  layerNames: string[] = ['0'],
  backendCoordinateScale: number,
  width: number = 1920,
  height: number = 1080
): ConversionResult {
  console.log('[DXF转SVG] 🎯 使用后端坐标比例:', backendCoordinateScale.toFixed(2), '像素/米')
  console.log('[DXF转SVG] 目标尺寸:', width, 'x', height, '图层:', layerNames)

  // 1. 解析DXF
  const dxf = parseDxf(dxfContent)

  // 2. 提取图层
  const entities = extractLayerEntities(dxf, layerNames)

  // 3. 计算边界
  const bounds = calculateBounds(entities)

  // 4. 将DXF坐标转换为像素坐标（使用后端coordinateScale）
  const scaledEntities = entities.map(entity => {
    const scaled = { ...entity }
    
    // DXF单位是毫米，先转为米，再乘以coordinateScale得到像素
    const scale = backendCoordinateScale / 1000
    
    if (entity.vertices) {
      scaled.vertices = entity.vertices.map(v => ({
        x: v.x * scale,
        y: v.y * scale
      }))
    }
    if (entity.center) {
      scaled.center = {
        x: entity.center.x * scale,
        y: entity.center.y * scale
      }
    }
    if (entity.radius !== undefined) {
      scaled.radius = entity.radius * scale
    }
    if (entity.startPoint) {
      scaled.startPoint = {
        x: entity.startPoint.x * scale,
        y: entity.startPoint.y * scale
      }
    }
    if (entity.endPoint) {
      scaled.endPoint = {
        x: entity.endPoint.x * scale,
        y: entity.endPoint.y * scale
      }
    }
    
    return scaled
  })

  // 5. 计算缩放后的边界
  const scaledBounds = calculateBounds(scaledEntities)

  // 6. 生成SVG（不使用viewBox，直接使用像素坐标）
  const { svg, offsetX, offsetY } = entitiesToSvgDirect(scaledEntities, scaledBounds, width, height)

  // 7. 计算建筑尺寸
  const buildingWidth = bounds.width / 1000
  const buildingLength = bounds.height / 1000

  // 8. 统计信息
  const layers = new Set(entities.map((e) => e.layer))

  const result: ConversionResult = {
    svg,
    bounds,
    entityCount: entities.length,
    layerCount: layers.size,
    buildingWidth,
    buildingLength,
    coordinateScale: backendCoordinateScale,
    dxfOffsetX: offsetX,  // 🔑 返回DXF偏移参数
    dxfOffsetY: offsetY   // 🔑 返回DXF偏移参数
  }

  console.log('[DXF转SVG] ✅ 转换完成（与后端Aspose.CAD一致）:', {
    实体数: result.entityCount,
    图层数: result.layerCount,
    建筑尺寸: `${buildingWidth.toFixed(2)}m x ${buildingLength.toFixed(2)}m`,
    坐标比例: `${backendCoordinateScale.toFixed(2)} 像素/米`,
    SVG尺寸: `${width}x${height}`,
    DXF偏移: `(${offsetX.toFixed(2)}, ${offsetY.toFixed(2)})`
  })

  return result
}

/**
 * 完整转换流程：DXF -> SVG（自动计算坐标比例）
 */
export function convertDxfToSvg(
  dxfContent: string,
  layerNames: string[] = ['0'],
  width: number = 1920,
  height: number = 1080
): ConversionResult {
  console.log('[DXF转SVG] 开始转换，目标尺寸:', width, 'x', height, '图层:', layerNames)

  // 1. 解析DXF
  const dxf = parseDxf(dxfContent)

  // 2. 提取图层
  const entities = extractLayerEntities(dxf, layerNames)

  // 3. 计算边界
  const bounds = calculateBounds(entities)

  // 4. 转换为SVG
  const svg = entitiesToSvg(entities, width, height)

  // 5. 计算建筑尺寸和坐标比例
  // DXF单位通常是毫米，转换为米
  const buildingWidth = bounds.width / 1000
  const buildingLength = bounds.height / 1000

  // 计算坐标比例（像素/米）
  const scaleX = width / buildingWidth
  const scaleY = height / buildingLength
  const coordinateScale = Math.min(scaleX, scaleY) * 0.9

  // 6. 统计信息
  const layers = new Set(entities.map((e) => e.layer))

  const result: ConversionResult = {
    svg,
    bounds,
    entityCount: entities.length,
    layerCount: layers.size,
    buildingWidth,
    buildingLength,
    coordinateScale
  }

  console.log('[DXF转SVG] 转换完成:', {
    实体数: result.entityCount,
    图层数: result.layerCount,
    建筑尺寸: `${buildingWidth.toFixed(2)}m x ${buildingLength.toFixed(2)}m`,
    坐标比例: `${coordinateScale.toFixed(2)} 像素/米`
  })

  return result
}

/**
 * 批量转换多个图层
 */
export function convertDxfLayersToSvg(
  dxfContent: string,
  layerGroups: { name: string; layers: string[] }[],
  width: number = 1920,
  height: number = 1080
): Map<string, ConversionResult> {
  const results = new Map<string, ConversionResult>()

  layerGroups.forEach((group) => {
    try {
      const result = convertDxfToSvg(dxfContent, group.layers, width, height)
      results.set(group.name, result)
    } catch (error) {
      console.error(`[DXF转SVG] 转换图层组失败: ${group.name}`, error)
    }
  })

  return results
}

