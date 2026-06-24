import {getHashParams} from './util';

const queryParams = new URLSearchParams(window.location.search);
const hashParams = getHashParams();
const portParam = queryParams.get('port') ?? hashParams.get('port');
const wsPortParam = queryParams.get('wsPort') ?? hashParams.get('wsPort') ?? portParam;
export const isStatic = queryParams.has('static') || hashParams.has('static');
export const hasEventSocket = wsPortParam !== null;

export const REST_BASE = portParam
    ? `http://localhost:${portParam}`
    : window.location.origin;

export const WS_BASE = wsPortParam
    ? `ws://localhost:${wsPortParam}`
    : `ws://${window.location.host}`;
