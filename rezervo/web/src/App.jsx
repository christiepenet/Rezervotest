import { Link, NavLink, Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { useAuth } from './auth';
import Services from './pages/Services';
import ServiceDetail from './pages/ServiceDetail';
import Login from './pages/Login';
import Register from './pages/Register';
import Appointments from './pages/Appointments';
import ProviderServices from './pages/ProviderServices';

function RequireAuth({ children, role }) {
  const { user, loading } = useAuth();
  if (loading) return <p className="muted">Se încarcă…</p>;
  if (!user) return <Navigate to="/login" replace />;
  if (role && user.role !== role) return <Navigate to="/" replace />;
  return children;
}

export default function App() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <>
      <header className="topbar">
        <div className="container topbar-inner">
          <Link to="/" className="brand">Rezervo</Link>
          <nav>
            <NavLink to="/" end>Servicii</NavLink>
            {user && <NavLink to="/programari">Programări</NavLink>}
            {user?.role === 'PROVIDER' && <NavLink to="/serviciile-mele">Serviciile mele</NavLink>}
            {user ? (
              <button className="link" onClick={() => { logout(); navigate('/'); }}>
                Ieșire ({user.fullName.split(' ')[0]})
              </button>
            ) : (
              <>
                <NavLink to="/login">Intră în cont</NavLink>
                <NavLink to="/cont-nou" className="nav-cta">Cont nou</NavLink>
              </>
            )}
          </nav>
        </div>
      </header>

      <main className="container">
        <Routes>
          <Route path="/" element={<Services />} />
          <Route path="/servicii/:id" element={<ServiceDetail />} />
          <Route path="/login" element={<Login />} />
          <Route path="/cont-nou" element={<Register />} />
          <Route path="/programari" element={<RequireAuth><Appointments /></RequireAuth>} />
          <Route path="/serviciile-mele" element={<RequireAuth role="PROVIDER"><ProviderServices /></RequireAuth>} />
          <Route path="*" element={<p>Pagina nu există.</p>} />
        </Routes>
      </main>
    </>
  );
}
