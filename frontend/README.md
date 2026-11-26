# Frontend

React and TypeScript frontend. API requests use `openapi-fetch` with types generated
from the repository OpenAPI contract.

## Development

Run `npm run dev` to start the development server. API calls default to
`http://localhost:8080`; set `VITE_API_BASE_URL` to override the backend URL.

Run `npm run generate:api` after updating `../docs/openapi.yaml` to regenerate
`src/api/schema.d.ts`. Run `npm run typecheck`, `npm run lint`, and `npm run build`
to check the frontend.
