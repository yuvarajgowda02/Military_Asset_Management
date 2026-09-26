import React, { useCallback, useEffect, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import FilterBar from "../components/FilterBar";
import DataTable from "../components/DataTable";
import Modal from "../components/Modal";

const emptyFilters = { startDate: "", endDate: "", baseId: "", equipmentTypeId: "" };

const emptyForm = {
  fromBaseId: "",
  toBaseId: "",
  equipmentTypeId: "",
  quantity: "",
  transferDate: new Date().toISOString().slice(0, 10),
  remarks: "",
};

export default function Transfers() {
  const { user } = useAuth();
  const [bases, setBases] = useState([]);
  const [equipmentTypes, setEquipmentTypes] = useState([]);
  const [filters, setFilters] = useState(emptyFilters);
  const [transfers, setTransfers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [formError, setFormError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const loadLookups = async () => {
      const [basesRes, typesRes] = await Promise.all([
        api.get("/bases"),
        api.get("/equipment-types"),
      ]);
      setBases(basesRes.data);
      setEquipmentTypes(typesRes.data);
      if (user.role !== "ADMIN") {
        setForm((f) => ({ ...f, fromBaseId: user.baseId }));
      }
    };
    loadLookups();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadTransfers = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const params = {};
      if (filters.startDate) params.startDate = filters.startDate;
      if (filters.endDate) params.endDate = filters.endDate;
      if (filters.baseId) params.baseId = filters.baseId;
      if (filters.equipmentTypeId) params.equipmentTypeId = filters.equipmentTypeId;
      const { data } = await api.get("/transfers", { params });
      setTransfers(data.sort((a, b) => (a.transferDate < b.transferDate ? 1 : -1)));
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to load transfers");
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    loadTransfers();
  }, [loadTransfers]);

  const openForm = () => {
    setForm({ ...emptyForm, fromBaseId: user.role !== "ADMIN" ? user.baseId : "" });
    setFormError("");
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError("");
    if (form.fromBaseId === form.toBaseId) {
      setFormError("Source and destination base must be different");
      return;
    }
    setSaving(true);
    try {
      await api.post("/transfers", {
        fromBaseId: Number(form.fromBaseId),
        toBaseId: Number(form.toBaseId),
        equipmentTypeId: Number(form.equipmentTypeId),
        quantity: Number(form.quantity),
        transferDate: form.transferDate,
        remarks: form.remarks,
      });
      setShowForm(false);
      loadTransfers();
    } catch (err) {
      setFormError(err?.response?.data?.message || "Failed to record transfer");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Transfers</h2>
          <p className="page-subtitle">Move assets between bases with a full audit trail</p>
        </div>
        <button className="btn btn-primary" onClick={openForm}>+ New Transfer</button>
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

      {loading ? (
        <div className="empty-state">Loading transfers...</div>
      ) : (
        <DataTable
          columns={[
            { key: "transferDate", header: "Date" },
            { key: "fromBase", header: "From", render: (r) => r.fromBase?.name },
            { key: "toBase", header: "To", render: (r) => r.toBase?.name },
            { key: "equipmentType", header: "Equipment", render: (r) => r.equipmentType?.name },
            { key: "quantity", header: "Quantity", render: (r) => r.quantity.toLocaleString() },
            { key: "status", header: "Status" },
            { key: "remarks", header: "Remarks" },
          ]}
          rows={transfers}
          emptyMessage="No transfers match the selected filters."
        />
      )}

      {showForm && (
        <Modal title="New Transfer" onClose={() => setShowForm(false)}>
          <form onSubmit={handleSubmit} className="modal-form">
            <label>From Base</label>
            <select
              required
              value={form.fromBaseId}
              disabled={user.role !== "ADMIN"}
              onChange={(e) => setForm({ ...form, fromBaseId: e.target.value })}
            >
              <option value="">Select base</option>
              {bases.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
            </select>

            <label>To Base</label>
            <select required value={form.toBaseId} onChange={(e) => setForm({ ...form, toBaseId: e.target.value })}>
              <option value="">Select base</option>
              {bases.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
            </select>

            <label>Equipment Type</label>
            <select required value={form.equipmentTypeId} onChange={(e) => setForm({ ...form, equipmentTypeId: e.target.value })}>
              <option value="">Select equipment</option>
              {equipmentTypes.map((t) => <option key={t.id} value={t.id}>{t.name} ({t.category})</option>)}
            </select>

            <label>Quantity</label>
            <input type="number" min="1" required value={form.quantity} onChange={(e) => setForm({ ...form, quantity: e.target.value })} />

            <label>Transfer Date</label>
            <input type="date" required value={form.transferDate} onChange={(e) => setForm({ ...form, transferDate: e.target.value })} />

            <label>Remarks</label>
            <textarea rows="2" value={form.remarks} onChange={(e) => setForm({ ...form, remarks: e.target.value })} />

            {formError && <div className="form-error">{formError}</div>}

            <button className="btn btn-primary btn-block" type="submit" disabled={saving}>
              {saving ? "Saving..." : "Save Transfer"}
            </button>
          </form>
        </Modal>
      )}
    </div>
  );
}
