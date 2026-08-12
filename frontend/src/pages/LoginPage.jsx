import { useState } from "react";
import {
  Link,
  useNavigate,
} from "react-router";

import { loginUser } from "../services/api";

function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();

  async function handleSubmit(event) {
    event.preventDefault();

    setErrorMessage("");
    setIsLoading(true);

    try {
      const data = await loginUser(email, password);

      localStorage.setItem(
        "matchly_access_token",
        data.accessToken,
      );

      localStorage.setItem(
        "matchly_user",
        JSON.stringify(data.user),
      );

      window.dispatchEvent(
        new Event("matchly-auth-changed"),
      );

      navigate("/");
    } catch (error) {
      setErrorMessage(error.message);
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <main className="page-shell">
      <section className="page-card auth-card">
        <p className="badge">Welcome back</p>

        <h1>Log in to Matchly</h1>

        <p className="page-description">
          Log in to continue chatting and meeting new people.
        </p>

        <form className="login-form" onSubmit={handleSubmit}>
          <label htmlFor="email">Email</label>

          <input
            id="email"
            type="email"
            placeholder="you@example.com"
            autoComplete="email"
            value={email}
            onChange={(event) =>
              setEmail(event.target.value)
            }
            required
          />

          <label htmlFor="password">Password</label>

          <input
            id="password"
            type="password"
            placeholder="Enter your password"
            autoComplete="current-password"
            value={password}
            onChange={(event) =>
              setPassword(event.target.value)
            }
            required
          />

          {errorMessage && (
            <p className="auth-error" role="alert">
              {errorMessage}
            </p>
          )}

          <button
            className="primary-button"
            type="submit"
            disabled={isLoading}
          >
            {isLoading ? "Logging in..." : "Log In"}
          </button>

          <p className="auth-switch">
            Don&apos;t have an account?{" "}
            <Link to="/signup">Create one</Link>
          </p>
        </form>
      </section>
    </main>
  );
}

export default LoginPage;