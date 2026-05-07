chore(ibms-m1.7): Batch-1 删除 4 个孤立 test (被测 utils 已在 Z1 待删)

- src/utils/accessEventInsertion.test.ts
- src/utils/cardUtils.test.ts
- src/utils/deviceTreeUtils.test.ts
- src/utils/personCodeUtils.test.ts

保留 4 个被测 utils 仍活的 test (accessEventStatistics/Types/WebSocket, authModeUtils)
snapshot: pre-zombie-cleanup-20260507
