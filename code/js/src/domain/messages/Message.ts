import { Channel } from '../channels/Channel';
import { UserInfo } from '../users/UserInfo';

export type Message = {
  id: number;
  text: string;
  channel: Channel;
  user: UserInfo;
  created: string;
};
