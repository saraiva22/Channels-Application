import React, { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ChannelOutputModel } from '../../services/channels/models/ChannelOutputModel';
import privatechannel from '../../assets/privatechannel.png';
import publicchannel from '../../assets/publicchannel.png';
import { webRoutes } from '../../App';
import { useAuthentication } from '../../context/AuthProvider';
import { joinPublicChannel, leaveInChannel } from '../../services/channels/ChannelsServices';
import './css/Channel.css';
import { Problem } from '../../services/media/Problem';

type ChannelProps = {
  channel: ChannelOutputModel;
};

export function Channel({ channel }: ChannelProps) {
  const isPublic = channel.type.toString() === 'PUBLIC';
  const [navigateToChannel, setNavigateToChannel] = useState<string | null>(null);
  const channelId = channel.id;
  const [username] = useAuthentication();
  const [isMember, setIsMember] = useState(channel.members.some(member => member.username === username));
  const location = useLocation();
  function openChannel() {
    const route = webRoutes.channelMessages.replace(':id', channelId.toString());
    setNavigateToChannel(route);
  }

  if (navigateToChannel) {
    return <Navigate to={location.state?.source || navigateToChannel} />;
  }

  async function joinChannel() {
    try {
      await joinPublicChannel(channel.id);
      setIsMember(true)
      openChannel();
    } catch (error) {
      console.error('Error joining channel', error);
    }
  }

  async function leaveChannel() {
    try {
      await leaveInChannel(channel.id);
      setIsMember(false)
    } catch (error) {
      const problem = error as Problem;
      alert(`${problem.title.toUpperCase()} \n\n${problem.detail}`);
    }
  }


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
