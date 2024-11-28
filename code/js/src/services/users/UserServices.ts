import { RegisterOutput } from './models/RegisterOutputModel';
import { LoginOutput } from './models/LoginOutputModel';
import { LogoutOutput } from './models/LogoutOutput';
import httpServiceInit from '../utils/HttpService';
import { api } from '../utils/HttpService';

const httpService = httpServiceInit();

export async function register(
  username: string,
  email: string,
  password: string,
  inviteCode?: string
): Promise<RegisterOutput> {
  const path = `${api}/users`;
  return await httpService.post<RegisterOutput>(
    path,
    JSON.stringify({
      username,
      email,
      password,
      inviteCode,
    })
  );
}

export async function login(username: string, password: string): Promise<LoginOutput> {
  const path = `${api}/users/token`;
  return await httpService.post<LoginOutput>(
    path,
    JSON.stringify({
      username,
      password,
    })
  );
}

export async function logout(): Promise<LogoutOutput> {
  const path = `${api}/logout`;
  return await httpService.post<LogoutOutput>(path);
}
