import React, { useCallback, useEffect, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import FilterBar from "../components/FilterBar";
import MetricCard from "../components/MetricCard";
import Modal from "../components/Modal";
import DataTable from "../components/DataTable";

const emptyFilters = { startDate: "", endDate: "", baseId: "", equipmentTypeId: "" };

export default function Dashboard() {
  const { user } = useAuth();
  const [bases, setBases] = useState([]);
  const [equipmentTypes, setEquipmentTypes] = useState([]);
  const [filters, setFilters] = useState(emptyFilters);
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showMovement, setShowMovement] = useState(false);
  const [movementDetails, setMovementDetails] = useState(null);
  const [movementLoading, setMovementLoading] = useState(false);

  useEffect(() => {
    const loadLookups = async () => {
      const [basesRes, typesRes] = await Promise.all([
        api.get("/bases"),
        api.get("/equipment-types"),
      ]);
      setBases(basesRes.data);
      setEquipmentTypes(typesRes.data);
    };
    loadLookups();
  }, []);

  const buildParams = useCallback(() => {
    const params = {};
    if (filters.startDate) params.startDate = filters.startDate;
    if (filters.endDate) params.endDate = filters.endDate;
    if (filters.baseId) params.baseId = filters.baseId;
    if (filters.equipmentTypeId) params.equipmentTypeId = filters.equipmentTypeId;
    return params;
  }, [filters]);

  const loadMetrics = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await api.get("/dashboard/metrics", { params: buildParams() });
      setMetrics(data);
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to load dashboard metrics");
    } finally {
      setLoading(false);
    }
  }, [buildParams]);

  useEffect(() => {
    loadMetrics();
  }, [loadMetrics]);

  const openMovementDetails = async () => {
    setShowMovement(true);
    setMovementLoading(true);
    try {
      const { data } = await api.get("/dashboard/movement-details", { params: buildParams() });
      setMovementDetails(data);
    } catch (err) {
      setMovementDetails(null);
    } finally {
      setMovementLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Dashboard</h2>
          <p className="page-subtitle">
            {user.role === "ADMIN" ? "All bases" : user.baseName} &middot; Asset readiness overview
          </p>
        </div>
      </div>

      <FilterBar
        bases={bases}
        equipmentTypes={equipmentTypes}
        filters={filters}
        onChange={setFilters}
        onReset={() => setFilters(emptyFilters)}
        showBaseFilter={user.role === "ADMIN"}
      />

      {error && <div className="form-error">{error}</div>}

      {loading || !metrics ? (
        <div className="empty-state">Loading metrics...</div>
      ) : (
        <>
          <div className="metric-grid">
            <MetricCard label="Opening Balance" value={metrics.openingBalance.toLocaleString()} tone="neutral" />
            <MetricCard label="Closing Balance" value={metrics.closingBalance.toLocaleString()} tone="primary" />
            <MetricCard
              label="Net Movement"
              value={metrics.netMovement.toLocaleString()}
              tone="info"
              onClick={openMovementDetails}
              hint="Click for Purchases / Transfer In / Transfer Out breakdown"
            />
            <MetricCard label="Assigned" value={metrics.assigned.toLocaleString()} tone="warning" />
            <MetricCard label="Expended" value={metrics.expended.toLocaleString()} tone="danger" />
          </div>

          <div className="metric-grid secondary">
            <MetricCard label="Purchases" value={metrics.purchases.toLocaleString()} tone="neutral" />
            <MetricCard label="Transfer In" value={metrics.transferIn.toLocaleString()} tone="neutral" />
            <MetricCard label="Transfer Out" value={metrics.transferOut.toLocaleString()} tone="neutral" />
          </div>
        </>
      )}

      {showMovement && (
        <Modal title="Net Movement Breakdown" onClose={() => setShowMovement(false)} wide>
          {movementLoading || !movementDetails ? (
            <div className="empty-state">Loading details...</div>
          ) : (
            <div className="movement-details">
              <section>
                <h4>Purchases ({movementDetails.purchases.length})</h4>
                <DataTable
                  columns={[
                    { key: "purchaseDate", header: "Date" },
                    { key: "baseName", header: "Base" },
                    { key: "equipmentType", header: "Equipment" },
                    { key: "quantity", header: "Qty" },
                  ]}
                  rows={movementDetails.purchases}
                  emptyMessage="No purchases in this period."
                />
              </section>
              <section>
                <h4>Transfers In ({movementDetails.transfersIn.length})</h4>
                <DataTable
                  columns={[
                    { key: "transferDate", header: "Date" },
                    { key: "fromBase", header: "From" },
                    { key: "toBase", header: "To" },
                    { key: "equipmentType", header: "Equipment" },
                    { key: "quantity", header: "Qty" },
                  ]}
                  rows={movementDetails.transfersIn}
                  emptyMessage="No incoming transfers in this period."
                />
              </section>
              <section>
                <h4>Transfers Out ({movementDetails.transfersOut.length})</h4>
                <DataTable
                  columns={[
                    { key: "transferDate", header: "Date" },
                    { key: "fromBase", header: "From" },
                    { key: "toBase", header: "To" },
                    { key: "equipmentType", header: "Equipment" },
                    { key: "quantity", header: "Qty" },
                  ]}
                  rows={movementDetails.transfersOut}
                  emptyMessage="No outgoing transfers in this period."
                />
              </section>
            </div>
          )}
        </Modal>
      )}
    </div>
  );
}
