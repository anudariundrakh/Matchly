const API_BASE_URL =
  import.meta.env.VITE_API_URL ?? "http://localhost:8080";

async function handleResponse(response) {
  const isJson = response.headers
    .get("content-type")
    ?.includes("application/json");

  const data = isJson ? await response.json() : null;

  if (!response.ok) {
    let message = data?.message;

    if (!message && response.status === 401) {
      message = "Invalid email or password";
    }

    if (!message && response.status === 409) {
      message = "An account with this email already exists";
    }

    throw new Error(message ?? "Something went wrong");
  }

  return data;
}

export async function loginUser(email, password) {
  const response = await fetch(
    `${API_BASE_URL}/api/auth/login`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email,
        password,
      }),
    },
  );

  return handleResponse(response);
}

export async function registerUser(
  email,
  displayName,
  password,
) {
  const response = await fetch(
    `${API_BASE_URL}/api/users`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email,
        displayName,
        password,
      }),
    },
  );

  return handleResponse(response);
}

export async function getCurrentUser() {
  const accessToken = localStorage.getItem(
    "matchly_access_token",
  );

  if (!accessToken) {
    throw new Error("You are not logged in");
  }

  const response = await fetch(
    `${API_BASE_URL}/api/users/me`,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );

  return handleResponse(response);
}

export async function joinMatchmaking() {
  const accessToken = localStorage.getItem(
    "matchly_access_token",
  );

  const response = await fetch(
    `${API_BASE_URL}/api/matchmaking/join`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );

  return handleResponse(response);
}

export async function getMatchmakingStatus() {
  const accessToken = localStorage.getItem(
    "matchly_access_token",
  );

  const response = await fetch(
    `${API_BASE_URL}/api/matchmaking/status`,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );

  return handleResponse(response);
}

export async function leaveMatchmaking() {
  const accessToken = localStorage.getItem(
    "matchly_access_token",
  );

  const response = await fetch(
    `${API_BASE_URL}/api/matchmaking/leave`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );

  if (!response.ok) {
    throw new Error("Could not leave matchmaking");
  }
}