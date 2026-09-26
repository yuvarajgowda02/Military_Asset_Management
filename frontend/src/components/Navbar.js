import React, { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const NAV_ITEMS = [
  { to: "/dashboard", label: "Dashboard", roles: ["ADMIN", "BASE_COMMANDER"] },
  { to: "/purchases", label: "Purchases", roles: ["ADMIN", "BASE_COMMANDER", "LOGISTICS_OFFICER"] },
  { to: "/transfers", label: "Transfers", roles: ["ADMIN", "BASE_COMMANDER", "LOGISTICS_OFFICER"] },
  { to: "/assignments", label: "Assignments & Expenditures", roles: ["ADMIN", "BASE_COMMANDER"] },
  { to: "/admin", label: "Administration", roles: ["ADMIN"] },
];

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  if (!user) return null;

  const visibleItems = NAV_ITEMS.filter((item) => item.roles.includes(user.role));

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <div className="navbar-brand">
          <span className="brand-mark">&#9733;</span>
          <span>MAMS</span>
        </div>

        <button className="navbar-toggle" onClick={() => setMenuOpen((o) => !o)} aria-label="Toggle menu">
          &#9776;
        </button>

        <nav className={`navbar-links ${menuOpen ? "open" : ""}`}>
          {visibleItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => "nav-link" + (isActive ? " active" : "")}
              onClick={() => setMenuOpen(false)}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="navbar-user">
          <div className="user-meta">
            <span className="user-name">{user.fullName}</span>
            <span className="user-role">{user.role.replace("_", " ")}{user.baseName ? ` · ${user.baseName}` : ""}</span>
          </div>
          <button className="btn btn-ghost" onClick={handleLogout}>Logout</button>
        </div>
      </div>
    </header>
  );
}
