import { useEffect, useState } from 'react';
import React from 'react';

const list = 'http://localhost:8080/api/users/notifications';

export function Notifications() {
  const [data, setData] = useState();
  useEffect(() => {
    const sse = new EventSource(list);
    function handleStream(e) {
      console.log(e);
      setData(e.data);
    }

    sse.onmessage = e => {
      handleStream(e);
    };

    sse.onerror = e => {
      sse.close();
    };
    return () => {
      sse.close();
    };
  }, []);

  return <div>message : {}</div>;
}
