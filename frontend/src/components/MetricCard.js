import React from "react";

export default function MetricCard({ label, value, tone = "default", onClick, hint }) {
  return (
    <div
      className={`metric-card tone-${tone} ${onClick ? "clickable" : ""}`}
      onClick={onClick}
      role={onClick ? "button" : undefined}
      tabIndex={onClick ? 0 : undefined}
    >
      <div className="metric-label">{label}</div>
      <div className="metric-value">{value}</div>
      {hint && <div className="metric-hint">{hint}</div>}
    </div>
  );
}
