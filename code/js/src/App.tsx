import React from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { Layout } from './layout/Layout';
import { Login } from './components/authentication/Login';
import { Register } from './components/authentication/Register';
import { RequireAuthentication } from './components/authentication/RequireAuthentication';

export const webRoutes = {
  home: '/',
  me: '/me',
  login: '/login',
  register: '/register',
  logout: '/logout',
  channels: '/channels',
};

const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
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
