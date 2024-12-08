import React, { useReducer } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { Type } from '../../domain/channels/Type';
import { createChannel } from '../../services/channels/ChannelsServices';
import { webRoutes } from '../../App';

type State =
  | { tag: 'editing'; error?: string; inputs: { name: string; type: Type } }
  | { tag: 'submitting'; name: string; type: Type }
  | { tag: 'redirect' };

type Action =
  | { type: 'edit'; inputName: string; inputValue: string }
  | { type: 'submit' }
  | { type: 'error'; message: string }
  | { type: 'success' };

function logUnexpectedAction(state: State, action: Action) {
  console.log(`Unexpected action '${action.type} on state '${state.tag}'`);
}

function reducer(state: State, action: Action): State {
  switch (state.tag) {
    case 'editing':
      if (action.type === 'edit') {
        const updateInputs = {
          ...state.inputs,
          [action.inputName]: action.inputName === 'type' ? Number(action.inputValue) : action.inputValue,
        };
        return { tag: 'editing', error: undefined, inputs: updateInputs };
      } else if (action.type === 'submit') {
        return { tag: 'submitting', name: state.inputs.name, type: state.inputs.type };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }

    case 'submitting':
      if (action.type === 'success') {
        return { tag: 'redirect' };
      } else if (action.type === 'error') {
        return { tag: 'editing', error: action.message, inputs: { name: '', type: state.type } };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }

    case 'redirect':
      logUnexpectedAction(state, action);
      return state;
  }
}

export function ChannelCreate() {
  const [state, dispatch] = useReducer(reducer, { tag: 'editing', inputs: { name: '', type: undefined } });
  const location = useLocation();

  if (state.tag === 'redirect') {
    return <Navigate to={location.state?.source || webRoutes.channelsMembers} replace={true} />;
  }

  function handleChange(ev: React.FormEvent<HTMLInputElement>) {
    dispatch({ type: 'edit', inputName: ev.currentTarget.name, inputValue: ev.currentTarget.value });
  }

  async function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
    ev.preventDefault();
    if (state.tag !== 'editing') {
      return;
    }
    dispatch({ type: 'submit' });

    const name = state.inputs.name;
    const type = state.inputs.type;
    try {
      const result = await createChannel(name, type);
      if (result) {
        dispatch({ type: 'success' });
      } else {
        dispatch({ type: 'error', message: 'Error in create Channel' });
      }
    } catch (e) {
      dispatch({ type: 'error', message: e.message });
    }
  }

  const name = state.tag === 'submitting' ? '' : state.inputs.name;
  const type = state.tag === 'submitting' ? state.type : state.inputs.type;
  return (
    <form onSubmit={handleSubmit}>
      <fieldset disabled={state.tag !== 'editing'}>
        <h1>Create Game</h1>
        <div>
          <label htmlFor="name">Name</label>
          <input id="name" type="text" name="name" value={name} onChange={handleChange} />
        </div>
        <div>
          <label>
            <input
              type="radio"
              name="type"
              value={Type.PUBLIC}
              checked={type === Type.PUBLIC}
              onChange={handleChange}
            />
            Public
          </label>
          <label>
            <input
              type="radio"
              name="type"
              value={Type.PRIVATE}
              checked={type === Type.PRIVATE}
              onChange={handleChange}
            />
            Private
          </label>
        </div>
        <div>
          <button type="submit">Create Channel</button>
        </div>
      </fieldset>
    </form>
  );
}
