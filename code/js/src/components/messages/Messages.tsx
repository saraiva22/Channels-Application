import React from 'react';
import { MessageOutputModel } from '../../services/messages/models/MessageOutputModel';
import { deleteMessage } from '../../services/messages/MessagesService';
import './css/Message.css';
import { Problem } from '../../services/media/Problem';

export function formatDate(dateString: string): string {
  const specificDate = new Date(dateString);
  const day = String(specificDate.getDate()).padStart(2, '0');
  const month = String(specificDate.getMonth() + 1).padStart(2, '0');
  const year = specificDate.getFullYear();
  const hours = String(specificDate.getHours()).padStart(2, '0');
  const minutes = String(specificDate.getMinutes()).padStart(2, '0');
  return `${day}/${month}/${year} at ${hours}:${minutes}`;
}

export function Message({ message }: MessageOutputModel) {
  const localFormattedDate = formatDate(message.created);
  async function handleOnClick() {
    try {
      const result = await deleteMessage(message.channel.id, message.id);
      if (result) {
        window.location.reload();
      }
    } catch (error) {
      const problem = error as Problem;
      alert(`${problem.title.toUpperCase()} \n\n${problem.detail}`);
    }
  }
  return (
    <div className="message">
      <small className="message small">
        <b>{message.user.username}</b> {localFormattedDate}
        <button onClick={handleOnClick}>Delete</button>
      </small>
      <p className="message p">{message.text}</p>
    </div>
  );
}
