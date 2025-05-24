const loginButton = document.getElementById("login-button");
const callResourceButton = document.getElementById("call-resource-button");
const resultDiv = document.getElementById("result");

// OAuth 2.0 Configuration
const clientId = "spa-client";
const redirectUri = "http://127.0.0.1:8080/";
const authorizationEndpoint = "http://localhost:9001/oauth2/authorize";
const tokenEndpoint = "http://localhost:9001/oauth2/token";
const resourceServerEndpoint = "http://localhost:8091";
const scope = "read write openid profile";

let accessToken = null;

// Create a new PKCE instance with state handling
const pkce = new PKCE({
  client_id: clientId,
  redirect_uri: redirectUri,
  authorization_endpoint: authorizationEndpoint,
  token_endpoint: tokenEndpoint,
  requested_scopes: scope,
  storage: sessionStorage,
  state: true, // Enable state parameter
  token_endpoint_auth_method: "none",
  response_type: "code",
  code_challenge_method: "S256",
});

// Function to update UI based on authentication status
function updateUI() {
  if (accessToken) {
    loginButton.disabled = true;
    callResourceButton.disabled = false;
  } else {
    loginButton.disabled = false;
    callResourceButton.disabled = true;
  }
}

// Handle the redirect from the Authorization Server and token exchange
async function handleAuthorizationResponse() {
  if (window.location.search.includes("code=")) {
    resultDiv.innerText =
      "Authorization code received. Exchanging for tokens...";

    try {
      const tokenResponse = await pkce.exchangeForAccessToken(
        window.location.href
      );
      console.log("Raw Token Response:", tokenResponse); // Debug log

      if (tokenResponse.error) {
        throw new Error(tokenResponse.error_description || tokenResponse.error);
      }

      // Handle different response formats
      let tokenData;
      if (typeof tokenResponse === "string") {
        try {
          tokenData = JSON.parse(tokenResponse);
        } catch (e) {
          console.error("Failed to parse token response:", e);
          throw new Error("Invalid token response format");
        }
      } else if (tokenResponse instanceof Response) {
        tokenData = await tokenResponse.json();
      } else {
        tokenData = tokenResponse;
      }

      console.log("Parsed Token Data:", tokenData); // Debug log

      if (!tokenData || !tokenData.access_token) {
        console.error("Token data:", tokenData);
        throw new Error("No access token in response");
      }

      accessToken = tokenData.access_token;
      console.log("Access Token:", accessToken); // Debug log

      // Store the token
      localStorage.setItem("accessToken", accessToken);
      resultDiv.innerText = "Access token obtained!";
      updateUI();

      // Remove code and state parameters from URL
      history.replaceState({}, document.title, window.location.pathname);
    } catch (error) {
      console.error("Token exchange error:", error);
      resultDiv.innerText = `Error exchanging code for tokens: ${error.message}`;
    }
  }
}

// Check for existing token in local storage on page load
function checkLocalStorageForToken() {
  const storedToken = localStorage.getItem("accessToken");
  if (storedToken) {
    accessToken = storedToken;
    resultDiv.innerText = "Using stored access token.";
    updateUI();
  }
}

// Event listener for the login button
loginButton.addEventListener("click", () => {
  // Generate and store state
  const state = pkce.generateState();
  sessionStorage.setItem("oauth_state", state);

  // Redirect to the Authorization Server using js-pkce
  window.location.replace(pkce.authorizeUrl());
});

// Get the social login buttons
const googleLoginButton = document.getElementById("google-login-button");
const githubLoginButton = document.getElementById("github-login-button");

// Event listener for Google login button
googleLoginButton.addEventListener("click", () => {
  // Redirect to the Authorization Server's Google authorization endpoint
  window.location.replace("http://localhost:9001/oauth2/authorization/google");
});

// Event listener for GitHub login button
githubLoginButton.addEventListener("click", () => {
  // Redirect to the Authorization Server's GitHub authorization endpoint
  window.location.replace("http://localhost:9001/oauth2/authorization/github");
});

// Event listener for the call resource button
callResourceButton.addEventListener("click", async () => {
  if (!accessToken) {
    resultDiv.innerText = "No access token available. Please login first.";
    return;
  }

  try {
    resultDiv.innerText = "Calling Resource Server...";
    console.log("Using Access Token:", accessToken); // Debug log

    const resourceResponse = await fetch(resourceServerEndpoint + "/api/home", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      credentials: "include",
    });

    if (resourceResponse.ok) {
      const resourceData = await resourceResponse.text();
      resultDiv.innerText = `Resource Server Response: ${resourceData}`;
    } else {
      const errorText = await resourceResponse.text();
      console.error("Resource Server Error:", errorText); // Debug log
      resultDiv.innerText = `Error calling Resource Server: ${resourceResponse.status} ${resourceResponse.statusText} - ${errorText}`;
    }
  } catch (error) {
    console.error("Resource Server Error:", error); // Debug log
    resultDiv.innerText = `Error calling Resource Server: ${error}`;
  }
});

// Get the logout button
const logoutButton = document.getElementById("logoutButton");

// Function to handle logout
function handleLogout() {
  localStorage.removeItem("accessToken");
  accessToken = null;
  resultDiv.innerText = "Logged out.";
  updateUI();
}

// Event listener for the logout button
logoutButton.addEventListener("click", handleLogout);

// Run checks on page load
window.addEventListener("load", () => {
  checkLocalStorageForToken();
  handleAuthorizationResponse();
});
