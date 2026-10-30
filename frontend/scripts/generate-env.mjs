import { writeFileSync } from 'node:fs';
import { resolve } from 'node:path';

const DEFAULT_API_URL = 'http://localhost:8080';

const apiUrl = (process.env['API_URL'] ?? '').trim() || DEFAULT_API_URL;
const target = resolve(process.cwd(), 'public', 'env.js');

writeFileSync(target, `window.__env = { apiUrl: ${JSON.stringify(apiUrl)} };\n`);

console.log(`Generated ${target} with apiUrl=${apiUrl}`);
