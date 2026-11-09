import { createContext, useContext, useEffect, useState } from 'react';
import { api } from './api';

const AuthContext = createContext(null);
const STORAGE_KEY = 'rezervo_token';

function readToken() {
  try { return localStorage.getItem(STORAGE_KEY); } catch { return null; }
}
function writeToken(token) {
  try {
    if (token) localStorage.setItem(STORAGE_KEY, token);
    else localStorage.removeItem(STORAGE_KEY);
  } catch { /* stocarea poate fi blocata in modul privat */ }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(readToken);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(Boolean(readToken()));

  // La pornire, verificam daca token-ul salvat mai e valid.
  useEffect(() => {
    if (!token) { setLoading(false); return; }
    api('/api/auth/me', { token })
      .then(setUser)
      .catch(() => { writeToken(null); setToken(null); })
      .finally(() => setLoading(false));
  }, [token]);

  function saveSession({ token: newToken, user: newUser }) {
    writeToken(newToken);
    setToken(newToken);
    setUser(newUser);
  }

  const value = {
    token,
    user,
    loading,
    async login(email, password) {
      saveSession(await api('/api/auth/login', { method: 'POST', body: { email, password } }));
    },
    async register(form) {
      saveSession(await api('/api/auth/register', { method: 'POST', body: form }));
    },
    logout() {
      writeToken(null);
      setToken(null);
      setUser(null);
    },
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
