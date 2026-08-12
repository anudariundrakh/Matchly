import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";

import BackendStatus from "./BackendStatus";
import { getCurrentUser } from "../services/api";

function Navbar() {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    async function syncUser() {
      const accessToken = localStorage.getItem(
        "matchly_access_token",
      );

      if (!accessToken) {
        setUser(null);
        return;
      }

      try {
        const currentUser = await getCurrentUser();

        localStorage.setItem(
          "matchly_user",
          JSON.stringify(currentUser),
        );

        setUser(currentUser);
      } catch {
        localStorage.removeItem("matchly_access_token");
        localStorage.removeItem("matchly_user");
        setUser(null);
      }
    }

    syncUser();

    window.addEventListener(
      "matchly-auth-changed",
      syncUser,
    );

    return () => {
      window.removeEventListener(
        "matchly-auth-changed",
        syncUser,
      );
    };
  }, []);

  function handleLogout() {
    localStorage.removeItem("matchly_access_token");
    localStorage.removeItem("matchly_user");

    setUser(null);
    navigate("/");
  }

  return (
    <nav className="navbar">
      <Link className="logo" to="/">
        Matchly
      </Link>

      <div className="nav-links">
        <BackendStatus />

        <Link className="nav-button" to="/safety">
          Safety
        </Link>

        {user ? (
          <>
            <span className="nav-user">
              Hi, {user.displayName}
            </span>

            <button
              className="login-button logout-button"
              type="button"
              onClick={handleLogout}
            >
              Log Out
            </button>
          </>
        ) : (
          <Link className="login-button" to="/login">
            Log In
          </Link>
        )}
      </div>
    </nav>
  );
}

export default Navbar;