import React, { useEffect, useReducer, useState, useRef } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { Privacy } from '../../domain/channels/Privacy';
import { webRoutes } from '../../App';
import { createPrivateInvite } from '../../services/channels/ChannelsServices';
import { Problem } from '../../services/media/Problem';
import { searchUsers } from '../../services/users/UserServices';
import { HomeOutput } from '../../services/users/models/HomeOutputModel';
import './css/CreatePrivateInvite.css';
import { useChannel } from '../../context/ChannelProvider';

type State =
  | { type: 'editing'; error?: Problem | string; inputs: { privacy: Privacy; username: string } }
  | { type: 'submitting'; privacy: Privacy; username: string }
  | { type: 'redirect' };

type Action =
  | { type: 'edit'; inputName: string; inputValue: string }
  | { type: 'submit' }
  | { type: 'error'; error: Problem | string }
  | { type: 'success' };

function logUnexpectedAction(state: State, action: Action) {
  console.log(`Unexpected action '${action.type}' on state '${state.type}'`);
}

const SIZE_USERNAME_MIN = 3;

function reducer(state: State, action: Action): State {
  switch (state.type) {
    case 'editing':
      if (action.type === 'edit') {
        const newInputs = { ...state.inputs, [action.inputName]: action.inputValue };
        if (action.inputName === 'privacy') {
          newInputs.privacy =
            action.inputValue === Privacy.READ_ONLY.toString() ? Privacy.READ_ONLY : Privacy.READ_WRITE;
        }
        return {
          type: 'editing',
          error: undefined,
          inputs: newInputs,
        };
      } else if (action.type === 'submit') {
        return { type: 'submitting', privacy: state.inputs.privacy, username: state.inputs.username };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }

    case 'submitting':
      if (action.type === 'success') {
        return { type: 'redirect' };
      } else if (action.type === 'error') {
        return { type: 'editing', error: action.error, inputs: { privacy: state.privacy, username: state.username } };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }

    case 'redirect':
      logUnexpectedAction(state, action);
      return state;
  }
}

const firstState: State = { type: 'editing', inputs: { privacy: undefined, username: '' } };

export function CreatePrivateInvite() {
  const [state, dispatch] = useReducer(reducer, firstState);
  const location = useLocation();
  const { selectedChannel } = useChannel();

  const [inputValue, setInputValue] = useState('');
  const [debouncedValue, setDebouncedValue] = useState('');
  const [suggestions, setSuggestions] = useState<HomeOutput[]>([]);
  const [selectedUsername, setSelectedUsername] = useState<string | null>(null);

  // Debounce to update searches
  useEffect(() => {
    const handler = setTimeout(() => {
      if (inputValue.length >= SIZE_USERNAME_MIN) {
        setDebouncedValue(inputValue);
      }
    }, 600);
    return () => {
      clearTimeout(handler);
    };
  }, [inputValue]);

  // Fetch suggestions when debounce is updated
  useEffect(() => {
    if (debouncedValue.length >= SIZE_USERNAME_MIN) {
      fetchSuggestions(debouncedValue);
    }
  }, [debouncedValue]);

  if (state.type === 'redirect') {
    return <Navigate to={location.state?.source || webRoutes.channel} replace={true} />;
  }

  async function fetchSuggestions(username: string) {
    try {
      const result = await searchUsers(username);
      setSuggestions(result.users || []);
    } catch (error) {
      console.error('Error fetching suggestions:', error);
    }
  }

  function handleChange(ev: React.FormEvent<HTMLInputElement>) {
    dispatch({ type: 'edit', inputName: ev.currentTarget.name, inputValue: ev.currentTarget.value });
  }

  function handleInputChange(ev: React.ChangeEvent<HTMLInputElement>) {
    setInputValue(ev.target.value);
  }

  function handleUserClick(username: string) {
    setSelectedUsername(username);
    setInputValue('');
    setSuggestions([]);
    dispatch({ type: 'edit', inputName: 'username', inputValue: username });
  }

  async function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
    ev.preventDefault();
    if (state.type !== 'editing') {
      return;
    }
    dispatch({ type: 'submit' });

    const privacy = state.inputs.privacy;
    const username = state.inputs.username;
    const abort = new AbortController();
    let cancelled = false;

    try {
      if (selectedChannel === null) {
        dispatch({ type: 'error', error: 'Channel ID not provided' });
        return;
      }
      const result = await createPrivateInvite(selectedChannel.id, privacy, username);
      if (result) {
        if (!cancelled) {
          dispatch({ type: 'success' });
        }
      }
    } catch (error) {
      if (!cancelled) {
        dispatch({ type: 'error', error: error });
      }
    }
  }

  const privacy = state.type === 'submitting' ? '' : state.inputs.privacy;
  return (
    <div>
      <h1>Create Private Invite</h1>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          name="username"
          value={inputValue}
          onChange={handleInputChange}
          placeholder="Search for a user"
        />
        {suggestions.length > 0 && (
          <ul className="suggestions-list">
            {suggestions.map(user => (
              <li key={user.id} onClick={() => handleUserClick(user.username)} className="suggestion-item">
                {user.username}
              </li>
            ))}
          </ul>
        )}
        {selectedUsername && (
          <p className="selected-username">
            User to Invite: <strong>{selectedUsername}</strong>
          </p>
        )}
        <fieldset className="privacy-fieldset" disabled={state.type !== 'editing'}>
          <h1 className="fieldset-title">Privacy in Channel</h1>
          <div className="privacy-options">
            <label className="privacy-option">
              <input
                type="radio"
                name="privacy"
                value={Privacy.READ_ONLY}
                checked={privacy === Privacy.READ_ONLY}
                onChange={handleChange}
              />
              READ ONLY
            </label>
            <label className="privacy-option">
              <input
                type="radio"
                name="privacy"
                value={Privacy.READ_WRITE}
                checked={privacy === Privacy.READ_WRITE}
                onChange={handleChange}
              />
              READ WRITE
            </label>
          </div>
          <div className="fieldset-actions">
            <button className="submit-button" type="submit" disabled={privacy === undefined || !selectedUsername}>
              Send Private Invite
            </button>
          </div>
        </fieldset>
      </form>
      {state.type === 'editing' && state.error && (
        <div className="error-alert">
          <span className="error-icon">❗</span>
          <span className="error-message">{typeof state.error === 'string' ? state.error : state.error.detail}</span>
        </div>
      )}
    </div>
  );
}
