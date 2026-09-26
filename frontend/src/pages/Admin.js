import React, { useCallback, useEffect, useState } from "react";
import api from "../api/axios";
import DataTable from "../components/DataTable";
import Modal from "../components/Modal";

const emptyUserForm = { username: "", password: "", fullName: "", role: "BASE_COMMANDER", baseId: "" };
const emptyBaseForm = { name: "", location: "" };
const emptyEquipForm = { name: "", category: "WEAPON", unit: "units" };

export default function Admin() {
  const [tab, setTab] = useState("users");

  const [users, setUsers] = useState([]);
  const [bases, setBases] = useState([]);
  const [equipmentTypes, setEquipmentTypes] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [error, setError] = useState("");

  const [showUserForm, setShowUserForm] = useState(false);
  const [userForm, setUserForm] = useState(emptyUserForm);
  const [showBaseForm, setShowBaseForm] = useState(false);
  const [baseForm, setBaseForm] = useState(emptyBaseForm);
  const [showEquipForm, setShowEquipForm] = useState(false);
  const [equipForm, setEquipForm] = useState(emptyEquipForm);
  const [formError, setFormError] = useState("");
  const [saving, setSaving] = useState(false);

  const loadAll = useCallback(async () => {
    setError("");
    try {
      const [usersRes, basesRes, typesRes, logsRes] = await Promise.all([
        api.get("/users"),
        api.get("/bases"),
        api.get("/equipment-types"),
        api.get("/audit-logs", { params: { limit: 100 } }),
      ]);
      setUsers(usersRes.data);
      setBases(basesRes.data);
      setEquipmentTypes(typesRes.data);
      setAuditLogs(logsRes.data);
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to load administration data");
    }
  }, []);

  useEffect(() => {
    loadAll();
  }, [loadAll]);

  const handleCreateUser = async (e) => {
    e.preventDefault();
    setFormError("");
    setSaving(true);
    try {
      await api.post("/users", {
        ...userForm,
        baseId: userForm.role === "ADMIN" ? null : Number(userForm.baseId),
      });
      setShowUserForm(false);
      setUserForm(emptyUserForm);
      loadAll();
    } catch (err) {
      setFormError(err?.response?.data?.message || "Failed to create user");
    } finally {
      setSaving(false);
    }
  };

  const toggleUserEnabled = async (user) => {
    try {
      await api.patch(`/users/${user.id}/enabled`, null, { params: { enabled: !user.enabled } });
      loadAll();
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to update user");
    }
  };

  const handleCreateBase = async (e) => {
    e.preventDefault();
    setFormError("");
    setSaving(true);
    try {
      await api.post("/bases", baseForm);
      setShowBaseForm(false);
      setBaseForm(emptyBaseForm);
      loadAll();
    } catch (err) {
      setFormError(err?.response?.data?.message || "Failed to create base");
    } finally {
      setSaving(false);
    }
  };

  const handleCreateEquip = async (e) => {
    e.preventDefault();
    setFormError("");
    setSaving(true);
    try {
      await api.post("/equipment-types", equipForm);
      setShowEquipForm(false);
      setEquipForm(emptyEquipForm);
      loadAll();
    } catch (err) {
      setFormError(err?.response?.data?.message || "Failed to create equipment type");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Administration</h2>
          <p className="page-subtitle">Manage users, bases, equipment catalog and audit trail</p>
        </div>
      </div>

      {error && <div className="form-error">{error}</div>}

      <div className="tabs">
        <button className={`tab ${tab === "users" ? "active" : ""}`} onClick={() => setTab("users")}>Users</button>
        <button className={`tab ${tab === "bases" ? "active" : ""}`} onClick={() => setTab("bases")}>Bases</button>
        <button className={`tab ${tab === "equipment" ? "active" : ""}`} onClick={() => setTab("equipment")}>Equipment Types</button>
        <button className={`tab ${tab === "audit" ? "active" : ""}`} onClick={() => setTab("audit")}>Audit Log</button>
      </div>

      {tab === "users" && (
        <>
          <div className="section-actions">
            <button className="btn btn-primary" onClick={() => { setFormError(""); setShowUserForm(true); }}>+ New User</button>
          </div>
          <DataTable
            columns={[
              { key: "username", header: "Username" },
              { key: "fullName", header: "Full Name" },
              { key: "role", header: "Role" },
              { key: "base", header: "Base", render: (r) => r.base?.name || "—" },
              { key: "enabled", header: "Status", render: (r) => (r.enabled ? "Active" : "Disabled") },
              {
                key: "actions", header: "", render: (r) => (
                  <button className="btn btn-ghost btn-sm" onClick={() => toggleUserEnabled(r)}>
                    {r.enabled ? "Disable" : "Enable"}
                  </button>
                )
              },
            ]}
            rows={users}
          />
        </>
      )}

      {tab === "bases" && (
        <>
          <div className="section-actions">
            <button className="btn btn-primary" onClick={() => { setFormError(""); setShowBaseForm(true); }}>+ New Base</button>
          </div>
          <DataTable
            columns={[
              { key: "name", header: "Name" },
              { key: "location", header: "Location" },
            ]}
            rows={bases}
          />
        </>
      )}

      {tab === "equipment" && (
        <>
          <div className="section-actions">
            <button className="btn btn-primary" onClick={() => { setFormError(""); setShowEquipForm(true); }}>+ New Equipment Type</button>
          </div>
          <DataTable
            columns={[
              { key: "name", header: "Name" },
              { key: "category", header: "Category" },
              { key: "unit", header: "Unit" },
            ]}
            rows={equipmentTypes}
          />
        </>
      )}

      {tab === "audit" && (
        <DataTable
          columns={[
            { key: "timestamp", header: "Timestamp", render: (r) => new Date(r.timestamp).toLocaleString() },
            { key: "username", header: "User" },
            { key: "userRole", header: "Role" },
            { key: "method", header: "Method" },
            { key: "endpoint", header: "Endpoint" },
            { key: "action", header: "Action" },
            { key: "statusCode", header: "Status" },
          ]}
          rows={auditLogs}
          emptyMessage="No audit events recorded yet."
        />
      )}

      {showUserForm && (
        <Modal title="Create User" onClose={() => setShowUserForm(false)}>
          <form onSubmit={handleCreateUser} className="modal-form">
            <label>Username</label>
            <input type="text" required value={userForm.username} onChange={(e) => setUserForm({ ...userForm, username: e.target.value })} />

            <label>Password</label>
            <input type="password" required value={userForm.password} onChange={(e) => setUserForm({ ...userForm, password: e.target.value })} />

            <label>Full Name</label>
            <input type="text" required value={userForm.fullName} onChange={(e) => setUserForm({ ...userForm, fullName: e.target.value })} />

            <label>Role</label>
            <select value={userForm.role} onChange={(e) => setUserForm({ ...userForm, role: e.target.value })}>
              <option value="ADMIN">Admin</option>
              <option value="BASE_COMMANDER">Base Commander</option>
              <option value="LOGISTICS_OFFICER">Logistics Officer</option>
            </select>

            {userForm.role !== "ADMIN" && (
              <>
                <label>Base</label>
                <select required value={userForm.baseId} onChange={(e) => setUserForm({ ...userForm, baseId: e.target.value })}>
                  <option value="">Select base</option>
                  {bases.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
                </select>
              </>
            )}

            {formError && <div className="form-error">{formError}</div>}

            <button className="btn btn-primary btn-block" type="submit" disabled={saving}>
              {saving ? "Saving..." : "Create User"}
            </button>
          </form>
        </Modal>
      )}

      {showBaseForm && (
        <Modal title="Create Base" onClose={() => setShowBaseForm(false)}>
          <form onSubmit={handleCreateBase} className="modal-form">
            <label>Name</label>
            <input type="text" required value={baseForm.name} onChange={(e) => setBaseForm({ ...baseForm, name: e.target.value })} />
            <label>Location</label>
            <input type="text" value={baseForm.location} onChange={(e) => setBaseForm({ ...baseForm, location: e.target.value })} />
            {formError && <div className="form-error">{formError}</div>}
            <button className="btn btn-primary btn-block" type="submit" disabled={saving}>
              {saving ? "Saving..." : "Create Base"}
            </button>
          </form>
        </Modal>
      )}

      {showEquipForm && (
        <Modal title="Create Equipment Type" onClose={() => setShowEquipForm(false)}>
          <form onSubmit={handleCreateEquip} className="modal-form">
            <label>Name</label>
            <input type="text" required value={equipForm.name} onChange={(e) => setEquipForm({ ...equipForm, name: e.target.value })} />
            <label>Category</label>
            <select value={equipForm.category} onChange={(e) => setEquipForm({ ...equipForm, category: e.target.value })}>
              <option value="WEAPON">Weapon</option>
              <option value="VEHICLE">Vehicle</option>
              <option value="AMMUNITION">Ammunition</option>
            </select>
            <label>Unit</label>
            <input type="text" value={equipForm.unit} onChange={(e) => setEquipForm({ ...equipForm, unit: e.target.value })} />
            {formError && <div className="form-error">{formError}</div>}
            <button className="btn btn-primary btn-block" type="submit" disabled={saving}>
              {saving ? "Saving..." : "Create Equipment Type"}
            </button>
          </form>
        </Modal>
      )}
    </div>
  );
}
