import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';

const cookieName = 'login';

export function RequireAuthentication({ children }) {
  const location = useLocation();
  const loginCookie = getCookie(cookieName);
  if (loginCookie) {
    return <>{children}</>;
  } else {
    console.log('redirecting to login');
    return <Navigate to="/login" state={{ source: location.pathname }} replace={true} />;
  }
}

export function isLoggedIn(): boolean {
  return getCookie(cookieName) != null;
}

export function getCookie(name: string): string | null {
  return document.cookie
    .split(';')
    .find(row => row.startsWith(name))
    ?.split('=')[1];
}
