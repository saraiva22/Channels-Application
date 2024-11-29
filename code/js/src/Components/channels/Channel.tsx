import React from 'react';
import { ChannelOutputModel } from '../../services/channels/models/ChannelOutputModel';

export function Channel({ channel }: ChannelOutputModel) {
  return (
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
  );
}
