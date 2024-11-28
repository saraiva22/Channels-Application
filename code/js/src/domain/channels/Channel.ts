import { UserInfo } from '../users/UserInfo';
import { Type } from './Type';

export type Channel = {
  id: number;
  name: string;
  owner: UserInfo;
  type: Type;
  members: Array<UserInfo>;
  bannedMembers: Array<UserInfo>;
};
