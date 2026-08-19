import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";

import {
  joinMatchmaking,
  getMatchmakingStatus,
  leaveMatchmaking,
} from "../services/api";

const WEBSOCKET_URL =
  import.meta.env.VITE_WEBSOCKET_URL ??
  "ws://localhost:8080/ws";

function getStoredUser() {
  const storedUser = localStorage.getItem("matchly_user");

  if (!storedUser) {
    return null;
  }

  try {
    return JSON.parse(storedUser);
  } catch {
    return null;
  }
}

function TextChatPage() {
  const [matchStatus, setMatchStatus] = useState("idle");
  const [isConnected, setIsConnected] = useState(false);
  const [roomId, setRoomId] = useState(null);

  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState([]);
  const [errorMessage, setErrorMessage] = useState("");

  const clientRef = useRef(null);
  const pollingRef = useRef(null);

  const currentUser = getStoredUser();
  const displayName = currentUser?.displayName ?? "Guest";

  useEffect(() => {
    return () => {
      stopPolling();

      if (clientRef.current) {
        void clientRef.current.deactivate();
      }
    };
  }, []);

  function stopPolling() {
    if (pollingRef.current) {
      clearInterval(pollingRef.current);
      pollingRef.current = null;
    }
  }

  function connectToRoom(newRoomId) {
    if (clientRef.current?.active) {
      return;
    }

    const token = localStorage.getItem(
      "matchly_access_token",
    );

    if (!token) {
      setErrorMessage(
        "You must be logged in to connect to chat.",
      );
      return;
    }

    const client = new Client({
      brokerURL: WEBSOCKET_URL,

      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },

      reconnectDelay: 5000,

      onConnect: () => {
        setIsConnected(true);
        setMatchStatus("matched");

        client.subscribe(
          `/topic/chat/${newRoomId}`,
          (messageFrame) => {
            const receivedMessage = JSON.parse(
              messageFrame.body,
            );

            const newMessage = {
              id: crypto.randomUUID(),
              sender: receivedMessage.sender,
              text: receivedMessage.content,
              sentAt: receivedMessage.sentAt,
              isOwn:
                receivedMessage.sender === displayName,
            };

            setMessages((currentMessages) => [
              ...currentMessages,
              newMessage,
            ]);
          },
        );
      },

      onWebSocketClose: () => {
        setIsConnected(false);
      },

      onWebSocketError: (error) => {
        console.error(
          "WebSocket error:",
          error,
        );

        setErrorMessage(
          "Could not connect to the chat server.",
        );
      },

      onStompError: (frame) => {
        console.error(
          "STOMP error:",
          frame.headers.message,
        );

        setErrorMessage(
          "A real-time chat error occurred.",
        );
      },
    });

    clientRef.current = client;
    client.activate();
  }

  async function checkForMatch() {
    try {
      const result = await getMatchmakingStatus();

      if (result.status === "MATCHED") {
        stopPolling();

        setRoomId(result.roomId);
        setMatchStatus("matched");

        connectToRoom(result.roomId);
      }
    } catch (error) {
      stopPolling();
      setMatchStatus("idle");
      setErrorMessage(error.message);
    }
  }

  async function handleStartMatching() {
    setErrorMessage("");
    setMessages([]);
    setMatchStatus("searching");

    try {
      const result = await joinMatchmaking();

      if (result.status === "MATCHED") {
        setRoomId(result.roomId);
        setMatchStatus("matched");

        connectToRoom(result.roomId);

        return;
      }

      setMatchStatus("waiting");

      pollingRef.current = setInterval(
        checkForMatch,
        1500,
      );
    } catch (error) {
      setMatchStatus("idle");
      setErrorMessage(error.message);
    }
  }

  async function handleEndChat() {
    stopPolling();

    if (clientRef.current) {
      await clientRef.current.deactivate();
      clientRef.current = null;
    }

    try {
      await leaveMatchmaking();
    } catch (error) {
      console.error(
        "Could not leave matchmaking:",
        error,
      );
    }

    setIsConnected(false);
    setMatchStatus("idle");
    setRoomId(null);
    setMessage("");
    setMessages([]);
  }

  function handleSendMessage(event) {
    event.preventDefault();

    const cleanedMessage = message.trim();
    const client = clientRef.current;

    if (
      !isConnected ||
      !client?.connected ||
      !roomId ||
      cleanedMessage === ""
    ) {
      return;
    }

    client.publish({
      destination: "/app/chat.send",

      body: JSON.stringify({
        roomId,
        sender: displayName,
        content: cleanedMessage,
      }),
    });

    setMessage("");
  }

  function getStatusText() {
    if (matchStatus === "searching") {
      return "Searching...";
    }

    if (matchStatus === "waiting") {
      return "Waiting for someone...";
    }

    if (isConnected) {
      return "Matched";
    }

    return "Not connected";
  }

  return (
    <main className="page-shell">
      <section className="page-card">
        <div className="chat-page-header">
          <div>
            <p className="badge">Text chat</p>
            <h1>Find a text-chat match</h1>
          </div>

          <span
            className={
              isConnected
                ? "chat-status connected"
                : "chat-status disconnected"
            }
          >
            {getStatusText()}
          </span>
        </div>

        <p className="page-description">
          Match with another person and chat privately
          in real time.
        </p>

        {errorMessage && (
          <p
            className="auth-error"
            role="alert"
          >
            {errorMessage}
          </p>
        )}

        <div className="chat-window">
          {messages.length === 0 ? (
            <div className="empty-chat">
              <p>
                {isConnected
                  ? "You found a match!"
                  : matchStatus === "waiting"
                    ? "Looking for another person..."
                    : "No conversation yet."}
              </p>

              <span>
                {isConnected
                  ? "Send a message to begin."
                  : matchStatus === "waiting"
                    ? "Keep this page open while we search."
                    : "Start matching to find someone."}
              </span>
            </div>
          ) : (
            <div className="message-list">
              {messages.map((chatMessage) => (
                <div
                  className={
                    chatMessage.isOwn
                      ? "message-bubble own-message"
                      : "message-bubble other-message"
                  }
                  key={chatMessage.id}
                >
                  <span className="message-sender">
                    {chatMessage.isOwn
                      ? "You"
                      : chatMessage.sender}
                  </span>

                  <p>{chatMessage.text}</p>
                </div>
              ))}
            </div>
          )}
        </div>

        <form
          className="message-row"
          onSubmit={handleSendMessage}
        >
          <input
            type="text"
            placeholder={
              isConnected
                ? "Type a message..."
                : "Find a match first..."
            }
            value={message}
            onChange={(event) =>
              setMessage(event.target.value)
            }
            disabled={!isConnected}
          />

          <button
            type="submit"
            disabled={
              !isConnected ||
              message.trim() === ""
            }
          >
            Send
          </button>
        </form>

        <div className="chat-actions">
          {matchStatus !== "idle" ? (
            <button
              className="secondary-button"
              type="button"
              onClick={handleEndChat}
            >
              {isConnected
                ? "End Chat"
                : "Cancel"}
            </button>
          ) : (
            <button
              className="primary-button"
              type="button"
              onClick={handleStartMatching}
            >
              Start Matching
            </button>
          )}
        </div>
      </section>
    </main>
  );
}

export default TextChatPage;