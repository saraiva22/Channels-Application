import * as React from 'react';
import { getMemberChannels } from "../../Service/Channels/ChannelsServices";

export function ChannelsMember() {
    async function handleResults() {
        const res = await getMemberChannels()
        return await JSON.stringify(res)
    }

    return(
        <div>
            <textarea value={handleResults} readOnly cols={80} rows={40}></textarea>
        </div>
    )
}

