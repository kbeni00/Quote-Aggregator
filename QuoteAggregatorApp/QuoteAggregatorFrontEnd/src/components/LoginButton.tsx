// src/components/LoginButton.tsx
import React from 'react';
import { useKeycloak } from '@react-keycloak/web';

const LoginButton: React.FC = () => {
  const { keycloak, initialized } = useKeycloak();

  if (!initialized) {
    return <div>Loading authentication...</div>;
  }

  return !keycloak.authenticated ? (
    <button
      className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
      onClick={() => keycloak.login()}
    >
      Login
    </button>
  ) : (
    <button
      className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
      onClick={() => keycloak.logout()}
    >
      Logout
    </button>
  );
};

export default LoginButton;
