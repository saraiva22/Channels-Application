import { ChannelsListOutputModel } from './models/ChannelsListOutputModel';
import httpServiceInit, { apiRoutes } from '../utils/HttpService';
import { PREFIX_API } from '../utils/HttpService';
import { Type } from '../../domain/channels/Type';
import { IdOutputModel } from '../utils/models/IdOutputModel';
import { ChannelOutputModel } from './models/ChannelOutputModel';
import { webRoutes } from '../../App';

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
  const path = PREFIX_API + apiRoutes.GET_BY_NAME.replace('name', name)
  return await httpService.get<ChannelsListOutputModel>(path)
}

export async function getChannelById(channelId: number): Promise<ChannelOutputModel> {
  const path = PREFIX_API + apiRoutes.GET_CHANNEL_BY_ID.replace(':id', String(channelId));
  return await httpService.get<ChannelOutputModel>(path);
}

export async function getChannelsList(uri: string, sort?: string): Promise<ChannelsListOutputModel> {
  const path = PREFIX_API + uri;
  return await httpService.get<ChannelsListOutputModel>(path);
}
