# Google OAuth2 Setup Guide

This guide explains how to set up Google OAuth2 for social login in the OAuth server.

## Prerequisites

1. A Google Cloud Platform account
2. Access to Google Cloud Console

## Setup Steps

### 1. Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Google+ API and Google OAuth2 API

### 2. Configure OAuth2 Credentials

1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "OAuth 2.0 Client IDs"
3. Choose "Web application" as the application type
4. Set the following:
   - **Name**: OAuth Server (or any name you prefer)
   - **Authorized JavaScript origins**: `http://localhost:9001`
   - **Authorized redirect URIs**: `http://localhost:9001/oauth2/authorization/google`

### 3. Get Client ID and Secret

1. After creating the OAuth2 client, you'll get:
   - **Client ID**: A long string ending with `.apps.googleusercontent.com`
   - **Client Secret**: A secret string

### 4. Configure Environment Variables

Set the following environment variables:

```bash
export GOOGLE_CLIENT_ID="your-client-id.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="your-client-secret"
```

Or add them to your `application.properties`:

```properties
spring.security.oauth2.client.registration.google.client-id=your-client-id.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=your-client-secret
```

### 5. Test the Setup

1. Start the OAuth server
2. Navigate to `http://localhost:9001`
3. Click "Login with Google"
4. You should be redirected to Google's consent screen
5. After successful authentication, you'll be redirected back with a JWT token

## Security Considerations

1. **Never commit client secrets to version control**
2. Use environment variables or secure configuration management
3. In production, use HTTPS URLs
4. Regularly rotate client secrets
5. Monitor OAuth2 usage and implement rate limiting

## Troubleshooting

### Common Issues

1. **"Invalid redirect URI" error**:
   - Ensure the redirect URI in Google Console matches exactly: `http://localhost:9001/oauth2/authorization/google`

2. **"Client ID not found" error**:
   - Verify the client ID is correct and the OAuth2 client is properly configured

3. **"Access denied" error**:
   - Check if the Google+ API is enabled in your Google Cloud project

### Debug Mode

Enable debug logging by adding to `application.properties`:

```properties
logging.level.org.springframework.security.oauth2=DEBUG
logging.level.org.springframework.security=DEBUG
```

## Production Deployment

For production deployment:

1. Update redirect URIs to use HTTPS
2. Use a proper domain name
3. Implement proper session management
4. Add CSRF protection
5. Use secure cookie settings
6. Implement proper error handling and logging 