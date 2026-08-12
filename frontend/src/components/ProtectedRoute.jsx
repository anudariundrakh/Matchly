import { Navigate, useLocation } from "react-router";

function ProtectedRoute({ children }) {
  const location = useLocation();

  const accessToken = localStorage.getItem(
    "matchly_access_token",
  );

  if (!accessToken) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: location.pathname }}
      />
    );
  }

  return children;
}

export default ProtectedRoute;