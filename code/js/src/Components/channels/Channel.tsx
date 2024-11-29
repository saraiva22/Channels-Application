import React from 'react';
import { ChannelOutputModel } from '../../services/channels/models/ChannelOutputModel';
import privatechannel from '../../assets/privatechannel.png';
import publicchannel from '../../assets/publicchannel.png';
import './Channel.css';

export function Channel({ channel }: ChannelOutputModel) {
  const isPublic = channel.type.toString() === 'PUBLIC';

  const handleClick = () => {
    console.log(`Clicked on channel:`, channel);
  };

  return (
    <div className="card" onClick={handleClick}>
      <img
        className="card-image"
        src={isPublic ? publicchannel : privatechannel}
        alt={isPublic ? 'Public Channel' : 'Private Channel'}
      />
      <h3 className="card-title">{isPublic ? 'Public Channel' : 'Private Channel'}</h3>
      <p className="card-text">
        <b>Name:</b> {channel.name}
      </p>
      <p className="card-text">
        <b>Owner:</b> {channel.owner.username}
      </p>
      <p className="card-text">
        <b>Type:</b> {channel.type}
      </p>
    </div>
  );
}
