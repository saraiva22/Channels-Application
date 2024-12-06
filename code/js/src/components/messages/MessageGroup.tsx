import React from 'react';
import './MessageGroup.css';
import { deleteMessage } from '../../services/messages/MessagesService';

export function MessageGroup({ user, messages }) {
  async function handleOnClick(channelId: number, messageId: number) {
    await deleteMessage(channelId, messageId);
    window.location.reload();
  }

  const formatDate = (date: string) => {
    const specificDate = new Date(date);
    const day = String(specificDate.getDate()).padStart(2, '0');
    const month = String(specificDate.getMonth() + 1).padStart(2, '0');
    const year = specificDate.getFullYear();
    const hours = String(specificDate.getHours()).padStart(2, '0');
    const minutes = String(specificDate.getMinutes()).padStart(2, '0');
    return `${day}/${month}/${year} at ${hours}:${minutes}`;
  };

  return (
    <div className="message-group">
      <small className="message-group small">
        <b>{user.username}</b>
      </small>
      {messages.map(message => (
        <p key={message.id}>
          <small className="message-group p small">{formatDate(message.created)}</small>
          {message.text}
          <button className="delete-button" onClick={() => handleOnClick(message.channel.id, message.id)}>
            Delete
          </button>
        </p>
      ))}
    </div>
  );
}
