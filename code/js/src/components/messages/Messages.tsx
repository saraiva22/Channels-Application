import React from 'react';
import { MessageOutputModel } from '../../services/messages/models/MessageOutputModel';

export function Message({ message }: MessageOutputModel) {
  return (
    <div>
      <small>
        <b>{message.user.username}</b> {message.created}
      </small>
      <p>{message.text}</p>
    </div>
  );
}
