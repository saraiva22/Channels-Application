import React from 'react';
import { useContext } from 'react';
import { HomeOutput } from '../../services/users/models/HomeOutputModel';
import { useAuthentication } from './AuthProvider';

export function Me() {
  const [username] = useAuthentication();
  return (
    <div>
      <h1>My Profile</h1>
      <p>Hello {username}</p>
    </div>
  );
}
