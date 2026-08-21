import {
  useEffect,
  useRef,
  useState,
} from "react";

import { Link } from "react-router";

import { verifyEmail } from "../services/api";

function VerifyEmailPage() {
  const [status, setStatus] = useState("verifying");
  const [message, setMessage] = useState(
    "Verifying your email...",
  );

  const verificationStarted = useRef(false);

  useEffect(() => {
    if (verificationStarted.current) {
      return;
    }

    verificationStarted.current = true;

    async function handleVerification() {
      const searchParams =
        new URLSearchParams(window.location.search);

      const token = searchParams.get("token");

      if (!token) {
        setStatus("error");
        setMessage(
          "This verification link is missing a token.",
        );
        return;
      }

      try {
        const verifiedUser =
          await verifyEmail(token);

        setStatus("success");
        setMessage(
          "Your email has been verified successfully!",
        );

        const storedUser =
          localStorage.getItem(
            "matchly_user",
          );

        if (storedUser) {
          try {
            const parsedUser =
              JSON.parse(storedUser);

            if (
              parsedUser.email ===
              verifiedUser.email
            ) {
              localStorage.setItem(
                "matchly_user",
                JSON.stringify(
                  verifiedUser,
                ),
              );

              window.dispatchEvent(
                new Event(
                  "matchly-auth-change",
                ),
              );
            }
          } catch {
            // Ignore invalid local storage data.
          }
        }
      } catch (error) {
        setStatus("error");
        setMessage(
          error.message ||
            "Could not verify your email.",
        );
      }
    }

    handleVerification();
  }, []);

  return (
    <main
      style={{
        minHeight: "65vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: "40px 20px",
      }}
    >
     <div
  style={{
    width: "100%",
    maxWidth: "520px",
    textAlign: "center",
    padding: "40px",
    borderRadius: "20px",
    background: "white",
    color: "#111827",
    boxShadow:
      "0 12px 35px rgba(0, 0, 0, 0.08)",
  }}
>
        {status === "verifying" && (
          <>
            <h1>Verifying email</h1>

            <p>{message}</p>
          </>
        )}

        {status === "success" && (
          <>
            <h1>✅ Email verified!</h1>

            <p>{message}</p>

            <Link to="/">
              Go to Matchly
            </Link>
          </>
        )}

        {status === "error" && (
          <>
            <h1>Verification failed</h1>

            <p>{message}</p>

            <Link to="/login">
              Go to login
            </Link>
          </>
        )}
      </div>
    </main>
  );
}

export default VerifyEmailPage;