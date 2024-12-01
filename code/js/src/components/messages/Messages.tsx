import React from 'react';
import { MessageOutputModel } from '../../services/messages/models/MessageOutputModel';

export function Message({ message }: MessageOutputModel) {
  const specificDate = new Date(message.created);
  const day = String(specificDate.getDate()).padStart(2, '0');
  const month = String(specificDate.getMonth() + 1).padStart(2, '0');
  const year = specificDate.getFullYear();
  const hours = String(specificDate.getHours()).padStart(2, '0');
  const minutes = String(specificDate.getMinutes()).padStart(2, '0');
  const localFormattedDate = `${day}/${month}/${year} at ${hours}:${minutes}`;

  return (
    <div>
      <small>
        <b style={{ marginRight: '30px' }}>{message.user.username}</b> {localFormattedDate}
      </small>
      <p>{message.text}</p>
    </div>
  );
}
