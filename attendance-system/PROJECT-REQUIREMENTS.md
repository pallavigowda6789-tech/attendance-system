# Attendance System - Project Requirements & Enhancement Plan

## Current State Analysis

### What Currently Exists
- ✅ Basic Spring Boot application structure
- ✅ MySQL database integration
- ✅ JPA entities (Attendance)
- ✅ Basic REST API endpoints
- ✅ Two simple Thymeleaf pages (home, dashboard)
- ✅ Basic security config (currently permits all requests - NO REAL SECURITY)

### Current Issues
1. **Security**: All endpoints are open (`permitAll()`), CSRF disabled
2. **No Authentication**: No login/register functionality
3. **No User Management**: No User entity or user-related tables
4. **Basic UI**: Minimal HTML pages without proper styling or navigation
5. **No Authorization**: No role-based access control
6. **Missing Pages**: No login, register, or profile pages

---

## Project Requirements

### 1. User Authentication & Authorization

#### User Entity & Database
- [ ] Create `User` entity with fields:
  - `id` (Long, primary key)
  - `username` (String, unique)
  - `email` (String, unique)
  - `password` (String, encrypted with BCrypt) - **Optional if SSO only**
  - `firstName` (String)
  - `lastName` (String)
  - `role` (Enum: ADMIN, USER, MANAGER)
  - `enabled` (boolean)
  - `createdAt` (LocalDateTime)
  - `authProvider` (Enum: LOCAL, SSO) - **New field to track authentication source**
  - `providerId` (String) - **External IDP user ID**

#### Repositories & Services
- [ ] Create `UserRepository` (JpaRepository)
- [ ] Create `UserService` for user management
- [ ] Create `CustomUserDetailsService` implementing `UserDetailsService`
- [ ] Password encryption using `BCryptPasswordEncoder` (for local auth)
- [ ] Create `OAuth2UserService` for SSO user handling

#### Authentication Methods

##### Option A: Local Authentication (Traditional)
- [ ] **Login Page** (`/login`)
  - Username/Email field
  - Password field
  - "Remember me" checkbox
  - "Register" link
  - **"Sign in with SSO" button** (redirects to IDP)
  - Error messages for failed login
  - Proper styling and form validation

- [ ] **Register Page** (`/register`)
  - Username field (unique validation)
  - Email field (format validation)
  - Password field (strength requirements)
  - Confirm password field
  - First name & Last name
  - Submit button
  - "Already have account? Login" link
  - Server-side validation with error messages

##### Option B: SSO Authentication (Single Sign-On)

**🔐 SSO/IDP Integration (Details TBD)**

- [ ] **IDP Configuration**
  - IDP Provider: **[TO BE DETERMINED]** 
    - Potential options: Okta, Azure AD, Keycloak, Google Workspace, AWS Cognito, Auth0, etc.
  - Authentication Protocol: **[TO BE DETERMINED]**
    - Options: OAuth 2.0, SAML 2.0, OpenID Connect (OIDC)
  - Client ID/Application ID: **[TO BE CONFIGURED]**
  - Client Secret: **[TO BE CONFIGURED]**
  - Authorization Endpoint: **[TO BE CONFIGURED]**
  - Token Endpoint: **[TO BE CONFIGURED]**
  - User Info Endpoint: **[TO BE CONFIGURED]**
  - Redirect URI: `http://localhost:8080/login/oauth2/code/{provider}`
  - Scopes: `openid`, `profile`, `email` (standard OIDC scopes)

- [ ] **Spring Security OAuth2 Setup**
  - Add dependency: `spring-boot-starter-oauth2-client` (currently commented out in pom.xml)
  - Configure OAuth2 client properties in `application.properties` or `application.yml`
  - Implement `OAuth2UserService` to handle user creation/update from IDP
  - Map IDP user attributes to local User entity
  - Handle role assignment for SSO users

- [ ] **SSO Login Flow**
  - User clicks "Sign in with SSO" button
  - Redirect to IDP login page
  - User authenticates with IDP credentials
  - IDP redirects back with authorization code
  - Application exchanges code for access token
  - Retrieve user info from IDP
  - Create/update local user record
  - Establish application session
  - Redirect to dashboard

- [ ] **User Provisioning**
  - Auto-create user account on first SSO login (Just-In-Time provisioning)
  - Sync user profile from IDP (name, email)
  - Map IDP roles/groups to application roles
  - Handle user updates on subsequent logins
  - Option to link SSO account with existing local account

- [ ] **Hybrid Authentication Support**
  - Support both local and SSO authentication simultaneously
  - Allow users to choose authentication method on login page
  - Admin users can be created locally as fallback
  - Account linking: Allow SSO users to set local password (optional)

**Configuration Checklist (Once IDP is Known):**
- [ ] Register application with IDP
- [ ] Obtain Client ID and Client Secret
- [ ] Configure allowed redirect URIs in IDP
- [ ] Set up user claims/attributes mapping
- [ ] Configure role/group mappings
- [ ] Test authentication flow
- [ ] Configure session timeout alignment with IDP
- [ ] Set up token refresh mechanism (if applicable)
- [ ] Configure logout propagation (Single Logout)

- [ ] **Logout Functionality**
  - Logout button in navbar
  - Clear local application session
  - **SSO Logout**: Redirect to IDP logout endpoint (if supported)
  - Handle back-channel logout (if IDP supports it)
  - Redirect to login page after logout

### 2. Security Configuration

#### Spring Security Setup
- [ ] Configure `SecurityFilterChain` with:
  - Custom login page (`/login`)
  - Login processing URL (`/login`)
  - Default success URL (redirect to dashboard)
  - Failure URL (login with error)
  - Logout configuration (`/logout`)
  - CSRF protection enabled for forms
  - Remember-me functionality
  - **OAuth2 login configuration** (once IDP details available)
  - **OAuth2 success handler** for post-SSO login processing
  - **OAuth2 failure handler** for SSO error handling

- [ ] URL Access Rules:
  ```
  /login, /register, /oauth2/**, /login/oauth2/**, /css/**, /js/**, /images/** - permitAll
  /admin/** - hasRole('ADMIN')
  /api/** - authenticated
  /dashboard, / - authenticated
  ```

- [ ] Password Encoder Bean (BCrypt) - for local authentication
- [ ] Session management configuration
- [ ] **OAuth2 Client Registration Bean** (configure when IDP known)
- [ ] **Custom OAuth2UserService** for SSO user provisioning

### 3. Dashboard & Navigation

#### Navbar Component
- [ ] Responsive navigation bar with:
  - Application logo/title
  - Navigation links:
    - Home/Dashboard
    - Mark Attendance
    - View Records
    - Profile (if USER)
    - User Management (if ADMIN)
  - User dropdown menu:
    - Logged-in username display
    - Profile link
    - Settings link
    - Logout button
  - Mobile responsive (hamburger menu)

#### Dashboard Page Enhancements
- [ ] Welcome message with user's name
- [ ] Statistics cards:
  - Total attendance days
  - Present days
  - Absent days
  - Attendance percentage
- [ ] Quick actions section:
  - Mark attendance button
  - View full records
- [ ] Recent attendance records table
- [ ] Date range filter for records

### 4. Additional Pages

#### Profile Page (`/profile`)
- [ ] View user information
- [ ] Edit profile details
- [ ] Change password functionality
- [ ] Profile picture upload (optional)

#### Attendance Management Page (`/attendance`)
- [ ] Mark attendance form
- [ ] View personal attendance history
- [ ] Filter by date range
- [ ] Export to CSV/PDF (optional)
- [ ] Attendance calendar view

#### Admin Pages (Admin Role Only)
- [ ] **User Management** (`/admin/users`)
  - List all users
  - Add new user
  - Edit user details
  - Enable/Disable user accounts
  - Delete users
  
- [ ] **All Attendance Records** (`/admin/attendance`)
  - View all users' attendance
  - Filter by user, date range
  - Generate reports
  - Export functionality

#### Error Pages
- [ ] Custom 404 page
- [ ] Custom 403 (Access Denied) page
- [ ] Custom 500 (Server Error) page

### 5. Enhanced Attendance Features

#### Attendance Entity Updates
- [ ] Add relationship to User entity
- [ ] Add timestamp fields (check-in time)
- [ ] Add location tracking (optional)
- [ ] Add notes/remarks field
- [ ] Add approval status (for manager review)

#### Attendance Business Logic
- [ ] Prevent duplicate attendance for same day
- [ ] Automatic attendance marking system (optional)
- [ ] Late attendance tracking
- [ ] Leave management integration (optional)
- [ ] Notification system (optional)

### 6. UI/UX Improvements

#### Styling & Design
- [ ] Integrate CSS framework (Bootstrap 5 or Tailwind CSS)
- [ ] Consistent color scheme and branding
- [ ] Professional layout and typography
- [ ] Responsive design for mobile devices
- [ ] Loading spinners and animations
- [ ] Toast notifications for actions (success/error)

#### JavaScript Enhancements
- [ ] Form validation (client-side)
- [ ] Interactive calendar
- [ ] Charts for attendance statistics (Chart.js)
- [ ] AJAX for dynamic content updates
- [ ] Confirmation dialogs for delete actions

### 7. API Enhancements

#### RESTful API Endpoints
- [ ] Authentication APIs:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `POST /api/auth/logout`

- [ ] User APIs:
  - `GET /api/users/profile`
  - `PUT /api/users/profile`
  - `POST /api/users/change-password`

- [ ] Attendance APIs:
  - `POST /api/attendance/mark` (current user)
  - `GET /api/attendance/my-records`
  - `GET /api/attendance/stats`
  - `GET /api/admin/attendance` (all users - admin only)

- [ ] Admin APIs:
  - `GET /api/admin/users`
  - `POST /api/admin/users`
  - `PUT /api/admin/users/{id}`
  - `DELETE /api/admin/users/{id}`

### 8. Configuration & Best Practices

#### Application Properties Improvements
- [ ] Use environment variables for sensitive data
- [ ] Configure proper session timeout
- [ ] Set up logging levels
- [ ] Configure file upload limits (if needed)
- [ ] **OAuth2 client configuration** (when IDP details available):
  ```properties
  # SSO Configuration (Example - to be updated with actual IDP details)
  spring.security.oauth2.client.registration.{provider}.client-id=${IDP_CLIENT_ID}
  spring.security.oauth2.client.registration.{provider}.client-secret=${IDP_CLIENT_SECRET}
  spring.security.oauth2.client.registration.{provider}.scope=openid,profile,email
  spring.security.oauth2.client.registration.{provider}.redirect-uri={baseUrl}/login/oauth2/code/{provider}
  spring.security.oauth2.client.registration.{provider}.client-name={Provider Name}
  
  spring.security.oauth2.client.provider.{provider}.authorization-uri=${IDP_AUTHORIZATION_URI}
  spring.security.oauth2.client.provider.{provider}.token-uri=${IDP_TOKEN_URI}
  spring.security.oauth2.client.provider.{provider}.user-info-uri=${IDP_USERINFO_URI}
  spring.security.oauth2.client.provider.{provider}.user-name-attribute=sub
  spring.security.oauth2.client.provider.{provider}.jwk-set-uri=${IDP_JWK_SET_URI}
  ```

#### Security Best Practices
- [ ] Remove hardcoded credentials
- [ ] Enable CSRF protection
- [ ] Implement rate limiting (optional)
- [ ] Add security headers
- [ ] SQL injection prevention (already handled by JPA)
- [ ] XSS prevention
- [ ] **Store OAuth2 client secrets securely** (environment variables or secrets manager)
- [ ] **Validate JWT tokens from IDP** (signature verification)
- [ ] **Implement token expiry handling**
- [ ] **Set up HTTPS for production** (required for OAuth2)

#### Testing
- [ ] Unit tests for services
- [ ] Integration tests for controllers
- [ ] Security tests for authentication
- [ ] **OAuth2 integration tests** (mock IDP responses)
- [ ] **Test SSO login flow** with test IDP accounts
- [ ] **Test account linking** scenarios

---

## Technology Stack Additions

### Frontend
- **CSS Framework**: Bootstrap 5 or Tailwind CSS
- **JavaScript**: Vanilla JS or jQuery for interactivity
- **Icons**: Font Awesome or Bootstrap Icons
- **Charts**: Chart.js for statistics

### Backend
- **Spring Security**: Complete authentication setup with OAuth2 support
- **OAuth2 Client**: For SSO integration (uncomment in pom.xml when IDP decided)
- **Validation**: Jakarta Bean Validation
- **Lombok**: Reduce boilerplate code (optional)
- **JWT Library**: For token validation (if using JWT from IDP)

### Database
- **Tables to Add**:
  - `users` table with auth_provider and provider_id columns
  - Update `attendance` table with user_id foreign key
  - `roles` table (if using separate role entity)
  - `persistent_logins` (for remember-me)
  - `oauth2_authorized_client` (Spring Security OAuth2 - auto-created)

---

## Implementation Priority

### Phase 1: Authentication & Security (High Priority)
1. Create User entity (with SSO support fields), repository, and service
2. Implement local registration functionality
3. Implement local login functionality
4. Configure Spring Security for local auth
5. Add password encryption
6. **Phase 1B: SSO Integration (Once IDP Details Available)**
   - Uncomment OAuth2 dependency in pom.xml
   - Configure IDP settings in application.properties
   - Implement OAuth2UserService for SSO provisioning
   - Add "Sign in with SSO" button to login page
   - Test SSO login flow
   - Implement hybrid authentication (local + SSO)

### Phase 2: UI Enhancement (High Priority)
1. Design and implement navbar (with SSO login option)
2. Create proper login and register pages
3. Enhance dashboard with better layout
4. Add responsive design
5. Display authentication method on user profile (Local vs SSO)

### Phase 3: Core Features (Medium Priority)
1. Link attendance to users
2. Implement profile page
3. Add attendance statistics
4. Implement date range filtering
5. Show user's authentication provider in UI

### Phase 4: Admin Features (Medium Priority)
1. Create admin panel
2. Implement user management (support both local and SSO users)
3. Add all attendance records view
4. Implement reports
5. Admin ability to manually link SSO users

### Phase 5: Advanced Features (Low Priority)
1. Export functionality
2. Notifications
3. Leave management
4. Advanced statistics and charts
5. Account linking UI (allow users to link SSO with local account)

---

## File Structure After Enhancement

```
src/main/java/com/example/attendance_system/
├── config/
│   └── SecurityConfig.java ✅ (to be enhanced)
├── controller/
│   ├── AuthController.java (new)
│   ├── UserController.java (new)
│   ├── AdminController.java (new)
│   ├── AttendanceController.java ✅
│   └── ViewController.java ✅ (to be enhanced)
├── entity/
│   ├── User.java (new)
│   ├── Role.java (new - optional)
│   └── Attendance.java ✅ (to be enhanced)
├── repository/
│   ├── UserRepository.java (new)
│   └── AttendanceRepository.java ✅
├── service/
│   ├── UserService.java (new)
│   ├── CustomUserDetailsService.java (new)
│   ├── CustomOAuth2UserService.java (new - for SSO)
│   └── AttendanceService.java ✅ (to be enhanced)
├── dto/ (new)
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── OAuth2UserInfo.java (new - for SSO user data)
│   └── UserProfileDTO.java
└── exception/ (new)
    ├── UserAlreadyExistsException.java
    ├── OAuth2AuthenticationProcessingException.java (new)
    └── GlobalExceptionHandler.java

src/main/resources/
├── templates/
│   ├── layout/
│   │   ├── base.html (new)
│   │   └── navbar.html (new)
│   ├── auth/
│   │   ├── login.html (new)
│   │   └── register.html (new)
│   ├── user/
│   │   ├── profile.html (new)
│   │   └── attendance.html (new)
│   ├── admin/
│   │   ├── users.html (new)
│   │   └── attendance-all.html (new)
│   ├── error/
│   │   ├── 404.html (new)
│   │   └── 403.html (new)
│   ├── dashboard.html ✅ (to be enhanced)
│   └── home.html ✅ (to be enhanced)
├── static/
│   ├── css/
│   │   ├── style.css (to be created)
│   │   ├── login.css (new)
│   │   └── dashboard.css (new)
│   ├── js/
│   │   ├── main.js (new)
│   │   └── attendance.js (new)
│   └── images/ (new)
└── application.properties ✅
```

---

## SSO/IDP Decision Matrix

### Pending Information Required:
1. **IDP Provider Name** - Which identity provider will be used?
2. **Protocol** - OAuth 2.0, SAML 2.0, or OpenID Connect?
3. **IDP Endpoints** - Authorization, Token, UserInfo URLs
4. **Credentials** - Client ID and Client Secret
5. **User Attributes** - Which claims/attributes are provided by IDP?
6. **Role Mapping** - How do IDP groups/roles map to application roles?
7. **Logout Behavior** - Does IDP support Single Logout?

### Common IDP Options & Configuration:

#### Option 1: Azure AD (Microsoft Entra ID)
- Protocol: OpenID Connect (OAuth 2.0)
- Spring Boot: `spring-boot-starter-oauth2-client`
- Provider: `azure`
- Well-known configuration: `https://login.microsoftonline.com/{tenant}/v2.0/.well-known/openid-configuration`

#### Option 2: Okta
- Protocol: OpenID Connect (OAuth 2.0)
- Spring Boot: `spring-boot-starter-oauth2-client`
- Provider: `okta`
- Issuer URI: `https://{yourOktaDomain}/oauth2/default`

#### Option 3: Keycloak (Self-hosted)
- Protocol: OpenID Connect (OAuth 2.0)
- Spring Boot: `spring-boot-starter-oauth2-client`
- Provider: `keycloak`
- Issuer URI: `http://localhost:8080/realms/{realm-name}`

#### Option 4: Google Workspace
- Protocol: OpenID Connect (OAuth 2.0)
- Spring Boot: Built-in support
- Provider: `google`
- Pre-configured in Spring Security

#### Option 5: AWS Cognito
- Protocol: OpenID Connect (OAuth 2.0)
- Spring Boot: `spring-boot-starter-oauth2-client`
- Provider: `cognito`
- Issuer URI: `https://cognito-idp.{region}.amazonaws.com/{userPoolId}`

#### Option 6: Auth0
- Protocol: OpenID Connect (OAuth 2.0)
- Spring Boot: `spring-boot-starter-oauth2-client`
- Provider: `auth0`
- Issuer URI: `https://{yourDomain}.auth0.com/`

### Decision Impact:
- **Development**: Configuration complexity varies by IDP
- **Cost**: Cloud IDPs have licensing costs; Keycloak is free
- **Integration**: Some IDPs have Spring Boot auto-configuration
- **Features**: Advanced features like MFA, conditional access vary
- **Support**: Enterprise support availability

**ACTION REQUIRED**: Once IDP is selected, uncomment OAuth2 dependency in pom.xml and update configuration accordingly.

---

## Notes
- All passwords must be encrypted using BCryptPasswordEncoder (for local auth)
- CSRF protection should be enabled for all POST requests
- Session management should be configured properly
- Remember to create initial admin user (via data.sql or CLI)
- Consider adding email verification for registration (advanced feature)
- **SSO users may not have passwords in local database** - handle this in User entity and login logic
- **Test both authentication methods** (local and SSO) thoroughly before production
- **Document IDP onboarding process** for new users once IDP is configured
