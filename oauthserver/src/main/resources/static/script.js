// // Elements
// const loginButton = document.getElementById("login-button");
// const callResourceButton = document.getElementById("call-resource-button");
// const logoutButton = document.getElementById("logoutButton");
// const resultDiv = document.getElementById("result");

// // OAuth 2.0 config
// const clientId = "spa-client";
// const redirectUri = "http://localhost:9001/html/callback.html";
// const authorizationEndpoint = "http://localhost:9001/oauth2/authorize";
// const tokenEndpoint = "http://localhost:9001/oauth2/token";
// const resourceServerEndpoint = "http://localhost:8091/api/home";
// const scope = "openid profile read write offline_access";

// let accessToken = null;
// let idleTimer = null;
// const IDLE_TIMEOUT_MS = 4 * 60 * 1000; // 4 minutes

// // PKCE utils
// async function generateCodeVerifier() {
//   const array = new Uint8Array(32);
//   window.crypto.getRandomValues(array);
//   return btoa(String.fromCharCode(...array))
//     .replace(/\+/g, "-")
//     .replace(/\//g, "_")
//     .replace(/=+$/, "");
// }

// async function generateCodeChallenge(codeVerifier) {
//   const encoder = new TextEncoder();
//   const data = encoder.encode(codeVerifier);
//   const digest = await window.crypto.subtle.digest("SHA-256", data);
//   return btoa(String.fromCharCode(...new Uint8Array(digest)))
//     .replace(/\+/g, "-")
//     .replace(/\//g, "_")
//     .replace(/=+$/, "");
// }

// function generateRandomState() {
//   return Array.from(window.crypto.getRandomValues(new Uint8Array(16)))
//     .map((b) => b.toString(16).padStart(2, "0"))
//     .join("");
// }

// // Auth Flow
// async function initiateAuthFlow() {
//   const codeVerifier = await generateCodeVerifier();
//   const codeChallenge = await generateCodeChallenge(codeVerifier);
//   const state = generateRandomState();

//   sessionStorage.setItem("code_verifier", codeVerifier);
//   sessionStorage.setItem("oauth_state", state);

//   const authUrl =
//     `${authorizationEndpoint}?response_type=code` +
//     `&client_id=${encodeURIComponent(clientId)}` +
//     `&redirect_uri=${encodeURIComponent(redirectUri)}` +
//     `&scope=${encodeURIComponent(scope)}` +
//     `&code_challenge=${encodeURIComponent(codeChallenge)}` +
//     `&code_challenge_method=S256` +
//     `&state=${encodeURIComponent(state)}`;

//   window.location.href = authUrl;
// }

// async function handleAuthorizationResponse() {
//   const params = new URLSearchParams(window.location.search);
//   if (params.has("code")) {
//     const code = params.get("code");
//     const returnedState = params.get("state");
//     const expectedState = sessionStorage.getItem("oauth_state");

//     if (returnedState !== expectedState) {
//       resultDiv.innerText = "Invalid state: possible CSRF detected.";
//       return;
//     }

//     const codeVerifier = sessionStorage.getItem("code_verifier");

//     const body = new URLSearchParams();
//     body.append("grant_type", "authorization_code");
//     body.append("code", code);
//     body.append("redirect_uri", redirectUri);
//     body.append("client_id", clientId);
//     body.append("code_verifier", codeVerifier);

//     try {
//       const response = await fetch(tokenEndpoint, {
//         method: "POST",
//         headers: { "Content-Type": "application/x-www-form-urlencoded" },
//         body,
//       });

//       if (!response.ok) throw new Error("Token request failed");

//       const tokenData = await response.json();
//       accessToken = tokenData.access_token;
//       localStorage.setItem("accessToken", tokenData.access_token || "");
//       localStorage.setItem("refreshToken", tokenData.refresh_token || "");
//       localStorage.setItem("idToken", tokenData.id_token || "");
//       localStorage.setItem("alldata", JSON.stringify(tokenData));
//       resultDiv.innerText = "Access token received!";
//       updateUI();
//       resetIdleTimer();
//       history.replaceState({}, document.title, window.location.pathname);
//     } catch (err) {
//       console.error(err);
//       resultDiv.innerText = "Token exchange failed";
//     }
//   }
// }

// // Refresh token logic
// async function refreshAccessToken() {
//   const refreshToken = localStorage.getItem("refreshToken");
//   if (!refreshToken) {
//     resultDiv.innerText = "Session expired. Please login again.";
//     logout();
//     return;
//   }

//   const body = new URLSearchParams();
//   body.append("grant_type", "refresh_token");
//   body.append("refresh_token", refreshToken);
//   body.append("client_id", clientId);

//   try {
//     const response = await fetch(tokenEndpoint, {
//       method: "POST",
//       headers: { "Content-Type": "application/x-www-form-urlencoded" },
//       body,
//     });

//     if (!response.ok) throw new Error("Refresh failed");

//     const tokenData = await response.json();
//     accessToken = tokenData.access_token;
//     localStorage.setItem("accessToken", tokenData.access_token || "");
//     if (tokenData.refresh_token) {
//       localStorage.setItem("refreshToken", tokenData.refresh_token);
//     }

//     resultDiv.innerText = "Token refreshed silently";
//     updateUI();
//     resetIdleTimer();
//   } catch (err) {
//     console.error(err);
//     resultDiv.innerText = "Unable to refresh. Please login again.";
//     logout();
//   }
// }

// // UI
// function updateUI() {
//   if (accessToken) {
//     loginButton.disabled = true;
//     callResourceButton.disabled = false;
//   } else {
//     loginButton.disabled = false;
//     callResourceButton.disabled = true;
//   }
// }

// // API Call
// callResourceButton.addEventListener("click", async () => {
//   try {
//     resultDiv.innerText = "Calling API...";
//     const response = await fetch(resourceServerEndpoint, {
//       headers: {
//         Authorization: `Bearer ${accessToken}`,
//       },
//     });

//     const text = await response.text();
//     resultDiv.innerText = `Response: ${text}`;
//   } catch (err) {
//     resultDiv.innerText = "API call failed";
//   }
// });

// // Logout
// logoutButton.addEventListener("click", logout);
// function logout() {
//   localStorage.removeItem("accessToken");
//   localStorage.removeItem("refreshToken");
//   localStorage.removeItem("idToken");
//   accessToken = null;
//   clearTimeout(idleTimer);
//   resultDiv.innerText = "Logged out.";
//   updateUI();
// }

// // Idle Timer
// function resetIdleTimer() {
//   clearTimeout(idleTimer);
//   idleTimer = setTimeout(refreshAccessToken, IDLE_TIMEOUT_MS);
// }

// ["mousemove", "keydown", "click", "scroll"].forEach((event) =>
//   window.addEventListener(event, resetIdleTimer)
// );

// // On load
// window.addEventListener("load", () => {
//   accessToken = localStorage.getItem("accessToken");
//   if (accessToken) {
//     resultDiv.innerText = "Using stored token.";
//     updateUI();
//     resetIdleTimer();
//   }
//   handleAuthorizationResponse();
// });

// // Login
// loginButton.addEventListener("click", initiateAuthFlow);

// Elements
const loginButton = document.getElementById("login-button");
const callResourceButton = document.getElementById("call-resource-button");
const logoutButton = document.getElementById("logoutButton");
const resultDiv = document.getElementById("result");

// OAuth 2.0 config
const clientId = "spa-client";
const redirectUri = "http://localhost:9001/";
const authorizationEndpoint = "http://localhost:9001/oauth2/authorize";
const tokenEndpoint = "http://localhost:9001/oauth2/token";
const resourceServerEndpoint = "http://localhost:8091/api/home";
const scope = "openid profile read write offline_access";

let accessToken = null;
let refreshInterval = null;
let idleTimeout = null;

// PKCE utils
async function generateCodeVerifier() {
  const array = new Uint8Array(32);
  window.crypto.getRandomValues(array);
  return btoa(String.fromCharCode(...array))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
}

async function generateCodeChallenge(codeVerifier) {
  const encoder = new TextEncoder();
  const data = encoder.encode(codeVerifier);
  const digest = await window.crypto.subtle.digest("SHA-256", data);
  return btoa(String.fromCharCode(...new Uint8Array(digest)))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
}

function generateRandomState() {
  return Array.from(window.crypto.getRandomValues(new Uint8Array(16)))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

// Build auth URL
async function initiateAuthFlow() {
  const codeVerifier = await generateCodeVerifier();
  const codeChallenge = await generateCodeChallenge(codeVerifier);
  const state = generateRandomState();

  sessionStorage.setItem("code_verifier", codeVerifier);
  sessionStorage.setItem("oauth_state", state);

  const authUrl =
    `${authorizationEndpoint}?response_type=code` +
    `&client_id=${encodeURIComponent(clientId)}` +
    `&redirect_uri=${encodeURIComponent(redirectUri)}` +
    `&scope=${encodeURIComponent(scope)}` +
    `&code_challenge=${encodeURIComponent(codeChallenge)}` +
    `&code_challenge_method=S256` +
    `&state=${encodeURIComponent(state)}`;

  window.location.href = authUrl;
}

// Exchange code for token
async function handleAuthorizationResponse() {
  const params = new URLSearchParams(window.location.search);
  if (params.has("code")) {
    const code = params.get("code");
    const returnedState = params.get("state");
    const expectedState = sessionStorage.getItem("oauth_state");

    if (returnedState !== expectedState) {
      resultDiv.innerText = "Invalid state: possible CSRF detected.";
      return;
    }

    const codeVerifier = sessionStorage.getItem("code_verifier");
    const body = new URLSearchParams();
    body.append("grant_type", "authorization_code");
    body.append("code", code);
    body.append("redirect_uri", redirectUri);
    body.append("client_id", clientId);
    body.append("code_verifier", codeVerifier);

    try {
      const response = await fetch(tokenEndpoint, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body,
      });

      if (!response.ok) throw new Error("Token request failed");

      const tokenData = await response.json();
      accessToken = tokenData.access_token;
      localStorage.setItem("access_token", tokenData.access_token || "");
      localStorage.setItem("refresh_token", tokenData.refresh_token || "");
      localStorage.setItem("id_token", tokenData.id_token || "");

      resultDiv.innerText = "Access token received!";
      updateUI();
      startRefreshTimer();
      resetIdleTimer();

      history.replaceState({}, document.title, window.location.pathname);
    } catch (err) {
      console.error(err);
      resultDiv.innerText = "Token exchange failed";
    } finally {
      sessionStorage.removeItem("code_verifier");
      sessionStorage.removeItem("oauth_state");
    }
  }
}

// Refresh token
async function refreshAccessToken() {
  const refreshToken = localStorage.getItem("refresh_token");
  if (!refreshToken) return;

  const body = new URLSearchParams();
  body.append("grant_type", "refresh_token");
  body.append("refresh_token", refreshToken);
  body.append("client_id", clientId);

  try {
    const response = await fetch(tokenEndpoint, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body,
    });

    if (!response.ok) throw new Error("Refresh token failed");

    const tokenData = await response.json();
    accessToken = tokenData.access_token;

    localStorage.setItem("access_token", tokenData.access_token || "");
    localStorage.setItem("id_token", tokenData.id_token || "");
    if (tokenData.refresh_token) {
      localStorage.setItem("refresh_token", tokenData.refresh_token);
    }

    updateUI();
    console.log("🔁 Token silently refreshed");
  } catch (err) {
    console.error("🔒 Refresh failed", err);
    logout();
  }
}

// Timer for refresh
function startRefreshTimer() {
  clearInterval(refreshInterval);
  refreshInterval = setInterval(refreshAccessToken, 4 * 60 * 1000); // every 4 min
}

// Idle timer: logout if inactive for 10 minutes
function resetIdleTimer() {
  clearTimeout(idleTimeout);
  idleTimeout = setTimeout(() => {
    resultDiv.innerText = "Logged out due to inactivity.";
    logout();
  }, 10 * 60 * 1000);
}

// Update UI
function updateUI() {
  if (accessToken) {
    loginButton.disabled = true;
    callResourceButton.disabled = false;
  } else {
    loginButton.disabled = false;
    callResourceButton.disabled = true;
  }
}

// Logout
function logout() {
  localStorage.removeItem("access_token");
  localStorage.removeItem("refresh_token");
  localStorage.removeItem("id_token");
  accessToken = null;
  clearInterval(refreshInterval);
  clearTimeout(idleTimeout);
  updateUI();
}

// Call resource
callResourceButton.addEventListener("click", async () => {
  try {
    resultDiv.innerText = "Calling API...";
    const response = await fetch(resourceServerEndpoint, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });

    const text = await response.text();
    resultDiv.innerText = `Response: ${text}`;
  } catch (err) {
    resultDiv.innerText = "API call failed";
  }
});

// Events
loginButton.addEventListener("click", initiateAuthFlow);
logoutButton.addEventListener("click", logout);

// Monitor user activity
["click", "mousemove", "keypress"].forEach((event) =>
  document.addEventListener(event, resetIdleTimer)
);

// On load
window.addEventListener("load", () => {
  accessToken = localStorage.getItem("access_token");
  if (accessToken) {
    resultDiv.innerText = "Using stored token.";
    updateUI();
    startRefreshTimer();
    resetIdleTimer();
  }
  handleAuthorizationResponse();
});
