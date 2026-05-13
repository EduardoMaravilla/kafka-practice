import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 200,
  duration: '30s',
  // Esto ayuda a que k6 no abra una conexión nueva para cada petición
  // reutilizando las existentes y evitando el error connectex de Windows.
  noConnectionReuse: false, 
};

export default function () {
  const url = 'http://localhost:9595/gps/ingest';
  
  const payload = JSON.stringify({
    deviceId: 'RYZEN-3700X-01',
    latitude: -13.70,
    longitude: -89.20,
    // Date.now() devuelve el timestamp en milisegundos (long)
    timestamp: Date.now(), 
  });

  const params = {
    headers: { 
      'Content-Type': 'application/json',
      'Connection': 'keep-alive' 
    },
  };

  const res = http.post(url, payload, params);
  
  check(res, {
    'status is 201': (r) => r.status === 201,
  });
}