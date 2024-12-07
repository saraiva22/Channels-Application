import React from 'react';
import { ChannelsListOutputModel } from '../../services/channels/models/ChannelsListOutputModel';
import { Channel } from './Channel';
import './css/ChannelList.css';

export function ChannelList(channelsList: ChannelsListOutputModel) {
  return (
    <div>
      <h1>Channels</h1>
      {channelsList.channels && channelsList.channels.length > 0 ? (
        <ul className="channels-list">
          {channelsList.channels.map(channel => (
            <Channel key={channel.id} channel={channel} />
          ))}
        </ul>
      ) : (
        <p>No channels to show.</p>
      )}
    </div>
  );
}
