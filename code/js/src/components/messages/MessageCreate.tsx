import { useReducer } from 'react';
import React from 'react';
import { useChannel } from '../../context/ChannelProvider';
import { createMessage } from '../../services/messages/MessagesService';
import './css/MessageCreate.css';

type State = { tag: 'editing'; error?: string; message: string } | { tag: 'submitting'; message: string };

type Action =
  | { type: 'edit'; message: string }
  | { type: 'submit' }
  | { type: 'error'; error: string }
  | { type: 'success' };

function logUnexpectedAction(state: State, action: Action) {
  console.log(`Unexpected action '${action.type} on state '${state.tag}'`);
}

function reduce(state: State, action: Action): State {
  switch (state.tag) {
    case 'editing':
      if (action.type === 'edit') {
        return { tag: 'editing', error: undefined, message: action.message };
      } else if (action.type === 'submit') {
        return { tag: 'submitting', message: state.message };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }

    case 'submitting':
      if (action.type === 'success') {
        return { tag: 'editing', error: '', message: '' };
      } else if (action.type === 'error') {
        return { tag: 'editing', error: '', message: state.message };
      } else {
        logUnexpectedAction(state, action);
        return state;
      }
  }
}

export function MessageCreate({ onMessageCreated }: { onMessageCreated: () => void }) {
  const [state, dispatch] = useReducer(reduce, { tag: 'editing', message: '' });
  const { selectedChannel } = useChannel();

  function handleChange(ev: React.FormEvent<HTMLInputElement>) {
    dispatch({ type: 'edit', message: ev.currentTarget.value });
  }

  async function handleSubmit(ev: React.FormEvent<HTMLFormElement>) {
    ev.preventDefault();
    if (state.tag !== 'editing') {
      return;
    }
    dispatch({ type: 'submit' });

    const message = state.message;
    try {
      const result = await createMessage(selectedChannel.id, message);
      if (result) {
        dispatch({ type: 'success' });
        dispatch({ type: 'edit', message: '' });
        onMessageCreated();
      } else {
        dispatch({ type: 'error', error: 'Error in create Channel' });
      }
    } catch (e) {
      dispatch({ type: 'error', error: e.message });
    }
  }

  const message = state.tag === 'submitting' ? '' : state.message;

  return (
    <form className="create-message-form" onSubmit={handleSubmit}>
      <fieldset className="fieldset-message" disabled={state.tag !== 'editing'}>
        <input className="input-message" id="name" type="text" name="message" value={message} onChange={handleChange} />
        <button className="send-message" type="submit">
          Send Message
        </button>
      </fieldset>
    </form>
  );
}
