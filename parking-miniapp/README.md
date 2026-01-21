# iSmart Charging Pile Mini-Program

A WeChat mini-program for electric vehicle charging station management and payment.

## Features

### Core Functionality
- **User Authentication**: Username/password login with secure token-based authentication
- **WeChat Account Binding**: Seamless integration with WeChat accounts for payment
- **WeChat Pay Integration**: Secure recharge functionality using WeChat Pay
- **Account Balance Management**: Real-time balance display with caching and error recovery
- **Charging Station Management**: QR code scanning to identify and use charging piles
- **Order Management**: View charging history, pending orders, and payment records
- **Coupon System**: Apply discount coupons to charging sessions
- **License Plate Management**: Save and manage vehicle license plates

### Recent Enhancements (WeChat Payment Integration)

#### 1. WeChat Account Binding
- Automatic binding check before payment attempts
- User-friendly binding dialog with clear instructions
- Secure openid management and storage
- Seamless integration with existing username/password authentication

#### 2. Enhanced Payment Flow
- Fresh WeChat authorization code for each payment
- Comprehensive error handling with user-friendly messages
- Payment status tracking (pending, completed, failed, timeout)
- Duplicate payment prevention
- Loading states and success animations

#### 3. Improved Balance Display
- Real-time balance updates after payments
- Graceful error handling with cached balance fallback
- Pull-to-refresh functionality
- Zero balance display support

#### 4. Error Recovery Mechanisms
- Token expiry recovery with payment intent preservation
- Automatic retry for failed WeChat API calls
- Network error handling with retry options
- 401 authentication error handling with login redirect

#### 5. Security Enhancements
- Secure token management (never logged)
- OpenID protection
- Input sanitization for payment amounts
- Rate limiting for payment attempts
- HTTPS enforcement for all image URLs

#### 6. Performance Optimizations
- Balance caching in Vuex store
- Request debouncing to prevent duplicate API calls
- Request cancellation for outdated requests
- Loading states to prevent duplicate submissions

#### 7. User Experience Improvements
- Success animations after payment completion
- Loading spinners during API calls
- Toast messages for all user actions
- Haptic feedback for button clicks (where supported)
- Smooth navigation with proper state management

## Technology Stack

- **Framework**: uni-app (Vue 2)
- **State Management**: Vuex
- **UI Components**: uView UI, ColorUI
- **HTTP Client**: Custom wrapper around uni.request
- **Testing**: Jest with fast-check for property-based testing
- **Payment**: WeChat Pay API

## Project Structure

```
ismart-chargingPile-miniapp/
├── common/                    # Shared utilities and services
│   ├── http/                 # HTTP client and API configuration
│   │   ├── ajax.js          # HTTP request wrapper
│   │   ├── API.js           # API endpoint definitions
│   │   ├── URL.js           # API URL constants
│   │   ├── interceptor.js   # Request/response interceptors
│   │   └── intermediary.js  # Request middleware
│   └── js/                   # Business logic services
│       ├── wechatBinding.js # WeChat account binding service
│       ├── paymentService.js # Payment operations
│       ├── errorRecovery.js # Error recovery mechanisms
│       ├── securityService.js # Security utilities
│       ├── performanceOptimizer.js # Performance optimization
│       ├── licensePlateValidator.js # License plate validation
│       ├── passwordValidator.js # Password validation
│       ├── couponFilter.js  # Coupon filtering logic
│       ├── qrCodeRouter.js  # QR code routing
│       └── orderDataRenderer.js # Order data formatting
├── components/               # Reusable Vue components
│   ├── wechat-bind-dialog/  # WeChat binding dialog
│   ├── nx-coupon/           # Coupon display component
│   ├── nx-can-use-coupon/   # Available coupons component
│   └── ...                  # Other UI components
├── pages/                    # Main application pages
│   ├── login/               # Login page
│   ├── register/            # Registration page
│   ├── index/               # Home page
│   └── ...                  # Other pages
├── package/chargingPile/     # Charging pile feature pages
│   ├── my/                  # User profile and balance
│   ├── preTopUp.vue         # Recharge page
│   ├── recharge-record/     # Recharge history
│   ├── confirmCharge.vue    # Charging confirmation
│   ├── isCharging.vue       # Active charging session
│   ├── historyOrderList.vue # Order history
│   └── ...                  # Other feature pages
├── store/                    # Vuex store
│   ├── index.js             # Store configuration
│   └── modules/             # Store modules
├── tests/                    # Test files
│   └── property/            # Property-based tests
├── docs/                     # Documentation
│   ├── 微信支付集成实现说明.md
│   ├── 充值功能说明.md
│   ├── 余额显示优化说明.md
│   ├── 错误处理机制说明.md
│   ├── 安全措施说明.md
│   ├── 性能优化说明.md
│   └── 用户反馈和动画说明.md
├── App.vue                   # Application entry point
├── main.js                   # Application initialization
├── manifest.json             # Mini-program configuration
├── pages.json                # Page routing configuration
└── package.json              # Dependencies

```

## Getting Started

### Prerequisites

- Node.js (v12 or higher)
- HBuilderX (recommended) or other uni-app compatible IDE
- WeChat Developer Tools
- WeChat Mini-Program account

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd ismart-chargingPile-miniapp
```

2. Install dependencies:
```bash
npm install
```

3. Configure API endpoints in `common/http/URL.js`:
```javascript
const baseUrl = 'https://your-api-domain.com'
```

4. Configure WeChat Mini-Program credentials in `manifest.json`:
```json
{
  "mp-weixin": {
    "appid": "your-appid"
  }
}
```

### Development

1. Open the project in HBuilderX or your preferred IDE

2. Run in WeChat Developer Tools:
   - Click "Run" → "Run to Mini-Program Simulator" → "WeChat Developer Tools"
   - Or use the command: `npm run dev:mp-weixin`

3. The project will compile and open in WeChat Developer Tools

### Testing

Run unit tests:
```bash
npm test
```

Run property-based tests:
```bash
npm test -- tests/property
```

### Building for Production

1. Build the project:
```bash
npm run build:mp-weixin
```

2. The compiled files will be in `unpackage/dist/dev/mp-weixin/`

3. Upload to WeChat Mini-Program platform:
   - Open WeChat Developer Tools
   - Click "Upload" and follow the prompts
   - Submit for review in WeChat Mini-Program admin panel

## API Documentation

### Authentication

All API requests require authentication headers:
```javascript
{
  'Authorization': 'Bearer <token>',
  'clientId': 'app',
  'clientSecret': 'app'
}
```

### Key Endpoints

#### User Authentication
- `POST /record/login` - User login
- `POST /user/register` - User registration
- `POST /user/bindWechat` - Bind WeChat account
- `GET /user/wechatBinding/{userId}` - Check binding status

#### Payment
- `POST /wechat/pay/unified/request` - Create payment order
- `GET /record/accountMoney/{userId}` - Get account balance
- `GET /record/rechargeRecord` - Get recharge history

#### Charging
- `GET /record/chargingStatus` - Get charging status
- `POST /record/startCharging` - Start charging session
- `POST /record/stopCharging` - Stop charging session
- `GET /record/chargingHistory` - Get charging history

#### Coupons
- `GET /discount/available` - Get available coupons
- `POST /discount/claim` - Claim a coupon
- `GET /discount/usable` - Get usable coupons for order

For detailed API documentation, see `docs/` directory.

## Configuration

### Environment Variables

The application uses different base URLs for different environments:

- Development: Configure in `.env.development`
- Production: Configure in `.env.production`

### WeChat Mini-Program Settings

Configure in `manifest.json`:
- `appid`: Your WeChat Mini-Program AppID
- `setting`: Mini-program settings (ES6, minify, etc.)
- `permission`: Required permissions (location, camera, etc.)

## Error Handling

The application implements comprehensive error handling:

1. **Authentication Errors (401)**: Automatic redirect to login with payment intent preservation
2. **Business Logic Errors**: Display backend error messages
3. **WeChat API Errors**: User-friendly error messages with retry options
4. **Network Errors**: Graceful degradation with cached data

See `docs/错误处理机制说明.md` for details.

## Security

Security measures implemented:

- Secure token storage and management
- OpenID protection (never logged or exposed)
- Input sanitization for payment amounts
- Rate limiting for payment attempts
- HTTPS enforcement
- No sensitive data in console logs

See `docs/安全措施说明.md` for details.

## Performance

Performance optimizations:

- Vuex state caching for balance and user info
- Request debouncing (1 second throttle)
- Request cancellation for outdated requests
- Loading states to prevent duplicate submissions
- Lazy loading of components

See `docs/性能优化说明.md` for details.

## Testing Strategy

### Unit Tests
- Service layer tests (payment, binding, validation)
- Component tests (UI interactions)
- Utility function tests

### Property-Based Tests
- License plate validation across all valid formats
- Password validation with various inputs
- Coupon filtering logic
- Order data rendering
- QR code routing

### Integration Tests
- End-to-end payment flow
- WeChat binding flow
- Error recovery scenarios

## Troubleshooting

### Common Issues

**Issue**: Payment fails with 401 error
- **Solution**: Check if token is valid, try logging in again

**Issue**: WeChat binding fails
- **Solution**: Ensure WeChat Developer Tools is logged in with a valid account

**Issue**: Balance not updating after payment
- **Solution**: Pull down to refresh on the My page

**Issue**: QR code scanning not working
- **Solution**: Grant camera permission in WeChat settings

## Contributing

1. Create a feature branch from `main`
2. Make your changes with clear commit messages
3. Write tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

[Your License Here]

## Support

For issues and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation in `docs/` directory

## Changelog

### Version 2.0.0 (Current)
- ✨ Added WeChat account binding functionality
- ✨ Integrated WeChat Pay for recharges
- ✨ Enhanced error handling and recovery
- ✨ Improved balance display with caching
- ✨ Added security measures and input validation
- ✨ Performance optimizations
- ✨ User experience improvements (animations, haptic feedback)
- 🐛 Fixed authentication issues
- 🐛 Fixed duplicate payment prevention
- 📝 Comprehensive documentation

### Version 1.0.0
- Initial release with basic charging functionality
