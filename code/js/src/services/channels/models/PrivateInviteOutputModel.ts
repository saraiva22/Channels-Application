import { Privacy } from '../../../domain/channels/Privacy';
import { Status } from '../../../domain/channels/Status';
import { UserInfo } from '../../../domain/users/UserInfo';

export type PrivateInviteOutputModel = {
  codHash: string;
  privacy: Privacy;
  status: Status;
  userInfo: UserInfo;
  channelId: number;
  channelName: string;
};

export type PrivateInviteOutput = {
  value: PrivateInviteOutputModel;
};
