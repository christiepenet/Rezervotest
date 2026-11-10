import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth';
import '../landing.css';

// Date de exemplu pentru demonstratia din hero (nu vin din backend).
const DEMO_DAYS = [
  { key: 'mar', label: 'Mar', date: '18' },
  { key: 'mie', label: 'Mie', date: '19' },
  { key: 'joi', label: 'Joi', date: '20' },
  { key: 'vin', label: 'Vin', date: '21' },
];
const DEMO_SLOTS = ['09:00', '09:30', '10:00', '10:30', '11:00', '11:30', '12:00', '12:30', '14:00', '14:30', '15:00', '15:30'];
const TAKEN = { mar: ['09:30', '11:00', '14:30'], mie: ['09:00', '10:00', '12:30'], joi: ['11:30', '15:00'], vin: ['09:00', '09:30', '14:00'] };

const DOMAINS = ['Frizerii', 'Saloane de înfrumusețare', 'Service-uri auto', 'Cabinete stomatologice', 'Kinetoterapie', 'Fotografi', 'Meditații', 'Spălătorii auto', 'Croitorii', 'Antrenori personali'];

function AgendaDemo() {
  const [day, setDay] = useState('mar');
  const [picked, setPicked] = useState(null);
  const dayLabel = DEMO_DAYS.find((d) => d.key === day);

  return (
    <div className="agenda" aria-label="Exemplu de programare">
      <div className="agenda-head">
        <div>
          <p className="agenda-provider">Frizeria Ion</p>
          <p className="agenda-service">Tuns clasic, 30 min, 50 lei</p>
        </div>
        <span className="agenda-tag">Exemplu</span>
      </div>

      <div className="agenda-days" role="tablist">
        {DEMO_DAYS.map((d) => (
          <button
            key={d.key}
            role="tab"
            aria-selected={d.key === day}
            className={d.key === day ? 'on' : ''}
            onClick={() => { setDay(d.key); setPicked(null); }}
          >
            <span>{d.label}</span>
            <strong>{d.date}</strong>
          </button>
        ))}
      </div>

      <div className="agenda-slots">
        {DEMO_SLOTS.map((t, i) => {
          const taken = TAKEN[day].includes(t);
          const isPicked = picked === t;
          return (
            <button
              key={`${day}-${t}`}
              className={`agenda-slot${taken ? ' taken' : ''}${isPicked ? ' picked' : ''}`}
              style={{ '--i': i }}
              disabled={taken}
              aria-pressed={isPicked}
              aria-label={taken ? `${t}, ocupat` : `${t}, liber`}
              onClick={() => setPicked(t)}
            >
              {t}
            </button>
          );
        })}
      </div>

      <div className={`agenda-ticket${picked ? ' show' : ''}`} aria-live="polite">
        {picked ? (
          <>
            <span>Programare confirmată</span>
            <strong>{dayLabel.label} {dayLabel.date}, ora {picked}</strong>
          </>
        ) : (
          <span>Atinge o oră liberă ca să vezi cum arată.</span>
        )}
      </div>
    </div>
  );
}

export default function Landing() {
  const { user } = useAuth();

  return (
    <div className="landing">
      <section className="l-hero">
        <div className="l-wrap l-hero-grid">
          <div className="l-hero-copy">
            <h1>Programări fără telefoane care sună în gol.</h1>
            <p className="l-lead">
              Vezi orele libere la frizer, la service sau la cabinet și rezervă în zece secunde.
              Afacerea primește programarea direct în cont, fără caiet și fără apeluri pierdute.
            </p>
            <div className="l-actions">
              <Link to="/servicii" className="l-btn l-btn-main">Caută un serviciu</Link>
              {!user && <Link to="/cont-nou?rol=furnizor" className="l-btn l-btn-ghost">Înscrie-ți afacerea gratuit</Link>}
            </div>
          </div>
          <AgendaDemo />
        </div>
      </section>

      <section className="l-split" aria-label="Pentru cine este Rezervo">
        <div className="l-split-col l-split-client">
          <h2>Pentru tine, când ai nevoie de o programare</h2>
          <ul>
            <li>Vezi doar orele care chiar sunt libere.</li>
            <li>Rezervi oricând, și duminică seara.</li>
            <li>Ai toate programările într-un singur loc și le anulezi dintr-un click.</li>
          </ul>
          <Link to="/servicii" className="l-link">Vezi serviciile disponibile</Link>
        </div>
        <div className="l-split-col l-split-biz">
          <h2>Pentru afacerea ta, care are clienți de primit</h2>
          <ul>
            <li>Adaugi serviciile cu durată și preț în două minute.</li>
            <li>Programul se umple singur, fără suprapuneri.</li>
            <li>Vezi cine vine, la ce oră și ce notă ți-a lăsat.</li>
          </ul>
          {!user && <Link to="/cont-nou?rol=furnizor" className="l-link">Creează cont de furnizor</Link>}
        </div>
      </section>

      <section className="l-steps l-wrap">
        <h2>Trei pași până la scaun</h2>
        <ol>
          <li>
            <span className="l-step-n">1</span>
            <h3>Alegi serviciul</h3>
            <p>Cauți după nume sau după locul unde vrei să mergi.</p>
          </li>
          <li>
            <span className="l-step-n">2</span>
            <h3>Alegi ziua și ora</h3>
            <p>Vezi grila cu intervalele libere și apeși pe cel care îți convine.</p>
          </li>
          <li>
            <span className="l-step-n">3</span>
            <h3>Vii la programare</h3>
            <p>Programarea apare în contul tău și la furnizor. Dacă se schimbă planurile, o anulezi.</p>
          </li>
        </ol>
      </section>

      <section className="l-domains" aria-label="Domenii">
        <div className="l-wrap">
          <p className="l-domains-intro">Merge pentru orice afacere care lucrează cu ora fixă:</p>
          <p className="l-domains-list">
            {DOMAINS.map((d) => <span key={d}>{d}</span>)}
          </p>
        </div>
      </section>

      <section className="l-final">
        <div className="l-wrap l-final-inner">
          <h2>Clienții tăi te caută pe telefon. Lasă-i să se programeze tot de acolo.</h2>
          {user ? (
            <Link to="/servicii" className="l-btn l-btn-gold">Mergi la servicii</Link>
          ) : (
            <Link to="/cont-nou?rol=furnizor" className="l-btn l-btn-gold">Creează cont de furnizor</Link>
          )}
        </div>
      </section>

      <footer className="l-footer">
        <div className="l-wrap">© {new Date().getFullYear()} Rezervo</div>
      </footer>
    </div>
  );
}
