# Firebase Authentication in API Requests

## Overview
All HTTP requests to the backend server now include Firebase Authentication tokens in the `Authorization` header. This ensures that only authenticated users can access protected endpoints.

## Implementation Details

### Authentication Flow
1. **Check User Authentication**: Before making any API request, verify that a Firebase user is currently signed in
2. **Get ID Token**: Retrieve the Firebase ID token from the current user
3. **Add Authorization Header**: Include the token in the request header as `Authorization: Bearer <token>`
4. **Make Request**: Send the authenticated request to the server

### Modified Files

#### 1. CreateGroupFragment.java
**Location**: `app/src/main/java/com/beeproductive/android/CreateGroupFragment.java`

**Changes**:
- Added Firebase Auth initialization
- Split `createGroup()` method into two:
  - `createGroup(String groupName)`: Gets Firebase token
  - `createGroupWithToken(String groupName, String token)`: Makes authenticated POST request
- Authorization header format: `Authorization: Bearer <firebase-token>`

**Request Format**:
```json
POST /group/create
Headers:
  Authorization: Bearer <firebase-id-token>
  Content-Type: application/json
Body:
  {
    "name": "Group Name"
  }
```

#### 2. JoinGroupFragment.java
**Location**: `app/src/main/java/com/beeproductive/android/JoinGroupFragment.java`

**Changes**:
- Added Firebase Auth initialization
- Split `joinGroup()` method into two:
  - `joinGroup(String groupCode)`: Gets Firebase token
  - `joinGroupWithToken(String groupCode, String token)`: Makes authenticated POST request
- Authorization header format: `Authorization: Bearer <firebase-token>`

**Request Format**:
```json
POST /group/join
Headers:
  Authorization: Bearer <firebase-id-token>
  Content-Type: application/json
Body:
  {
    "groupCode": "ABC123"
  }
```

#### 3. ApiHelper.java (New Utility Class)
**Location**: `app/src/main/java/com/beeproductive/android/utils/ApiHelper.java`

**Purpose**: Reusable utility class for making authenticated API requests

**Features**:
- Shared OkHttpClient instance for efficient connection pooling
- `makeAuthenticatedPostRequest()`: POST requests with automatic token handling
- `makeAuthenticatedGetRequest()`: GET requests with automatic token handling
- `ApiCallback` interface for handling responses

**Usage Example**:
```java
JSONObject requestBody = new JSONObject();
requestBody.put("name", "My Group");

ApiHelper.makeAuthenticatedPostRequest(
    requireContext(),
    ServerConfig.ENDPOINT_GROUPS_CREATE,
    requestBody,
    new ApiHelper.ApiCallback() {
        @Override
        public void onSuccess(String responseBody) {
            // Handle success
        }

        @Override
        public void onError(String errorMessage, int statusCode) {
            // Handle error
        }
    }
);
```

## Error Handling

### Authentication Errors
- **No User Signed In**: Shows toast "Please sign in to [action]"
- **Failed to Get Token**: Shows toast "Failed to get authentication token"
- **Authentication Failed**: Shows toast with detailed error message

### Network Errors
- **Connection Failed**: Shows toast with network error details
- **Server Error**: Shows toast with error message from server response

## Security Considerations

1. **Token Refresh**: Firebase automatically handles token refresh. Tokens are requested with `getIdToken(false)` where `false` means "use cached token if valid"

2. **Token Expiration**: Firebase ID tokens expire after 1 hour. The SDK automatically refreshes them as needed

3. **HTTPS Required**: In production, always use HTTPS to protect tokens in transit

4. **Backend Verification**: The backend server must verify the Firebase ID token on each request using the Firebase Admin SDK

## Backend Requirements

The backend server should:
1. Extract the token from the `Authorization: Bearer <token>` header
2. Verify the token using Firebase Admin SDK
3. Extract user information (UID, email, etc.) from the verified token
4. Use the UID to associate data with the user

**Example Backend Verification (Java/Spring)**:
```java
String token = request.getHeader("Authorization").replace("Bearer ", "");
FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
String uid = decodedToken.getUid();
```

## Testing

### Emulator Testing
- Server URL: `http://10.0.2.2:8080` (configured in ServerConfig.java)
- Ensure user is signed in before testing API calls

### Physical Device Testing
- Update ServerConfig.BASE_URL to your computer's local IP address
- Ensure device is on the same network as the development server

## Future Enhancements

1. **Token Caching**: Consider caching the token locally to reduce Firebase API calls
2. **Retry Logic**: Add automatic retry for failed requests due to token expiration
3. **Request Interceptor**: Create an OkHttp interceptor to automatically add auth headers to all requests
4. **Error Recovery**: Implement automatic sign-in prompt when auth errors occur
