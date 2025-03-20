import React, { useState } from 'react';
import { Navigate } from 'react-router-dom';
import invitation from '../../assets/invitation.png';
import './css/Invite.css';
import { webRoutes } from '../../App';
import { InviteProps } from '../../services/channels/models/InviteProps';

export function Invite({ value, isReceived }: InviteProps) {
  const isPending = value.status.toString() === 'PENDING';
  const [navigate, setNavigate] = useState<string | null>(null);
  const valuesNav = { channelId: value.channelId, cod: value.codHash };

  function handleClick() {
    if (isPending && isReceived) {
      const route = webRoutes.validateChannelInvite;
      setNavigate(route);
    }
  }

  if (navigate) {
    return <Navigate to={navigate} state={{ valuesNav }} replace={true} />;
  }

  return (
    <div className="invite-container" onClick={handleClick}>
      <img src={invitation} alt="Invitation" />
      <h3>Invite Details</h3>
      <p>
        CodHash: <span className="highlight">{value.codHash}</span>
      </p>
      <p>Privacy: {value.privacy}</p>
      <p>Status: {value.status}</p>
      <p>User Info</p>
      <b>Id: {value.userInfo.id}</b>
      <b>Username: {value.userInfo.username}</b>
      <b>Email: {value.userInfo.email}</b>
      <div className="channel-info">
        <p>ChannelId: {value.channelId}</p>
        <p>ChannelName: {value.channelName}</p>
      </div>
    </div>
  );
}
