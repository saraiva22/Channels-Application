import React, { useState } from 'react';
import { searchChannels } from '../../services/channels/ChannelsServices';
import { Fetch } from '../fetch/Fetch';
import './SearchChannels.css';

export function SearchChannels() {
  const [text, setText] = useState('');
  const [sort, setSort] = useState('');

  const changeHandler: React.ChangeEventHandler<HTMLInputElement> = ev => {
    setText(ev.target.value);
  };

  const sortChangeHandler: React.ChangeEventHandler<HTMLSelectElement> = ev => {
    setSort(ev.target.value);
  };

  return (
    <div>
      <h1>Search For Channels</h1>
      <input type="text" value={text} onChange={changeHandler} />
      <label>Sort by:</label>
      <select id="sort" value={sort} onChange={sortChangeHandler}>
        <option value=""></option>
        <option value="name">Name</option>
      </select>
      <Fetch fetchFunction={searchChannels} fetchArgs={[text, sort]} />
    </div>
  );
}
