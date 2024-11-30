import httpServiceInit, { PREFIX_API } from '../utils/HttpService';
import { MessageListOutputModel } from './models/MessageListOutputModel';

const httpService = httpServiceInit();

export function getChannelMessages(channelId: number): Promise<MessageListOutputModel> {
  const path = PREFIX_API + `/channels/${channelId}/messages`;
  return httpService.get<MessageListOutputModel>(path);
}
