import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullName: '', email: '', password: '', role: 'CLIENT' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  async function submit(e) {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      await register(form);
      navigate(form.role === 'PROVIDER' ? '/serviciile-mele' : '/');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className="card form" onSubmit={submit}>
      <h1>Cont nou</h1>

      <div className="role-picker">
        <button type="button" className={form.role === 'CLIENT' ? 'active' : ''} onClick={() => setForm({ ...form, role: 'CLIENT' })}>
          <strong>Client</strong><span>Vreau să fac programări</span>
        </button>
        <button type="button" className={form.role === 'PROVIDER' ? 'active' : ''} onClick={() => setForm({ ...form, role: 'PROVIDER' })}>
          <strong>Furnizor</strong><span>Ofer servicii</span>
        </button>
      </div>

      <label>{form.role === 'PROVIDER' ? 'Numele afacerii' : 'Nume complet'}
        <input required minLength={2} maxLength={100} autoComplete="name" value={form.fullName} onChange={set('fullName')} />
      </label>
      <label>Email<input type="email" required autoComplete="email" value={form.email} onChange={set('email')} /></label>
      <label>Parolă
        <input type="password" required minLength={8} maxLength={72} autoComplete="new-password" value={form.password} onChange={set('password')} />
        <small className="muted">Minimum 8 caractere, cel puțin o literă și o cifră.</small>
      </label>
      {error && <p className="error">{error}</p>}
      <button className="primary" disabled={busy}>{busy ? 'Se creează…' : 'Creează contul'}</button>
      <p className="muted">Ai deja cont? <Link to="/login">Intră</Link></p>
    </form>
  );
}
