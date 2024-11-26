import { RegisterOutput } from '../models/users/RegisterOutputModel';
import { LoginOutput } from '../models/users/LoginOutputModel';
import httpServiceInit from '../HttpService';
const httpService = httpServiceInit();

export async function register(
  username: string,
  email: string,
  password: string,
  inviteCode?: string
): Promise<RegisterOutput> {
  const path = 'http://localhost:8080/api/users';
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
  const path = 'http://localhost:8080/api/users/token';
  return await httpService.post<LoginOutput>(
    path,
    JSON.stringify({
      username,
      password,
    })
  );
}
