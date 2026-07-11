import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Register from './Register';
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

describe('Register Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('should render register form inputs and submit button', () => {
    render(
      <MemoryRouter>
        <Register />
      </MemoryRouter>
    );

    expect(screen.getByLabelText(/full name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /register/i })).toBeInTheDocument();
  });

  test('should register successfully and redirect to login page', async () => {
    axios.post.mockResolvedValueOnce({ data: { message: 'User registered successfully' } });

    render(
      <MemoryRouter>
        <Register />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByLabelText(/full name/i), { target: { value: 'Jay Thummar' } });
    fireEvent.change(screen.getByLabelText(/email address/i), { target: { value: 'jay@gmail.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'Password@123' } });
    fireEvent.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith('http://localhost:8080/api/auth/register', {
        name: 'Jay Thummar',
        email: 'jay@gmail.com',
        password: 'Password@123',
      });
      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });
  });

  test('should display error message on registration failure', async () => {
    axios.post.mockRejectedValueOnce({
      response: { data: { error: 'Email already exists' } },
    });

    render(
      <MemoryRouter>
        <Register />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByLabelText(/full name/i), { target: { value: 'Jay Thummar' } });
    fireEvent.change(screen.getByLabelText(/email address/i), { target: { value: 'existing@gmail.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'Password@123' } });
    fireEvent.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(screen.getByText(/Email already exists/i)).toBeInTheDocument();
    });
  });
});
