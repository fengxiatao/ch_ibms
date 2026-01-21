# API Documentation

This document provides detailed information about the backend API endpoints used by the iSmart Charging Pile Mini-Program.

## Base URL

```
Production: https://your-production-domain.com
Development: https://your-dev-domain.com
```

## Authentication

All API requests (except login and registration) require authentication headers:

```javascript
{
  'Authorization': 'Bearer <access_token>',
  'clientId': 'app',
  'clientSecret': 'app'
}
```

The access token is obtained after successful login and should be stored securely in local storage.

## Response Format

All API responses follow this standard format:

```javascript
{
  "code": 200,           // Status code (200 = success, 401 = unauthorized, 999 = business error, etc.)
  "msg": "Success",      // Message describing the result
  "data": {}            // Response data (structure varies by endpoint)
}
```

## Endpoints

### User Authentication

#### Login
```
POST /record/login
```

**Request Body:**
```javascript
{
  "username": "string",  // Username
  "password": "string"   // Password (should be encrypted on client side)
}
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "123",
      "username": "user123",
      "phone": "13800138000",
      "openid": "oABC123..."  // May be null if not bound
    }
  }
}
```

#### Register
```
POST /user/register
```

**Request Body:**
```javascript
{
  "username": "string",
  "password": "string",
  "phone": "string",
  "verificationCode": "string"
}
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "注册成功",
  "data": {
    "userId": "123"
  }
}
```

#### Bind WeChat Account
```
POST /user/bindWechat
```

**Request Body:**
```javascript
{
  "userId": "string",    // User ID
  "code": "string"       // WeChat authorization code from wx.login()
}
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "绑定成功",
  "data": {
    "openid": "oABC123..."
  }
}
```

**Error Responses:**
- `401`: Invalid or expired code
- `999`: User already bound to another WeChat account

#### Check WeChat Binding Status
```
GET /user/wechatBinding/{userId}
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "Success",
  "data": {
    "bound": true,
    "openid": "oABC123..."  // Only returned if bound
  }
}
```

### Payment

#### Create Payment Order
```
POST /wechat/pay/unified/request
```

**Headers:**
```javascript
{
  'Authorization': 'Bearer <token>',
  'clientId': 'app',
  'clientSecret': 'app'
}
```

**Request Body:**
```javascript
{
  "code": "string",      // Fresh WeChat authorization code from wx.login()
  "money": 100,          // Recharge amount in yuan (must be > 0)
  "userId": "string"     // User ID
}
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "Success",
  "data": {
    "timeStamp": "1234567890",
    "nonceStr": "abc123",
    "package": "prepay_id=wx123456789",
    "signType": "MD5",
    "paySign": "ABC123...",
    "outTradeNo": "ORDER123456"  // Order number for tracking
  }
}
```

**Error Responses:**
- `401`: Invalid or expired token
- `999`: Duplicate recharge attempt
- `400`: Invalid amount (must be > 0)

#### Get Account Balance
```
GET /record/accountMoney/{userId}
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "Success",
  "data": {
    "balance": 150.50,     // Current balance in yuan
    "updatedAt": "2024-01-01T12:00:00Z"
  }
}
```

**Error Responses:**
- `401`: Invalid or expired token
- `404`: User not found

#### Get Recharge History
```
GET /record/rechargeRecord
```

**Query Parameters:**
```
userId: string (required)
pageNum: number (default: 1)
pageSize: number (default: 10)
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "Success",
  "data": {
    "currentAccountMoney": 150.50,
    "cumulativeTopUpMoney": 500.00,
    "rows": [
      {
        "id": "1",
        "outTradeNo": "ORDER123456",
        "money": 100,
        "paymentStatus": 1,  // 0: pending, 1: completed, 2: failed, 3: refunded
        "paymentDateTime": "2024-01-01T12:00:00Z",
        "transactionId": "wx123456789"
      }
    ],
    "total": 50
  }
}
```

### Charging Operations

#### Get Charging Status
```
GET /record/chargingStatus
```

**Query Parameters:**
```
userId: string (required)
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "Success",
  "data": {
    "isCharging": true,
    "chargingPileId": "PILE001",
    "startTime": "2024-01-01T12:00:00Z",
    "currentPower": 15.5,  // kWh
    "currentCost": 12.50   // yuan
  }
}
```

#### Start Charging
```
POST /record/startCharging
```

**Request Body:**
```javascript
{
  "userId": "string",
  "chargingPileId": "string",
  "socketNumber": "string",  // Socket number (e.g., "1", "2")
  "licensePlate": "string"   // Optional
}
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "充电已开始",
  "data": {
    "orderId": "ORDER123",
    "startTime": "2024-01-01T12:00:00Z"
  }
}
```

**Error Responses:**
- `400`: Insufficient balance
- `400`: Charging pile not available
- `401`: Invalid or expired token

#### Stop Charging
```
POST /record/stopCharging
```

**Request Body:**
```javascript
{
  "userId": "string",
  "orderId": "string"
}
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "充电已停止",
  "data": {
    "orderId": "ORDER123",
    "endTime": "2024-01-01T13:00:00Z",
    "totalPower": 20.5,    // kWh
    "totalCost": 16.40,    // yuan
    "duration": 3600       // seconds
  }
}
```

#### Get Charging History
```
GET /record/chargingHistory
```

**Query Parameters:**
```
userId: string (required)
pageNum: number (default: 1)
pageSize: number (default: 10)
startDate: string (optional, format: YYYY-MM-DD)
endDate: string (optional, format: YYYY-MM-DD)
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "Success",
  "data": {
    "rows": [
      {
        "id": "1",
        "orderId": "ORDER123",
        "chargingPileId": "PILE001",
        "startTime": "2024-01-01T12:00:00Z",
        "endTime": "2024-01-01T13:00:00Z",
        "totalPower": 20.5,
        "totalCost": 16.40,
        "status": 1  // 0: charging, 1: completed, 2: cancelled
      }
    ],
    "total": 100
  }
}
```

### Coupon Management

#### Get Available Coupons
```
GET /discount/available
```

**Query Parameters:**
```
userId: string (required)
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "Success",
  "data": [
    {
      "id": "1",
      "name": "新用户优惠券",
      "discountType": 1,  // 1: fixed amount, 2: percentage
      "discountValue": 10,
      "minAmount": 50,
      "expiryDate": "2024-12-31T23:59:59Z",
      "claimed": false
    }
  ]
}
```

#### Claim Coupon
```
POST /discount/claim
```

**Request Body:**
```javascript
{
  "userId": "string",
  "discountId": "string"
}
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "领取成功",
  "data": {
    "userDiscountId": "123"
  }
}
```

#### Get Usable Coupons for Order
```
GET /discount/usable
```

**Query Parameters:**
```
userId: string (required)
orderId: string (required)
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "Success",
  "data": {
    "tbAppChargingDiscountUserRespList": [
      {
        "id": "1",
        "name": "新用户优惠券",
        "discountMoney": 10,
        "usable": true
      }
    ],
    "discountMoney": 10,
    "orderMoney": 40,
    "usableSelectIds": ["1"]
  }
}
```

### Charging Pile Information

#### Get Charging Pile Details
```
GET /chargingPile/detail
```

**Query Parameters:**
```
chargingPileId: string (required)
```

**Response:**
```javascript
{
  "code": 200,
  "msg": "Success",
  "data": {
    "id": "PILE001",
    "name": "充电桩001",
    "location": "停车场A区",
    "status": 1,  // 0: offline, 1: available, 2: in use, 3: fault
    "sockets": [
      {
        "number": "1",
        "status": 1,
        "power": 7  // kW
      },
      {
        "number": "2",
        "status": 2,
        "power": 7
      }
    ],
    "pricePerKwh": 0.8  // yuan per kWh
  }
}
```

## Error Codes

| Code | Description |
|------|-------------|
| 200  | Success |
| 400  | Bad Request (invalid parameters) |
| 401  | Unauthorized (invalid or expired token) |
| 403  | Forbidden (insufficient permissions) |
| 404  | Not Found (resource doesn't exist) |
| 500  | Internal Server Error |
| 999  | Business Logic Error (see msg for details) |

## Rate Limiting

To prevent abuse, the following rate limits are enforced:

- Login: 5 requests per minute per IP
- Payment: 3 requests per minute per user
- Other endpoints: 60 requests per minute per user

Exceeding rate limits will result in a 429 (Too Many Requests) response.

## Webhooks

### Payment Notification

The backend will send a webhook notification when payment status changes:

```
POST <your-webhook-url>
```

**Payload:**
```javascript
{
  "outTradeNo": "ORDER123456",
  "transactionId": "wx123456789",
  "status": "SUCCESS",  // SUCCESS, FAIL, REFUND
  "amount": 100,
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## Testing

### Test Credentials

For development/testing, use these credentials:

```
Username: test_user
Password: test123456
```

### Test Payment

In development mode, payments will use WeChat's sandbox environment. No real money will be charged.

## Support

For API issues or questions:
- Email: api-support@example.com
- Documentation: https://docs.example.com
- Status Page: https://status.example.com
