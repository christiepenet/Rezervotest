import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, formatPrice } from '../api';

export default function Services() {
  const [services, setServices] = useState(null);
  const [error, setError] = useState('');
  const [query, setQuery] = useState('');

  useEffect(() => {
    api('/api/services').then(setServices).catch((e) => setError(e.message));
  }, []);

  const q = query.trim().toLowerCase();
  const visible = (services || []).filter((s) =>
    !q || s.name.toLowerCase().includes(q) || s.providerName.toLowerCase().includes(q));

  return (
    <>
      <section className="hero">
        <h1>Programează-te în câteva secunde</h1>
        <p>Frizerii, service-uri, cabinete și alte servicii locale — vezi orele libere și rezervă direct.</p>
        <input
          className="search"
          placeholder="Caută un serviciu sau un furnizor…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </section>

      {error && <p className="error">{error}</p>}
      {!services && !error && <p className="muted">Se încarcă serviciile…</p>}
      {services && visible.length === 0 && (
        <p className="muted">Nu există încă servicii. Creează un cont de furnizor și adaugă primul serviciu.</p>
      )}

      <div className="grid">
        {visible.map((s) => (
          <Link key={s.id} to={`/servicii/${s.id}`} className="card service-card">
            <span className="provider">{s.providerName}</span>
            <h3>{s.name}</h3>
            {s.description && <p className="muted clamp">{s.description}</p>}
            <div className="meta">
              <span>{s.durationMinutes} min</span>
              <strong>{formatPrice(s.price)}</strong>
            </div>
          </Link>
        ))}
      </div>
    </>
  );
}
