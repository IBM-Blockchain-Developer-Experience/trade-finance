import { DataStore, PrettyError } from 'wt-common-cli';

export class Command {

    answers: any;
    dataStore: DataStore;
    requiredArgs: string[];

    constructor(dataStore: DataStore, requiredArgs: string[], answers: any) {
        this.dataStore = dataStore;
        this.requiredArgs = requiredArgs;
        this.answers = answers;

        this.hasRequiredArgs();
    }

    public isLoggedIn() {
        return this.dataStore.auth !== null;
    }

    private hasRequiredArgs() {
        for (const arg in this.requiredArgs) {
            if (!this.answers.hasOwnProperty(arg) && this.answers[arg] !== null) {
                throw new PrettyError(`Missing arg ${arg}`);
            }
        }
    }
}
