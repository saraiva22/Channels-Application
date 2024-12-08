import React from 'react';
import { UserInfo } from '../../domain/users/UserInfo';
import { useAuthentication } from '../../context/AuthProvider';
import { Message } from '../../domain/messages/Message';
import './css/MessageGroup.css';

interface MessageGroupProps {
  groupUser: UserInfo;
  messagesList: Array<Message>;
  onDeleteMessage: (channelId: number, messageId: number) => void;
}

export function MessageGroup({ groupUser, messagesList, onDeleteMessage }: MessageGroupProps) {
  const [username] = useAuthentication();

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
        <b>{groupUser.username}</b>
      </small>
      {messagesList.map(message => (
        <p key={message.id}>
          <small className="message-group p small">{formatDate(message.created)}</small>
          {message.text}
          {(username === message.user.username || username === message.channel.owner.username) && (
            <button className="delete-button" onClick={() => onDeleteMessage(message.channel.id, message.id)}>
              Delete
            </button>
          )}
        </p>
      ))}
    </div>
  );
}
