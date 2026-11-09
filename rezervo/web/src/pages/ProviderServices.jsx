import { useEffect, useState } from 'react';
import { api, formatPrice } from '../api';
import { useAuth } from '../auth';

const EMPTY = { name: '', description: '', durationMinutes: 30, price: '' };

export default function ProviderServices() {
  const { token } = useAuth();
  const [services, setServices] = useState([]);
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  function load() {
    api('/api/services/mine', { token }).then(setServices).catch((e) => setError(e.message));
  }
  useEffect(load, [token]);

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  async function submit(e) {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      await api('/api/services', {
        method: 'POST', token,
        body: { ...form, durationMinutes: Number(form.durationMinutes), price: Number(form.price) },
      });
      setForm(EMPTY);
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function remove(id) {
    if (!window.confirm('Ștergi serviciul? Programările existente rămân valabile.')) return;
    try {
      await api(`/api/services/${id}`, { method: 'DELETE', token });
      load();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <>
      <h1>Serviciile mele</h1>
      <p className="muted">Program de lucru: luni–sâmbătă, 09:00–17:00.</p>

      <form className="card form wide" onSubmit={submit}>
        <h2>Adaugă un serviciu</h2>
        <label>Nume<input required minLength={2} maxLength={100} value={form.name} onChange={set('name')} placeholder="ex: Tuns bărbați" /></label>
        <label>Descriere<textarea maxLength={1000} rows={3} value={form.description} onChange={set('description')} /></label>
        <div className="two">
          <label>Durată (minute)<input type="number" required min={10} max={480} step={5} value={form.durationMinutes} onChange={set('durationMinutes')} /></label>
          <label>Preț (lei)<input type="number" required min={0} max={100000} step="0.01" value={form.price} onChange={set('price')} /></label>
        </div>
        {error && <p className="error">{error}</p>}
        <button className="primary" disabled={busy}>{busy ? 'Se salvează…' : 'Adaugă serviciul'}</button>
      </form>

      <div className="list">
        {services.map((s) => (
          <div key={s.id} className="card row">
            <div>
              <strong>{s.name}</strong>
              <div className="muted">{s.durationMinutes} min · {formatPrice(s.price)}</div>
            </div>
            <button className="danger" onClick={() => remove(s.id)}>Șterge</button>
          </div>
        ))}
      </div>
    </>
  );
}
