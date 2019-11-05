import request = require('request-promise-native');
import { PrettyError } from '../utils/prettyerror';

export interface HttpServiceConfig {
    host: string;
    port: string;
    baseUrl: string;
}

export class HttpService {
    config: HttpServiceConfig;
    public constructor(config: HttpServiceConfig) {
        this.config = config;
    }

    public async get(endpoint: string) {
        let response;
        try {
            response = await request
                .get({
                    method: 'GET',
                    url: this.buildUri(endpoint),
                });
            response = JSON.parse(response);
        } catch (err) {
            throw new PrettyError(err);
        }

        return response;

    }

    public async post(endpoint: string, body: {[key: string]: any}) {
        let response;

        try {
            response = await request
                .post(this.buildUri(endpoint), {body, json: true});
        } catch (err) {
            throw new PrettyError(err);
        }

        return response;
    }

    public async put(endpoint: string, body: {[key: string]: any}, options: any = {}) {
        const {user} = options;
        let response;
        try {
        response = await request
                .put(this.buildUri(endpoint), {body: JSON.stringify({user, ...body})});

        } catch (err) {
            throw new PrettyError(err);
        }
        return response;
    }

    private buildUri(endpoint: string) {
        return `${this.config.host}:${this.config.port}${this.config.baseUrl}/${endpoint}`;
    }
}
