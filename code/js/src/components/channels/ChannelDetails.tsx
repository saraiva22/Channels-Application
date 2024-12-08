import React from 'react';
import { useEffect, useReducer } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChannelOutputModel } from '../../services/channels/models/ChannelOutputModel';
import { useChannel } from './ChannelProvider';
import { getChannelById } from '../../services/channels/ChannelsServices';
import { User } from '../user/User';
import { Problem, ProblemComponent } from '../../services/media/Problem';
import './ChannelDetails.css';
import { useAuthentication } from '../authentication/AuthProvider';
import { webRoutes } from '../../App';

type State =
  | { type: 'start' }
  | { type: 'loading' }
  | { type: 'success'; rsp: ChannelOutputModel }
  | { type: 'error'; error: Problem };

type Action =
  | { type: 'started-loading' }
  | { type: 'success'; rsp: ChannelOutputModel }
  | { type: 'error'; error: Problem }
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
  const { selectedChannel } = useChannel();
  const [username] = useAuthentication();
  const navigate = useNavigate();

  const handleNavigateCreate = () => {
    navigate(webRoutes.createPrivateInvite);
  };

  const handleNavigateUpdate = () => {
    navigate(webRoutes.updateChannel);
  };

  useEffect(() => {
    const abort = new AbortController();
    let cancelled = false;
    async function doFetch() {
      dispatch({ type: 'started-loading' });
      try {
        const channel = await getChannelById(selectedChannel.id);
        if (!cancelled) {
          dispatch({ type: 'success', rsp: channel });
        }
      } catch (error) {
        if (!cancelled) {
          dispatch({ type: 'error', error: error });
        }
      }
    }

    if (selectedChannel?.id) {
      doFetch();
    }

    return () => {
      console.log('cleanup');
      abort.abort();
      cancelled = true;
    };
  }, [dispatch]);

  switch (state.type) {
    case 'start':
      return <p>Idle</p>;
    case 'loading':
      return <p>loading...</p>;
    case 'error':
      return <ProblemComponent problem={state.error} />;
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
            {channel.type.toString() === 'PRIVATE' && (
              <div className="details-action">
                <button onClick={handleNavigateCreate}>Create Private Invite</button>
                {channel.owner.username === username && <button onClick={handleNavigateUpdate}>Update Channel</button>}
              </div>
            )}
          </div>
          <p>
            <b>Members:</b>
          </p>
          {channel.members.map(member => (
            <User key={member.id} user={member} />
          ))}
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
