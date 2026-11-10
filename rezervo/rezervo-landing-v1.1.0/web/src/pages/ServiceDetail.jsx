import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, formatDateTime, formatPrice } from '../api';
import { useAuth } from '../auth';

function todayIso() {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 10);
}

export default function ServiceDetail() {
  const { id } = useParams();
  const { user, token } = useAuth();
  const navigate = useNavigate();

  const [service, setService] = useState(null);
  const [date, setDate] = useState(todayIso());
  const [slots, setSlots] = useState([]);
  const [selected, setSelected] = useState('');
  const [note, setNote] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api(`/api/services/${id}`).then(setService).catch((e) => setError(e.message));
  }, [id]);

  useEffect(() => {
    setSelected('');
    api(`/api/services/${id}/slots?date=${date}`).then(setSlots).catch(() => setSlots([]));
  }, [id, date]);

  async function book() {
    setError('');
    setBusy(true);
    try {
      await api('/api/appointments', { method: 'POST', token, body: { serviceId: Number(id), startTime: selected, note } });
      navigate('/programari');
    } catch (e) {
      setError(e.message);
      api(`/api/services/${id}/slots?date=${date}`).then(setSlots).catch(() => {});
    } finally {
      setBusy(false);
    }
  }

  if (!service) return error ? <p className="error">{error}</p> : <p className="muted">Se încarcă…</p>;

  return (
    <div className="detail">
      <Link to="/servicii" className="back">← Înapoi la servicii</Link>
      <div className="card">
        <span className="provider">{service.providerName}</span>
        <h1>{service.name}</h1>
        {service.description && <p>{service.description}</p>}
        <div className="meta">
          <span>{service.durationMinutes} minute</span>
          <strong>{formatPrice(service.price)}</strong>
        </div>
      </div>

      <div className="card">
        <h2>Alege ziua și ora</h2>
        <label>
          Data
          <input type="date" value={date} min={todayIso()} onChange={(e) => setDate(e.target.value)} />
        </label>

        {slots.length === 0 ? (
          <p className="muted">Nu sunt ore libere în această zi (duminica nu se lucrează).</p>
        ) : (
          <div className="slots">
            {slots.map((s) => (
              <button key={s} className={`slot ${selected === s ? 'active' : ''}`} onClick={() => setSelected(s)}>
                {s.slice(11, 16)}
              </button>
            ))}
          </div>
        )}

        {selected && (
          <div className="confirm">
            <p>Ai ales: <strong>{formatDateTime(selected)}</strong></p>
            {!user && <p>Pentru a rezerva, <Link to="/login">intră în cont</Link> sau <Link to="/cont-nou">creează unul</Link>.</p>}
            {user?.role === 'PROVIDER' && <p className="muted">Conturile de furnizor nu pot face programări.</p>}
            {user?.role === 'CLIENT' && (
              <>
                <label>
                  Notă pentru furnizor (opțional)
                  <textarea maxLength={500} rows={3} value={note} onChange={(e) => setNote(e.target.value)} />
                </label>
                <button className="primary" disabled={busy} onClick={book}>
                  {busy ? 'Se rezervă…' : 'Confirmă programarea'}
                </button>
              </>
            )}
          </div>
        )}
        {error && <p className="error">{error}</p>}
      </div>
    </div>
  );
}
