chore(ibms-m1.7): Batch-4 删除 38 个 Z1+Z2 旧 api/hooks/utils (BFS 0 引用)

Z1 32 项 (api 24 + assets 1 + hooks 2 + utils 5):

api/access (8):
- src/api/access/accessControl/accessAuthorization/index.ts
- src/api/access/accessControl/accessRecord/index.ts
- src/api/access/accessControl/device/index.ts
- src/api/access/accessControl/dispatchCenter/index.ts
- src/api/access/accessControl/guardManagement/index.ts
- src/api/access/elevator/elevatorManagement/index.ts
- src/api/access/parking/parkingManagement/index.ts
- src/api/access/vehicle/vehicleManagement/index.ts

api/bpm (1):
- src/api/bpm/simple/index.ts

api/iot (14):
- src/api/iot/camera.ts
- src/api/iot/device/config.ts
- src/api/iot/device/display/index.ts
- src/api/iot/device/event.ts
- src/api/iot/device/group/index.ts
- src/api/iot/device/jobConfig.ts
- src/api/iot/device/location.ts
- src/api/iot/device/service.ts
- src/api/iot/jobType/index.ts
- src/api/iot/product/category/index.ts
- src/api/iot/product/jobConfig.ts
- src/api/iot/recording/types.ts
- src/api/iot/task/index.ts
- src/api/iot/videoView/index.ts

api/security (1):
- src/api/security/camera/index.ts

assets (1):
- src/assets/floorplan-icons/iconConfig.ts

hooks (2):
- src/hooks/iot/useDeviceDisplay.ts
- src/hooks/useAlertWebSocket.ts

utils (5, 与 Batch-1 中已删 test 配对):
- src/utils/accessEventInsertion.ts
- src/utils/cardUtils.ts
- src/utils/coordinate.ts
- src/utils/deviceTreeUtils.ts
- src/utils/personCodeUtils.ts

Z2 6 项:
- src/components/IoT/DeviceComponents/index.ts
- src/hooks/web/useEmitt.ts
- src/hooks/web/useGuide.ts
- src/hooks/web/useNetwork.ts
- src/hooks/web/useTimeAgo.ts
- src/utils/domUtils.ts

依据: docs/ibms-zombie-candidates.md Z1 Batch-4 (32) + Z2 Batch-4 (6)
snapshot: pre-zombie-cleanup-20260507
后续: 用户侧 pnpm install/build/vue-tsc 验证 (TS baseline=1692, 应保持或下降)
