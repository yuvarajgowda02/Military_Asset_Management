import React from "react";

/**
 * Shared filter bar used across Dashboard, Purchases, Transfers and
 * Assignments & Expenditures pages: Date range + Base + Equipment Type.
 */
export default function FilterBar({
  bases,
  equipmentTypes,
  filters,
  onChange,
  onReset,
  showBaseFilter = true,
}) {
  const update = (field, value) => onChange({ ...filters, [field]: value });

  return (
    <div className="filter-bar">
      <div className="filter-field">
        <label>Start Date</label>
        <input
          type="date"
          value={filters.startDate || ""}
          onChange={(e) => update("startDate", e.target.value)}
        />
      </div>
      <div className="filter-field">
        <label>End Date</label>
        <input
          type="date"
          value={filters.endDate || ""}
          onChange={(e) => update("endDate", e.target.value)}
        />
      </div>
      {showBaseFilter && (
        <div className="filter-field">
          <label>Base</label>
          <select value={filters.baseId || ""} onChange={(e) => update("baseId", e.target.value)}>
            <option value="">All Bases</option>
            {bases.map((b) => (
              <option key={b.id} value={b.id}>{b.name}</option>
            ))}
          </select>
        </div>
      )}
      <div className="filter-field">
        <label>Equipment Type</label>
        <select value={filters.equipmentTypeId || ""} onChange={(e) => update("equipmentTypeId", e.target.value)}>
          <option value="">All Types</option>
          {equipmentTypes.map((t) => (
            <option key={t.id} value={t.id}>{t.name}</option>
          ))}
        </select>
      </div>
      <button className="btn btn-ghost" onClick={onReset}>Clear Filters</button>
    </div>
  );
}
