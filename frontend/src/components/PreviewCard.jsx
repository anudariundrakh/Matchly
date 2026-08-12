import { Link } from "react-router";
import {
  Video,
  MessageCircle,
} from "lucide-react";

function PreviewCard({ chatMode }) {
  const isVideoMode = chatMode === "video";

  return (
    <section className="preview-card">
      <div className="preview-header">
        <div>
          <p className="preview-label">Live preview</p>

          <h3>
            {isVideoMode
              ? "Ready for a video chat?"
              : "Ready for a text chat?"}
          </h3>
        </div>

        <span className="online-status">Online</span>
      </div>

      <div className="video-area">
        <div className="person-icon">
          {isVideoMode ? (
            <Video aria-hidden="true" />
          ) : (
            <MessageCircle aria-hidden="true" />
          )}
        </div>

        <p>
          {isVideoMode
            ? "Your next video conversation starts here"
            : "Your next text conversation starts here"}
        </p>
      </div>

      <div className="interest-box">
        <label htmlFor="interests">
          Your interests
        </label>

        <input
          id="interests"
          type="text"
          placeholder="Gaming, music, coding..."
        />
      </div>

      <Link
        className="match-button"
        to={
          isVideoMode
            ? "/video-chat"
            : "/text-chat"
        }
      >
        {isVideoMode
          ? "Find Video Match"
          : "Find Text Match"}
      </Link>
    </section>
  );
}

export default PreviewCard;