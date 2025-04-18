import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { webRoutes } from '../../App';

const cookieName = 'login';

export function RequireAuthentication({ children }) {
  const location = useLocation();
  const loginCookie = getCookie(cookieName);
  if (loginCookie) {
    return <>{children}</>;
  } else {
    console.log('redirecting to login');
    return <Navigate to={webRoutes.login} state={{ source: location.pathname }} replace={true} />;
  }
}

export function isLoggedIn(): boolean {
  return getCookie(cookieName) != null;
}

export function getCookie(name: string): string | null {
  const loginCookie = document.cookie.split('; ').find(row => row.startsWith(name));
  return loginCookie ? loginCookie.split('=')[1] : null;
}
