import * as React from 'react';
import { getMemberChannels } from '../../services/channels/ChannelsServices';

export function ChannelsMember() {
  async function handleResults() {
    const res = await getMemberChannels();
    return JSON.stringify(res);
  }

  return (
    <div>
      <textarea onChange={handleResults} readOnly cols={80} rows={40}></textarea>
    </div>
  );
}
