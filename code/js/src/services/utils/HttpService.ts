import { Problem, problemMediaType } from '../media/Problem';

export const PREFIX_API = 'http://192.168.1.72:3000/api';

export const apiRoutes = {
  // Channel
  GET_BY_NAME: '/channels',
  GET_PUBLIC_CHANNELS: '/channels/public',
  GET_CHANNEL_BY_ID: '/channels/:id',
  GET_USER_MEMBER_CHANNELS: '/channels/member',
  GET_USER_OWNED_CHANNELS: '/channels/owner',
  RECEIVED_CHANNEL_INVITES: '/channels/invites/received',
  SENT_CHANNEL_INVITES: '/channels/invites/sent',
  VALIDATE_CHANNEL_INVITE: '/channels/:id/invite/:code',
  CREATE_PRIVATE_INVITE: '/channels/:id/private-invite',
  UPDATE_CHANNEL: '/channels/:id/update',
  JOIN_PUBLIC_CHANNELS: '/channels/:id',
  LEAVE_CHANNEL: '/channels/:id/leave',
  // Messages
  GET_CHANNEL_MESSAGES: '/channels/:id/messages',
  DELETE_MESSAGE: '/channels/:channelId/messages/:messageId',
  // Users
  REGISTER_USER: '/users',
  LOGIN: '/users/token',
  LOGOUT: '/logout',
  GET_USER_BY_ID: '/users/:id',
  INVITE_USERS: '/users/invite',
  SEARCH_USERS: '/users?username=',
  // Chat
  LISTEN_CHAT: '/users/notifications',
};

export default function httpService() {
  return {
    get: get,
    post: post,
    put: put,
    delete: del,
    patch: patch,
  };

  async function processRequest<T>(uri: string, method: string, body?: string): Promise<T> {
    const config: RequestInit = {
      method,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
      body: body,
    };

    const response = await fetch(uri, config);

    if (!response.ok) {
      if (response.headers.get('Content-Type')?.includes(problemMediaType)) {
        const res = await response.json();
        throw res as Problem;
      } else throw new Error(`HTTP error! Status: ${response.status}`);
    }

    const contentType = response.headers.get('Content-Type');
    if (!contentType || !contentType.includes('application/json')) {
      return {} as T;
    }

    return (await response.json()) as T;
  }

  async function get<T>(path: string): Promise<T> {
    return processRequest<T>(path, 'GET', undefined);
  }

  async function post<T>(path: string, body?: string): Promise<T> {
    return processRequest<T>(path, 'POST', body);
  }

  async function put<T>(path: string, body?: string): Promise<T> {
    return processRequest<T>(path, 'PUT', body);
  }

  async function del<T>(path: string, body?: string): Promise<T> {
    return processRequest<T>(path, 'DELETE', body);
  }

  async function patch<T>(path: string, body?: string): Promise<T> {
    return processRequest<T>(path, 'PATCH', body);
  }
}
