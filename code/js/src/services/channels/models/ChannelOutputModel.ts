import { Type } from '../../../domain/channels/Type';
import { UserInfo } from '../../../domain/users/UserInfo';

export type ChannelOutputModel = {
  id: number;
  name: string;
  owner: UserInfo;
  type: Type;
  members: Array<UserInfo>;
  bannedMembers: Array<UserInfo>;
};
