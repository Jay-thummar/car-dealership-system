import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Dashboard from './Dashboard';
import axios from 'axios';
import { vi } from 'vitest';

// Mock axios
vi.mock('axios');

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock JWT Tokens
const userToken = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGdtYWlsLmNvbSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdfQ.sig';
const adminToken = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJyb2xlcyI6WyJST0xFX0FETUlOIl19.sig';

const mockVehicles = [
  { id: '1', make: 'Toyota', model: 'Camry', category: 'Sedan', price: 30000.00, quantity: 5 },
  { id: '2', make: 'Honda', model: 'Civic', category: 'Sedan', price: 25000.00, quantity: 3 }
];

describe('Dashboard Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  test('should redirect to login if token is missing', () => {
    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  test('should render vehicle list for authenticated user', async () => {
    localStorage.setItem('token', userToken);
    axios.get.mockResolvedValueOnce({ data: mockVehicles });

    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    expect(screen.getByText(/loading vehicles/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/api/vehicles', {
        headers: { Authorization: `Bearer ${userToken}` }
      });
      expect(screen.getByText('Toyota Camry')).toBeInTheDocument();
      expect(screen.getByText('Honda Civic')).toBeInTheDocument();
    });
  });

  test('should perform search and display filtered list', async () => {
    localStorage.setItem('token', userToken);
    axios.get.mockResolvedValueOnce({ data: mockVehicles }); // Initial load
    axios.get.mockResolvedValueOnce({ data: [mockVehicles[0]] }); // Search load

    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Toyota Camry')).toBeInTheDocument();
    });

    // Enter search keyword
    fireEvent.change(screen.getByPlaceholderText(/search make, model/i), {
      target: { value: 'Toyota' }
    });

    fireEvent.click(screen.getByRole('button', { name: /search/i }));

    await waitFor(() => {
      expect(axios.get).toHaveBeenLastCalledWith('http://localhost:8080/api/vehicles/search', {
        headers: { Authorization: `Bearer ${userToken}` },
        params: { make: 'Toyota', model: '', category: '', minPrice: '', maxPrice: '' }
      });
      expect(screen.queryByText('Honda Civic')).not.toBeInTheDocument();
    });
  });

  test('should trigger purchase vehicle API successfully', async () => {
    localStorage.setItem('token', userToken);
    axios.get.mockResolvedValueOnce({ data: mockVehicles });
    axios.post.mockResolvedValueOnce({ 
      data: { ...mockVehicles[0], quantity: 4 } 
    });

    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('Toyota Camry')).toBeInTheDocument();
    });

    // Click Purchase button
    const purchaseButtons = screen.getAllByRole('button', { name: /purchase/i });
    fireEvent.click(purchaseButtons[0]);

    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith('http://localhost:8080/api/vehicles/1/purchase', null, {
        headers: { Authorization: `Bearer ${userToken}` }
      });
    });
  });

  test('should hide admin features for regular user role', async () => {
    localStorage.setItem('token', userToken);
    axios.get.mockResolvedValueOnce({ data: mockVehicles });

    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.queryByRole('button', { name: /add vehicle/i })).not.toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /restock/i })).not.toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /delete/i })).not.toBeInTheDocument();
    });
  });

  test('should render and handle admin features for admin role', async () => {
    localStorage.setItem('token', adminToken);
    axios.get.mockResolvedValueOnce({ data: mockVehicles });
    axios.delete.mockResolvedValueOnce({}); // Mock delete success

    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /add vehicle/i })).toBeInTheDocument();
      expect(screen.getAllByRole('button', { name: /restock/i })[0]).toBeInTheDocument();
      expect(screen.getAllByRole('button', { name: /delete/i })[0]).toBeInTheDocument();
    });

    // Simulate delete click
    const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
    fireEvent.click(deleteButtons[0]);

    await waitFor(() => {
      expect(axios.delete).toHaveBeenCalledWith('http://localhost:8080/api/vehicles/1', {
        headers: { Authorization: `Bearer ${adminToken}` }
      });
    });
  });
});
