import { ChannelsListOutputModel } from './models/ChannelsListOutputModel';
import httpServiceInit from '../utils/HttpService';
import { PREFIX_API } from '../utils/HttpService';
import { Type } from '../../domain/channels/Type';
import { IdModel } from '../utils/models/IdModel';
import { webRoutes } from '../../App';

const httpService = httpServiceInit();

export async function getMemberChannels(...args: Array<string | number | null>): Promise<ChannelsListOutputModel> {
  const path = PREFIX_API + args[0];
  return await httpService.get<ChannelsListOutputModel>(path);
}

export async function createChannel(name: string, type: Type): Promise<IdModel> {
  const path = PREFIX_API + webRoutes.channelCreate;
  return await httpService.post<IdModel>(
    path,
    JSON.stringify({
      name,
      type,
    })
  );
}
