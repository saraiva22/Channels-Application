import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';

export function RequireAuthentication({ children }) {
  const location = useLocation();
  const loginCookie = document.cookie
    .split('; ')
    .find(row => row.startsWith('token'))
    ?.split('=')[1];
  if (loginCookie) {
    return <>{children}</>;
  } else {
    console.log('redirecting to login');
    return <Navigate to="/login" state={{ source: location.pathname }} replace={true} />;
  }
}
