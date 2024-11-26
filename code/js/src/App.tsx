import React from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { LayoutWithNavBar } from './layout/NavBar';
import { Login } from './components/authentication/Login';
import { Register } from './components/authentication/Register';
import { RequireAuthentication } from './components/authentication/RequireAuthentication';
import { HomePage } from './components/home/Home';

export const webRoutes = {
  home: '/',
  me: '/me',
  login: '/login',
  register: '/register',
  logout: '/logout',
  channels: '/channels',
  about: '/about',
};

const router = createBrowserRouter([
  {
    path: '/',
    element: <LayoutWithNavBar />,
    children: [
      {
        path: webRoutes.home,
        element: <HomePage />,
      },
      {
        path: webRoutes.login,
        element: <Login />,
      },
      {
        path: webRoutes.register,
        element: <Register />,
      },
    ],
  },
]);

export function App() {
  return <RouterProvider router={router} />;
}
