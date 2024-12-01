import httpServiceInit, { apiRoutes, PREFIX_API } from '../utils/HttpService';
import { MessageListOutputModel } from './models/MessageListOutputModel';

const httpService = httpServiceInit();

export function getChannelMessages(channelId: number): Promise<MessageListOutputModel> {
  const path = PREFIX_API + apiRoutes.GET_CHANNEL_MESSAGES.replace(':id', String(channelId));
  return httpService.get<MessageListOutputModel>(path);
}

export function deleteMessage<T>(channelId: number, messageId: number): Promise<T> {
  const path =
    PREFIX_API +
    apiRoutes.DELETE_MESSAGE.replace(':channelId', String(channelId)).replace(':messageId', String(messageId));
  return httpService.delete(path);
}
