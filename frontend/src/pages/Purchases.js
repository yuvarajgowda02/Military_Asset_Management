import React, { useCallback, useEffect, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import FilterBar from "../components/FilterBar";
import DataTable from "../components/DataTable";
import Modal from "../components/Modal";

const emptyFilters = { startDate: "", endDate: "", baseId: "", equipmentTypeId: "" };

const emptyForm = {
  baseId: "",
  equipmentTypeId: "",
  quantity: "",
  unitCost: "",
  purchaseDate: new Date().toISOString().slice(0, 10),
  remarks: "",
};

export default function Purchases() {
  const { user } = useAuth();
  const [bases, setBases] = useState([]);
  const [equipmentTypes, setEquipmentTypes] = useState([]);
  const [filters, setFilters] = useState(emptyFilters);
  const [purchases, setPurchases] = useState([]);
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
        setForm((f) => ({ ...f, baseId: user.baseId }));
      }
    };
    loadLookups();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadPurchases = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const params = {};
      if (filters.startDate) params.startDate = filters.startDate;
      if (filters.endDate) params.endDate = filters.endDate;
      if (filters.baseId) params.baseId = filters.baseId;
      if (filters.equipmentTypeId) params.equipmentTypeId = filters.equipmentTypeId;
      const { data } = await api.get("/purchases", { params });
      setPurchases(data.sort((a, b) => (a.purchaseDate < b.purchaseDate ? 1 : -1)));
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to load purchases");
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    loadPurchases();
  }, [loadPurchases]);

  const openForm = () => {
    setForm({ ...emptyForm, baseId: user.role !== "ADMIN" ? user.baseId : "" });
    setFormError("");
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError("");
    setSaving(true);
    try {
      await api.post("/purchases", {
        baseId: Number(form.baseId),
        equipmentTypeId: Number(form.equipmentTypeId),
        quantity: Number(form.quantity),
        unitCost: form.unitCost ? Number(form.unitCost) : null,
        purchaseDate: form.purchaseDate,
        remarks: form.remarks,
      });
      setShowForm(false);
      loadPurchases();
    } catch (err) {
      setFormError(err?.response?.data?.message || "Failed to record purchase");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Purchases</h2>
          <p className="page-subtitle">Record and review asset procurement</p>
        </div>
        <button className="btn btn-primary" onClick={openForm}>+ Record Purchase</button>
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
        <div className="empty-state">Loading purchases...</div>
      ) : (
        <DataTable
          columns={[
            { key: "purchaseDate", header: "Date" },
            { key: "base", header: "Base", render: (r) => r.base?.name },
            { key: "equipmentType", header: "Equipment", render: (r) => r.equipmentType?.name },
            { key: "quantity", header: "Quantity", render: (r) => r.quantity.toLocaleString() },
            { key: "totalCost", header: "Total Cost", render: (r) => (r.totalCost ? `$${Number(r.totalCost).toLocaleString()}` : "—") },
            { key: "remarks", header: "Remarks" },
            { key: "createdBy", header: "Recorded By", render: (r) => r.createdBy?.fullName || "—" },
          ]}
          rows={purchases}
          emptyMessage="No purchases match the selected filters."
        />
      )}

      {showForm && (
        <Modal title="Record Purchase" onClose={() => setShowForm(false)}>
          <form onSubmit={handleSubmit} className="modal-form">
            {user.role === "ADMIN" && (
              <>
                <label>Base</label>
                <select required value={form.baseId} onChange={(e) => setForm({ ...form, baseId: e.target.value })}>
                  <option value="">Select base</option>
                  {bases.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
                </select>
              </>
            )}

            <label>Equipment Type</label>
            <select required value={form.equipmentTypeId} onChange={(e) => setForm({ ...form, equipmentTypeId: e.target.value })}>
              <option value="">Select equipment</option>
              {equipmentTypes.map((t) => <option key={t.id} value={t.id}>{t.name} ({t.category})</option>)}
            </select>

            <label>Quantity</label>
            <input type="number" min="1" required value={form.quantity} onChange={(e) => setForm({ ...form, quantity: e.target.value })} />

            <label>Unit Cost (optional)</label>
            <input type="number" min="0" step="0.01" value={form.unitCost} onChange={(e) => setForm({ ...form, unitCost: e.target.value })} />

            <label>Purchase Date</label>
            <input type="date" required value={form.purchaseDate} onChange={(e) => setForm({ ...form, purchaseDate: e.target.value })} />

            <label>Remarks</label>
            <textarea rows="2" value={form.remarks} onChange={(e) => setForm({ ...form, remarks: e.target.value })} />

            {formError && <div className="form-error">{formError}</div>}

            <button className="btn btn-primary btn-block" type="submit" disabled={saving}>
              {saving ? "Saving..." : "Save Purchase"}
            </button>
          </form>
        </Modal>
      )}
    </div>
  );
}
