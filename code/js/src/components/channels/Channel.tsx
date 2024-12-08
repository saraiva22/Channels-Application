import React, { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ChannelOutputModel } from '../../services/channels/models/ChannelOutputModel';
import privatechannel from '../../assets/privatechannel.png';
import publicchannel from '../../assets/publicchannel.png';
import { webRoutes } from '../../App';
import { useChannel } from '../../context/ChannelProvider';
import { useAuthentication } from '../../context/AuthProvider';
import { joinPublicChannel, leaveInChannel } from '../../services/channels/ChannelsServices';
import './css/Channel.css'

type ChannelProps = {
  channel: ChannelOutputModel;
};

export function Channel({ channel }: ChannelProps) {
  const isPublic = channel.type.toString() === 'PUBLIC';
  const [navigateToChannel, setNavigateToChannel] = useState<string | null>(null);
  const { setChannel } = useChannel();
  const [username] = useAuthentication();
  function openChannel() {
    setChannel(channel);
    const route = webRoutes.channelMessages;
    setNavigateToChannel(route);
  }

  if (navigateToChannel) {
    return <Navigate to={navigateToChannel} replace={true} />;
  }

  async function joinChannel() {
    try {
      await joinPublicChannel(channel.id);
      openChannel();
    } catch (error) {
      console.error('Error joining channel', error);
    }
  }

  async function leaveChannel() {
    try {
      await leaveInChannel(channel.id);
      window.location.reload(); // melhorar após fazer leave o que devo fazer ??!!
    } catch (error) {
      console.error('Error leaving channel', error);
    }
  }

  const isMember = channel.members.some(member => member.username === username);
  const isOwner = channel.owner.username === username;
  return (
    <div className="card">
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
      <div className="card-buttons">
        <>
          {(isMember || isOwner) && (
            <button className="btn view-messages" onClick={openChannel}>
              📩 View Messages
            </button>
          )}
        </>
        <>
          {isMember && (
            <button className="btn leave-channel" onClick={leaveChannel}>
              🚪 Leave Channel
            </button>
          )}
        </>
        <>
          {!isMember && isPublic && (
            <button className="btn join-channel" onClick={joinChannel}>
              ➕ Join Channel
            </button>
          )}
        </>
      </div>
    </div>
  );
}
