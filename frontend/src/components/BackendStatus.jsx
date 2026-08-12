import { useEffect, useState } from "react";

function BackendStatus() {
  const [status, setStatus] = useState("checking");

  useEffect(() => {
    async function checkBackend() {
      try {
        const response = await fetch(
          "http://localhost:8080/api/health",
        );

        if (!response.ok) {
          throw new Error("Backend request failed");
        }

        const data = await response.json();

        if (data.status === "ok") {
          setStatus("online");
        } else {
          setStatus("offline");
        }
      } catch (error) {
        console.error("Backend check failed:", error);
        setStatus("offline");
      }
    }

    checkBackend();
  }, []);

  const statusText = {
    checking: "Checking backend",
    online: "Backend online",
    offline: "Backend offline",
  };

  return (
    <span className={`backend-status ${status}`}>
      <span className="backend-status-dot"></span>
      {statusText[status]}
    </span>
  );
}

export default BackendStatus;