import { RegisterOutputModel } from './models/RegisterOutputModel';
import { LoginOutputModel } from './models/LoginOutputModel';
import httpServiceInit from '../HttpService';
const httpService = httpServiceInit();

export async function register(
  username: string,
  email: string,
  password: string,
  inviteCode?: string
): Promise<RegisterOutputModel> {
  const path = 'http://localhost:8080/api/users';
  return await httpService.post<RegisterOutputModel>(
    path,
    JSON.stringify({
      username,
      email,
      password,
      inviteCode,
    })
  );
}

export async function login(username: string, password: string): Promise<LoginOutputModel> {
  const path = 'http://localhost:8080/api/users/token';
  return await httpService.post<LoginOutputModel>(
    path,
    JSON.stringify({
      username,
      password,
    })
  );
}
