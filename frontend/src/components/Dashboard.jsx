import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { API_BASE } from '../config';
import './Dashboard.css';

const decodeJwt = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
};

export default function Dashboard() {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isAdmin, setIsAdmin] = useState(false);
  const [userEmail, setUserEmail] = useState('');

  // Search States
  const [searchMake, setSearchMake] = useState('');
  const [searchModel, setSearchModel] = useState('');
  const [searchCategory, setSearchCategory] = useState('');
  const [searchMinPrice, setSearchMinPrice] = useState('');
  const [searchMaxPrice, setSearchMaxPrice] = useState('');

  // Modal / Form States
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingVehicle, setEditingVehicle] = useState(null); // holds vehicle if editing
  const [showRestockModal, setShowRestockModal] = useState(null); // holds vehicle ID if restocking
  const [restockAmount, setRestockAmount] = useState('5');

  // Form Fields
  const [make, setMake] = useState('');
  const [model, setModel] = useState('');
  const [category, setCategory] = useState('');
  const [price, setPrice] = useState('');
  const [quantity, setQuantity] = useState('');

  const navigate = useNavigate();
  const token = localStorage.getItem('token');

  useEffect(() => {
    if (!token) {
      navigate('/login');
      return;
    }

    const payload = decodeJwt(token);
    if (payload) {
      setUserEmail(payload.sub || '');
      const roles = payload.roles || [];
      setIsAdmin(roles.includes('ROLE_ADMIN'));
    }

    fetchVehicles();
  }, [token]);

  const getHeaders = () => ({
    headers: { Authorization: `Bearer ${token}` },
  });

  const fetchVehicles = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await axios.get(`${API_BASE}/vehicles`, getHeaders());
      setVehicles(res.data);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to fetch vehicles');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await axios.get(`${API_BASE}/vehicles/search`, {
        ...getHeaders(),
        params: {
          make: searchMake,
          model: searchModel,
          category: searchCategory,
          minPrice: searchMinPrice,
          maxPrice: searchMaxPrice,
        },
      });
      setVehicles(res.data);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to search vehicles');
    } finally {
      setLoading(false);
    }
  };

  const handleClearSearch = () => {
    setSearchMake('');
    setSearchModel('');
    setSearchCategory('');
    setSearchMinPrice('');
    setSearchMaxPrice('');
    fetchVehicles();
  };

  const handlePurchase = async (id) => {
    setError('');
    try {
      const res = await axios.post(`${API_BASE}/vehicles/${id}/purchase`, null, getHeaders());
      setVehicles(vehicles.map((v) => (v.id === id ? res.data : v)));
    } catch (err) {
      setError(err.response?.data?.error || 'Purchase failed');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this vehicle?')) return;
    setError('');
    try {
      await axios.delete(`${API_BASE}/vehicles/${id}`, getHeaders());
      setVehicles(vehicles.filter((v) => v.id !== id));
    } catch (err) {
      setError(err.response?.data?.error || 'Delete failed');
    }
  };

  const handleRestock = async (e) => {
    e.preventDefault();
    setError('');
    const id = showRestockModal;
    const qty = parseInt(restockAmount, 10);
    try {
      const res = await axios.post(
        `${API_BASE}/vehicles/${id}/restock`,
        null,
        {
          ...getHeaders(),
          params: { quantity: qty },
        }
      );
      setVehicles(vehicles.map((v) => (v.id === id ? res.data : v)));
      setShowRestockModal(null);
    } catch (err) {
      setError(err.response?.data?.error || 'Restock failed');
    }
  };

  const handleSaveVehicle = async (e) => {
    e.preventDefault();
    setError('');
    const vehicleData = {
      make,
      model,
      category,
      price: parseFloat(price),
      quantity: parseInt(quantity, 10),
    };

    try {
      let saved;
      if (editingVehicle) {
        const res = await axios.put(`${API_BASE}/vehicles/${editingVehicle.id}`, vehicleData, getHeaders());
        saved = res.data;
        setVehicles(vehicles.map((v) => (v.id === editingVehicle.id ? saved : v)));
      } else {
        const res = await axios.post(`${API_BASE}/vehicles`, vehicleData, getHeaders());
        saved = res.data;
        setVehicles([...vehicles, saved]);
      }
      closeAddModal();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to save vehicle');
    }
  };

  const openAddModal = (vehicle = null) => {
    if (vehicle) {
      setEditingVehicle(vehicle);
      setMake(vehicle.make);
      setModel(vehicle.model);
      setCategory(vehicle.category);
      setPrice(vehicle.price.toString());
      setQuantity(vehicle.quantity.toString());
    } else {
      setEditingVehicle(null);
      setMake('');
      setModel('');
      setCategory('');
      setPrice('');
      setQuantity('');
    }
    setShowAddModal(true);
  };

  const closeAddModal = () => {
    setShowAddModal(false);
    setEditingVehicle(null);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <div className="dashboard-container">
      {/* Navbar */}
      <nav className="navbar">
        <div className="nav-logo">
          <span className="logo-text">Auto<span className="accent">Sphere</span></span>
        </div>
        <div className="nav-actions">
          <span className="user-badge">{userEmail} ({isAdmin ? 'Admin' : 'Customer'})</span>
          <button className="btn-logout" onClick={handleLogout}>Logout</button>
        </div>
      </nav>

      <div className="dashboard-content">
        {/* Error Alert */}
        {error && (
          <div className="error-alert dashboard-error">
            <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
            <span>{error}</span>
          </div>
        )}

        {/* Search & Actions Bar */}
        <div className="actions-bar">
          <form onSubmit={handleSearch} className="search-form">
            <input
              type="text"
              placeholder="Search make, model..."
              value={searchMake}
              onChange={(e) => {
                setSearchMake(e.target.value);
              }}
              className="search-input"
            />
            <select
              value={searchCategory}
              onChange={(e) => setSearchCategory(e.target.value)}
              className="search-select"
            >
              <option value="">All Categories</option>
              <option value="Sedan">Sedan</option>
              <option value="SUV">SUV</option>
              <option value="Hatchback">Hatchback</option>
              <option value="Truck">Truck</option>
            </select>
            <input
              type="number"
              placeholder="Min Price"
              value={searchMinPrice}
              onChange={(e) => setSearchMinPrice(e.target.value)}
              className="price-input"
            />
            <input
              type="number"
              placeholder="Max Price"
              value={searchMaxPrice}
              onChange={(e) => setSearchMaxPrice(e.target.value)}
              className="price-input"
            />
            <button type="submit" className="btn-search">Search</button>
            <button type="button" className="btn-clear" onClick={handleClearSearch}>Clear</button>
          </form>

          {isAdmin && (
            <button className="btn-add-vehicle" onClick={() => openAddModal()}>
              + Add Vehicle
            </button>
          )}
        </div>

        {/* Vehicle Cards Grid */}
        {loading ? (
          <div className="loading-state">Loading vehicles...</div>
        ) : vehicles.length === 0 ? (
          <div className="empty-state">No vehicles available matching search bounds.</div>
        ) : (
          <div className="vehicle-grid">
            {vehicles.map((v) => (
              <div key={v.id} className="vehicle-card">
                <div className="card-badge">{v.category}</div>
                <div className="card-info">
                  <h3>{v.make} {v.model}</h3>
                  <div className="price-tag">${v.price.toLocaleString(undefined, { minimumFractionDigits: 2 })}</div>
                  <div className="stock-level">
                    Stock: <span className={v.quantity === 0 ? 'out-of-stock' : ''}>{v.quantity} units</span>
                  </div>
                </div>

                <div className="card-actions">
                  <button
                    className="btn-purchase"
                    onClick={() => handlePurchase(v.id)}
                    disabled={v.quantity === 0}
                  >
                    {v.quantity === 0 ? 'Out of Stock' : 'Purchase'}
                  </button>

                  {isAdmin && (
                    <div className="admin-actions">
                      <button className="btn-edit" onClick={() => openAddModal(v)}>Edit</button>
                      <button className="btn-restock" onClick={() => setShowRestockModal(v.id)}>Restock</button>
                      <button className="btn-delete" onClick={() => handleDelete(v.id)}>Delete</button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add / Edit Vehicle Modal */}
      {showAddModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2>{editingVehicle ? 'Edit Vehicle Details' : 'Add New Vehicle'}</h2>
            <form onSubmit={handleSaveVehicle} className="modal-form">
              <div className="form-group">
                <label>Make</label>
                <input type="text" required value={make} onChange={(e) => setMake(e.target.value)} placeholder="Toyota" />
              </div>
              <div className="form-group">
                <label>Model</label>
                <input type="text" required value={model} onChange={(e) => setModel(e.target.value)} placeholder="Camry" />
              </div>
              <div className="form-group">
                <label>Category</label>
                <input type="text" required value={category} onChange={(e) => setCategory(e.target.value)} placeholder="Sedan" />
              </div>
              <div className="form-group">
                <label>Price ($)</label>
                <input type="number" step="0.01" required value={price} onChange={(e) => setPrice(e.target.value)} placeholder="30000.00" />
              </div>
              <div className="form-group">
                <label>Initial Quantity</label>
                <input type="number" required value={quantity} onChange={(e) => setQuantity(e.target.value)} placeholder="5" />
              </div>

              <div className="modal-actions">
                <button type="submit" className="btn-save">Save Vehicle</button>
                <button type="button" className="btn-cancel" onClick={closeAddModal}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Restock Modal */}
      {showRestockModal && (
        <div className="modal-overlay">
          <div className="modal-content restock-modal">
            <h2>Restock Vehicle Inventory</h2>
            <form onSubmit={handleRestock} className="modal-form">
              <div className="form-group">
                <label>Amount to Restock</label>
                <input
                  type="number"
                  required
                  min="1"
                  value={restockAmount}
                  onChange={(e) => setRestockAmount(e.target.value)}
                />
              </div>

              <div className="modal-actions">
                <button type="submit" className="btn-save">Restock</button>
                <button type="button" className="btn-cancel" onClick={() => setShowRestockModal(null)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
