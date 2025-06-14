// Elements
const loginButton = document.getElementById("login-button");
const callResourceButton = document.getElementById("call-resource-button");
const logoutButton = document.getElementById("logoutButton");
const resultDiv = document.getElementById("result");

// OAuth 2.0 config
const clientId = "spa-client";
const redirectUri = "http://127.0.0.1:8080/";
const authorizationEndpoint = "http://localhost:9001/oauth2/authorize";
const tokenEndpoint = "http://localhost:9001/oauth2/token";
const resourceServerEndpoint = "http://localhost:8091/api/home";
const scope = "openid profile read write offline_access";

let accessToken = null;

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
  const base64 = btoa(String.fromCharCode(...new Uint8Array(digest)))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
  return base64;
}

function generateRandomState() {
  return Array.from(window.crypto.getRandomValues(new Uint8Array(16)))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

// Build authorization URL
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

// Handle redirect and exchange code
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
        body: body,
      });

      if (!response.ok) throw new Error("Token request failed");

      const tokenData = await response.json();
      accessToken = tokenData.access_token;
      localStorage.setItem("accessToken", accessToken);

      resultDiv.innerText = "Access token received!";
      updateUI();

      history.replaceState({}, document.title, window.location.pathname);
    } catch (err) {
      console.error(err);
      resultDiv.innerText = "Token exchange failed";
    }
  }
}

// UI
function updateUI() {
  if (accessToken) {
    loginButton.disabled = true;
    callResourceButton.disabled = false;
  } else {
    loginButton.disabled = false;
    callResourceButton.disabled = true;
  }
}

// Call protected resource
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

// Login
loginButton.addEventListener("click", initiateAuthFlow);

// Logout
logoutButton.addEventListener("click", () => {
  localStorage.removeItem("accessToken");
  accessToken = null;
  resultDiv.innerText = "Logged out.";
  updateUI();
});

// On load
window.addEventListener("load", () => {
  accessToken = localStorage.getItem("accessToken");
  if (accessToken) {
    resultDiv.innerText = "Using stored token.";
  }
  updateUI();
  handleAuthorizationResponse();
});
