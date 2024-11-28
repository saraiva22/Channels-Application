import React from 'react';
import { ChannelListOutputModel } from '../../services/channels/models/ChannelsListOutputModel';

export function ChannelList(channelsList: ChannelListOutputModel) {
  return (
    <div>
      <h1>Channels</h1>
      <ul>
        {channelsList.channels.map(channel => (
          <div style={{ width: '300px', height: '150px' }} key={channel.id}>
            <p>
              <b>Name:</b> {channel.name}
            </p>
            <p>
              <b>Owner:</b> {channel.owner.username}
            </p>
            <p>
              <b>Type:</b> {channel.type}
            </p>
          </div>
        ))}
      </ul>
    </div>
  );
}
