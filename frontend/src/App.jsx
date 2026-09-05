import {
  Routes,
  Route,
  useLocation,
} from "react-router";

import "./App.css";

import Navbar from "./components/Navbar";
import Footer from "./components/Footer";
import ProtectedRoute from "./components/ProtectedRoute";

import HomePage from "./pages/HomePage";
import TextChatPage from "./pages/TextChatPage";
import VideoChatPage from "./pages/VideoChatPage";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import SafetyPage from "./pages/SafetyPage";
import VerifyEmailPage from "./pages/VerifyEmailPage";

function App() {
  const location = useLocation();

  return (
    <div className="app">
      <Navbar />

      <div
        className="route-transition"
        key={location.pathname}
      >
        <Routes location={location}>
          <Route
            path="/"
            element={<HomePage />}
          />

          <Route
            path="/text-chat"
            element={
              <ProtectedRoute>
                <TextChatPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/video-chat"
            element={
              <ProtectedRoute>
                <VideoChatPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/login"
            element={<LoginPage />}
          />

          <Route
            path="/signup"
            element={<SignupPage />}
          />

          <Route
            path="/safety"
            element={<SafetyPage />}
          />

          <Route
            path="/verify-email"
            element={<VerifyEmailPage />}
          />
        </Routes>
      </div>

      <Footer />
    </div>
  );
}

export default App;