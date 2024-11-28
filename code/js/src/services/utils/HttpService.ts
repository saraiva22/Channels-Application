import { Problem, problemMediaType } from '../media/Problem';

export const api = 'http://localhost:8080/api';


export default function httpService() {
  return {
    get: get,
    post: post,
    put: put,
    del: del,
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
}
