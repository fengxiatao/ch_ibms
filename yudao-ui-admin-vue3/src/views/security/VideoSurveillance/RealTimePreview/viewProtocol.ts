export interface ViewSaveDialogPayload {
  id?: number
  name?: string
  groupIds?: number[]
}

export interface ViewManagerProtocol {
  syncCurrentView: (viewId: number | null) => void
  reloadViews: () => Promise<void>
  openViewSaveDialog: (payload?: ViewSaveDialogPayload) => void
}
