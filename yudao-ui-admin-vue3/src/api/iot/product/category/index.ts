import request from '@/config/axios'

// IoT 产品分类 VO
export interface ProductCategoryVO {
  id: number // 分类 ID
  name: string // 分类名字
  parentId?: number // 父分类 ID
  level?: number // 分类层级
  moduleCode?: string // 关联的 IBMS 模块编码
  sort: number // 分类排序
  status: number // 分类状态
  description: string // 分类描述
}

// IoT 产品分类树形 VO
export interface ProductCategoryTreeVO {
  id: number // 分类 ID
  name: string // 分类名称
  parentId: number // 父分类 ID
  level: number // 分类层级
  moduleCode?: string // 模块编码
  sort: number // 排序
  status: number // 状态
  description?: string // 描述
  children?: ProductCategoryTreeVO[] // 子分类列表
}

// IBMS 模块 VO
export interface IbmsModuleVO {
  code: string // 模块编码
  name: string // 模块名称
  icon?: string // 模块图标
  path?: string // 模块路径
  sort: number // 排序
}

// IoT 产品分类 API
export const ProductCategoryApi = {
  // 查询产品分类分页
  getProductCategoryPage: async (params: any) => {
    return await request.get({ url: `/iot/product-category/page`, params })
  },

  // 查询产品分类详情
  getProductCategory: async (id: number) => {
    return await request.get({ url: `/iot/product-category/get?id=` + id })
  },

  // 新增产品分类
  createProductCategory: async (data: ProductCategoryVO) => {
    return await request.post({ url: `/iot/product-category/create`, data })
  },

  // 修改产品分类
  updateProductCategory: async (data: ProductCategoryVO) => {
    return await request.put({ url: `/iot/product-category/update`, data })
  },

  // 删除产品分类
  deleteProductCategory: async (id: number) => {
    return await request.delete({ url: `/iot/product-category/delete?id=` + id })
  },

  /** 获取产品分类精简列表 */
  getSimpleProductCategoryList: () => {
    return request.get({ url: '/iot/product-category/simple-list' })
  },

  /** 获取产品分类树 */
  getProductCategoryTree: async (): Promise<ProductCategoryTreeVO[]> => {
    return await request.get({ url: '/iot/product-category/tree' })
  },

  /** 根据模块获取分类树 */
  getProductCategoryTreeByModule: async (moduleCode: string): Promise<ProductCategoryTreeVO[]> => {
    return await request.get({ url: '/iot/product-category/tree-by-module', params: { moduleCode } })
  },

  /** 获取子分类列表 */
  getChildCategories: async (parentId: number): Promise<ProductCategoryTreeVO[]> => {
    return await request.get({ url: '/iot/product-category/children', params: { parentId } })
  },

  /** 获取 IBMS 智慧模块列表 */
  getIbmsModules: async (): Promise<IbmsModuleVO[]> => {
    return await request.get({ url: '/iot/product-category/modules' })
  }
}
