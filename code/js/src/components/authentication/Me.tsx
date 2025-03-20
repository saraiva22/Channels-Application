import React, { useState } from 'react';
import { useAuthentication } from '../../context/AuthProvider';
import { createInvitationRegster } from '../../services/users/UserServices';
import './css/Me.css';

export function Me() {
  const [username] = useAuthentication();
  const [code, setCode] = useState<string | null>(null);

  const handleOnClick: React.MouseEventHandler = async ev => {
    ev.preventDefault();
    const result = await createInvitationRegster();
    setCode(result.code);
  };

  return (
    <div>
      <h1>My Profile</h1>
      <p>Hello {username}</p>
      <button className="invite-button" onClick={handleOnClick}>
        Invite User
      </button>
      {code && <p className="code">Share this code: {code}</p>}
    </div>
  );
}
