import React, { useState } from 'react';
import { Navigate } from 'react-router-dom';
import { ChannelOutputModel } from '../../services/channels/models/ChannelOutputModel';
import privatechannel from '../../assets/privatechannel.png';
import publicchannel from '../../assets/publicchannel.png';
import { webRoutes } from '../../App';
import { useChannel } from '../../context/ChannelProvider';
import './css/Channel.css';

type ChannelProps = {
  channel: ChannelOutputModel;
};

export function Channel({ channel }: ChannelProps) {
  const isPublic = channel.type.toString() === 'PUBLIC';
  const [navigateToChannel, setNavigateToChannel] = useState<string | null>(null);

  const { setChannel } = useChannel();

  function handleClick() {
    setChannel(channel);
    const route = webRoutes.channelMessages;
    setNavigateToChannel(route);
  }

  if (navigateToChannel) {
    return <Navigate to={navigateToChannel} replace={true} />;
  }

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
    </div>
  );
}
