import { Argv } from 'yargs';
import { ConnectionProfileGenerator } from '../src/ConnectionProfileGenerator';

export const command = 'connection-profile-generate [options]';

export const desc = 'generate connection profile for an org based on their peer info';

export const builder = (yargs: Argv) => {
    yargs.options({
        'config': {
            type: 'string',
            alias: 'c',
            required: true
        },
        'output': {
            type: 'string',
            alias: 'o',
            required: true
        }
    });
    yargs.usage('wt-cli connection-profile-generate --config <JSON> --output \'connection_profile.json\'');

    return yargs;
};

export const handler = (argv: any) => {
    return argv.thePromise = ConnectionProfileGenerator.generate(JSON.parse(argv['config']), argv['output']);
}