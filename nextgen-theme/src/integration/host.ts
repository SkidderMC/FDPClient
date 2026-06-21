import {getHashParams} from './util';

const queryParams = new URLSearchParams(window.location.search);
const hashParams = getHashParams();
const portParam = queryParams.get('port') ?? hashParams.get('port');
export const isStatic = queryParams.has('static') || hashParams.has('static');

export const REST_BASE = portParam
    ? `http://localhost:${portParam}`
    : window.location.origin;

export const WS_BASE = portParam
    ? `ws://localhost:${portParam}`
    : `ws://${window.location.host}`;
