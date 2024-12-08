import { ChannelsListOutputModel } from './models/ChannelsListOutputModel';
import httpServiceInit, { apiRoutes } from '../utils/HttpService';
import { PREFIX_API } from '../utils/HttpService';
import { Type } from '../../domain/channels/Type';
import { IdOutputModel } from '../utils/models/IdOutputModel';
import { ChannelOutputModel } from './models/ChannelOutputModel';
import { webRoutes } from '../../App';
import { PrivateListInviteOutputModel } from './models/PrivateListInviteOutputModel';
import { Status } from '../../domain/channels/Status';
import { Privacy } from '../../domain/channels/Privacy';
import { RegisterPrivateInviteOutputModel } from './models/RegisterPrivateInviteOutputModel';

const httpService = httpServiceInit();

export async function createChannel(name: string, type: Type): Promise<IdOutputModel> {
  const path = PREFIX_API + webRoutes.channelCreate;
  return await httpService.post<IdOutputModel>(
    path,
    JSON.stringify({
      name,
      type,
    })
  );
}

export async function searchChannels(name?: string, sort?: string): Promise<ChannelsListOutputModel> {
  const path = PREFIX_API + apiRoutes.GET_BY_NAME + `?name=${name}` + `&sort=${sort}`;
  return await httpService.get<ChannelsListOutputModel>(path);
}

export async function getChannelById(channelId: number): Promise<ChannelOutputModel> {
  const path = PREFIX_API + apiRoutes.GET_CHANNEL_BY_ID.replace(':id', String(channelId));
  return await httpService.get<ChannelOutputModel>(path);
}

export async function getChannelsList(uri: string, sort?: string): Promise<ChannelsListOutputModel> {
  const path = PREFIX_API + uri;
  return await httpService.get<ChannelsListOutputModel>(path);
}

export async function getSentChannelInvites(): Promise<PrivateListInviteOutputModel> {
  const path = PREFIX_API + apiRoutes.SENT_CHANNEL_INVITES;
  return await httpService.get<PrivateListInviteOutputModel>(path);
}

export async function getReceivedChannelInvites(): Promise<PrivateListInviteOutputModel> {
  const path = PREFIX_API + apiRoutes.RECEIVED_CHANNEL_INVITES;
  return await httpService.get<PrivateListInviteOutputModel>(path);
}

export async function validateChannelInvite(
  channelId: number,
  code: string,
  status: Status
): Promise<ChannelOutputModel> {
  const path =
    PREFIX_API + apiRoutes.VALIDATE_CHANNEL_INVITE.replace(':id', channelId.toString()).replace(':code', code);
  return await httpService.post<ChannelOutputModel>(path, JSON.stringify({ status }));
}

export async function createPrivateInvite(
  channelId: number,
  privacy: Privacy,
  username: string
): Promise<RegisterPrivateInviteOutputModel> {
  const path = PREFIX_API + apiRoutes.CREATE_PRIVATE_INVITE.replace(':id', channelId.toString());
  return await httpService.post<RegisterPrivateInviteOutputModel>(path, JSON.stringify({ privacy, username }));
}

export async function updateChannel(channelId: number, name: string, type: Type): Promise<ChannelsListOutputModel> {
  const path = PREFIX_API + apiRoutes.UPDATE_CHANNEL.replace(':id', channelId.toString());
  return await httpService.patch<ChannelsListOutputModel>(path, JSON.stringify({ name, type }));
}

export async function joinPublicChannel(channelId: number): Promise<ChannelsListOutputModel> {
  const path = PREFIX_API + apiRoutes.JOIN_PUBLIC_CHANNELS.replace(':id', channelId.toString());
  return await httpService.post<ChannelsListOutputModel>(path);
}

export async function leaveInChannel(channelId: number): Promise<Response> {
  const path = PREFIX_API + apiRoutes.LEAVE_CHANNEL.replace(':id', channelId.toString());
  return await httpService.post<undefined>(path);
}
