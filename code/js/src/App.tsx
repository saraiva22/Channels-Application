import React from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { LayoutWithNavBar } from './layout/NavBar';
import { apiRoutes } from './services/utils/HttpService';
import { MessageList } from './components/messages/MessageList';
import { SearchChannels } from './components/channels/SearchChannels';
import { Login } from './components/authentication/Login';
import { HomePage } from './components/home/Home';
import { Register } from './components/authentication/Register';
import { About } from './components/about/About';
import { Notifications } from './components/notifications/Notifications';
import { RequireAuthentication } from './components/authentication/RequireAuthentication';
import { ChannelCreate } from './components/channels/ChannelCreate';
import { getChannelsList } from './services/channels/ChannelsServices';
import { AuthProvider } from './components/authentication/AuthProvider';
import { Me } from './components/authentication/Me';
import { Fetch } from './components/fetch/Fetch';
import { SendInvites } from './components/channels/SendInvites';
import { ReceivedInvites } from './components/channels/ReceivedInvites';
import { ValidateChannelInvite } from './components/channels/ValidateChannelInvite';

export const webRoutes = {
  home: '/',
  me: '/me',
  login: '/login',
  register: '/register',
  logout: '/logout',
  channel: 'channel/:id',
  publicChannels: '/public-channels',
  searchChannels: '/channels/search',
  channelsMembers: '/channels-members',
  channelsOwned: '/channels-owned',
  channelCreate: '/channels/create',
  channelMessages: '/channel/messages',
  receivedChannelInvites: '/channels/inv/received',
  sentChannelInvites: '/channels/inv/sent',
  validateChannelInvite: '/channels/invite/validate',
  about: '/about',
  notifications: '/notifications',
};

const router = createBrowserRouter([
  {
    path: '/',
    element: <LayoutWithNavBar />,
    children: [
      // User
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
      // Channels
      {
        path: webRoutes.channelCreate,
        element: (
          <RequireAuthentication>
            <ChannelCreate />
          </RequireAuthentication>
        ),
      },
      {
        path: webRoutes.validateChannelInvite,
        element: (
          <RequireAuthentication>
            <ValidateChannelInvite />
          </RequireAuthentication>
        ),
      },
      {
        path: webRoutes.sentChannelInvites,
        element: (
          <RequireAuthentication>
            <SendInvites />
          </RequireAuthentication>
        ),
      },
      {
        path: webRoutes.receivedChannelInvites,
        element: (
          <RequireAuthentication>
            <ReceivedInvites />
          </RequireAuthentication>
        ),
      },
      {
        path: webRoutes.publicChannels,
        element: (
          <RequireAuthentication>
            <Fetch fetchFunction={getChannelsList} fetchArgs={[apiRoutes.GET_PUBLIC_CHANNELS]} />
          </RequireAuthentication>
        ),
      },
      {
        path: webRoutes.searchChannels,
        element: (
          <RequireAuthentication>
            <SearchChannels />
          </RequireAuthentication>
        ),
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
      // Messages
      {
        path: webRoutes.channelMessages,
        element: (
          <RequireAuthentication>
            <MessageList />
          </RequireAuthentication>
        ),
      },
      {
        path: webRoutes.me,
        element: (
          <RequireAuthentication>
            <Me />
          </RequireAuthentication>
        ),
      },
    ],
  },
]);

export function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  );
}
