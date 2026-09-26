import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import PrivateRoute from "./components/PrivateRoute";
import Navbar from "./components/Navbar";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Purchases from "./pages/Purchases";
import Transfers from "./pages/Transfers";
import Assignments from "./pages/Assignments";
import Admin from "./pages/Admin";

function Layout({ children }) {
  return (
    <div className="app-shell">
      <Navbar />
      <main className="app-content">{children}</main>
    </div>
  );
}

function RootRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={user.role === "LOGISTICS_OFFICER" ? "/purchases" : "/dashboard"} replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />

          <Route
            path="/dashboard"
            element={
              <PrivateRoute roles={["ADMIN", "BASE_COMMANDER"]}>
                <Layout><Dashboard /></Layout>
              </PrivateRoute>
            }
          />
          <Route
            path="/purchases"
            element={
              <PrivateRoute roles={["ADMIN", "BASE_COMMANDER", "LOGISTICS_OFFICER"]}>
                <Layout><Purchases /></Layout>
              </PrivateRoute>
            }
          />
          <Route
            path="/transfers"
            element={
              <PrivateRoute roles={["ADMIN", "BASE_COMMANDER", "LOGISTICS_OFFICER"]}>
                <Layout><Transfers /></Layout>
              </PrivateRoute>
            }
          />
          <Route
            path="/assignments"
            element={
              <PrivateRoute roles={["ADMIN", "BASE_COMMANDER"]}>
                <Layout><Assignments /></Layout>
              </PrivateRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <PrivateRoute roles={["ADMIN"]}>
                <Layout><Admin /></Layout>
              </PrivateRoute>
            }
          />

          <Route path="/" element={<RootRedirect />} />
          <Route path="*" element={<RootRedirect />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
