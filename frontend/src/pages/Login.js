import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const user = await login(username, password);
      navigate(user.role === "LOGISTICS_OFFICER" ? "/purchases" : "/dashboard");
    } catch (err) {
      setError(err?.response?.data?.message || "Invalid username or password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-brand">
          <span className="brand-mark large">&#9733;</span>
          <h1>Military Asset Management System</h1>
          <p>Secure, role-based logistics command &amp; control</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          <label>Username</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="e.g. admin"
            required
            autoFocus
          />

          <label>Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="********"
            required
          />

          {error && <div className="form-error">{error}</div>}

          <button className="btn btn-primary btn-block" type="submit" disabled={loading}>
            {loading ? "Signing in..." : "Sign In"}
          </button>
        </form>

        <div className="auth-hint">
          <strong>Demo credentials</strong>
          <ul>
            <li>Admin &mdash; admin / Admin@123</li>
            <li>Base Commander &mdash; commander.alpha / Commander@123</li>
            <li>Logistics Officer &mdash; logistics.alpha / Logistics@123</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
