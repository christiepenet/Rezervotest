import { useEffect, useState } from 'react';
import { api, formatDateTime } from '../api';
import { useAuth } from '../auth';

export default function Appointments() {
  const { token, user } = useAuth();
  const [items, setItems] = useState(null);
  const [error, setError] = useState('');

  function load() {
    api('/api/appointments/mine', { token }).then(setItems).catch((e) => setError(e.message));
  }
  useEffect(load, [token]);

  async function cancel(id) {
    if (!window.confirm('Sigur vrei să anulezi programarea?')) return;
    try {
      await api(`/api/appointments/${id}/cancel`, { method: 'POST', token });
      load();
    } catch (e) {
      setError(e.message);
    }
  }

  const isProvider = user.role === 'PROVIDER';
  const now = new Date();

  return (
    <>
      <h1>{isProvider ? 'Programările clienților' : 'Programările mele'}</h1>
      {error && <p className="error">{error}</p>}
      {items && items.length === 0 && <p className="muted">Nu ai nicio programare încă.</p>}
      <div className="list">
        {(items || []).map((a) => {
          const upcoming = new Date(a.startTime) > now;
          const cancelled = a.status === 'CANCELLED';
          return (
            <div key={a.id} className={`card row ${cancelled ? 'cancelled' : ''}`}>
              <div>
                <strong>{a.serviceName}</strong>
                <div className="muted">{isProvider ? `Client: ${a.clientName}` : a.providerName}</div>
                <div>{formatDateTime(a.startTime)}</div>
                {a.note && <div className="note">„{a.note}”</div>}
              </div>
              <div className="row-actions">
                {cancelled ? <span className="badge">Anulată</span>
                  : upcoming ? <button className="danger" onClick={() => cancel(a.id)}>Anulează</button>
                  : <span className="badge">Încheiată</span>}
              </div>
            </div>
          );
        })}
      </div>
    </>
  );
}
