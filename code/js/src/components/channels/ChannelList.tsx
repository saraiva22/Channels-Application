import React from 'react';
import { ChannelsListOutputModel } from '../../services/channels/models/ChannelsListOutputModel';
import { Channel } from './Channel';

export function ChannelList(channelsList: ChannelsListOutputModel) {
  return (
    <div>
      <h1>Channels</h1>
      <ul>
        {channelsList.channels.map(channel => (
          <Channel key={channel.id} channel={channel} />
        ))}
      </ul>
    </div>
  );
}
