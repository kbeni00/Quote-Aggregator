import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import keycloak from './keycloak';

// Remove StrictMode for development to prevent double initialization
// Alternatively, you could conditionally disable StrictMode in development only.
createRoot(document.getElementById('root')!).render(
  <ReactKeycloakProvider
    authClient={keycloak}
    initOptions={{
      onLoad: 'check-sso', // or 'login-required' depending on your desired flow
      checkLoginIframe: false, // Disable iframe checks to avoid 404 errors
    }}
  >
    <App />
  </ReactKeycloakProvider>
);
