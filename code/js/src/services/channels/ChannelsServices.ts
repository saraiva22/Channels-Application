import httpServiceInit from '../utils/HttpService';
const httpService = httpServiceInit();

export async function getMemberChannels(sort?: String): Promise<string> {
  const path = 'http://localhost:8080/channels/member';
  return await httpService.get<string>(path);
}
