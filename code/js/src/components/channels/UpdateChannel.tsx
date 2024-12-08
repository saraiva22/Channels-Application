import React, { useReducer } from 'react';
import { Problem } from '../../services/media/Problem';
import { Type } from '../../domain/channels/Type';
import { Navigate, useLocation } from 'react-router-dom';
import { useChannel } from '../../context/ChannelProvider';
import { updateChannel } from '../../services/channels/ChannelsServices';
import { webRoutes } from '../../App';
import './css/UpdateChannel.css';

type State =
  | { type: 'editing'; error?: Problem | string; inputs: { name: string; typeChannel: Type } }
  | { type: 'submitting'; name: string; typeChannel: Type }
  | { type: 'redirect' };

type Action =
  | { type: 'edit'; inputName: string; inputValue: string }
  | { type: 'submit' }
  | { type: 'error'; error: Problem | string }
  | { type: 'success' };

function logUnexpectedAction(state: State, action: Action) {
  console.log(`Unexpected action '${action.type}' on state '${state.type}'`);
}

function reducer(state: State, action: Action): State {
  switch (state.type) {
    case 'editing':
      if (action.type === 'edit') {
        const newInputs = { ...state.inputs, [action.inputName]: action.inputValue };
        if (action.inputName === 'typeChannel') {
          newInputs.typeChannel = action.inputValue === Type.PUBLIC.toString() ? Type.PUBLIC : Type.PRIVATE;
        }
        return {
          type: 'editing',
          error: undefined,
          inputs: newInputs,
        };
      } else if (action.type === 'submit') {
        return { type: 'submitting', name: state.inputs.name, typeChannel: state.inputs.typeChannel };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }
    case 'submitting':
      if (action.type === 'success') {
        return { type: 'redirect' };
      } else if (action.type === 'error') {
        return { type: 'editing', error: action.error, inputs: { name: state.name, typeChannel: state.typeChannel } };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }

    case 'redirect':
      logUnexpectedAction(state, action);
      return state;
  }
}

const firstState: State = { type: 'editing', inputs: { name: '', typeChannel: undefined } };

export function UpdateChannel() {
  const [state, dispatch] = useReducer(reducer, firstState);
  const location = useLocation();
  const { selectedChannel } = useChannel();

  if (state.type === 'redirect') {
    return <Navigate to={location.state?.source || webRoutes.channel} replace={true} />;
  }

  function handleChange(ev: React.FormEvent<HTMLInputElement>) {
    dispatch({ type: 'edit', inputName: ev.currentTarget.name, inputValue: ev.currentTarget.value });
  }

  async function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
    ev.preventDefault();
    if (state.type !== 'editing') {
      return;
    }
    dispatch({ type: 'submit' });

    const name = state.inputs.name.trim() || null;
    const typeChannel = state.inputs.typeChannel;
    const abort = new AbortController();
    let cancelled = false;

    try {
      if (selectedChannel === null) {
        dispatch({ type: 'error', error: 'Channel ID not provided' });
        return;
      }

      const result = await updateChannel(selectedChannel.id, name, typeChannel);
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

  const typeChannel = state.type === 'submitting' ? '' : state.inputs.typeChannel;
  const name = state.type === 'submitting' ? '' : state.inputs.name;
  console.log(typeChannel);

  return (
    <div>
      <h1>Update Channel</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="name">Username</label>
          <input
            id="name"
            type="text"
            name="name"
            value={name}
            onChange={handleChange}
            placeholder="Enter channel name"
          />
        </div>
        <fieldset className="privacy-fieldset" disabled={state.type !== 'editing'}>
          <h1 className="fieldset-title">Privacy in Channel</h1>
          <div className="privacy-options">
            <label className="privacy-option">
              <input
                type="radio"
                name="typeChannel"
                value={Type.PUBLIC}
                checked={typeChannel === Type.PUBLIC}
                onChange={handleChange}
              />
              PUBLIC
            </label>
            <label className="privacy-option">
              <input
                type="radio"
                name="typeChannel"
                value={Type.PRIVATE}
                checked={typeChannel === Type.PRIVATE}
                onChange={handleChange}
              />
              PRIVATE
            </label>
          </div>
          <div className="fieldset-actions">
            <button className="submit-button" type="submit" disabled={typeChannel === undefined || name === undefined}>
              Update Channel
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
