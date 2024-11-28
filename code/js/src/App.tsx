import React from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { LayoutWithNavBar } from './layout/NavBar';
import { Login } from './components/authentication/Login';
import { Register } from './components/authentication/Register';
import { RequireAuthentication } from './components/authentication/RequireAuthentication';
import { HomePage } from './components/home/Home';
import { About } from './components/about/About';
import { Notifications } from './components/notifications/Notifications';
import { Fetch } from './services/Fetch';
import { getMemberChannels } from './services/channels/ChannelsServices';

export const webRoutes = {
  home: '/',
  me: '/me',
  login: '/login',
  register: '/register',
  logout: '/logout',
  channelsMembers: '/channels-members',
  channelsOwned: '/channels-owned',
  about: '/about',
  notifications: '/notifications',
};

const apiRoutes = {
  GET_USER_MEMBER_CHANNELS: '/channels/member',
  GET_USER_OWNED_CHANNELS: '/channels/owner',
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
      {
        path: webRoutes.about,
        element: <About />,
      },
      {
        path: webRoutes.notifications,
        element: <Notifications />,
      },
      {
        path: webRoutes.channelsMembers,
        element: (
          <RequireAuthentication>
            <Fetch fetchFunction={getMemberChannels} fetchArgs={[apiRoutes.GET_USER_MEMBER_CHANNELS]} />
          </RequireAuthentication>
        ),
      },
      {
        path: webRoutes.channelsOwned,
        element: (
          <RequireAuthentication>
            <Fetch fetchFunction={getMemberChannels} fetchArgs={[apiRoutes.GET_USER_OWNED_CHANNELS]} />
          </RequireAuthentication>
        ),
      },
    ],
  },
]);

export function App() {
  return <RouterProvider router={router} />;
}
