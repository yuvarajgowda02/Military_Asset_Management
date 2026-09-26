import React, { useCallback, useEffect, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import FilterBar from "../components/FilterBar";
import DataTable from "../components/DataTable";
import Modal from "../components/Modal";

const emptyFilters = { startDate: "", endDate: "", baseId: "", equipmentTypeId: "" };

const emptyAssignForm = {
  baseId: "",
  equipmentTypeId: "",
  personnelName: "",
  personnelServiceNumber: "",
  quantity: "",
  assignedDate: new Date().toISOString().slice(0, 10),
  remarks: "",
};

const emptyExpendForm = {
  baseId: "",
  equipmentTypeId: "",
  quantity: "",
  expendedDate: new Date().toISOString().slice(0, 10),
  reason: "",
};

export default function Assignments() {
  const { user } = useAuth();
  const [tab, setTab] = useState("assignments");
  const [bases, setBases] = useState([]);
  const [equipmentTypes, setEquipmentTypes] = useState([]);
  const [filters, setFilters] = useState(emptyFilters);

  const [assignments, setAssignments] = useState([]);
  const [expenditures, setExpenditures] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [showAssignForm, setShowAssignForm] = useState(false);
  const [assignForm, setAssignForm] = useState(emptyAssignForm);
  const [showExpendForm, setShowExpendForm] = useState(false);
  const [expendForm, setExpendForm] = useState(emptyExpendForm);
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
        setAssignForm((f) => ({ ...f, baseId: user.baseId }));
        setExpendForm((f) => ({ ...f, baseId: user.baseId }));
      }
    };
    loadLookups();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const buildParams = useCallback(() => {
    const params = {};
    if (filters.startDate) params.startDate = filters.startDate;
    if (filters.endDate) params.endDate = filters.endDate;
    if (filters.baseId) params.baseId = filters.baseId;
    if (filters.equipmentTypeId) params.equipmentTypeId = filters.equipmentTypeId;
    return params;
  }, [filters]);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const [assignRes, expendRes] = await Promise.all([
        api.get("/assignments", { params: buildParams() }),
        api.get("/expenditures", { params: buildParams() }),
      ]);
      setAssignments(assignRes.data.sort((a, b) => (a.assignedDate < b.assignedDate ? 1 : -1)));
      setExpenditures(expendRes.data.sort((a, b) => (a.expendedDate < b.expendedDate ? 1 : -1)));
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to load records");
    } finally {
      setLoading(false);
    }
  }, [buildParams]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleAssignSubmit = async (e) => {
    e.preventDefault();
    setFormError("");
    setSaving(true);
    try {
      await api.post("/assignments", {
        baseId: Number(assignForm.baseId),
        equipmentTypeId: Number(assignForm.equipmentTypeId),
        personnelName: assignForm.personnelName,
        personnelServiceNumber: assignForm.personnelServiceNumber,
        quantity: Number(assignForm.quantity),
        assignedDate: assignForm.assignedDate,
        remarks: assignForm.remarks,
      });
      setShowAssignForm(false);
      loadData();
    } catch (err) {
      setFormError(err?.response?.data?.message || "Failed to record assignment");
    } finally {
      setSaving(false);
    }
  };

  const handleExpendSubmit = async (e) => {
    e.preventDefault();
    setFormError("");
    setSaving(true);
    try {
      await api.post("/expenditures", {
        baseId: Number(expendForm.baseId),
        equipmentTypeId: Number(expendForm.equipmentTypeId),
        quantity: Number(expendForm.quantity),
        expendedDate: expendForm.expendedDate,
        reason: expendForm.reason,
      });
      setShowExpendForm(false);
      loadData();
    } catch (err) {
      setFormError(err?.response?.data?.message || "Failed to record expenditure");
    } finally {
      setSaving(false);
    }
  };

  const markReturned = async (id) => {
    try {
      await api.patch(`/assignments/${id}/return`);
      loadData();
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to update assignment");
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Assignments &amp; Expenditures</h2>
          <p className="page-subtitle">Assign assets to personnel and track expended stock</p>
        </div>
        <div className="header-actions">
          <button className="btn btn-secondary" onClick={() => { setFormError(""); setShowExpendForm(true); }}>+ Record Expenditure</button>
          <button className="btn btn-primary" onClick={() => { setFormError(""); setShowAssignForm(true); }}>+ New Assignment</button>
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

      <div className="tabs">
        <button className={`tab ${tab === "assignments" ? "active" : ""}`} onClick={() => setTab("assignments")}>
          Assignments ({assignments.length})
        </button>
        <button className={`tab ${tab === "expenditures" ? "active" : ""}`} onClick={() => setTab("expenditures")}>
          Expenditures ({expenditures.length})
        </button>
      </div>

      {error && <div className="form-error">{error}</div>}

      {loading ? (
        <div className="empty-state">Loading records...</div>
      ) : tab === "assignments" ? (
        <DataTable
          columns={[
            { key: "assignedDate", header: "Assigned On" },
            { key: "base", header: "Base", render: (r) => r.base?.name },
            { key: "equipmentType", header: "Equipment", render: (r) => r.equipmentType?.name },
            { key: "personnelName", header: "Personnel", render: (r) => `${r.personnelName}${r.personnelServiceNumber ? ` (${r.personnelServiceNumber})` : ""}` },
            { key: "quantity", header: "Qty" },
            { key: "status", header: "Status" },
            {
              key: "actions", header: "", render: (r) => (
                r.status === "ASSIGNED" ? (
                  <button className="btn btn-ghost btn-sm" onClick={() => markReturned(r.id)}>Mark Returned</button>
                ) : (
                  <span className="text-muted">Returned {r.returnedDate}</span>
                )
              )
            },
          ]}
          rows={assignments}
          emptyMessage="No assignments match the selected filters."
        />
      ) : (
        <DataTable
          columns={[
            { key: "expendedDate", header: "Date" },
            { key: "base", header: "Base", render: (r) => r.base?.name },
            { key: "equipmentType", header: "Equipment", render: (r) => r.equipmentType?.name },
            { key: "quantity", header: "Qty" },
            { key: "reason", header: "Reason" },
          ]}
          rows={expenditures}
          emptyMessage="No expenditures match the selected filters."
        />
      )}

      {showAssignForm && (
        <Modal title="New Assignment" onClose={() => setShowAssignForm(false)}>
          <form onSubmit={handleAssignSubmit} className="modal-form">
            {user.role === "ADMIN" && (
              <>
                <label>Base</label>
                <select required value={assignForm.baseId} onChange={(e) => setAssignForm({ ...assignForm, baseId: e.target.value })}>
                  <option value="">Select base</option>
                  {bases.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
                </select>
              </>
            )}

            <label>Equipment Type</label>
            <select required value={assignForm.equipmentTypeId} onChange={(e) => setAssignForm({ ...assignForm, equipmentTypeId: e.target.value })}>
              <option value="">Select equipment</option>
              {equipmentTypes.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
            </select>

            <label>Personnel Name</label>
            <input type="text" required value={assignForm.personnelName} onChange={(e) => setAssignForm({ ...assignForm, personnelName: e.target.value })} />

            <label>Service Number</label>
            <input type="text" value={assignForm.personnelServiceNumber} onChange={(e) => setAssignForm({ ...assignForm, personnelServiceNumber: e.target.value })} />

            <label>Quantity</label>
            <input type="number" min="1" required value={assignForm.quantity} onChange={(e) => setAssignForm({ ...assignForm, quantity: e.target.value })} />

            <label>Assigned Date</label>
            <input type="date" required value={assignForm.assignedDate} onChange={(e) => setAssignForm({ ...assignForm, assignedDate: e.target.value })} />

            <label>Remarks</label>
            <textarea rows="2" value={assignForm.remarks} onChange={(e) => setAssignForm({ ...assignForm, remarks: e.target.value })} />

            {formError && <div className="form-error">{formError}</div>}

            <button className="btn btn-primary btn-block" type="submit" disabled={saving}>
              {saving ? "Saving..." : "Save Assignment"}
            </button>
          </form>
        </Modal>
      )}

      {showExpendForm && (
        <Modal title="Record Expenditure" onClose={() => setShowExpendForm(false)}>
          <form onSubmit={handleExpendSubmit} className="modal-form">
            {user.role === "ADMIN" && (
              <>
                <label>Base</label>
                <select required value={expendForm.baseId} onChange={(e) => setExpendForm({ ...expendForm, baseId: e.target.value })}>
                  <option value="">Select base</option>
                  {bases.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
                </select>
              </>
            )}

            <label>Equipment Type</label>
            <select required value={expendForm.equipmentTypeId} onChange={(e) => setExpendForm({ ...expendForm, equipmentTypeId: e.target.value })}>
              <option value="">Select equipment</option>
              {equipmentTypes.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
            </select>

            <label>Quantity</label>
            <input type="number" min="1" required value={expendForm.quantity} onChange={(e) => setExpendForm({ ...expendForm, quantity: e.target.value })} />

            <label>Expended Date</label>
            <input type="date" required value={expendForm.expendedDate} onChange={(e) => setExpendForm({ ...expendForm, expendedDate: e.target.value })} />

            <label>Reason</label>
            <textarea rows="2" value={expendForm.reason} onChange={(e) => setExpendForm({ ...expendForm, reason: e.target.value })} />

            {formError && <div className="form-error">{formError}</div>}

            <button className="btn btn-primary btn-block" type="submit" disabled={saving}>
              {saving ? "Saving..." : "Save Expenditure"}
            </button>
          </form>
        </Modal>
      )}
    </div>
  );
}
