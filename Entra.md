# Entra ID BFF & Composable MFE Implementation Plan

## Project Overview
Implementing a **Backend-for-Frontend (BFF)** pattern to secure a **Composable Micro-Frontend (MFE)** application. The UI follows a "Left-Right" design where the side menu is dynamically composed based on Entra ID App Roles.

---

## Phase 1: Entra ID Configuration (Portal) [DONE]
- [x] **Application Registered:** Web App registration is complete.
- [x] **Redirect URI:** Set to `http://localhost:3001/v1/auth/session/accessToken` (to match the BFF exchange endpoint).
- [x] **Client Secret:** Generated and stored.
- [x] **App Roles Setup:** Defined `role.alpha` and `role.beta` in the portal.
  - ⚠️ NOTE: User currently has `role.aplha` (typo) - need to fix to `role.alpha`
- [x] **Expose an API:** Added scope `api://<client-id>/Data.Read`.

> **⚠️ INSTRUCTIONS FOR GEMINI CLI (Configuration):**
> When generating the `application.yml` for the `bff-backend`, you **MUST** include the following parameters:
> - **React App URL:** `http://localhost:5173`
> - **Usage:** >    1. Set as an `allowed-origin` in CORS configuration (with `allow-credentials: true`).
>    2. Set as the target for the `302 Redirect` after successful token exchange in the `accessToken` controller.

---

## Phase 2.1: The BFF (Java, Spring Boot) [DONE]
The BFF manages sessions, PKCE, and acts as a middle-proxy.
- **Directory:** Peer directory `bff-backend`.
- **Status:** Scaffolding and core logic implemented.
- **Logic:** - Extract `roles` from the Entra ID Token.
    - Proxy requests to `data-backend`, injecting the `Authorization` header.
- **Required Endpoints:**
    - `POST /v1/auth/session/create`: Init session/bind client fingerprint.
    - `POST /v1/auth/session/clear`: Logout and clear session.
    - `GET /v1/auth/session/codeUrl`: Generate PKCE and return Entra Auth URL.
    - `GET /v1/auth/session/accessToken`: Exchange code for token and **redirect to React App URL**.
    - `GET /v1/auth/session/refreshToken`: Refresh access token.
    - `GET /v1/auth/me`: User info and roles.

---

## Phase 2.2: The Data Backend (Java, Spring Boot) [DONE]
Protected resource server providing business data.
- **Directory:** Peer directory `data-backend`.
- **Status:** Scaffolding and core endpoints implemented.
- **Required Endpoints:**
    - `GET /v1/data/metrics`: Returns data for the Metrics MFE.
    - `GET /v1/data/analytics`: Returns data for the Analytics MFE.
- **Security:** Requires valid JWT with `Data.Read` scope.

---

## Phase 3: Composable MFE & Shell (React)
Independent React apps using a "Left-Right" layout.

### 1. The Shell App (`/react-bff-app`)
- **Port:** `5173`.
- **Role:** Fetches roles from `/v1/auth/me` and orchestrates MFE loading.

### 2. Independent MFEs
- **Metrics MFE (`/react-metrics`):** Independent app for **role.alpha**.
- **Analytics MFE (`/react-analytics`):** Independent app for **role.beta**.

```javascript
// Shell Role Mapping
const menuConfig = [
  { id: 'metrics', role: 'role.alpha', label: 'Metrics', port: 5177 },
  { id: 'analytics', role: 'role.beta', label: 'Analytics', port: 5178 }
];
```

---

## Phase 3: Composable MFE & Shell (React) [IN PROGRESS]

### Status Summary:
- [x] **React Analytics MFE** - Built with Module Federation, data fetching, styling
- [x] **React Metrics MFE** - Built with Module Federation, data fetching, styling
- [x] **React Shell App (BFF)** - Built with:
  - [x] Module Federation host configuration
  - [x] Authentication flow (login/logout via BFF)
  - [x] Role-based menu filtering
  - [x] Left-right layout UI
  - [x] Suspense-based lazy loading
- [ ] **Module Federation Runtime Loading** - PENDING (see issues below)

### What Works ✅
1. **Authentication Flow:**
   - User can login with Entra ID credentials
   - Session tokens stored server-side in BFF
   - User info and roles fetched successfully via `/v1/auth/me`
   - Logout functionality working

2. **BFF Backend Fixes Applied:**
   - Fixed OAuth2 authorize URL construction (removed duplicate `/v2.0`)
   - Fixed token endpoint URL construction (removed duplicate `/v2.0`)
   - Updated SecurityConfig to explicitly permitAll on auth endpoints
   - Added logging to verify token claims extraction
   - Roles correctly extracted from Entra ID ID token

3. **Role-Based Menu:**
   - Menu correctly shows/hides based on user's assigned roles
   - Active menu item highlighting implemented
   - Menu items automatically select first available MFE on login

4. **React Apps Configuration:**
   - react-metrics: Port 5177, Module Federation enabled
   - react-analytics: Port 5178, Module Federation enabled
   - react-bff-app: Port 5173, Module Federation host configured

### Known Issues ❌ [PENDING INVESTIGATION]

**Issue: Module Federation remoteEntry.js not being served in dev mode**
- Error: `404 Not Found on GET http://localhost:5177/remoteEntry.js`
- Behavior: Menu appears for a split second, then vanishes when trying to load MFE
- Root Cause: @originjs/vite-plugin-federation (v1.4.1) not generating/serving remoteEntry.js in Vite dev mode
- Expected in production build: `/assets/remoteEntry.js` (exists, verified)
- Missing in dev mode: `remoteEntry.js` at root level

**Attempted Fixes:**
- Restarted all dev servers ❌
- Updated vite.config.ts with explicit server/preview port config ❌
- Changed shared dependency config (singleton flags) ❌
- Added middlewareMode and minify settings ❌

**Next Steps to Try:**
1. Check federation plugin compatibility with Vite 7.2.4
2. May need different Module Federation plugin (e.g., webpack-based or different version)
3. Consider alternative MFE approach (build-time composition vs runtime)
4. Test with production build to confirm remoteEntry.js works there

### Files Modified in This Session:

**Backend:**
- `bff-backend/src/main/java/com/example/entra/bff_backend/controller/AuthController.java`
  - Fixed OAuth2 authorize URL construction
  - Added logging for token claims

- `bff-backend/src/main/java/com/example/entra/bff_backend/service/AuthService.java`
  - Fixed token endpoint URL construction in exchangeCodeForToken()
  - Fixed token endpoint URL construction in refreshToken()

- `bff-backend/src/main/java/com/example/entra/bff_backend/config/SecurityConfig.java`
  - Changed from wildcard `/v1/auth/session/**` to explicit endpoint matchers
  - Added `/v1/auth/me` to permitAll()

**Frontend:**
- `react-bff-app/vite.config.ts` - Module Federation host config, port 5173
- `react-bff-app/src/App.tsx` - Complete shell app with auth, menu, MFE loading
- `react-bff-app/src/menuConfig.ts` - Created menu configuration
- `react-bff-app/src/types/federation.d.ts` - TypeScript declarations for remote modules
- `react-bff-app/src/App.css` - Left-right layout styling
- `react-bff-app/src/index.css` - Global styles

- `react-metrics/vite.config.ts` - Module Federation remote config, port 5177
- `react-metrics/src/App.tsx` - Metrics data fetching and display
- `react-metrics/src/App.css` - Metrics card styling

- `react-analytics/vite.config.ts` - Module Federation remote config, port 5178
- `react-analytics/src/App.tsx` - Analytics data fetching and display
- `react-analytics/src/App.css` - Analytics card styling

### Environment Variables Configured:
- `ENTRA_CLIENT_ID` - Your Azure app registration ID
- `ENTRA_CLIENT_SECRET` - Your client secret (in .aiexclude)
- `ENTRA_TENANT_ID` - Your Azure tenant ID

### To Resume After Reboot:

1. **Fix Entra ID Role Typo:**
   - Go to Entra ID portal
   - Change user's role from `role.aplha` → `role.alpha`
   - Or delete and recreate the role correctly

2. **Start All Services:**
   ```bash
   # Terminal 1
   cd entra-workspace/bff-backend && ./mvnw spring-boot:run

   # Terminal 2
   cd entra-workspace/data-backend && ./mvnw spring-boot:run

   # Terminal 3
   cd entra-workspace/react-metrics && npm run dev

   # Terminal 4
   cd entra-workspace/react-analytics && npm run dev

   # Terminal 5
   cd entra-workspace/react-bff-app && npm run dev
   ```

3. **Primary Blocker:** Resolve Module Federation remoteEntry.js issue
   - This is preventing MFEs from loading dynamically
   - May require switching federation plugin or architecture approach

