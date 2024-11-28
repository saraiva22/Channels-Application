import { ChannelListOutputModel } from './models/ChannelsListOutputModel';
import httpServiceInit from '../utils/HttpService';
const httpService = httpServiceInit();

const PREFIX = 'http://localhost:8080/api';

export async function getMemberChannels(...args: Array<string | number | null>): Promise<ChannelListOutputModel> {
  const path = PREFIX + args[0];
  return await httpService.get<ChannelListOutputModel>(path);
}
