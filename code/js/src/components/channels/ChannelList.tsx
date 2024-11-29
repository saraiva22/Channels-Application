import React from 'react';
import { ChannelsListOutputModel } from '../../services/channels/models/ChannelsListOutputModel';
import { Channel } from './Channel';
import './ChannelList.css';

export function ChannelList(channelsList: ChannelsListOutputModel) {
  return (
    <div>
      <h1>Channels</h1>
      <ul className="channels-list">
        {channelsList.channels.map(channel => (
          <Channel key={channel.id} channel={channel} />
        ))}
      </ul>
    </div>
  );
}
