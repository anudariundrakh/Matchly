import { useState } from "react";
import { Link, useNavigate } from "react-router";

import { loginUser, registerUser } from "../services/api";

function SignupPage() {
  const [displayName, setDisplayName] = useState("");
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
      await registerUser(email, displayName, password);

      const loginData = await loginUser(email, password);

      localStorage.setItem(
        "matchly_access_token",
        loginData.accessToken,
      );

      localStorage.setItem(
        "matchly_user",
        JSON.stringify(loginData.user),
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
        <p className="badge">Join Matchly</p>

        <h1>Create your account</h1>

        <p className="page-description">
          Create an account to start using text and video chat.
        </p>

        <form className="login-form" onSubmit={handleSubmit}>
          <label htmlFor="display-name">
            Display name
          </label>

          <input
            id="display-name"
            type="text"
            placeholder="Your name"
            value={displayName}
            onChange={(event) =>
              setDisplayName(event.target.value)
            }
            minLength={2}
            maxLength={50}
            required
          />

          <label htmlFor="signup-email">Email</label>

          <input
            id="signup-email"
            type="email"
            placeholder="you@example.com"
            autoComplete="email"
            value={email}
            onChange={(event) =>
              setEmail(event.target.value)
            }
            required
          />

          <label htmlFor="signup-password">
            Password
          </label>

          <input
            id="signup-password"
            type="password"
            placeholder="At least 8 characters"
            autoComplete="new-password"
            value={password}
            onChange={(event) =>
              setPassword(event.target.value)
            }
            minLength={8}
            maxLength={64}
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
            {isLoading
              ? "Creating account..."
              : "Create Account"}
          </button>

          <p className="auth-switch">
            Already have an account?{" "}
            <Link to="/login">Log in</Link>
          </p>
        </form>
      </section>
    </main>
  );
}

export default SignupPage;