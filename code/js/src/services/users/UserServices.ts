import { RegisterOutput } from './models/RegisterOutputModel';
import { LoginOutput } from './models/LoginOutputModel';
import { LogoutOutput } from './models/LogoutOutput';
import httpServiceInit, { apiRoutes } from '../utils/HttpService';
import { PREFIX_API } from '../utils/HttpService';
import { UserInviteOutput } from './models/UserInviteOutputModel';

const httpService = httpServiceInit();

export async function register(
  username: string,
  email: string,
  password: string,
  inviteCode?: string
): Promise<RegisterOutput> {
  const path = PREFIX_API + apiRoutes.REGISTER_USER;
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
  const path = PREFIX_API + apiRoutes.LOGIN;
  return await httpService.post<LoginOutput>(
    path,
    JSON.stringify({
      username,
      password,
    })
  );
}

export async function logout(): Promise<LogoutOutput> {
  const path = PREFIX_API + apiRoutes.LOGOUT;
  return await httpService.post<LogoutOutput>(path);
}

export async function createInvitationRegster(): Promise<UserInviteOutput> {
  const path = PREFIX_API + apiRoutes.INVITE_USERS;
  return await httpService.post<UserInviteOutput>(path);
}
