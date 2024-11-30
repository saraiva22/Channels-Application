import React from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { LayoutWithNavBar } from './layout/NavBar';
import { Login } from './components/authentication/Login';
import { Register } from './components/authentication/Register';
import { RequireAuthentication } from './components/authentication/RequireAuthentication';
import { HomePage } from './components/home/Home';
import { About } from './components/about/About';
import { Notifications } from './components/notifications/Notifications';
import { Fetch } from './components/fetch/Fetch';
import { getChannelsList } from './services/channels/ChannelsServices';
import { ChannelCreate } from './components/channels/ChannelCreate';
import { apiRoutes, webRoutes } from './services/utils/HttpService';
import { getChannelMessages } from './services/messages/MessagesService';
import { MessageList } from './components/messages/MessageList';

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
        path: webRoutes.channelCreate,
        element: (
          <RequireAuthentication>
            <ChannelCreate />
          </RequireAuthentication>
        ),
      },
      {
        path: webRoutes.notifications,
        element: <Notifications />,
      },
      {
        path: webRoutes.channelsMembers,
        element: (
          <RequireAuthentication>
            <Fetch fetchFunction={getChannelsList} fetchArgs={[apiRoutes.GET_USER_MEMBER_CHANNELS]} />
          </RequireAuthentication>
        ),
      },
      {
        path: webRoutes.channelsOwned,
        element: (
          <RequireAuthentication>
            <Fetch fetchFunction={getChannelsList} fetchArgs={[apiRoutes.GET_USER_OWNED_CHANNELS]} />
          </RequireAuthentication>
        ),
      },
      {
        path: webRoutes.channelMessages,
        element: (
          <RequireAuthentication>
            <MessageList />
          </RequireAuthentication>
        ),
      },
    ],
  },
]);

export function App() {
  return <RouterProvider router={router} />;
}
