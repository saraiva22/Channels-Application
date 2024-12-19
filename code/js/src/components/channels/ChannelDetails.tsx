import React, { useState } from 'react';
import { useEffect, useReducer } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { ChannelOutputModel } from '../../services/channels/models/ChannelOutputModel';
import { banUserFromChannel, getChannelById, unbanUserFromChannel } from '../../services/channels/ChannelsServices';
import { User } from '../user/User';
import { Problem, ProblemComponent } from '../../services/media/Problem';
import { useAuthentication } from '../../context/AuthProvider';
import { webRoutes } from '../../App';
import './css/ChannelDetails.css';
import { IdStringOutputModel } from '../../services/utils/models/IdOutputModel';

type State =
  | { type: 'start' }
  | { type: 'loading' }
  | { type: 'success'; rsp: ChannelOutputModel }
  | { type: 'error'; error: Problem | string };

type Action =
  | { type: 'started-loading' }
  | { type: 'success'; rsp: ChannelOutputModel }
  | { type: 'error'; error: Problem | string }
  | { type: 'cancel' };

function unexpectedAction(action: Action, state: State): State {
  console.log(`unexpected action ${action.type} on state ${state.type}`);
  return state;
}

function reducer(state: State, action: Action): State {
  console.log(`handling ${action.type} on ${state.type}`);
  switch (action.type) {
    case 'started-loading':
      return { type: 'loading' };
    case 'success':
      switch (state.type) {
        case 'loading':
          return { ...state, type: 'success', rsp: action.rsp };
        default:
          return unexpectedAction(action, state);
      }
    case 'error':
      switch (state.type) {
        case 'loading':
          return { ...state, type: 'error', error: action.error };
        default:
          return unexpectedAction(action, state);
      }
    case 'cancel':
      return { type: 'start' };
  }
}

const firstState: State = { type: 'start' };

export function ChannelDetails() {
  const [state, dispatch] = useReducer(reducer, firstState);
  const [username] = useAuthentication();
  const navigate = useNavigate();
  const { id } = useParams<IdStringOutputModel>();
  const location = useLocation();

  const handleNavigateCreate = () => {
    const path = webRoutes.createPrivateInvite.replace(':id', id.toString());
    navigate(path);
  };

  const handleNavigateUpdate = () => {
    const path = webRoutes.updateChannel.replace(':id', id.toString());
    navigate(path);
  };

  useEffect(() => {
    const abort = new AbortController();
    let cancelled = false;
    async function doFetch() {
      dispatch({ type: 'started-loading' });
      if (id === null) {
        dispatch({ type: 'error', error: 'Channel ID not provided' });
        return;
      }
      try {
        const channel = await getChannelById(Number(id));
        if (!cancelled) {
          dispatch({ type: 'success', rsp: channel });
        }
      } catch (error) {
        if (!cancelled) {
          dispatch({ type: 'error', error: error });
        }
      }
    }

    if (Number(id)) {
      doFetch();
    }

    return () => {
      console.log('cleanup');
      abort.abort();
      cancelled = true;
    };
  }, [dispatch, location]);

  async function handleBan(username: string, channelId: number) {
    try {
      const channel = await banUserFromChannel(username, channelId);
    } catch (error) {
      console.log(error); // melhorar
    }
  }

  async function handleUnban(username: string, channelId: number) {
    try {
      const channel = await unbanUserFromChannel(username, channelId);
    } catch (error) {
      console.log(error); // melhorar
    }
  }

  switch (state.type) {
    case 'start':
      return <p>Idle</p>;
    case 'loading':
      return <p>loading...</p>;
    case 'error':
      return typeof state.error === 'string' ? <p>{state.error}</p> : <ProblemComponent problem={state.error} />;
    case 'success': {
      const channel = state.rsp;
      return (
        <div className="channel-details">
          <h1>Channel Details</h1>
          <div className="details-container">
            <div className="details-info">
              <p>
                <b>Name:</b> {channel.name}
              </p>
              <p>
                <b>Owner:</b> {channel.owner.username}
              </p>
              <p>
                <b>Type:</b> {channel.type}
              </p>
            </div>
            <div className="details-action">
              {channel.type.toString() === 'PRIVATE' && (
                <button onClick={handleNavigateCreate}>Create Private Invite</button>
              )}
              {channel.owner.username === username && <button onClick={handleNavigateUpdate}>Update Channel</button>}
            </div>
          </div>
          <p>
            <b>Members:</b>
          </p>
          {channel.members.length > 0 ? (
            <ul>
              {channel.members.map(member => (
                <li key={member.id}>
                  <User user={member} />
                  {channel.owner.username === username && member.username !== channel.owner.username && (
                    <button className="button-ban" onClick={() => handleBan(member.username, Number(id))}>
                      Ban
                    </button>
                  )}
                </li>
              ))}
            </ul>
          ) : (
            <p>No members.</p>
          )}
          {channel.owner.username === username && (
            <>
              <p>
                <b>Banned Members:</b>
              </p>
              {channel.bannedMembers.length > 0 ? (
                <ul>
                  {channel.bannedMembers.map(member => (
                    <li key={member.id}>
                      <User user={member} />
                      <button className="button-unban" onClick={() => handleUnban(member.username, Number(id))}>
                        Unban
                      </button>
                    </li>
                  ))}
                </ul>
              ) : (
                <p>No banned members.</p>
              )}
            </>
          )}
        </div>
      );
    }
  }
}
