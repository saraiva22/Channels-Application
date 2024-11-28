import { ChannelListOutputModel } from './models/ChannelsListOutputModel';
import httpServiceInit from '../utils/HttpService';
import { PREFIX_API } from '../utils/HttpService';

const httpService = httpServiceInit();

export async function getMemberChannels(...args: Array<string | number | null>): Promise<ChannelListOutputModel> {
  const path = PREFIX_API + args[0];
  return await httpService.get<ChannelListOutputModel>(path);
}
