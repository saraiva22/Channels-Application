import React from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { Layout } from './Layout/Layout';
import { Login } from './Components/Authentication/Login';
import { Register } from './Components/Authentication/Register';
import { RequireAuthentication } from './Components/Authentication/requireAuthentication';

export function App() {
  return (
    <RequireAuthentication>
      <RouterProvider router={router} />
    </RequireAuthentication>
  );
}

const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        path: '/login',
        element: <Login />,
      },
      {
        path: '/register',
        element: <Register />,
      }
    ],
  },
]);

export default App;
