import { Argv } from 'yargs';
import * as fs from 'fs-extra';
import { EnrollIdentity, IWTUser } from '../src/EnrollIdentity';

export const command = 'enroll [options]';

export const desc = 'enroll users to trade finance';

export const builder = (yargs: Argv) => {
    yargs.options({
        'wallet': {
            type: 'string',
            alias: 'w',
            required: true
        },
        'org': {
            type: 'string',
            alias: 'o',
            required: true
        },
        'connection-profile': {
            type: 'string',
            alias: 'c',
            required: true
        },
        'enroller-name': {
            type: 'string',
            alias: 'N',
            required: true
        },
        'enrollment-name': {
            type: 'string',
            alias: 'n',
            required: true
        },
        'attrs': {
            type: 'string',
            alias: 'a',
            required: true
        }
    });
    yargs.usage('wt-cli enroll --wallet local_fabric/wallet --org Org1 --enroller-name admin --enrollment-name andy --attrs \'[]\'');

    return yargs;
};

export const handler = (argv: any) => {
    const attrs = fs.readJSONSync(argv['attrs']);
    const user: IWTUser = {
        name: argv['enrollment-name'],
        attrs,
    }

    return argv.thePromise = EnrollIdentity.enroll(argv['wallet'], argv['connection-profile'], user, argv['enroller-name'], argv['org']);
}
