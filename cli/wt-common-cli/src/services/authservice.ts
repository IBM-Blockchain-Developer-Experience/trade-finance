import { DataStore } from '../interfaces';
import { PrettyError } from '../utils';
import 'colors';
import Table = require('cli-table');

export class AuthService {

    glnMap: { [key: string]: number };
    dataStore: DataStore;

    async login(username: string, password: string): Promise<number> {
        const userInfo = this.getUserInfo();
        if (username === undefined || password === undefined) {
            throw new PrettyError('Missing username or password');
        }
        if (!userInfo[username]) {
            throw new PrettyError(`No user information for user ${username}`);
        }

        const splitUser = username.split('@');
        const user: string = splitUser[0];
        const org: string = splitUser[1];

        const message = 'Welcome ' + user.underline + ' to ' + org.underline + "'s portal";

        const table = new Table({
            chars: { 'top': '═', 'top-left': '╔', 'top-right': '╗', 'bottom': '═', 'bottom-left': '╚', 'bottom-right': '╝', 'left': '║', 'right': '║', }
        });
        table.push([message]);
        console.log(table.toString());

        return userInfo[username];
    }

    getUserInfo(): { [key: string]: number } {
        return {
            'alice@DigiBankPO': 1000000000000,
            'bob@MagnetoCorpPO': 1100000000000,
            'admin@DigiBankSP': 1200000000000,
            'admin@MagnetoCorpSP': 1300000000000,
            'admin@HedgematicSP': 1400000000000,
            'admin@EcoBankSP': 1500000000000
        };
    }

    getGln(username: string) {
        const userInfo = this.getUserInfo();
        return userInfo[username];
    }
}
