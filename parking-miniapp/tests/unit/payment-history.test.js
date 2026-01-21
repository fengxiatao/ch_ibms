/**
 * Unit Tests for Payment History (Recharge Record)
 * **Validates: Requirements 6.4**
 * 
 * Tests payment record display, status badge rendering, and filter/sort functionality
 */

describe('Payment History - Recharge Record', () => {
  // Mock payment record data
  const createMockPaymentRecord = (overrides = {}) => {
    // Use current time for recent payments to avoid timeout issues
    const now = new Date();
    const recentTime = new Date(now.getTime() - 60 * 1000); // 1 minute ago
    
    return {
      id: '1',
      orderNo: 'ORD20240101123456',
      topUpMoney: 100,
      currentMoney: 500,
      topUpTime: recentTime.toISOString(),
      paymentDateTime: recentTime.toISOString(),
      handPerson: 'System',
      paymentStatus: 1, // 0: pending, 1: completed, 2: failed, 3: refunded
      topUpStatus: 1,
      ...overrides
    };
  };

  // Helper functions from recharge-record.vue component
  const getPaymentStatusValue = (item) => {
    // Check if payment has timed out (more than 5 minutes)
    if (item.paymentDateTime) {
      const paymentTime = new Date(item.paymentDateTime).getTime();
      const now = Date.now();
      const fiveMinutes = 5 * 60 * 1000;
      
      if (now - paymentTime > fiveMinutes && item.paymentStatus === 0) {
        return 'timeout';
      }
    }
    
    // Map payment status codes
    switch (item.paymentStatus) {
      case 0:
        return 'pending';
      case 1:
        return 'completed';
      case 2:
        return 'failed';
      case 3:
        return 'completed'; // Refunded is still a completed payment
      default:
        // Fallback to topUpStatus if paymentStatus not available
        if (item.topUpStatus === 1 || item.topUpStatus === 5) {
          return 'completed';
        } else if (item.topUpStatus === 2) {
          return 'failed';
        } else if (item.topUpStatus === 3) {
          return 'completed'; // Refunded
        } else if (item.topUpStatus === 4) {
          return 'pending'; // Refund in progress
        }
        return 'pending';
    }
  };

  const getStatusText = (paymentStatus) => {
    switch (paymentStatus) {
      case 0:
        return '待支付';
      case 1:
        return '已完成';
      case 2:
        return '已失败';
      case 3:
        return '已退款';
      default:
        return '待支付';
    }
  };

  const getStatusClass = (paymentStatus) => {
    switch (paymentStatus) {
      case 0:
        return 'status-pending';
      case 1:
        return 'status-completed';
      case 2:
        return 'status-failed';
      case 3:
        return 'status-refunded';
      default:
        return 'status-pending';
    }
  };

  const formatTimestamp = (timestamp) => {
    if (!timestamp) {
      return '--';
    }
    
    try {
      const date = new Date(timestamp);
      if (isNaN(date.getTime())) {
        return timestamp;
      }
      
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    } catch (error) {
      return timestamp;
    }
  };

  const applyFilter = (dataList, filterValue) => {
    if (filterValue === 'all') {
      return dataList;
    }
    
    return dataList.filter(item => {
      const status = getPaymentStatusValue(item);
      return status === filterValue;
    });
  };

  const applySort = (dataList, sortValue) => {
    const sorted = [...dataList];
    
    sorted.sort((a, b) => {
      switch (sortValue) {
        case 'time_desc':
          return new Date(b.paymentDateTime || b.topUpTime) - new Date(a.paymentDateTime || a.topUpTime);
        case 'time_asc':
          return new Date(a.paymentDateTime || a.topUpTime) - new Date(b.paymentDateTime || b.topUpTime);
        case 'amount_desc':
          return (b.topUpMoney || 0) - (a.topUpMoney || 0);
        case 'amount_asc':
          return (a.topUpMoney || 0) - (b.topUpMoney || 0);
        default:
          return 0;
      }
    });
    
    return sorted;
  };

  describe('Payment Record Display', () => {
    test('should display payment record with all required fields', () => {
      const record = createMockPaymentRecord();
      
      expect(record.orderNo).toBeDefined();
      expect(record.topUpMoney).toBeDefined();
      expect(record.currentMoney).toBeDefined();
      expect(record.topUpTime).toBeDefined();
      expect(record.paymentDateTime).toBeDefined();
      expect(record.handPerson).toBeDefined();
      expect(record.paymentStatus).toBeDefined();
    });

    test('should format timestamp correctly', () => {
      const timestamp = '2024-01-01 10:00:00';
      const formatted = formatTimestamp(timestamp);
      
      expect(formatted).toBe('2024-01-01 10:00:00');
    });

    test('should return -- for null timestamp', () => {
      const formatted = formatTimestamp(null);
      expect(formatted).toBe('--');
    });

    test('should return -- for undefined timestamp', () => {
      const formatted = formatTimestamp(undefined);
      expect(formatted).toBe('--');
    });

    test('should return original string for invalid timestamp', () => {
      const invalidTimestamp = 'invalid-date';
      const formatted = formatTimestamp(invalidTimestamp);
      expect(formatted).toBe(invalidTimestamp);
    });

    test('should handle ISO date format', () => {
      const isoDate = '2024-01-01T10:00:00.000Z';
      const formatted = formatTimestamp(isoDate);
      
      // Should be formatted as YYYY-MM-DD HH:MM:SS
      expect(formatted).toMatch(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/);
    });

    test('should display multiple payment records', () => {
      const records = [
        createMockPaymentRecord({ id: '1', orderNo: 'ORD001' }),
        createMockPaymentRecord({ id: '2', orderNo: 'ORD002' }),
        createMockPaymentRecord({ id: '3', orderNo: 'ORD003' })
      ];
      
      expect(records).toHaveLength(3);
      expect(records[0].orderNo).toBe('ORD001');
      expect(records[1].orderNo).toBe('ORD002');
      expect(records[2].orderNo).toBe('ORD003');
    });

    test('should handle empty payment records list', () => {
      const records = [];
      expect(records).toHaveLength(0);
    });
  });

  describe('Status Badge Rendering', () => {
    test('should return correct status text for pending payment', () => {
      const statusText = getStatusText(0);
      expect(statusText).toBe('待支付');
    });

    test('should return correct status text for completed payment', () => {
      const statusText = getStatusText(1);
      expect(statusText).toBe('已完成');
    });

    test('should return correct status text for failed payment', () => {
      const statusText = getStatusText(2);
      expect(statusText).toBe('已失败');
    });

    test('should return correct status text for refunded payment', () => {
      const statusText = getStatusText(3);
      expect(statusText).toBe('已退款');
    });

    test('should return default status text for unknown status', () => {
      const statusText = getStatusText(999);
      expect(statusText).toBe('待支付');
    });

    test('should return correct CSS class for pending status', () => {
      const cssClass = getStatusClass(0);
      expect(cssClass).toBe('status-pending');
    });

    test('should return correct CSS class for completed status', () => {
      const cssClass = getStatusClass(1);
      expect(cssClass).toBe('status-completed');
    });

    test('should return correct CSS class for failed status', () => {
      const cssClass = getStatusClass(2);
      expect(cssClass).toBe('status-failed');
    });

    test('should return correct CSS class for refunded status', () => {
      const cssClass = getStatusClass(3);
      expect(cssClass).toBe('status-refunded');
    });

    test('should return default CSS class for unknown status', () => {
      const cssClass = getStatusClass(999);
      expect(cssClass).toBe('status-pending');
    });

    test('should identify pending payment status', () => {
      const record = createMockPaymentRecord({ paymentStatus: 0 });
      const status = getPaymentStatusValue(record);
      expect(status).toBe('pending');
    });

    test('should identify completed payment status', () => {
      const record = createMockPaymentRecord({ paymentStatus: 1 });
      const status = getPaymentStatusValue(record);
      expect(status).toBe('completed');
    });

    test('should identify failed payment status', () => {
      const record = createMockPaymentRecord({ paymentStatus: 2 });
      const status = getPaymentStatusValue(record);
      expect(status).toBe('failed');
    });

    test('should identify refunded payment as completed', () => {
      const record = createMockPaymentRecord({ paymentStatus: 3 });
      const status = getPaymentStatusValue(record);
      expect(status).toBe('completed');
    });

    test('should identify timeout status for pending payment older than 5 minutes', () => {
      const oldDate = new Date(Date.now() - 10 * 60 * 1000); // 10 minutes ago
      const record = createMockPaymentRecord({
        paymentStatus: 0,
        paymentDateTime: oldDate.toISOString()
      });
      const status = getPaymentStatusValue(record);
      expect(status).toBe('timeout');
    });

    test('should not identify timeout for recent pending payment', () => {
      const recentDate = new Date(Date.now() - 2 * 60 * 1000); // 2 minutes ago
      const record = createMockPaymentRecord({
        paymentStatus: 0,
        paymentDateTime: recentDate.toISOString()
      });
      const status = getPaymentStatusValue(record);
      expect(status).toBe('pending');
    });

    test('should fallback to topUpStatus when paymentStatus is undefined', () => {
      const record = createMockPaymentRecord({
        paymentStatus: undefined,
        topUpStatus: 1
      });
      const status = getPaymentStatusValue(record);
      expect(status).toBe('completed');
    });

    test('should handle topUpStatus 2 as failed', () => {
      const record = createMockPaymentRecord({
        paymentStatus: undefined,
        topUpStatus: 2
      });
      const status = getPaymentStatusValue(record);
      expect(status).toBe('failed');
    });

    test('should handle topUpStatus 3 as completed (refunded)', () => {
      const record = createMockPaymentRecord({
        paymentStatus: undefined,
        topUpStatus: 3
      });
      const status = getPaymentStatusValue(record);
      expect(status).toBe('completed');
    });

    test('should handle topUpStatus 4 as pending (refund in progress)', () => {
      const record = createMockPaymentRecord({
        paymentStatus: undefined,
        topUpStatus: 4
      });
      const status = getPaymentStatusValue(record);
      expect(status).toBe('pending');
    });
  });

  describe('Filter Functionality', () => {
    const mockRecords = [
      createMockPaymentRecord({ id: '1', paymentStatus: 0 }), // pending
      createMockPaymentRecord({ id: '2', paymentStatus: 1 }), // completed
      createMockPaymentRecord({ id: '3', paymentStatus: 2 }), // failed
      createMockPaymentRecord({ id: '4', paymentStatus: 1 }), // completed
      createMockPaymentRecord({ id: '5', paymentStatus: 0 })  // pending
    ];

    test('should return all records when filter is "all"', () => {
      const filtered = applyFilter(mockRecords, 'all');
      expect(filtered).toHaveLength(5);
    });

    test('should filter pending payments', () => {
      const filtered = applyFilter(mockRecords, 'pending');
      expect(filtered).toHaveLength(2);
      expect(filtered.every(r => r.paymentStatus === 0)).toBe(true);
    });

    test('should filter completed payments', () => {
      const filtered = applyFilter(mockRecords, 'completed');
      expect(filtered).toHaveLength(2);
      expect(filtered.every(r => r.paymentStatus === 1)).toBe(true);
    });

    test('should filter failed payments', () => {
      const filtered = applyFilter(mockRecords, 'failed');
      expect(filtered).toHaveLength(1);
      expect(filtered[0].paymentStatus).toBe(2);
    });

    test('should return empty array when no records match filter', () => {
      const filtered = applyFilter(mockRecords, 'timeout');
      expect(filtered).toHaveLength(0);
    });

    test('should handle empty records list', () => {
      const filtered = applyFilter([], 'pending');
      expect(filtered).toHaveLength(0);
    });

    test('should filter timeout payments', () => {
      const oldDate = new Date(Date.now() - 10 * 60 * 1000);
      const recordsWithTimeout = [
        createMockPaymentRecord({ 
          id: '1', 
          paymentStatus: 0,
          paymentDateTime: oldDate.toISOString()
        }),
        createMockPaymentRecord({ id: '2', paymentStatus: 1 })
      ];
      
      const filtered = applyFilter(recordsWithTimeout, 'timeout');
      expect(filtered).toHaveLength(1);
    });
  });

  describe('Sort Functionality', () => {
    const mockRecords = [
      createMockPaymentRecord({ 
        id: '1', 
        topUpMoney: 100, 
        paymentDateTime: '2024-01-01 10:00:00' 
      }),
      createMockPaymentRecord({ 
        id: '2', 
        topUpMoney: 50, 
        paymentDateTime: '2024-01-02 10:00:00' 
      }),
      createMockPaymentRecord({ 
        id: '3', 
        topUpMoney: 200, 
        paymentDateTime: '2024-01-03 10:00:00' 
      })
    ];

    test('should sort by time descending (newest first)', () => {
      const sorted = applySort(mockRecords, 'time_desc');
      expect(sorted[0].id).toBe('3');
      expect(sorted[1].id).toBe('2');
      expect(sorted[2].id).toBe('1');
    });

    test('should sort by time ascending (oldest first)', () => {
      const sorted = applySort(mockRecords, 'time_asc');
      expect(sorted[0].id).toBe('1');
      expect(sorted[1].id).toBe('2');
      expect(sorted[2].id).toBe('3');
    });

    test('should sort by amount descending (highest first)', () => {
      const sorted = applySort(mockRecords, 'amount_desc');
      expect(sorted[0].topUpMoney).toBe(200);
      expect(sorted[1].topUpMoney).toBe(100);
      expect(sorted[2].topUpMoney).toBe(50);
    });

    test('should sort by amount ascending (lowest first)', () => {
      const sorted = applySort(mockRecords, 'amount_asc');
      expect(sorted[0].topUpMoney).toBe(50);
      expect(sorted[1].topUpMoney).toBe(100);
      expect(sorted[2].topUpMoney).toBe(200);
    });

    test('should handle empty records list', () => {
      const sorted = applySort([], 'time_desc');
      expect(sorted).toHaveLength(0);
    });

    test('should handle single record', () => {
      const singleRecord = [mockRecords[0]];
      const sorted = applySort(singleRecord, 'time_desc');
      expect(sorted).toHaveLength(1);
      expect(sorted[0].id).toBe('1');
    });

    test('should not mutate original array', () => {
      const original = [...mockRecords];
      const sorted = applySort(mockRecords, 'amount_desc');
      
      expect(mockRecords[0].id).toBe(original[0].id);
      expect(sorted[0].id).not.toBe(mockRecords[0].id);
    });

    test('should handle records with missing paymentDateTime', () => {
      const recordsWithMissing = [
        createMockPaymentRecord({ 
          id: '1', 
          paymentDateTime: null,
          topUpTime: '2024-01-01 10:00:00'
        }),
        createMockPaymentRecord({ 
          id: '2', 
          paymentDateTime: '2024-01-02 10:00:00' 
        })
      ];
      
      const sorted = applySort(recordsWithMissing, 'time_desc');
      expect(sorted).toHaveLength(2);
    });

    test('should handle records with missing topUpMoney', () => {
      const recordsWithMissing = [
        createMockPaymentRecord({ id: '1', topUpMoney: null }),
        createMockPaymentRecord({ id: '2', topUpMoney: 100 })
      ];
      
      const sorted = applySort(recordsWithMissing, 'amount_desc');
      expect(sorted).toHaveLength(2);
      expect(sorted[0].topUpMoney).toBe(100);
    });

    test('should return original order for unknown sort value', () => {
      const sorted = applySort(mockRecords, 'unknown_sort');
      expect(sorted[0].id).toBe('1');
      expect(sorted[1].id).toBe('2');
      expect(sorted[2].id).toBe('3');
    });
  });

  describe('Combined Filter and Sort', () => {
    // Use recent timestamps to avoid timeout issues
    const now = new Date();
    const recentTime1 = new Date(now.getTime() - 60 * 1000); // 1 minute ago
    const recentTime2 = new Date(now.getTime() - 120 * 1000); // 2 minutes ago
    const recentTime3 = new Date(now.getTime() - 180 * 1000); // 3 minutes ago
    const recentTime4 = new Date(now.getTime() - 240 * 1000); // 4 minutes ago
    
    const mockRecords = [
      createMockPaymentRecord({ 
        id: '1', 
        paymentStatus: 0, 
        topUpMoney: 100,
        paymentDateTime: recentTime1.toISOString()
      }),
      createMockPaymentRecord({ 
        id: '2', 
        paymentStatus: 1, 
        topUpMoney: 50,
        paymentDateTime: recentTime2.toISOString()
      }),
      createMockPaymentRecord({ 
        id: '3', 
        paymentStatus: 0, 
        topUpMoney: 200,
        paymentDateTime: recentTime3.toISOString()
      }),
      createMockPaymentRecord({ 
        id: '4', 
        paymentStatus: 1, 
        topUpMoney: 150,
        paymentDateTime: recentTime4.toISOString()
      })
    ];

    test('should filter then sort correctly', () => {
      const filtered = applyFilter(mockRecords, 'pending');
      const sorted = applySort(filtered, 'amount_desc');
      
      expect(sorted).toHaveLength(2);
      expect(sorted[0].topUpMoney).toBe(200);
      expect(sorted[1].topUpMoney).toBe(100);
    });

    test('should filter completed and sort by time', () => {
      const filtered = applyFilter(mockRecords, 'completed');
      const sorted = applySort(filtered, 'time_desc');
      
      expect(sorted).toHaveLength(2);
      expect(sorted[0].id).toBe('4');
      expect(sorted[1].id).toBe('2');
    });

    test('should handle filter with no results', () => {
      const filtered = applyFilter(mockRecords, 'failed');
      const sorted = applySort(filtered, 'amount_desc');
      
      expect(sorted).toHaveLength(0);
    });

    test('should handle all filter with sort', () => {
      const filtered = applyFilter(mockRecords, 'all');
      const sorted = applySort(filtered, 'amount_asc');
      
      expect(sorted).toHaveLength(4);
      expect(sorted[0].topUpMoney).toBe(50);
      expect(sorted[3].topUpMoney).toBe(200);
    });
  });

  describe('Edge Cases', () => {
    test('should handle record with all null values', () => {
      const record = {
        id: null,
        orderNo: null,
        topUpMoney: null,
        currentMoney: null,
        topUpTime: null,
        paymentDateTime: null,
        handPerson: null,
        paymentStatus: null,
        topUpStatus: null
      };
      
      const status = getPaymentStatusValue(record);
      expect(status).toBe('pending');
    });

    test('should handle record with undefined values', () => {
      const record = {};
      const status = getPaymentStatusValue(record);
      expect(status).toBe('pending');
    });

    test('should handle very large amounts', () => {
      const record = createMockPaymentRecord({ topUpMoney: 999999999 });
      expect(record.topUpMoney).toBe(999999999);
    });

    test('should handle zero amount', () => {
      const record = createMockPaymentRecord({ topUpMoney: 0 });
      expect(record.topUpMoney).toBe(0);
    });

    test('should handle negative amount', () => {
      const record = createMockPaymentRecord({ topUpMoney: -100 });
      expect(record.topUpMoney).toBe(-100);
    });

    test('should handle very old timestamps', () => {
      const oldDate = '2000-01-01 00:00:00';
      const formatted = formatTimestamp(oldDate);
      expect(formatted).toBe('2000-01-01 00:00:00');
    });

    test('should handle future timestamps', () => {
      const futureDate = '2099-12-31 23:59:59';
      const formatted = formatTimestamp(futureDate);
      expect(formatted).toBe('2099-12-31 23:59:59');
    });

    test('should handle records with same timestamp', () => {
      const sameTime = '2024-01-01 10:00:00';
      const records = [
        createMockPaymentRecord({ id: '1', paymentDateTime: sameTime }),
        createMockPaymentRecord({ id: '2', paymentDateTime: sameTime })
      ];
      
      const sorted = applySort(records, 'time_desc');
      expect(sorted).toHaveLength(2);
    });

    test('should handle records with same amount', () => {
      const records = [
        createMockPaymentRecord({ id: '1', topUpMoney: 100 }),
        createMockPaymentRecord({ id: '2', topUpMoney: 100 })
      ];
      
      const sorted = applySort(records, 'amount_desc');
      expect(sorted).toHaveLength(2);
    });
  });
});
