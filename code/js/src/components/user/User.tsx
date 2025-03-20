import React from 'react';
import { UserInfo } from '../../domain/users/UserInfo';

type UserProps = {
  user: UserInfo;
};

export function User({ user }: UserProps) {
  return (
    <div className="user">
      <p>
        <b>Username: </b>
        {user.username}
      </p>
      <p>
        <b>Email: </b>
        {user.email}
      </p>
    </div>
  );
}
