import { useEffect, useRef, useState } from "react";

import {
  Camera,
  User,
  LoaderCircle,
} from "lucide-react";

function VideoChatPage() {
  const videoRef = useRef(null);
  const streamRef = useRef(null);

  const [cameraStatus, setCameraStatus] = useState("off");
  const [isMatching, setIsMatching] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  async function startCamera() {
    setErrorMessage("");
    setCameraStatus("starting");

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true,
      });

      streamRef.current = stream;

      if (videoRef.current) {
        videoRef.current.srcObject = stream;
      }

      setCameraStatus("on");
    } catch (error) {
      console.error("Camera error:", error);

      setCameraStatus("off");
      setErrorMessage(
        "Camera or microphone access was blocked. Allow permission and try again.",
      );
    }
  }

  function stopCamera() {
    if (streamRef.current) {
      streamRef.current
        .getTracks()
        .forEach((track) => track.stop());

      streamRef.current = null;
    }

    if (videoRef.current) {
      videoRef.current.srcObject = null;
    }

    setCameraStatus("off");
    setIsMatching(false);
  }

  function startMatching() {
    setIsMatching(true);
  }

  useEffect(() => {
    return () => {
      if (streamRef.current) {
        streamRef.current
          .getTracks()
          .forEach((track) => track.stop());
      }
    };
  }, []);

  const cameraIsOn = cameraStatus === "on";

  return (
    <main className="page-shell">
      <section className="page-card wide-card">
        <div className="video-page-header">
          <div>
            <p className="badge">Video chat</p>
            <h1>Find a video-chat match</h1>
          </div>

          <span
            className={
              cameraIsOn
                ? "chat-status connected"
                : "chat-status disconnected"
            }
          >
            {cameraIsOn ? "Camera on" : "Camera off"}
          </span>
        </div>

        <p className="page-description">
          Turn on your camera, check your preview, and then begin matching.
        </p>

        {errorMessage && (
          <p className="camera-error">{errorMessage}</p>
        )}

        <div className="video-grid">
          <div className="video-placeholder local-camera">
            <video
              ref={videoRef}
              autoPlay
              muted
              playsInline
              className={cameraIsOn ? "local-video" : "hidden-video"}
            />

            {!cameraIsOn && (
              <div className="video-message">
                <Camera
                  className="large-interface-icon"
                  aria-hidden="true"
                />

                <p>
                  {cameraStatus === "starting"
                    ? "Starting camera..."
                    : "Your camera is off"}
                </p>
              </div>
            )}

            {cameraIsOn && (
              <span className="video-label">You</span>
            )}
          </div>

          <div className="video-placeholder">
            <div className="video-message">
              {isMatching ? (
                  <LoaderCircle
                    className="large-interface-icon spinning-icon"
                    aria-hidden="true"
                  />
                ) : (
                  <User
                    className="large-interface-icon"
                    aria-hidden="true"
                  />
                )}

              <p>
                {isMatching
                  ? "Searching for someone..."
                  : "Waiting for a match"}
              </p>
            </div>
          </div>
        </div>

        <div className="video-controls">
          {!cameraIsOn ? (
            <button
              className="primary-button"
              onClick={startCamera}
              disabled={cameraStatus === "starting"}
            >
              {cameraStatus === "starting"
                ? "Starting..."
                : "Turn On Camera"}
            </button>
          ) : (
            <>
              <button
                className="primary-button"
                onClick={startMatching}
                disabled={isMatching}
              >
                {isMatching
                  ? "Searching..."
                  : "Start Matching"}
              </button>

              <button
                className="secondary-button"
                onClick={stopCamera}
              >
                Turn Off Camera
              </button>
            </>
          )}
        </div>
      </section>
    </main>
  );
}

export default VideoChatPage;